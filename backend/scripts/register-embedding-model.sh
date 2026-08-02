#!/usr/bin/env bash
# One-time setup: registers and deploys the multilingual embedding model
# (paraphrase-multilingual-MiniLM-L12-v2, 384 dims) chosen in BOH-23
# (see docs/semantic-car-search/design.md, "Embedding model & vector
# architecture") as an OpenSearch ML Commons custom local model.
#
# Run once per OpenSearch cluster (fresh local dev environment, or after
# wiping the cluster's .plugins-ml-* indices). Not part of application
# startup -- model download/load is slow and this only needs to happen
# once per cluster, not once per backend restart.
#
# Usage: ./register-embedding-model.sh [opensearch-host:port]
# Prints the deployed model ID and the application.yml value to set.
#
# Uses `node` (not python3/jq) for JSON field extraction, since node is
# the one interpreter this repo already assumes is present (frontend/).

set -euo pipefail

HOST="${1:-localhost:9200}"
BASE="http://${HOST}"

# Reads JSON from stdin, prints the value at the given dotted field path.
jf() {
  node -e "
    let d='';
    process.stdin.on('data', c => d += c);
    process.stdin.on('end', () => {
      const v = '$1'.split('.').reduce((o, k) => (o == null ? o : o[k]), JSON.parse(d));
      if (v === undefined) { process.stderr.write('field \"$1\" not found in: ' + d + '\n'); process.exit(1); }
      console.log(v);
    });
  "
}

echo "Enabling ML Commons on a non-dedicated ML node (local single-node dev cluster)..." >&2
curl -s -X PUT "${BASE}/_cluster/settings" -H "Content-Type: application/json" -d '{
  "persistent": {
    "plugins.ml_commons.only_run_on_ml_node": false,
    "plugins.ml_commons.model_access_control_enabled": true,
    "plugins.ml_commons.native_memory_threshold": 100
  }
}' > /dev/null

echo "Registering model group..." >&2
MODEL_GROUP_ID=$(curl -s -X POST "${BASE}/_plugins/_ml/model_groups/_register" -H "Content-Type: application/json" -d '{
  "name": "car-searcher-embedding-models",
  "description": "Local models for car-searcher semantic search"
}' | jf model_group_id)
echo "model_group_id=${MODEL_GROUP_ID}" >&2

echo "Registering paraphrase-multilingual-MiniLM-L12-v2 (async)..." >&2
REGISTER_TASK_ID=$(curl -s -X POST "${BASE}/_plugins/_ml/models/_register" -H "Content-Type: application/json" -d "{
  \"name\": \"huggingface/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2\",
  \"version\": \"1.0.1\",
  \"model_group_id\": \"${MODEL_GROUP_ID}\",
  \"model_format\": \"TORCH_SCRIPT\"
}" | jf task_id)

echo "Waiting for registration task ${REGISTER_TASK_ID} to complete..." >&2
MODEL_ID=""
for i in $(seq 1 60); do
  TASK=$(curl -s "${BASE}/_plugins/_ml/tasks/${REGISTER_TASK_ID}")
  STATE=$(echo "${TASK}" | jf state)
  if [ "${STATE}" = "COMPLETED" ]; then
    MODEL_ID=$(echo "${TASK}" | jf model_id)
    break
  elif [ "${STATE}" = "FAILED" ]; then
    echo "Model registration failed:" >&2
    echo "${TASK}" >&2
    exit 1
  fi
  sleep 2
done
if [ -z "${MODEL_ID}" ]; then
  echo "Timed out waiting for model registration." >&2
  exit 1
fi
echo "model_id=${MODEL_ID}" >&2

echo "Deploying model ${MODEL_ID} (async)..." >&2
DEPLOY_TASK_ID=$(curl -s -X POST "${BASE}/_plugins/_ml/models/${MODEL_ID}/_deploy" | jf task_id)

echo "Waiting for deploy task ${DEPLOY_TASK_ID} to complete..." >&2
for i in $(seq 1 60); do
  TASK=$(curl -s "${BASE}/_plugins/_ml/tasks/${DEPLOY_TASK_ID}")
  STATE=$(echo "${TASK}" | jf state)
  if [ "${STATE}" = "COMPLETED" ]; then
    break
  elif [ "${STATE}" = "FAILED" ]; then
    echo "Model deployment failed:" >&2
    echo "${TASK}" >&2
    exit 1
  fi
  sleep 2
done

echo "" >&2
echo "Done. Set this as an environment variable before starting the backend" >&2
echo "(don't hardcode it into application.yml -- it's checked into git and" >&2
echo "this ID is unique to this OpenSearch cluster):" >&2
echo "" >&2
echo "  export CAR_SEARCHER_EMBEDDING_MODEL_ID=${MODEL_ID}" >&2
echo "" >&2
echo "${MODEL_ID}"
