package org.mitre.taxii.client.example;

import gov.anl.cfm.logging.CFMLogFields;
import gov.anl.cfm.logging.CFMLogFields.Environment;
import gov.anl.cfm.logging.CFMLogFields.State;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mitre.taxii.messages.xml11.ContentBlock;
import org.mitre.taxii.messages.xml11.ContentInstanceType;
import org.mitre.taxii.messages.xml11.InboxMessage;
import org.mitre.taxii.messages.xml11.MessageHelper;
import org.mitre.taxii.messages.xml11.StatusMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class InboxClient extends AbstractClient {
    
    private static final String CB_STIX_XML_111 = "urn:stix.mitre.org:xml:1.1.1";
	private static CFMLogFields logger;

	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
        InboxClient client = new InboxClient();
        client.processArgs(args);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(1);
        }
    }
    
    public InboxClient() {
        super();
        defaultURL += "inbox/";
    }
    
    private void processArgs(String[] args) throws MalformedURLException, JAXBException, IOException, URISyntaxException, Exception {
                
        // NOTE: Add custom cli options here.
        // cli.getOptions().addOption(option);
        Options options = cli.getOptions();        
        options.addOption("content_binding", true, String.format("Content binding of the Content Block to send. Defaults to '%s'",CB_STIX_XML_111));
        options.addOption("subtype", true, "The subtype of the Content Binding. Defaults to none");        
        Option fileOpt = new Option("content_file", true, "REQUIRED. File containing the content of the Content Block to send.");
        fileOpt.setRequired(true);
        options.addOption(fileOpt);
        options.addOption("dcn", "destination_collection", true, "The Destination Collection Name for this Inbox Message. Defaults to none. This script only supports one Destination Collection Name");
        
        // add options for logging
        options.addOption("proc_name", true, "process name");
        options.addOption("subproc", true, "subprocess name");
        options.addOption("env", true, "environment enumeration");
        
        cli.parse(args);
        CommandLine cmd = cli.getCmd();

        // Handle default values.
        String binding = cmd.hasOption("content_binding") ? cmd.getOptionValue("content_binding") : CB_STIX_XML_111;
        String subtype = cmd.hasOption("subtype") ? cmd.getOptionValue("subtype") : null;
        String dcn = cmd.hasOption("dcn") ? cmd.getOptionValue("dcn") : null;
        String contentFileName = cmd.getOptionValue("content_file");
        
        // Validate the content file
        File contentFile = new File(contentFileName);
        if (!contentFile.canRead()) {
            throw new IOException("Unable to read content file.");
        }
           
        String procName = cmd.getOptionValue("proc_name","TaxiiClientBA");
        String subProc = cmd.getOptionValue("subproc","Inbox");
        Environment env = Environment.valueOf(cmd.getOptionValue("env","Other"));
        // use built-in UUID generator for the session ID
        String sessionID = MessageHelper.generateMessageId();
        
        CFMLogFields.setBaseProcName(procName);
        logger = new CFMLogFields(subProc, sessionID, env, State.PROCESSING);
    	Logger ilog = LogManager.getLogger(InboxClient.class.getName());
        
        // validate the STIX before setting up anything else
//        String xmlstr = new String(Files.readAllBytes(Paths.get(contentFileName)));
//        STIXPackage sp = STIXPackage.fromXMLString(xmlstr);
//        String version = sp.getVersion();
//        
//        StixValidator sv = new StixValidator(version);
//        List<SchemaError> errors = sv.validate(contentFile); // sigh. reads it in again.
//		if (errors.size() > 0) {
//			logger.updateState(State.ERROR);
//			logger.error(ilog,"There are {} errors in file {} ",errors.size(),contentFile);
//			for (SchemaError error : errors) {
//				logger.error(ilog,"SchemaError Category: {} - Message: {}",error.getCategory(),error.getMessage());
//				logger.error(ilog,"SchemaError Line: {} - Col: {}",error.getLine(),error.getColumn());
//			}
//			logger.info(ilog,"Message was not published due to STIX {} content validation errors!  Please check content and try again.",version);
//        	throw new RuntimeException("Schema mismatch");
//		}

        taxiiClient = generateClient(cmd);
        
        // Handle the content file.
        // Make the GIANT assumption that the input file is XML and parse it as such.
        // If not, and the file is XML, it will get escaped when marshaled. e.g. "<" becomes "&lt;".
        // Create a DOM Node from an XML string.
        // JAXB can handle a generic DOM NODE, but a String it will escape and treat as a String - not XML.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new FileReader(contentFile))); // now read in a third time?
        Node content = doc.getDocumentElement(); 
        
        // Prepare the message to send.
        InboxMessage request = factory.createInboxMessage()
                .withMessageId(sessionID);
        
        if (null != dcn) {
            request.withDestinationCollectionNames(dcn);
        }
        
        ContentBlock cb = factory.createContentBlock();
        ContentInstanceType cit = factory.createContentInstanceType().withBindingId(binding);
        if (null != subtype) {
            cit.setSubtype(factory.createSubtypeType().withSubtypeId(subtype));
        }
        
        cb.setContentBinding(cit);
        cb.setContent(factory.createAnyMixedContentType().withContent(content));
        
        request.withContentBlocks(cb);

        Object response = doCall(cmd, request, logger);

        if (response instanceof StatusMessage) {
        	StatusMessage msg = (StatusMessage)response;
        	if (msg.getStatusType().equals("SUCCESS")) {
        		logger.updateState(State.SUCCESS);
        		logger.info(ilog, "{} uploaded successfully", contentFileName);
        		logger.info(ilog, "{} ", msg.getMessage());
        	} else {
        		logger.updateState(State.FAILURE);
        		logger.error(ilog, "Status: {}", msg.getMessage());
        	}
        }
    }    
}
