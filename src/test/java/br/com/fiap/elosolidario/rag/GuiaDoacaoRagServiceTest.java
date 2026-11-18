package br.com.fiap.elosolidario.rag;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GuiaDoacaoRagServiceTest {
    private final GuiaDoacaoRagService rag = new GuiaDoacaoRagService();

    @Test
    void baseTemTrechosEFontesRastreaveis() {
        assertThat(rag.totalTrechos()).isGreaterThanOrEqualTo(8);
        assertThat(rag.buscar("armazenamento arroz", 3))
                .allSatisfy(t -> {
                    assertThat(t.id()).startsWith("RAG-");
                    assertThat(t.url()).startsWith("https://");
                });
    }

    @Test
    void encontraConservacaoParaFrangoCongelado() {
        assertThat(rag.buscar("Como transportar frango congelado?", 3))
                .extracting(GuiaDoacaoRagService.Trecho::id).contains("RAG-03");
    }

    @Test
    void recuperaOrientacoesSobreValidade() {
        assertThat(rag.buscar("Pacote de arroz validade embalagem", 3))
                .extracting(GuiaDoacaoRagService.Trecho::id).contains("RAG-01");
    }

    @Test
    void naoInventarQuandoNaoHaContexto() {
        assertThat(rag.buscar("galaxia telescopio astronauta", 3)).isEmpty();
        assertThat(rag.contextoPara("galaxia telescopio astronauta", 3))
                .contains("Nenhum trecho de referência pertinente");
    }

    @Test
    void limitaTrechosRetornados() {
        assertThat(rag.buscar("doacao de alimentos armazenar validade transporte", 2)).hasSizeLessThanOrEqualTo(2);
    }
}
