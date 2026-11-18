#!/usr/bin/env python3
"""Demonstração *isolada* de RAG lexical + Ollama usando o TSV da aplicação Java.

Não inicializa a API Java, Oracle ou RabbitMQ e não comprova a integração Java ponta a ponta.
Utiliza apenas bibliotecas padrão do Python.
"""
import argparse
import json
import re
import sys
import unicodedata
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

BASE = Path(__file__).resolve().parents[1] / "src/main/resources/rag/guia-doacao.tsv"
STOPWORDS = {
    "a", "o", "as", "os", "um", "uma", "uns", "umas", "de", "do", "da", "dos", "das",
    "e", "ou", "que", "para", "por", "com", "sem", "ao", "aos", "na", "nas", "no", "nos",
    "em", "se", "eu", "tenho", "quero", "fazer", "meu", "minha", "ele", "ela", "ser",
    "tem", "sao", "foi", "estou", "esta", "esse", "essa", "isso", "como", "qual", "quais",
    "mais", "pode", "podem", "preciso", "sobre", "orientacao", "orientacoes"
}


def tokens(texto):
    texto = unicodedata.normalize("NFD", (texto or "").lower())
    texto = "".join(c for c in texto if unicodedata.category(c) != "Mn")
    return {t for t in re.sub(r"[^a-z0-9]+", " ", texto).split()
            if len(t) > 2 and t not in STOPWORDS}


def carregar_base(caminho=BASE):
    if not caminho.is_file():
        raise FileNotFoundError(f"Base TSV não encontrada: {caminho}")
    trechos = []
    for num, linha in enumerate(caminho.read_text(encoding="utf-8").splitlines(), 1):
        if not linha.strip() or linha.startswith("#"):
            continue
        p = linha.split("\t")
        if len(p) != 5:
            raise ValueError(f"Linha {num}: esperadas 5 colunas TSV, obtidas {len(p)}")
        trechos.append(dict(zip(("id", "assunto", "conteudo", "fonte", "url"), p)))
    if not trechos:
        raise ValueError("Base TSV vazia")
    return trechos


def recuperar(consulta, limite=3, trechos=None):
    if limite <= 0:
        return []
    base = trechos if trechos is not None else carregar_base()
    termos = tokens(consulta)
    if not termos:
        return []
    pontuados = []
    for t in base:
        assunto = tokens(t["assunto"])
        conteudo = tokens(t["conteudo"])
        score = sum(3 * (termo in assunto) + (termo in conteudo) for termo in termos)
        if score > 0:
            pontuados.append((score, t))
    pontuados.sort(key=lambda par: (-par[0], par[1]["id"]))
    return [item for _, item in pontuados[:min(limite, 5)]]


def gerar_ollama(texto, recuperados, modelo="llama3.2", url="http://127.0.0.1:11434"):
    contexto = "\n\n".join(f"[{t['id']}] {t['assunto']} | fonte: {t['fonte']}\n{t['conteudo']}" for t in recuperados)
    if not contexto:
        contexto = "Nenhuma referência pertinente foi recuperada; não invente orientações sanitárias."
    system = (
        "Você auxilia o cadastro de doações de alimentos do EloSolidário. "
        "Use o contexto recuperado apenas para orientações gerais. "
        "Não declare que um alimento é seguro para consumo. "
        "Destaque que o doador deve conferir validade, embalagem e conservação. "
        "Baseie todas as orientações nas referências recuperadas. "
        "Não invente temperaturas numéricas, prazos ou exigências sanitárias "
        "que não estejam explicitamente presentes nas referências. "
        "Para alimentos secos, como arroz e feijão em embalagens fechadas, "
        "oriente a seguir o rótulo e proteger contra umidade, sujeira e pragas. "
        "Não recomende refrigeração para alimentos secos sem indicação do fabricante. "
        "Se as fontes não sustentarem uma orientação, omita-a. "
        "Responda em português com um resumo curto dos alimentos e orientações, "
        "citando os IDs dos trechos quando forem pertinentes.\n\n"
        f"Contexto:\n{contexto}"
    )
    payload = json.dumps({
        "model": modelo,
        "stream": False,
        "messages": [{"role": "system", "content": system},
                     {"role": "user", "content": texto}],
        "options": {"temperature": 0.2}
    }).encode("utf-8")
    req = Request(url.rstrip("/") + "/api/chat", data=payload,
                  headers={"Content-Type": "application/json"}, method="POST")
    with urlopen(req, timeout=90) as response:
        body = json.load(response)
    return body.get("message", {}).get("content", "<sem conteúdo retornado>")


def main():
    parser = argparse.ArgumentParser(description="Demonstração isolada de RAG e IA local com Ollama")
    parser.add_argument("--texto", default="Tenho 3 pacotes de arroz e 2 de feijao para doar")
    parser.add_argument("--modelo", default="llama3.2")
    parser.add_argument("--url", default="http://127.0.0.1:11434")
    parser.add_argument("--sem-llm", action="store_true", help="Mostra apenas recuperação, sem IA")
    args = parser.parse_args()
    try:
        base = carregar_base()
        recuperados = recuperar(args.texto, 3, base)
        print("MODO DE DEMONSTRAÇÃO ISOLADA (não substitui endpoint Java)")
        print(f"Trechos no corpus: {len(base)} | Consulta: {args.texto}")
        for item in recuperados:
            print(f"- {item['id']}: {item['assunto']} | {item['fonte']}\n  {item['url']}")
        if not recuperados:
            print("Nenhuma referência pertinente recuperada.")
        if args.sem_llm:
            print("IA não chamada (--sem-llm).")
            return 0
        print("\nResposta gerada pelo modelo local:")
        print(gerar_ollama(args.texto, recuperados, args.modelo, args.url))
        print("\nNota: revisão humana obrigatória; não é certificação sanitária.")
        return 0
    except (FileNotFoundError, ValueError, HTTPError, URLError, TimeoutError) as e:
        print(f"Não foi possível completar a demonstração: {e}", file=sys.stderr)
        print("Verifique o TSV, execute 'ollama list' e confirme que o Ollama está ativo.", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
