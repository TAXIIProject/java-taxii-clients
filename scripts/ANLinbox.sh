#!  Run Basic Authentication TAXII Client with polling date range.
# Parameters:
# $1 = STIX content_binding e.g. urn:stix.mitre.org:xml:1.1.1
# $2 = username on Soltra server
# $3 = password
# $4 = environment e.g. Stage or Production
# $5 = content_file dir for downloaded stix files e.g. stix_upload.xml
# $6 = destination_collection of soltra server feed e.g. 
#
java -cp ../java-taxii-clients-all.jar \
-content_binding $1 \
-username $2 -password $3 -env $4 \
-content_file $5 \
-destination_collection $6 \
 org.mitre.taxii.client.example.InboxClient $*
