# SCRIPT per testare la "condivisione di file tra più utenti"

echo ""
echo ""
echo "===                       USE SANDBOX                      ==="
echo "=============================================================="
api_domain=14ej9p2krd.execute-api.eu-south-1.amazonaws.com

echo " ########## Upload"
./upload_file.sh -a $api_domain -f multa.pdf -t PN_LEGAL_FACTS -s SAVED -c pn-delivery-push | tee up_and_down.txt

key=$( cat up_and_down.txt | grep 'Key:' | sed -e 's/Key: //')
echo "UPLOAD WITH KEY: ${key}"

echo " ########## Wait 10 seconds"
sleep 10

echo " ########## Download"
./download.sh -a $api_domain -k ${key} -c pn-delivery-push 
mv ${key}.pdf ${key}_sandbox.pdf



echo ""
echo ""
echo "===                        USE SVIL                        ==="
echo "=============================================================="
api_domain=ewlg4idc89.execute-api.eu-south-1.amazonaws.com

echo " ########## Upload"
./upload_file.sh -a $api_domain -f multa.pdf -t PN_LEGAL_FACTS -s SAVED -c pn-delivery-push | tee up_and_down.txt

key=$( cat up_and_down.txt | grep 'Key:' | sed -e 's/Key: //')
echo "UPLOAD WITH KEY: ${key}"

echo " ########## Wait 10 seconds"
sleep 10

echo " ########## Download"
./download.sh -a $api_domain -k ${key} -c pn-delivery-push 
mv ${key}.pdf ${key}_svil.pdf





echo ""
echo ""
echo "===                        USE COLL                        ==="
echo "=============================================================="
api_domain=eo6pdvms24.execute-api.eu-south-1.amazonaws.com

echo " ########## Upload"
./upload_file.sh -a $api_domain -f multa.pdf -t PN_LEGAL_FACTS -s SAVED -c pn-delivery-push -p coll | tee up_and_down.txt

key=$( cat up_and_down.txt | grep 'Key:' | sed -e 's/Key: //')
echo "UPLOAD WITH KEY: ${key}"

echo " ########## Wait 10 seconds"
sleep 10

echo " ########## Download"
./download.sh -a $api_domain -k ${key} -c pn-delivery-push -p coll 
mv ${key}.pdf ${key}_coll.pdf

