package org.mitre.taxii.client.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.mitre.taxii.ContentBindings;
import org.mitre.taxii.messages.xml11.ContentBlock;
import org.mitre.taxii.messages.xml11.MessageHelper;
import org.mitre.taxii.messages.xml11.PollRequest;
import org.mitre.taxii.messages.xml11.PollResponse;

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
        options.addOption("dest_dir", true, "The directory to save Content Blocks to. Defaults to the current directory.");
        
        cli.parse(args);
        CommandLine cmd = cli.getCmd();
        
        // Handle default values.
        String collection = cmd.hasOption("collection") ? cmd.getOptionValue("collection") : "default";
        String beginStr = cmd.hasOption("begin_timestamp") ? cmd.getOptionValue("begin_timestamp") : null;
        String endStr = cmd.hasOption("end_timestamp") ? cmd.getOptionValue("end_timestamp") : null;
        String subId = cmd.hasOption("subscription_id") ? cmd.getOptionValue("subscription_id") : null;
        String dest = cmd.hasOption("dest_dir") ? cmd.getOptionValue("dest_dir") : ".";
        

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
        
        Object response = doCall(cmd, request);
        
        if (response instanceof PollResponse) {
            handleResponse(dest, (PollResponse)response);
        }
    }    
    
    private void handleResponse(String dest, PollResponse response) {
        if (response.isMore()) {
            System.out.println("This response has More=True, to request additional parts, use the following command:");
            System.out.println(String.format("  fulfillment_client --collection %s --result_id %s --result_part_number %s\r\n",
                     response.getCollectionName(), response.getResultId() , response.getResultPartNumber().add(BigInteger.ONE)));            
        }
        
        // Build the filename for the output
        String dateString;
        String format;
        String ext;
        for (ContentBlock cb : response.getContentBlocks()) {
            Writer writer = null;
            try {
                String binding = cb.getContentBinding().getBindingId();
                if (ContentBindings.CB_STIX_XML_10.equals(binding)) {
                    format = "_STIX10_";
                    ext = ".xml";
                } else if (ContentBindings.CB_STIX_XML_101.equals(binding)) {
                    format = "_STIX101_";
                    ext = ".xml";
                } else if (ContentBindings.CB_STIX_XML_11.equals(binding)) {
                    format = "_STIX11_";
                    ext = ".xml";
                } else if (ContentBindings.CB_STIX_XML_111.equals(binding)) {
                    format = "_STIX111_";
                    ext = ".xml";
                } else { // Format and extension are unknown
                    format = "";
                    ext = "";
                }
                if (null != cb.getTimestampLabel()) {
                    dateString = 't' + cb.getTimestampLabel().toXMLFormat(); // This probably won't work due to illegal characters.
                } else {
                    try {
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(new Date()); // Now.
                        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
                        dateString = "s" + now.toXMLFormat();
                    } catch (DatatypeConfigurationException ex) {
                        dateString = "";
                    }
                }
                String filename = dest + response.getCollectionName() + format + dateString + ext;
                filename = filename.replaceAll("/\\:*?\"<>|",""); // TODO: I don't think this regex is right.
                File outFile = new File(filename);
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));
                writer.write(taxiiXml.marshalToString(cb.getContent(), true));
                writer.close();
                System.out.println(String.format("Wrote Content Block to %s", filename));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JAXBException ex) {
                Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
        }
    }
}
