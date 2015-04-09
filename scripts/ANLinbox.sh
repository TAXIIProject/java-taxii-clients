#!  Run Basic Authentication TAXII Client with polling date range.
# Parameters:
# $1 = STIX content_binding e.g. urn:stix.mitre.org:xml:1.1.1
# $2 = subtype e.g. none
# $3 = username on Soltra server
# $4 = password
# $5 = environment e.g. Stage or Production
# $6 = content_file dir for downloaded stix files e.g. stix_upload.xml
# $7 = destination_collection of soltra server feed e.g. 
#
java -cp ../java-taxii-clients-all.jar \
-content_binding $1 -subtype $2 \
-username $3 -password $4 -env $5 \
-content_file $6 \
-destination_collection $7 \
 org.mitre.taxii.client.example.InboxClient $*
