
# Esempio caricamento Atto Opponibile a Terzi
```
./upload_file.sh -a g7ssnjj8i3.execute-api.eu-south-1.amazonaws.com \
                 -f multa.pdf \
                 -t PN_LEGAL_FACTS \
                 -s SAVED \
                 -c pn-delivery-push
```

# Esempio caricamento Allegato Notifica
```
./upload_file.sh -a g7ssnjj8i3.execute-api.eu-south-1.amazonaws.com \
                 -f multa.pdf \
                 -t PN_NOTIFICATION_ATTACHMENTS \
                 -s PRELOADED \
                 -c pn-delivery
```
