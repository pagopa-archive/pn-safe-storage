# SCRIPT per testare la "condivisione di file tra più utenti"

#api_domain=14ej9p2krd.execute-api.eu-south-1.amazonaws.com
api_domain=ewlg4idc89.execute-api.eu-south-1.amazonaws.com
#api_domain=eo6pdvms24.execute-api.eu-south-1.amazonaws.com
stage='dev'


./upload_file.sh -a $api_domain -f multa.pdf -t PN_LEGAL_FACTS -s SAVED -c pn-delivery-push | tee up_and_down1.txt
key1=$( cat up_and_down1.txt | grep 'Key:' | sed -e 's/Key: //')
echo "UPLOAD WITH KEY: ${key1}"
./upload_file.sh -a $api_domain -f multa.pdf -t PN_LEGAL_FACTS -s SAVED -c pn-delivery-push | tee up_and_down2.txt
key2=$( cat up_and_down2.txt | grep 'Key:' | sed -e 's/Key: //')
echo "UPLOAD WITH KEY: ${key2}"
echo "### UPDATE METADATA on ${api_endpoint}"
curl -fvs -H"x-pagopa-safestorage-cx-id: pn-delivery-push" \
-XPOST https://${api_domain}/${stage}/safe-storage/v1/files/{key1} -d"{\"status\": \"ATTACHED\",\"retentionUntil\": \"2022-09-17T14:59:28.028Z\"}"

curl -fvs -H"x-pagopa-safestorage-cx-id: pn-delivery-push" \
-XPOST https://${api_domain}/${stage}/safe-storage/v1/files/{key2} -d"{\"status\": \"ATTACHED\",\"retentionUntil\": \"2022-09-17T14:59:28.028Z\"}"
