#!/usr/bin/env bash
set -euo pipefail

timeout=${WAIT_TIMEOUT:-300}
if [[ ! "$timeout" =~ ^[1-9][0-9]*$ ]]; then
  echo 'WAIT_TIMEOUT must be a positive number of seconds.' >&2
  exit 1
fi

deadline=$((SECONDS + timeout))
while (( SECONDS < deadline )); do
  if docker compose exec -T mysql mysql -uroot -psecret -Nse 'SELECT 1' >/dev/null 2>&1 &&
     curl --fail --silent --max-time 3 http://localhost:9001/.well-known/openid-configuration >/dev/null &&
     curl --fail --silent --max-time 3 http://localhost:8081/graphql \
       -H 'Content-Type: application/json' -d '{"query":"{ __typename }"}' | grep -q '"__typename"' &&
     curl --fail --silent --max-time 3 http://localhost:3001/ >/dev/null; then
    echo 'MySQL, auth, API and frontend are ready.'
    exit 0
  fi
  sleep 2
done

echo "Stand did not become ready within $timeout seconds. Check the service logs." >&2
exit 1
