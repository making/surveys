#!/bin/bash
set -eox
./mvnw clean package
VERSION=$(grep '<version>' pom.xml | head -n 2 | tail -n 1 | sed -e 's|<version>||g' -e 's|</version>||g' -e 's| ||g')
pack build making/surveys:${VERSION} \
  -p surveys-backend/target/surveys-backend-*-SNAPSHOT.jar \
  --publish \
  --builder making/java-cnb-builder

kapp deploy -a surveys -c --wait -f <(kbld -f k8s)