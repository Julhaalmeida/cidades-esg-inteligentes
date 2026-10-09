#!/usr/bin/env bash
# =============================================================================
# Smoke tests: confirma que uma instância da API está saudável e respondendo.
#
# Uso:
#   bash scripts/smoke-test.sh <url-base> [ambiente-esperado] [--escrita]
#
# Exemplos:
#   bash scripts/smoke-test.sh http://localhost:8080 local --escrita
#   bash scripts/smoke-test.sh https://cidades-esg-production.onrender.com production
#
# --escrita  também cadastra uma cidade de teste, registra um indicador, calcula
#            o score e remove a cidade. Usado no Compose do CI e em staging;
#            em produção os testes são somente leitura.
# =============================================================================
set -uo pipefail

BASE_URL="${1:?Informe a URL base, ex.: http://localhost:8080}"
BASE_URL="${BASE_URL%/}"
AMBIENTE_ESPERADO="${2:-}"
MODO_ESCRITA="${3:-}"

CORPO="$(mktemp)"
trap 'rm -f "$CORPO"' EXIT
TOTAL=0
FALHAS=0
RESUMO=()

# requisicao <método> <caminho> [json]  -> imprime o status HTTP; o corpo fica em $CORPO
requisicao() {
  local metodo="$1" caminho="$2" dados="${3:-}" codigo
  local args=(-sS -o "$CORPO" -w '%{http_code}' --max-time 90 -X "$metodo")
  if [[ -n "$dados" ]]; then
    args+=(-H 'Content-Type: application/json' --data "$dados")
  fi
  codigo="$(curl "${args[@]}" "$BASE_URL$caminho" 2>/dev/null)" || true
  echo "${codigo:-000}"
}

# verificar <descrição> <status esperado> <status obtido> [trecho esperado no corpo]
verificar() {
  local descricao="$1" esperado="$2" obtido="$3" trecho="${4:-}"
  TOTAL=$((TOTAL + 1))
  if [[ "$obtido" == "$esperado" ]] && { [[ -z "$trecho" ]] || grep -qF -- "$trecho" "$CORPO"; }; then
    echo "  ✅ $descricao (HTTP $obtido)"
    RESUMO+=("| ✅ | $descricao | $obtido |")
  else
    FALHAS=$((FALHAS + 1))
    local detalhe=""
    [[ -n "$trecho" ]] && detalhe=" contendo [$trecho]"
    echo "  ❌ $descricao: esperado HTTP $esperado$detalhe, recebido HTTP $obtido"
    echo "     resposta: $(head -c 300 "$CORPO" 2>/dev/null)"
    RESUMO+=("| ❌ | $descricao | $obtido |")
  fi
}

echo "🔎 Smoke tests em $BASE_URL"

verificar "Health check (aplicação)" 200 "$(requisicao GET /actuator/health)" '"status":"UP"'
verificar "Readiness (aplicação + banco)" 200 "$(requisicao GET /actuator/health/readiness)" '"status":"UP"'
if [[ -n "$AMBIENTE_ESPERADO" ]]; then
  verificar "Ambiente '$AMBIENTE_ESPERADO' em /api/info" 200 "$(requisicao GET /api/info)" "\"ambiente\":\"$AMBIENTE_ESPERADO\""
else
  verificar "Versão em /api/info" 200 "$(requisicao GET /api/info)" '"versao"'
fi
verificar "Ranking ESG" 200 "$(requisicao GET /api/ranking)" '"scoreGeral"'
verificar "Catálogo de indicadores" 200 "$(requisicao GET /api/indicadores/tipos)" 'ENERGIA_RENOVAVEL'
verificar "Documentação OpenAPI" 200 "$(requisicao GET /v3/api-docs)" '"openapi"'
verificar "Página inicial" 200 "$(requisicao GET /)" 'Cidades ESG Inteligentes'

if [[ "$MODO_ESCRITA" == "--escrita" ]]; then
  NOME="Smoke Test $(date +%s)-$RANDOM"
  HOJE="$(date -u +%Y-%m-%d)"
  verificar "Cadastrar cidade de teste" 201 \
    "$(requisicao POST /api/cidades "{\"nome\":\"$NOME\",\"uf\":\"SP\",\"populacao\":1000}")" '"id"'
  ID="$(sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p' "$CORPO" | head -n 1)"
  if [[ -n "$ID" ]]; then
    verificar "Registrar indicador ESG" 201 \
      "$(requisicao POST "/api/cidades/$ID/indicadores" "{\"tipo\":\"ENERGIA_RENOVAVEL\",\"valor\":80,\"dataReferencia\":\"$HOJE\",\"fonte\":\"smoke test\"}")" \
      '"notaNormalizada":80.0'
    verificar "Calcular score da cidade" 200 "$(requisicao GET "/api/cidades/$ID/score")" '"scoreGeral":80.0'
    verificar "Remover cidade de teste" 204 "$(requisicao DELETE "/api/cidades/$ID")"
    verificar "Cidade removida responde 404" 404 "$(requisicao GET "/api/cidades/$ID")"
  else
    TOTAL=$((TOTAL + 1))
    FALHAS=$((FALHAS + 1))
    echo "  ❌ não foi possível ler o id da cidade criada"
    RESUMO+=("| ❌ | Ler o id da cidade criada | - |")
  fi
fi

# Resumo na página da execução do GitHub Actions
if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "### 🔎 Smoke tests: ${AMBIENTE_ESPERADO:-$BASE_URL}"
    echo
    echo "URL: $BASE_URL"
    echo
    echo "| | Verificação | HTTP |"
    echo "|---|---|---|"
    printf '%s\n' "${RESUMO[@]}"
    echo
  } >> "$GITHUB_STEP_SUMMARY"
fi

echo
if (( FALHAS > 0 )); then
  echo "❌ $FALHAS de $TOTAL verificações falharam"
  exit 1
fi
echo "✅ $TOTAL verificações passaram"
