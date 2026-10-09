#!/usr/bin/env bash
# =============================================================================
# Deploy no Render via Deploy Hook, com verificação da versão publicada.
#
# Uso:
#   bash scripts/render-deploy.sh <ambiente> <sha-do-commit>
#
# Variáveis de ambiente:
#   RENDER_DEPLOY_HOOK  (obrigatória) URL secreta do deploy hook do serviço
#   APP_URL             (obrigatória) URL pública do serviço no Render
#   DEPLOY_TIMEOUT      (opcional)    segundos de espera pela nova versão (padrão: 1200)
#   DEPLOY_INTERVALO    (opcional)    segundos entre as consultas a /api/info (padrão: 20)
#
# Como funciona:
#   1. chama o deploy hook com "ref=<sha>", para o Render construir exatamente esse commit;
#   2. consulta /api/info até a aplicação responder com o mesmo commit (ou até o timeout).
#   Enquanto isso, a versão anterior continua no ar (deploy sem indisponibilidade).
# =============================================================================
set -euo pipefail

AMBIENTE="${1:?Informe o ambiente (staging ou production)}"
COMMIT="${2:?Informe o SHA do commit}"
COMMIT_CURTO="${COMMIT:0:7}"
TIMEOUT="${DEPLOY_TIMEOUT:-1200}"
INTERVALO="${DEPLOY_INTERVALO:-20}"

if [[ -z "${RENDER_DEPLOY_HOOK:-}" ]]; then
  echo "::error title=Deploy hook não configurado::Crie o secret com o deploy hook de $AMBIENTE (README, seção 'Configurar o deploy no Render')."
  exit 1
fi
if [[ -z "${APP_URL:-}" ]]; then
  echo "::error title=URL não configurada::Crie a variável com a URL de $AMBIENTE (README, seção 'Configurar o deploy no Render')."
  exit 1
fi
APP_URL="${APP_URL%/}"

echo "🚀 Disparando o deploy do commit $COMMIT_CURTO em $AMBIENTE..."
SEPARADOR='&'
[[ "$RENDER_DEPLOY_HOOK" == *\?* ]] || SEPARADOR='?'
if ! RESPOSTA="$(curl -sS --fail-with-body --max-time 60 -X POST "${RENDER_DEPLOY_HOOK}${SEPARADOR}ref=${COMMIT}")"; then
  echo "::error title=Deploy recusado::O Render não aceitou o deploy hook. Resposta: ${RESPOSTA:-sem resposta}"
  exit 1
fi
DEPLOY_ID="$(sed -n 's/.*"id":"\([^"]*\)".*/\1/p' <<<"$RESPOSTA")"
echo "Deploy aceito pelo Render${DEPLOY_ID:+ (id: $DEPLOY_ID)}"

echo "⏳ Aguardando $APP_URL/api/info responder com o commit $COMMIT_CURTO (até $((TIMEOUT / 60)) min)..."
INICIO="$(date +%s)"
while true; do
  INFO="$(curl -sS --max-time 30 "$APP_URL/api/info" 2>/dev/null || true)"
  DECORRIDO=$(( $(date +%s) - INICIO ))
  if grep -qF "\"commit\":\"$COMMIT_CURTO\"" <<<"$INFO"; then
    echo "✅ $AMBIENTE no ar com o commit $COMMIT_CURTO após ${DECORRIDO}s"
    echo "$INFO"
    break
  fi
  if (( DECORRIDO >= TIMEOUT )); then
    echo "::error title=Timeout no deploy::$AMBIENTE não respondeu com o commit $COMMIT_CURTO em ${TIMEOUT}s. Veja os logs do serviço no painel do Render."
    echo "Última resposta de /api/info: ${INFO:-sem resposta}"
    exit 1
  fi
  ATUAL="$(sed -n 's/.*"commit":"\([^"]*\)".*/\1/p' <<<"$INFO")"
  echo "   ${DECORRIDO}s: versão no ar = ${ATUAL:-indisponível (build ou inicialização em andamento)}"
  sleep "$INTERVALO"
done

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "### 🚀 Deploy em $AMBIENTE"
    echo
    echo "| Item | Valor |"
    echo "|---|---|"
    echo "| URL | $APP_URL |"
    echo "| Commit publicado | \`$COMMIT_CURTO\` |"
    echo "| Deploy no Render | ${DEPLOY_ID:-em fila} |"
    echo "| Tempo até a nova versão responder | ${DECORRIDO}s |"
    echo
  } >> "$GITHUB_STEP_SUMMARY"
fi
