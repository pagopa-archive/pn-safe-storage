# SCRIPT per testare la "condivisione di file tra più utenti"

# Ambiente sandbox
#api_domain=14ej9p2krd.execute-api.eu-south-1.amazonaws.com
#stage='dev'

# Ambiente SVIL
# api_domain=ewlg4idc89.execute-api.eu-south-1.amazonaws.com
# stage='dev'

# Ambiente COLL
api_domain=eo6pdvms24.execute-api.eu-south-1.amazonaws.com
stage='coll'


./upload_file.sh -a $api_domain -p $stage -f multa.pdf -t PN_NOTIFICATION_ATTACHMENTS -s PRELOADED -c pn-delivery \
  | tee up_and_metadata.txt
key1=$( cat up_and_metadata.txt | grep 'Key:' | sed -e 's/Key: //')
echo ""
echo "====> UPLOADED 1 WITH KEY: ${key1}"

./upload_file.sh -a $api_domain -p $stage -f multa.pdf -t PN_NOTIFICATION_ATTACHMENTS -s PRELOADED -c pn-delivery \
  | tee up_and_metadata.txt
key2=$( cat up_and_metadata.txt | grep 'Key:' | sed -e 's/Key: //')
echo ""
echo "====> UPLOADED 2 WITH KEY: ${key2}"


echo ""
echo ""
echo ""
echo "########################################################"
echo "###                 WAIT 10 SECONDS                  ###"
echo "########################################################"
sleep 10
echo "====> GET FILE METADATA FOR KEY 1: ${key1} "
./download.sh -a $api_domain -p $stage -k ${key1} -c pn-delivery-push 
echo "====> GET FILE METADATA FOR KEY 2: ${key2} "
./download.sh -a $api_domain -p $stage -k ${key2} -c pn-delivery-push 

echo ""
echo ""
echo ""
echo "====> TRY TO UPDATE METADATA FOR KEY 1: ${key1}"
curl -v -H "Content-Type: application/json" -H"x-pagopa-safestorage-cx-id: pn-delivery" \
    -XPOST https://${api_domain}/${stage}/safe-storage/v1/files/${key1} -d"{\"status\": \"ATTACHED\" }"

echo ""
echo ""
echo ""
echo "====> TRY TO UPDATE METADATA FOR KEY 2: ${key2}"
curl -v -H "Content-Type: application/json" -H"x-pagopa-safestorage-cx-id: pn-delivery" \
    -XPOST https://${api_domain}/${stage}/safe-storage/v1/files/${key2} -d"{\"status\": \"ATTACHED\" }"


