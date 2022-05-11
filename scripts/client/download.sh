#!/usr/bin/env bash

objectKey=$1

curl -v -X GET \
  -H "x-pagopa-safestorage-cx-id: pn-delivery-002" \
  https://07ivpqaag5.execute-api.eu-south-1.amazonaws.com/dev/safe-storage/v1/files/$objectKey \
  \
  | tee > ${TMPDIR}/out.txt

url=$( cat ${TMPDIR}/out.txt | jq -r ".download.uri")
key=$( cat ${TMPDIR}/out.txt | jq -r ".key")


curl -v -X GET \
  -H "Content-TYpe: application/pdf" \
  --output ${key}.pdf \
  $url
