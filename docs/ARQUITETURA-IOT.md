# Arquitetura de IA — EloSolidário

## Objetivo e entrada

O assistente recebe a descrição textual de uma doação, como “Tenho 3 pacotes de arroz e 2 de feijão”, busca itens válidos do catálogo e traz contexto sobre boas práticas de conservação e transporte.

## Pipeline de recuperação e geração

1. **Recuperação da base documental:** `GuiaDoacaoRagService` lê `src/main/resources/rag/guia-doacao.tsv`, uma coleção de 10 trechos resumidos com ID, assunto, conteúdo, fonte e URL.
2. **Preparação:** texto em letras minúsculas, normalização de acentos e remoção de palavras comuns. Cada trecho é pontuado por correspondência lexical, dando peso maior a palavras do assunto do que a palavras do corpo.
3. **Seleção:** até 3 trechos pertinentes são concatenados, incluindo identificador e fonte para fornecer contexto ao modelo. Se não houver correspondência, um aviso explícito orienta o modelo a não inventar práticas específicas.
4. **Geração:** `AssistenteDoacaoService` monta o prompt com catálogo válido e contexto do RAG; Spring AI conversa com um LLM configurável e solicita saída estruturada.
5. **Pós-processamento:** o serviço rejeita IDs inexistentes ou duplicados, normaliza unidades e valores inválidos e inclui aviso de revisão humana.
6. **Ação humana:** quem doa confirma itens, validade e publicação; IA não efetua publicação nem avaliação sanitária.

## Escolhas e trade-offs

**RAG lexical, não vetorial:** não exige banco vetorial, embedding ou custo de indexação; é simples de reproduzir, mas pode não capturar sinônimos ou variações sem coincidência lexical. Não é necessário sensor físico neste protótipo, porque o aprofundamento escolhido é RAG básico, integrado à IA generativa.

**Separação entre testes e produção:** o conjunto de 23 testes JUnit não usa necessariamente a API remota de IA. O script Python `demo_rag_local.py` permite visualizar um modelo generativo local com a mesma base TSV, mas é apenas uma demonstração complementar: o caminho oficial da aplicação continua sendo o Java.

**Dados pessoais:** o RAG não persiste dados pessoais. Evitar incluir nomes, endereços ou contatos reais nas perguntas e nas capturas. Não publicar senhas ou tokens do Swagger.

## Fontes do conhecimento

As URLs estão documentadas em `src/main/resources/rag/guia-doacao.tsv`. Parte dos trechos representa resumos de materiais citados como Anvisa e órgãos públicos, e parte representa regras próprias do protótipo. A presença da URL **não substitui a verificação da exatidão do resumo contra o documento original**. O sistema não deve ser usado como certificador de segurança de alimentos.
