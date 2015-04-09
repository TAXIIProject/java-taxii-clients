#!  Run Basic Authentication TAXII Client with polling date range.
# Parameters:
# $1 = Feed ID (collection) e.g. system.Default
# $2 = Feed Name (subscription_id) e.g. Default
# $3 = username on Soltra server
# $4 = password
# $5 = environment e.g. Stage or Production
# $6 = destination dir for downloaded stix files e.g. ./cfm
# $7 = url of soltra server endpoint e.g. https://ht-sluff-2.it.anl.gov/taxii-discovery-service
#
java -cp ../java-taxii-clients-all.jar \
-collection $1 -subscription_id $2 \
-username $3 -password $4 -env $5 \
-dest_dir $6 \
-u $7 \
org.mitre.taxii.client.example.PollClient $*