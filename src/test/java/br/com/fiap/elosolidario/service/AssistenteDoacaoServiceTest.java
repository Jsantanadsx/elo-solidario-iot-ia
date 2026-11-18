package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.model.Alimento;
import br.com.fiap.elosolidario.model.Categoria;
import br.com.fiap.elosolidario.model.Conservacao;
import br.com.fiap.elosolidario.model.Unidade;
import br.com.fiap.elosolidario.repository.AlimentoRepository;
import br.com.fiap.elosolidario.rag.GuiaDoacaoRagService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AssistenteDoacaoServiceTest {

    private final AssistenteDoacaoService service = new AssistenteDoacaoService(
            mock(ChatClient.Builder.class, invocacao -> mock(ChatClient.class)),
            mock(AlimentoRepository.class), new GuiaDoacaoRagService());

    private final List<Alimento> catalogo = List.of(
            alimento(1L, "Arroz branco", Conservacao.AMBIENTE),
            alimento(14L, "Peito de frango", Conservacao.CONGELADO));

    @Test
    void deveDescartarAlimentosForaDoCatalogoERepetidos() {
        var resposta = new AssistenteDoacaoService.RespostaIA("Cesta", List.of(
                new AssistenteDoacaoService.ItemIA(1L, new BigDecimal("3"), "pct", new BigDecimal("3")),
                new AssistenteDoacaoService.ItemIA(999L, BigDecimal.ONE, "UN", BigDecimal.ONE),
                new AssistenteDoacaoService.ItemIA(1L, BigDecimal.TEN, "PCT", BigDecimal.TEN)),
                List.of("sabonete"), List.of());

        var sugestao = service.montarResposta(resposta, catalogo);

        assertThat(sugestao.itens()).hasSize(1);
        assertThat(sugestao.itens().get(0).unidade()).isEqualTo(Unidade.PCT);
        assertThat(sugestao.naoReconhecidos()).containsExactly("sabonete");
        assertThat(sugestao.aviso()).contains("Confira");
    }

    @Test
    void deveCorrigirValoresInvalidosEOrientarSobreItensCongelados() {
        var resposta = new AssistenteDoacaoService.RespostaIA(null, List.of(
                new AssistenteDoacaoService.ItemIA(14L, BigDecimal.ZERO, "caixa", null)), null, null);

        var sugestao = service.montarResposta(resposta, catalogo);

        assertThat(sugestao.tituloSugerido()).isEqualTo("Doação de alimentos");
        assertThat(sugestao.itens().get(0).quantidade()).isEqualByComparingTo("1");
        assertThat(sugestao.itens().get(0).unidade()).isEqualTo(Unidade.UN);
        assertThat(sugestao.orientacoes()).anyMatch(o -> o.contains("bolsa térmica"));
    }

    private static Alimento alimento(Long id, String nome, Conservacao conservacao) {
        Categoria categoria = new Categoria();
        categoria.setNome("Categoria");
        Alimento alimento = new Alimento();
        alimento.setId(id);
        alimento.setNome(nome);
        alimento.setCategoria(categoria);
        alimento.setConservacao(conservacao);
        alimento.setAtivo(true);
        return alimento;
    }
}
