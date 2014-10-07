package org.mitre.taxii.client.example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.mitre.taxii.messages.xml11.MessageHelper;
import org.mitre.taxii.messages.xml11.PollRequest;

public class PollClient extends AbstractClient {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
        PollClient client = new PollClient();
        client.processArgs(args);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }
    
    public PollClient() {
        super();
    }
    
    private void processArgs(String[] args) throws MalformedURLException, JAXBException, IOException, URISyntaxException, Exception {
        // NOTE: Add custom cli options here.
        // cli.getOptions().addOption(option);
        Options options = cli.getOptions();        
        options.addOption("collection", true, "Data Collection to poll. Defaults to 'default'.");
        options.addOption("begin_timestamp", true, "The begin timestamp (format: YYYY-MM-DDTHH:MM:SS.ssssss+/-hh:mm) for the poll request. Defaults to none.");
        options.addOption("end_timestamp", true, "The end timestamp (format: YYYY-MM-DDTHH:MM:SS.ssssss+/-hh:mm) for the poll request. Defaults to none.");
        options.addOption("subscription_id", true, "The Subscription ID for the poll request. Defaults to none.");
//        options.addOption("dest_dir", true, "The directory to save Content Blocks to. Defaults to the current directory.");
        
        cli.parse(args);
        CommandLine cmd = cli.getCmd();
        
        // Handle default values.
        String collection = cmd.hasOption("collection") ? cmd.getOptionValue("collection") : "default";
        String beginStr = cmd.hasOption("begin_timestamp") ? cmd.getOptionValue("begin_timestamp") : null;
        String endStr = cmd.hasOption("end_timestamp") ? cmd.getOptionValue("end_timestamp") : null;
        String subId = cmd.hasOption("subscription_id") ? cmd.getOptionValue("subscription_id") : null;
//        String dest = cmd.hasOption("dest_dir") ? cmd.getOptionValue("dest_dir") : ".";
        

        taxiiClient = generateClient(cmd);
        
        // Prepare the message to send.
        PollRequest request = factory.createPollRequest()
                .withMessageId(MessageHelper.generateMessageId())
                .withCollectionName(collection);
        
        if (null != subId) {
            request.setSubscriptionID(subId);
        } else {
            request.withPollParameters(factory.createPollParametersType());
        }
        
        if (null != beginStr) {
            XMLGregorianCalendar beginTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(beginStr);
            request.setExclusiveBeginTimestamp(beginTime);
        }

        if (null != endStr) {
            XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(endStr);
            request.setInclusiveEndTimestamp(endTime);
        }
        
        if (cmd.hasOption("verbose")) {
            System.out.println(taxiiXml.marshalToString(request, true));
        }
        
        // Call the service
        Object responseObj = taxiiClient.callTaxiiService(new URI(cmd.getOptionValue("u")), request);

        System.out.println(taxiiXml.marshalToString(responseObj, true));        
    }    
}
