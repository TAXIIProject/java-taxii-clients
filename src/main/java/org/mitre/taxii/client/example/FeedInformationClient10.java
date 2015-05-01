package org.mitre.taxii.client.example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.mitre.taxii.messages.xml10.FeedInformationRequest;
import org.mitre.taxii.messages.xml10.ObjectFactory;
import org.mitre.taxii.messages.xml10.TaxiiXmlFactory;

public class FeedInformationClient10 extends AbstractClient {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            FeedInformationClient10 client = new FeedInformationClient10();
            client.processArgs(args);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }
    
    public FeedInformationClient10() {
        super();
        defaultURL += "feed-management/";
    }
    
    private void processArgs(String[] args) throws MalformedURLException, JAXBException, IOException, URISyntaxException, Exception {
        // NOTE: Add custom cli options here.
        // cli.getOptions().addOption(option);
        
        // add options for logging
        Options options = cli.getOptions();        
        options.addOption("proc_name", true, "process name");
        options.addOption("subproc", true, "subprocess name");
        options.addOption("env", true, "environment enumeration");
        
        cli.parse(args);
        CommandLine cmd = cli.getCmd();

        taxiiClient = generateClient(cmd);
        
        // Set things up for 1.0
        ObjectFactory factory10 = new ObjectFactory();
        TaxiiXmlFactory txf10 = new TaxiiXmlFactory();
        taxiiXml = txf10.createTaxiiXml();

        Random rand = new Random();        
        int id = rand.nextInt(100001);
        
        // Prepare the message to send.
        FeedInformationRequest request = factory10.createFeedInformationRequest()
                .withMessageId(String.valueOf(id));

        if (cmd.hasOption("xmloutput")) {
            System.out.println("Request:");
            System.out.println(taxiiXml.marshalToString(request, true));
        } // else {
//            System.out.println(PythonTextOutput.toText(request));
//        }
        
        // Call the service
        Object responseObj = taxiiClient.callTaxiiService(new URI(cmd.getOptionValue("u")), request);

        if (cmd.hasOption("xmloutput")) {
            System.out.println("Response:");        
            System.out.println(taxiiXml.marshalToString(responseObj, true));
        } //else {
//            System.out.println(PythonTextOutput.toText(responseObj));
//        }
    }    
}
