# Evidências e validação — IoT/IA Generativa

## 1. Resultado automatizado já confirmado

- **Data:** 18/11/2026.
- **Ambiente:** Windows 11, Temurin JDK 21.0.8, Maven 3.9.11.
- **Comando:** `mvn clean test`.
- **Resultado observado no terminal:** `Tests run: 23, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`.
- **RAG específico:** `GuiaDoacaoRagServiceTest` — 5 testes, 0 falhas, 0 erros.
- **Observação:** um stack trace em teste de tratamento de exceções foi produzido intencionalmente; o teste terminou aprovado. Os avisos de agente dinâmico Mockito não impediram a execução.

Este resumo documenta a execução relatada e revisada. **Ainda falta anexar captura de tela da execução em `evidencias/`**.

## 2. Cenários do RAG verificados por JUnit

| Cenário | Evidência esperada |
|---|---|
| Base carregada com fontes e URLs | IDs RAG e URLs presentes |
| Frango congelado / transporte | Recuperação de RAG-03 |
| Validade / embalagem de arroz | Recuperação de RAG-01 |
| Consulta fora do domínio (“galáxia...”) | Nenhuma referência recuperada |
| Limite de documentos | No máximo 2 ao configurar limite 2 |

## 3. Avaliação exploratória da busca

Execute `python .\scripts\avaliar_rag.py`. O script lista cada cenário, IDs recuperados, acerto em `top-3` e a consulta negativa. O indicador **Hit@3** é a razão entre consultas positivas em que o trecho esperado apareceu entre os três primeiros e o total de consultas positivas. É uma métrica exploratória baseada em consultas sintéticas, **não uma medida de qualidade do LLM ou validação científica**. Registre a saída real antes de preencher conclusões quantitativas.

## 4. Teste da geração real com o modelo

**Pendente até execução e captura.** Escolha um dos caminhos:

- **Prioritário para a disciplina:** executar a API Java completa com o provedor de IA e registrar a resposta real de `POST /assistente/sugestao`.
- **Complementar, sem Oracle:** executar `python scripts/demo_rag_local.py` com Ollama. Isso demonstra recuperação + geração sobre a base, mas **não confirma integração Java/Oracle/Mobile**.

Cenários sugeridos para demonstrar: (1) arroz e feijão; (2) frango congelado; (3) inclusão de sabonete como item fora do catálogo; (4) texto fora do domínio; (5) indisponibilidade do modelo. Não apresente como comprovado algo que não foi executado.

## 5. Evidências a capturar e anexar

1. `evidencias/01-maven-testes.png` — final do `mvn clean test` com 23 testes e `BUILD SUCCESS`.
2. `evidencias/02-rag-metricas.png` — resultado de `python scripts/avaliar_rag.py` com os casos e Hit@3.
3. `evidencias/03-rag-referencias.png` — IDs e fontes recuperadas pelo modo `--sem-llm`.
4. `evidencias/04-ia-resposta-real.png` — resposta do modelo em Ollama ou do endpoint Java, com legenda que diferencia os dois modos.
5. `evidencias/05-endpoint-integrado.png` — se possível, Swagger com `POST /assistente/sugestao`, status 200 e resposta; **não expor token**.

Não inclua senhas, chave de API, token JWT ou dados pessoais de beneficiários nos prints.

## 6. Limitações e trabalho futuro

- Base de 10 trechos; cobertura temática reduzida.
- Recuperação lexical não captura adequadamente todos os sinônimos e erros de digitação.
- Sem embeddings ou banco vetorial; não há ranqueamento semântico.
- Modelo generativo pode alucinar; o prompt instrui que não atribua segurança sanitária.
- Texto e referência não são atualizados automaticamente; curadoria humana é necessária.
- A qualidade da geração depende do provedor, do modelo e do contexto disponibilizado.

## Resultado da avaliacao de 18/11/2026

- Base de conhecimento: 10 trechos.
- Consultas sinteticas positivas: 8.
- Acertos de recuperacao no Top 3: 8.
- Hit@3: 100% (8/8).
- Consulta fora do dominio: nenhum trecho retornado.
- Limite: esta avaliacao nao mede a qualidade da resposta gerada pelo LLM.

