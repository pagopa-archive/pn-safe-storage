fileToUpload=$1

curl -v -X POST \
  -H "x-pagopa-safestorage-cx-id: pn-delivery-001" \
  -H "Content-TYpe: application/json" \
  -d '{ "contentType": "application/pdf", "documentType": "PN_NOTIFICATION_ATTACHMENTS", "status":"PRELOADED" }' \
  https://76k5rdz8h5.execute-api.eu-south-1.amazonaws.com/dev/safe-storage/v1/files \
  \
  | tee -a logs.txt > out.txt

method=$( cat out.txt | jq -r ".uploadMethod")
url=$( cat out.txt | jq -r ".uploadUrl")
key=$( cat out.txt | jq -r ".key")
secret=$( cat out.txt | jq -r ".secret")



curl -v -X $method \
  -H "Content-Type: application/pdf" \
  -H "x-amz-meta-secret: $secret" \
  --upload-file ${fileToUpload} \
  $url

echo "File uploaded with key = $key"

