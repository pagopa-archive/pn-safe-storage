# SCRIPT per testare la "condivisione di file tra più utenti"

api_domain=14ej9p2krd.execute-api.eu-south-1.amazonaws.com
#api_domain=ewlg4idc89.execute-api.eu-south-1.amazonaws.com


./upload_file.sh -a $api_domain -f multa.pdf -t PN_NOTIFICATION_ATTACHMENTS -s PRELOADED -c pn-delivery | tee up_and_down.txt

key=$( cat up_and_down.txt | grep 'Key:' | sed -e 's/Key: //')
echo "UPLOAD WITH KEY: ${key}"

sleep 5
echo ""
echo ""
echo ""
echo ""
echo "===                DOWNLOAD WITH PN-DELIVERY                ==="
echo "==============================================================="
./download.sh -a $api_domain -k ${key} -c pn-delivery
mv ${key}.pdf ${key}_delivery.pdf

sleep 1
echo ""
echo ""
echo ""
echo ""
echo "===              DOWNLOAD WITH PN-DELIVERY-PUSH             ==="
echo "==============================================================="
./download.sh -a $api_domain -k ${key} -c pn-delivery-push 
mv ${key}.pdf ${key}_delivery_push.pdf

