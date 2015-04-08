package org.mitre.taxii.client.example;

import gov.anl.cfm.logging.CFMLogFields;
import gov.anl.cfm.logging.CFMLogFields.Environment;
import gov.anl.cfm.logging.CFMLogFields.State;

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
        defaultURL += "collection-management/";
    }
    
    private void processArgs(String[] args) throws MalformedURLException, JAXBException, IOException, URISyntaxException, Exception {
        // NOTE: Add custom cli options here.
        // cli.getOptions().addOption(option);
        
        cli.parse(args);
        CommandLine cmd = cli.getCmd();
        
        String procName = cmd.getOptionValue("proc_name","TaxiiClientBA");
        String subProc = cmd.getOptionValue("subproc","Fulfillment");
        Environment env = Environment.valueOf(cmd.getOptionValue("env","Other"));
        // use built-in UUID generator for the session ID
        String sessionID = MessageHelper.generateMessageId();
        
        CFMLogFields.setBaseProcName(procName);
        CFMLogFields logger = new CFMLogFields(subProc, sessionID, env, State.PROCESSING);

        taxiiClient = generateClient(cmd);
        
        // Prepare the message to send.
        CollectionInformationRequest request = factory.createCollectionInformationRequest()
                .withMessageId(sessionID);
        
        doCall(cmd, request, logger);
    }    
    
}
