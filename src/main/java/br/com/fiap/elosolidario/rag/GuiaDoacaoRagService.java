package br.com.fiap.elosolidario.rag;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RAG básico, determinístico e local: recupera trechos curtos com origem
 * identificada e os disponibiliza ao Spring AI como contexto de geração.
 * Não classifica segurança sanitária nem guarda dados pessoais.
 */
@Service
public class GuiaDoacaoRagService {
    private static final String ARQUIVO = "/rag/guia-doacao.tsv";
    private static final Set<String> STOPWORDS = Set.of(
            "a", "o", "as", "os", "um", "uma", "uns", "umas", "de", "do", "da", "dos", "das",
            "e", "ou", "que", "para", "por", "com", "sem", "ao", "aos", "na", "nas", "no", "nos",
            "em", "se", "eu", "tenho", "quero", "fazer", "meu", "minha", "ele", "ela", "ser",
            "tem", "sao", "foi", "estou", "esta", "esse", "essa", "isso", "como", "qual", "quais",
            "mais", "pode", "podem", "preciso", "sobre", "orientacao", "orientacoes");

    public record Trecho(String id, String assunto, String conteudo, String fonte, String url) {
        public String citacao() {
            return "[" + id + "] " + assunto + " — " + fonte + " (" + url + ")";
        }
    }

    private final List<Trecho> base;

    public GuiaDoacaoRagService() {
        this.base = carregar();
    }

    public int totalTrechos() {
        return base.size();
    }

    public List<Trecho> buscar(String consulta, int limite) {
        Set<String> termosConsulta = tokens(consulta);
        if (termosConsulta.isEmpty() || limite <= 0) {
            return List.of();
        }
        record Resultado(Trecho trecho, int pontos) {}
        return base.stream()
                .map(t -> {
                    Set<String> termosTitulo = tokens(t.assunto());
                    Set<String> termosConteudo = tokens(t.conteudo());
                    int pontos = termosConsulta.stream().mapToInt(token ->
                            (termosTitulo.contains(token) ? 3 : 0)
                                    + (termosConteudo.contains(token) ? 1 : 0)).sum();
                    return new Resultado(t, pontos);
                })
                .filter(r -> r.pontos() > 0)
                .sorted(Comparator.<Resultado>comparingInt(Resultado::pontos).reversed()
                        .thenComparing(r -> r.trecho().id()))
                .limit(Math.min(limite, 5))
                .map(Resultado::trecho)
                .toList();
    }

    public String contextoPara(String descricao, int limite) {
        List<Trecho> encontrados = buscar(descricao, limite);
        if (encontrados.isEmpty()) {
            return "Nenhum trecho de referência pertinente foi recuperado. "
                    + "Não invente boas práticas específicas; peça para conferir as orientações oficiais.";
        }
        return encontrados.stream()
                .map(t -> t.citacao() + "\n" + t.conteudo())
                .collect(Collectors.joining("\n\n"));
    }

    private List<Trecho> carregar() {
        InputStream entrada = GuiaDoacaoRagService.class.getResourceAsStream(ARQUIVO);
        if (entrada == null) {
            throw new IllegalStateException("Base RAG não encontrada no classpath: " + ARQUIVO);
        }
        List<Trecho> trechos = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            String linha;
            int numero = 0;
            while ((linha = leitor.readLine()) != null) {
                numero++;
                if (linha.isBlank() || linha.startsWith("#")) continue;
                String[] partes = linha.split("\t", -1);
                if (partes.length != 5) {
                    throw new IllegalStateException("Linha " + numero + " inválida em " + ARQUIVO);
                }
                trechos.add(new Trecho(partes[0], partes[1], partes[2], partes[3], partes[4]));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro lendo base RAG", e);
        }
        if (trechos.isEmpty()) {
            throw new IllegalStateException("Base RAG vazia");
        }
        return List.copyOf(trechos);
    }

    private static Set<String> tokens(String texto) {
        if (texto == null || texto.isBlank()) return Set.of();
        String limpo = Normalizer.normalize(texto.toLowerCase(java.util.Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-z0-9]+", " ");
        Set<String> retorno = new HashSet<>();
        for (String termo : limpo.split("\\s+")) {
            if (termo.length() > 2 && !STOPWORDS.contains(termo)) retorno.add(termo);
        }
        return retorno;
    }
}
