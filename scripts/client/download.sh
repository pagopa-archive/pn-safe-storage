objectKey=$1

curl -v -X GET \
  -H "x-pagopa-safestorage-cx-id: pn-delivery-001" \
  https://76k5rdz8h5.execute-api.eu-south-1.amazonaws.com/dev/safe-storage/v1/files/$objectKey \
  \
  | tee -a logs.txt > out.txt

url=$( cat out.txt | jq -r ".download.uri")
key=$( cat out.txt | jq -r ".key")


curl -v -X GET \
  -H "Content-TYpe: application/pdf" \
  --output ${key}.pdf \
  $url
