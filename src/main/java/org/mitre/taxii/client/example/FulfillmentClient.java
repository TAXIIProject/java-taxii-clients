package org.mitre.taxii.client.example;

import gov.anl.cfm.logging.CFMLogFields;
import gov.anl.cfm.logging.CFMLogFields.Environment;
import gov.anl.cfm.logging.CFMLogFields.State;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.mitre.taxii.messages.xml11.MessageHelper;
import org.mitre.taxii.messages.xml11.PollFulfillment;

public class FulfillmentClient extends AbstractClient {
	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
        FulfillmentClient client = new FulfillmentClient();
        client.processArgs(args);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }
    
    public FulfillmentClient() {
        super();
        defaultURL += "poll/";
    }
    
    private void processArgs(String[] args) throws MalformedURLException, JAXBException, IOException, URISyntaxException, Exception {
        // NOTE: Add custom cli options here.
        // cli.getOptions().addOption(option);
        Options options = cli.getOptions();        
        options.addOption("collection", true, "Data Collection that this Fulfillment request applies to. Defaults to 'default'.");
        Option idOpt = new Option("result_id", true, "The result_id being requested.");
        idOpt.setRequired(true);
        options.addOption(idOpt);
        options.addOption("result_part_number", true, "The part number being requested. Defaults to '1'.");
        
        cli.parse(args);
        CommandLine cmd = cli.getCmd();

        // Handle default values.
        String collection = cmd.hasOption("collection") ? cmd.getOptionValue("collection") : "default";
        int part = cmd.hasOption("result_part_number") ? Integer.parseInt(cmd.getOptionValue("result_part_number")) : 1;
        
        String procName = cmd.getOptionValue("proc_name","TaxiiClientBA");
        String subProc = cmd.getOptionValue("subproc","Fulfillment");
        Environment env = Environment.valueOf(cmd.getOptionValue("env","Other"));
        // use built-in UUID generator for the session ID
        String sessionID = MessageHelper.generateMessageId();
        
        CFMLogFields.setBaseProcName(procName);
        CFMLogFields logger = new CFMLogFields(subProc, sessionID, env, State.PROCESSING);

        taxiiClient = generateClient(cmd);
        
        // Prepare the message to send.
        PollFulfillment request = factory.createPollFulfillment()
                .withMessageId(sessionID)
                .withCollectionName(collection)
                .withResultId(cmd.getOptionValue("result_id"))
                .withResultPartNumber(BigInteger.valueOf(part));                                

        doCall(cmd, request, logger);
        
    }    
}
