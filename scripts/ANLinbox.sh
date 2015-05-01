#!/bin/bash
# Run Basic Authentication TAXII Client to upload STIX file.
# Parameters:
# $1 = STIX content_binding e.g. urn:stix.mitre.org:xml:1.1.1
# $2 = username on Soltra server
# $3 = password
# $4 = environment e.g. Stage or Production
# $5 = content_file dir for downloaded stix files e.g. stix_upload.xml
# $6 = destination_collection of soltra server feed e.g. 
# $7 = url of soltra server endpoint e.g. https://ht-sluff-2.it.anl.gov/taxii-discovery-service
#
#
# if having trouble with ssl, try running with this property:  -Djavax.net.debug=ssl,handshake
#

IFILE=$(basename $5)
DFILE=$(cut -d - -f 4- <<< "$IFILE")
OFILE=$(cut -d . -f 1 <<< "$DFILE")

ARGS='env="PROD" level="INFO" process="ANLinbox" state="START"'
logger "$ARGS sessionID=\"$OFILE\"" -t cfm-anlinbox -p local1.info

TEXT=$(java \
-Dlog4j.configurationFile=/home/cfm/SoltraClient/log4j2.xml \
-cp /home/cfm/SoltraClient/java-taxii-clients-all.jar \
org.mitre.taxii.client.example.InboxClient -content_binding $1 \
-username $2 \
-password $3 \
-env $4 \
-content_file $5 \
-destination_collection $6 \
-u $7 \
-xmloutput 2>&1)
ARGS='env="PROD" level="INFO" process="ANLinbox" state="PROCESSING"'
logger "$ARGS text=\"$TEXT\"" -t cfm-anlinbox -p local1.info

ARGS='env="PROD" level="INFO" process="ANLinbox" state="END"'
logger "$ARGS sessionID=\"$OFILE\"" -t cfm-anlinbox -p local1.info

exit $?
