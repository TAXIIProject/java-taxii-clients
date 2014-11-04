package org.mitre.taxii.client.example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBException;
import org.apache.commons.cli.CommandLine;
import org.mitre.taxii.messages.xml11.CollectionInformationRequest;
import org.mitre.taxii.messages.xml11.MessageHelper;

/**
 *
 * @author jasenj1
 */
public class CollectionInformationClient extends AbstractClient {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
        CollectionInformationClient client = new CollectionInformationClient();
        client.processArgs(args);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }
    
    public CollectionInformationClient() {
        super();
    }
    
    private void processArgs(String[] args) throws MalformedURLException, JAXBException, IOException, URISyntaxException, Exception {
        // NOTE: Add custom cli options here.
        // cli.getOptions().addOption(option);
        
        cli.parse(args);
        CommandLine cmd = cli.getCmd();

        taxiiClient = generateClient(cmd);
        
        // Prepare the message to send.
        CollectionInformationRequest request = factory.createCollectionInformationRequest()
                .withMessageId(MessageHelper.generateMessageId());
        
        doCall(cmd, request);
    }    
    
}
