#!/usr/bin/env bash
# ==============================================================================
# Sobe o ambiente de staging (API + Oracle), espera ficar saudavel e roda a
# suite de testes de API e BDD contra ele.
#
#   ./run-tests.sh            # sobe o staging e roda tudo
#   ./run-tests.sh --sem-subir  # usa uma API que ja esta no ar
#
# Variaveis: API_BASE_URL (padrao http://localhost:8081)
# ==============================================================================
set -euo pipefail

cd "$(dirname "$0")"
export API_BASE_URL="${API_BASE_URL:-http://localhost:8081}"

if [[ "${1:-}" != "--sem-subir" ]]; then
  echo ">> Subindo o ambiente de staging..."
  docker compose -f ../docker-compose.staging.yml up -d
fi

echo ">> Aguardando ${API_BASE_URL}/actuator/health responder UP..."
for i in $(seq 1 60); do
  if curl -sf "${API_BASE_URL}/actuator/health" | grep -q '"status":"UP"'; then
    echo ">> API no ar apos $(( (i - 1) * 5 ))s de espera."
    break
  fi
  if [[ "$i" == 60 ]]; then
    echo "!! A API nao ficou saudavel em 5 minutos." >&2
    exit 1
  fi
  sleep 5
done

echo ">> Rodando os testes..."
mvn -B test

echo ">> Relatorio: $(pwd)/target/cucumber-report/index.html"
