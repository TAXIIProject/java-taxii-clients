package org.mitre.taxii.client.example;

import gov.anl.cfm.logging.CFMLogFields;
import gov.anl.cfm.logging.CFMLogFields.Environment;
import gov.anl.cfm.logging.CFMLogFields.State;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mitre.stix.stix_1.STIXPackage;
import org.mitre.taxii.ContentBindings;
import org.mitre.taxii.messages.xml11.ContentBlock;
import org.mitre.taxii.messages.xml11.MessageHelper;
import org.mitre.taxii.messages.xml11.PollRequest;
import org.mitre.taxii.messages.xml11.PollResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class PollClient extends AbstractClient {
	private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static CFMLogFields logger;
	private static Logger plog = LogManager.getLogger(PollClient.class.getName());


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            PollClient client = new PollClient();
            client.processArgs(args);
        } catch (Throwable t) {
            plog.fatal(t.getMessage());
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

        // add options for logging
        options.addOption("proc_name", true, "process name");
        options.addOption("subproc", true, "subprocess name");
        options.addOption("env", true, "environment enumeration");
        
        
        cli.parse(args);
        CommandLine cmd = cli.getCmd();
        
        // Handle default values.
        String collection = cmd.getOptionValue("collection", "default");
        String beginStr = cmd.getOptionValue("begin_timestamp", null);
        String endStr = cmd.getOptionValue("end_timestamp", null);
        String subId = cmd.getOptionValue("subscription_id", null);
        String dest = cmd.getOptionValue("dest_dir", ".");
        
        String procName = cmd.getOptionValue("proc_name","TaxiiClientBA");
        String subProc = cmd.getOptionValue("subproc","Poll");
        Environment env = Environment.valueOf(cmd.getOptionValue("env","Other"));
        // use built-in UUID generator for the session ID
        String sessionID = MessageHelper.generateMessageId();
        
        CFMLogFields.setBaseProcName(procName);
        logger = new CFMLogFields(subProc, sessionID, env, State.PROCESSING);
        
		Date lastTime = getLatestTime(dest);
		Date now = new Date();


        taxiiClient = generateClient(cmd);

        // Prepare the message to send.
        PollRequest request = factory.createPollRequest()
                .withMessageId(sessionID)
                .withCollectionName(collection);

        if (null != subId) {
            request.setSubscriptionID(subId);
        } else {
            request.withPollParameters(factory.createPollParametersType());
        }
/**
        if (null != beginStr) {
            XMLGregorianCalendar beginTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(beginStr);
            request.setExclusiveBeginTimestamp(beginTime);
        }

        if (null != endStr) {
            XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(endStr);
            request.setInclusiveEndTimestamp(endTime);
        }
 */
        // use the gregorian calendar instead of the string format
    	Calendar gc = GregorianCalendar.getInstance();
    	gc.setTime(lastTime);
    	XMLGregorianCalendar beginTime = DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)gc).normalize();
    	beginTime.setFractionalSecond(null);
    	logger.info(plog, "Begin Time: {}", beginTime);
        request.setExclusiveBeginTimestamp(beginTime);

    	
    	gc.setTime(now);
    	XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)gc).normalize();
    	endTime.setFractionalSecond(null);
    	logger.info(plog,"End Time: {}",endTime);
        request.setInclusiveEndTimestamp(endTime);



        Object response = doCall(cmd, request, logger);

        if (response instanceof PollResponse) {
        	// do I want to still do this for our use?  KLS
            handleResponse(dest, (PollResponse) response);
            
			// write out the returned results as a string. - this is for the FX translator
//			File output = new File(dest + "/" + now.getTime() + "_" + "PollResults.xml");
//			FileWriter fw = new FileWriter(output);
//			fw.write(response.toString());
//			fw.close();
//			
//			// write filepath to stdout, not the logger
//			System.out.println(output.getCanonicalPath());

        }
    }

    private void handleResponse(String dest, PollResponse response) {
        try {
            if (response.isMore()) {
                logger.info(plog,"This response has More=True, to request additional parts, use the following command:");
                logger.info(plog,"  fulfillment_client --collection {} --result_id {} --result_part_number {}\r\n",
                        response.getCollectionName(), response.getResultId(), response.getResultPartNumber().add(BigInteger.ONE));
            }
            // Build the filename for the output
            String dateString;
            String format;
            String ext;
            Writer fileWriter = null;
            
            List<ContentBlock> blocks =  response.getContentBlocks();
            if (blocks.size() > 0) {
           
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            dbf.setNamespaceAware(true);
	            DocumentBuilder db = dbf.newDocumentBuilder();
	            
	            for (ContentBlock cb : blocks) {

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
	                    m.marshal(cb,System.out);
	                    
	                    /* NOTE: It is bad practice to rely on the namespace prefix being a certain value.
	                    But in this case, the JAXB binding configuration dictates what it will be.
	                    */
	                    // KLS - the getElementByTagName DOES NOT WORK. 4/2015
	                   // NodeList contents = doc.getElementsByTagNameNS("*","Content");
	                    //NodeList contents = root.getChildNodes();
	                    Element root = doc.getDocumentElement();
	                    Node contentNode = root.getFirstChild().getNextSibling();
	                    
	                    /* According to the schema there must be exactly 1 Content element, but make sure we got one. */
	                    //if (0 < contents.getLength()) {
	                        //contentNode = contents.item(0);
	                    if (contentNode != null) {
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
	                            
	                            // validate the STIX here
	                            JAXBContext stixContext = JAXBContext.newInstance(STIXPackage.class.getPackage().getName());
	                            STIXPackage sp = STIXPackage.fromXMLString(childStr);
	                            
	                            // Append child string to output file.
	                            fileWriter.append(childStr);
	                        }
	                        fileWriter.flush();
	                        fileWriter.close();
	                    	logger.updateState(State.SUCCESS);
	            			// write filepath to stdout for FX, not the logger
	                        System.out.println(outFile.getCanonicalPath());
	                        logger.info(plog, "Wrote Content to {}", filepath);
	                    } // If Content element found.
	                } catch (JAXBException|IOException ex) {
	                	logger.updateState(State.ERROR);
	                    logger.error(plog, null, ex);
	                } finally {
	                    if (null != fileWriter) {
	                        try {
	                            fileWriter.close();
	                        } catch (IOException ex) {
	    	                	logger.updateState(State.ERROR);
	    	                    logger.error(plog, null, ex);
	                        }
	                    }
	                }
	            }// for each ContentBlock
            } else {
            	logger.updateState(State.FAILURE);
	            logger.warn(plog,"There were no Content Blocks returned");
	        }
        } catch (ParserConfigurationException ex) {
        	logger.updateState(State.ERROR);
            logger.error(plog, null, ex);
        }
    }// handleResponse()
    
	/**
	 * Read file with last download timestamp and return Date.  Rewrite file with current date for next time.
	 * @return Date
	 */
	public Date getLatestTime(String destDir) {
		File dlDir = new File(destDir);
		if (!dlDir.exists()) {
			dlDir.mkdirs();
		}

		// I'd much prefer to use Joda-time or switch to Java 8's new Date/times.
		//  there will be issues around daylight savings time since it's saving in local time
		//  and not UTC.
		Date lastTime = null;
		BufferedReader rd = null;
		lastTime = new Date();
		try {
			File fn = new File(dlDir,"lasttime.txt");
			if (fn.exists()) {
				// read the date from the last time this program was called.
				rd = new BufferedReader(new FileReader(fn));
				lastTime = fmt.parse(rd.readLine());
			}
			
		} catch (ParseException e) {
			logger.error(plog,"couldn't parse date, using current time", e);
		} catch (FileNotFoundException e) {
			logger.error(plog,"File doesn't exist, using current time", e);
		} catch (IOException e) {
			logger.error(plog,"couldn't read lasttime file, using current time", e);
		} finally {
			if (rd != null)
				try {
					rd.close();
				} catch (IOException e) {
                    logger.error(plog, null, e);
				}
		}
		
		// write out the current date to the file
		File fn = new File(dlDir,"lasttime.txt");
		FileWriter fw;
		try {
			fw = new FileWriter(fn);
			fw.write(fmt.format(new Date()));
			fw.close();
		} catch (IOException e) {
            logger.error(plog, null, e);
		}

		return lastTime;
	}
}
