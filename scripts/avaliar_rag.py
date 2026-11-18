#!/usr/bin/env python3
"""Métrica exploratória, sem LLM: Hit@3 sobre casos sintéticos predeterminados.

O resultado descreve o algoritmo Python de demonstração com a mesma base e pesos
lexicais do serviço Java. Não representa desempenho do LLM ou evidência de produção.
"""
from demo_rag_local import carregar_base, recuperar

CASOS = [
    ("Como transportar frango congelado?", "RAG-03"),
    ("Pacote de arroz validade embalagem", "RAG-01"),
    ("Como armazenar arroz e feijao seco evitando umidade", "RAG-02"),
    ("Cuidados com alimentos preparados e refeicoes prontas", "RAG-06"),
    ("Como higienizar superficies e evitar contaminacao cruzada", "RAG-05"),
    ("Como realizar transporte e retirada de doacoes", "RAG-04"),
    ("Separar sabonete detergente dos alimentos da doacao", "RAG-09"),
    ("Como reduzir perdas e desperdicio alimentar", "RAG-08"),
]
NEGATIVO = "astronauta galaxia telescopio"


def main():
    trechos = carregar_base()
    acertos = 0
    print("AVALIAÇÃO SINTÉTICA DA RECUPERAÇÃO LEXICAL (sem chamar LLM)")
    print(f"Base: {len(trechos)} trechos | Top K: 3\n")
    for i, (consulta, esperado) in enumerate(CASOS, 1):
        encontrados = [t["id"] for t in recuperar(consulta, 3, trechos)]
        ok = esperado in encontrados
        acertos += ok
        print(f"{i:02d}. {'ACERTO' if ok else 'ERRO':6} esperado={esperado} retornados={encontrados}")
        print(f"    {consulta}")
    neg = [t["id"] for t in recuperar(NEGATIVO, 3, trechos)]
    print(f"\nConsulta fora do domínio: {NEGATIVO} => {neg} ({'CORRETO' if not neg else 'FALHOU'})")
    print(f"Hit@3: {acertos}/{len(CASOS)} = {100.0 * acertos / len(CASOS):.1f}%")
    print("LIMITAÇÃO: amostra pequena, sintética e sem avaliação das respostas geradas.")
    return 0 if not neg else 1


if __name__ == "__main__":
    raise SystemExit(main())
