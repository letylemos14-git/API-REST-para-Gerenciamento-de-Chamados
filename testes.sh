#!/usr/bin/env bash
# Uso: com a API rodando (./mvnw spring-boot:run), execute: ./testes.sh [URL_BASE]
BASE="${1:-http://localhost:8080}"

t() { echo; echo "=== $1"; shift; echo "\$ curl $*"; curl -s -i "$@"; echo; }

t "1. Listar todos"                       "$BASE/chamados"
t "2. Consultar por ID (existente)"       "$BASE/chamados/1"
t "3. Consultar por ID (inexistente)"     "$BASE/chamados/999"
t "4. Filtrar por setor"                  "$BASE/chamados/setor?nome=financeiro"
t "5. Filtrar por prioridade"             "$BASE/chamados/prioridade?nivel=alta"
t "6. Buscar por titulo (sem diferenciar maiusculas)" "$BASE/chamados/busca?texto=IMPRESSORA"
t "7. Busca sem correspondencia"          "$BASE/chamados/busca?texto=satelite"
t "8. Criticos (ALTA e nao resolvidos)"   "$BASE/chamados/criticos"
t "9. Resumo por status"                  "$BASE/chamados/resumo"
t "10. Cadastrar novo chamado"            -X POST "$BASE/chamados" -H "Content-Type: application/json" \
  -d '{"titulo":"Teclado sem resposta","solicitante":"Maria Silva","setor":"Juridico","prioridade":"media"}'
t "11. Verificar cadastro (id 13)"        "$BASE/chamados/13"
t "12. Resumo apos cadastro"              "$BASE/chamados/resumo"
t "13. POST invalido (sem titulo)"        -X POST "$BASE/chamados" -H "Content-Type: application/json" \
  -d '{"solicitante":"Maria","setor":"TI","prioridade":"ALTA"}'
t "14. Prioridade invalida"               "$BASE/chamados/prioridade?nivel=urgente"
