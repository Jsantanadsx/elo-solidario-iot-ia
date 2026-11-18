# Roteiro rápido de demonstração — IoT/IA

**Objetivo:** produzir evidências reais sem depender inicialmente do banco Oracle.

## A. Executar testes da integração Java

No PowerShell, na pasta do repositório exclusivo de IoT:

```powershell
mvn clean test
```

Registrar o final do terminal com `Tests run: 23` e `BUILD SUCCESS`. O resultado já foi observado uma vez em 18/11; uma nova execução confirma o estado atual do novo repositório.

## B. Demonstrar a base RAG e métricas

```powershell
python .\scripts\avaliar_rag.py
python .\scripts\demo_rag_local.py --sem-llm --texto "Como transportar frango congelado para doacao?"
```

Fazer prints dos trechos recuperados e das fontes.

## C. Demonstrar IA real sem banco (opcional, mas recomendado se o tempo estiver curto)

Com Ollama disponível, execute:

```powershell
ollama list
python .\scripts\demo_rag_local.py --texto "Tenho arroz e feijao e quero saber como armazenar para doacao"
```

A saída deverá conter os trechos recuperados e um texto gerado pelo modelo local. Esse comando faz uma **demonstração independente em Python**, não o endpoint Java. Use essa descrição nas evidências.

## D. Demonstrar endpoint Java (mais forte, mas requer infraestrutura)

1. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `RABBITMQ_URL` e as variáveis `AI_*` no **terminal local**, nunca no GitHub.
2. Inicie `mvn spring-boot:run`.
3. Use Swagger em `http://localhost:8080/swagger-ui.html`.
4. Faça login como DOADOR; envie `POST /assistente/sugestao` com `{"texto":"Tenho 3 pacotes de arroz e 2 de feijao"}`.
5. Registre apenas a resposta, sem exibir token ou credenciais.

Se o backend não iniciar por ausência de banco ou broker, registre como **não validado ponta a ponta**; não invente resultado.

## E. Fechar a entrega

Confira o README, inclua prints em `evidencias/`, faça commit dos novos arquivos e envie o conteúdo do `ENTREGA-IOT-FIAP.txt` no Portal da FIAP conforme as instruções da disciplina.
