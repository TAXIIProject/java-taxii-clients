package org.mitre.taxii.client.example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import javax.xml.bind.JAXBException;
import org.apache.commons.cli.CommandLine;
import org.mitre.taxii.messages.xml10.DiscoveryRequest;
import org.mitre.taxii.messages.xml10.ObjectFactory;
import org.mitre.taxii.messages.xml10.TaxiiXmlFactory;

public class DiscoveryClient10 extends AbstractClient {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
        DiscoveryClient10 client = new DiscoveryClient10();
        client.processArgs(args);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }
    
    public DiscoveryClient10() {
        super();
        defaultURL += "discovery/";
    }
    
    private void processArgs(String[] args) throws MalformedURLException, JAXBException, IOException, URISyntaxException, Exception {
        // NOTE: Add custom cli options here.
        // cli.getOptions().addOption(option);
        
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
        DiscoveryRequest request = factory10.createDiscoveryRequest()
                .withMessageId(String.valueOf(id));

        if (cmd.hasOption("xmloutput")) {
            System.out.println("Request:");
            System.out.println(taxiiXml.marshalToString(request, true));
        }        
        // Call the service
        Object responseObj = taxiiClient.callTaxiiService(new URI(cmd.getOptionValue("u")), request);

        if (cmd.hasOption("xmloutput")) {
            System.out.println("Response:");        
            System.out.println(taxiiXml.marshalToString(responseObj, true));
        }
    }    
}
