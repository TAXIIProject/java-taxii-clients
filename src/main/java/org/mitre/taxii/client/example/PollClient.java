package org.mitre.taxii.client.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.mitre.taxii.ContentBindings;
import org.mitre.taxii.messages.xml11.AnyMixedContentType;
import org.mitre.taxii.messages.xml11.ContentBlock;
import org.mitre.taxii.messages.xml11.MessageHelper;
import org.mitre.taxii.messages.xml11.PollRequest;
import org.mitre.taxii.messages.xml11.PollResponse;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

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
        defaultURL += "poll/";
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
        String collection = cmd.getOptionValue("collection", "default");
        String beginStr = cmd.getOptionValue("begin_timestamp", null);
        String endStr = cmd.getOptionValue("end_timestamp", null);
        String subId = cmd.getOptionValue("subscription_id", null);
        String dest = cmd.getOptionValue("dest_dir", ".");

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
            handleResponse(dest, (PollResponse) response);
        }
    }

    private void handleResponse(String dest, PollResponse response) {
        try {
            if (response.isMore()) {
                System.out.println("This response has More=True, to request additional parts, use the following command:");
                System.out.println(String.format("  fulfillment_client --collection %s --result_id %s --result_part_number %s\r\n",
                        response.getCollectionName(), response.getResultId(), response.getResultPartNumber().add(BigInteger.ONE)));
            }
            // Build the filename for the output
            String dateString;
            String format;
            String ext;
            Writer fileWriter = null;
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            for (ContentBlock cb : response.getContentBlocks()) {
                // Build the filename for the output.
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
                    
                    // Construct the complete path to write the ContentBlock's content to.
                    String filename = response.getCollectionName() + format + dateString + ext;
                    // Remove characters that might make the OS unhappy with the filename.
                    filename = filename.replaceAll("[\\*:<>\\/\\?|]", "");
                    String filepath = dest + File.separator + filename;
                    File outFile = new File(filepath);
                    fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));
                    
                    /* Marshal the ContentBlock to a DOM document.
                    It must be done at this level because the child nodes are not
                    defined as root elements in the schema. So we need to marshal
                    the closest root element and then dig down to get to what we
                    really want to marshal.
                    */
                    Marshaller m = taxiiXml.createMarshaller(true);
                    Document doc = db.newDocument();
                    m.marshal(cb, doc);
                    
                    /* NOTE: It is bad practice to rely on the namespace prefix being a certain value.
                    But in this case, the JAXB binding configuration dictates what it will be.
                    */
                    NodeList contents = doc.getElementsByTagName("taxii_11:Content");
                    
                    /* According to the schema there must be exactly 1 Content element, but make sure we got one. */
                    if (0 < contents.getLength()) {
                        Node contentNode = contents.item(0);
                        /*
                        Content contains AnyMixedContentType. It is not necessarily a single element.
                        And may be text & XML elements mixed together.
                        */
                        NodeList contentChildren = contentNode.getChildNodes();
                        int numChildren = contentChildren.getLength();
                        
                        // System.out.println(numChildren + " Content children.");
                        for (int count = 0; count < numChildren; count++) {
                            
                            Node child = contentChildren.item(count);
                            // System.out.println("Processing child '" + child.getNodeName() + "': " + child.getNodeValue());
                            
                            DOMImplementationLS domImpl = (DOMImplementationLS)child.getOwnerDocument().getImplementation();
                            LSSerializer serializer = domImpl.createLSSerializer();
                            serializer.getDomConfig().setParameter("xml-declaration", false);
                                                        
                            // Write current child to a string.
                            String childStr = serializer.writeToString(child);
                            
                            // Append child string to output file.
                            fileWriter.append(childStr);
                        }
                        fileWriter.flush();
                        fileWriter.close();
                        System.out.println(String.format("Wrote Content to %s", filepath));;
                    } // If Content element found.
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JAXBException ex) {
                    Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (null != fileWriter) {
                        try {
                            fileWriter.close();
                        } catch (IOException ex) {
                            Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }// for each ContentBlock
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PollClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// handleResponse()
}
