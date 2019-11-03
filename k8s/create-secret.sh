#!/bin/bash
set -e

echo -n "Admin Username: "
read -s ADMIN_USERNAME
echo
echo -n "Admin Password: "
read -s ADMIN_PASSWORD
echo
echo -n "Postgres Password: "
read -s POSTGRES_PASSWORD
echo

kubectl -n surveys create secret generic surveys-secret \
  --from-literal=admin-username=${ADMIN_USERNAME} \
  --from-literal=admin-password=${ADMIN_PASSWORD} \
  --from-literal=postgres-password=${POSTGRES_PASSWORD} \
  --dry-run \
  -o yaml \
  > surveys-secret.yml