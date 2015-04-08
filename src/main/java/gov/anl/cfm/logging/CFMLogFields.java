package gov.anl.cfm.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.StructuredDataMessage;

/**
 * Re-usable wrapper object for logging standard header data.
 * 
 * The "log" methods can be called more than once for the object because they create
 * the actual Message objects and pass the message on to the logger.  The Message objects
 * are created with the current set of data keys, so removal or addition of keys will be
 * reflected in the next call to the "log" method.
 * 
 * @author kehrer
 *
 */
public class CFMLogFields {
	static class CFMMessage extends ParameterizedMessage {

		private MapMessage mapMessage;
		

		public CFMMessage(CFMLogFields header, String messagePattern, Object arg) {
			super(messagePattern.replace('\n', ' '), arg);
			init(header);
		}
		public CFMMessage(CFMLogFields header, String messagePattern, Object...arguments) {
			super(messagePattern.replace('\n', ' '), arguments);
			init(header);
		}
		public void setLogLevel(Level level) {
			mapMessage.put(LEVEL_KEY,level.name());
		}
		public void setLoggerName(String loggerName) {
			mapMessage.put(LOGGER_KEY, loggerName);			
		}
		public void setCfmException(Throwable t) {
			if (t != null) {
				mapMessage.put(EXCEPTION_KEY,t.toString());
			} else {
				mapMessage.remove(EXCEPTION_KEY);
			}
		}
		private void init(CFMLogFields header) {
			mapMessage = new MapMessage(header.getKeyMap());		
		}

		
		public void put(String key, String value) {
			mapMessage.put(key, value);
		}
		public void putAll(Map<String, String> map) {
			mapMessage.putAll(map);
		}
		
		@Override
		public String getFormattedMessage() {
			mapMessage.put(TEXT_KEY, super.getFormattedMessage());
			return mapMessage.getFormattedMessage();
		}
		
	}
//	static class CFMStructuredMessage extends StructuredDataMessage {
//		private static String SDM_ID = System.getProperty("anl.cfm.logging.sdm.id","CFM_sdm");
//		private static String SDM_TYPE = System.getProperty("anl.cfm.logging.sdm.type","cfmType");
//
//		private String messagePattern;
//		private Object[] params;
//
//		public CFMStructuredMessage(CFMLogFields header, String messagePattern, Object...params) {
//			super();
//			setId(SDM_ID);
//			setType(SDM_TYPE);
//			putAll(header.getKeyMap());
//			this.messagePattern = messagePattern.replace('\n', ' ');
//			this.params = params;
//		}
//		public void setCfmException(Throwable t) {
//			if (t != null) {
//				put("Exception",t.toString()); //add the exception as a key
//			}
//		}
//		public void setLogLevel(Level level) {
//			put("Level",level.name());
//		}
//
//		@Override
//		public String getFormat() {
//			return ParameterizedMessage.format(messagePattern, params);
//		}
//		
//		
//	}
	public enum Environment {
		Development("DEV"),
		Stage("STG"),
		Production("PROD"),
		Other("OTHER");
		
		String label;
		Environment(String label) {
			this.label = label;
		}
		public String getLabel() {return label;}
	}
	public enum State {
		START,
		INIT,
		PROCESSING,
		SUCCESS,
		FAILURE,
		ERROR,
		END
	}
	private static String baseProcName = null;
	public static void setBaseProcName(String baseName) {
		baseProcName = baseName;
	}
	private static final String LEVEL_KEY = "level";
	private static final String TEXT_KEY = "text";

	private static final String LOGGER_KEY = "logger";
	private static final String EXCEPTION_KEY = "exception";
	//required keys
	private static final String PROCESS_KEY = "process";
	private final String processName;
	private static final String SESSION_KEY = "sessionID";
	private final String sessionId;
	private static final String ENV_KEY = "env";
	private final Environment env;
	private static final String STATE_KEY = "state";
	private State state;
	
	//option common keys
	private static final String SITE_KEY = "site";
	private String site;
	private static final String SITE_IP_KEY = "siteIP";
	private String siteIP;
	private static final String SITE_USER_KEY = "siteUser";
	private String siteUser;
	private static final String PAYLOAD_TYPE_KEY = "payloadType";
	private String payloadType;
	private static final String PAYLOAD_FORMAT_KEY = "payloadFormat";
	private String payloadFormat;
	
	private Map<String,String> otherKeys;
	
	public CFMLogFields(String procName, String sessionId, State state) {
		this.processName = initProcName(procName);
		this.sessionId = sessionId;
		this.env = Environment.valueOf(System.getProperty("anl.cfm.server.environment","Other"));
		this.state = state;
		otherKeys = new HashMap<>();
	}
	public CFMLogFields(String procName, String sessionId, Environment env, State state) {
		this.processName = initProcName(procName);
		this.sessionId = sessionId;
		this.env = env;
		this.state = state;
		otherKeys = new HashMap();
	}
	/**
	 * Creates a new instance with the given process name and all other values initialized from the reference item
	 * @param procName
	 * @param fields
	 */
	public CFMLogFields(String procName, CFMLogFields fields) {
		this.processName = initProcName(procName);
		this.sessionId = fields.sessionId;
		this.env = fields.env;
		this.state = fields.state;
		this.site = fields.site;
		this.siteIP = fields.siteIP;
		this.siteUser = fields.siteUser;
		this.payloadType = fields.payloadType;
		this.payloadFormat = fields.payloadFormat;
	}
	private String initProcName(String procName) {
		if (baseProcName != null) {
			//if the baseProcName is set, pre-pend to the given process name
			return baseProcName+"-"+procName;
		}
		return procName;
	}

	public String getSite() {
		return site;
	}


	public void setSite(String site) {
		this.site = site;
	}


	public String getSiteIP() {
		return siteIP;
	}


	public void setSiteIP(String siteIP) {
		this.siteIP = siteIP;
	}


	public String getSiteUser() {
		return siteUser;
	}


	public void setSiteUser(String siteUser) {
		this.siteUser = siteUser;
	}


	public String getPayloadType() {
		return payloadType;
	}


	public void setPayloadType(String payloadType) {
		this.payloadType = payloadType;
	}


	public String getPayloadFormat() {
		return payloadFormat;
	}


	public void setPayloadFormat(String payloadFormat) {
		this.payloadFormat = payloadFormat;
	}


	public String getProcessName() {
		return processName;
	}


	public String getSessionId() {
		return sessionId;
	}


	public Environment getEnv() {
		return env;
	}


	public State getState() {
		return state;
	}
	public void updateState(State state) {
		this.state = state;
	}
	
	public void putOtherKey(String key, String value) {
		otherKeys.put(key, value);
	}
	public boolean removeOtherKey(String key) {
		return otherKeys.remove(key) != null;
	}
	
	final Map<String,String> getKeyMap() {
		Map<String,String> map = new HashMap<>();
		//required keys
		map.put(PROCESS_KEY,processName);
		map.put(SESSION_KEY,sessionId);
		map.put(ENV_KEY,env.getLabel());
		map.put(STATE_KEY,state.name());
		
		//optional common keys
		if (site != null) {
			map.put(SITE_KEY,site);
		}
		if (siteIP != null) {
			map.put(SITE_IP_KEY,siteIP);
		}
		if (siteUser != null) {
			map.put(SITE_USER_KEY,siteUser);
		}
		if (payloadType != null) {
			map.put(PAYLOAD_TYPE_KEY,payloadType);
		}
		if (payloadFormat != null) {
			map.put(PAYLOAD_FORMAT_KEY,payloadFormat);
		}

		if (!otherKeys.isEmpty()) {
			map.putAll(otherKeys);
		}
		return map;
	}

	/**
	 * Create the Message object and passes it to the logger, includes support for logging an exception
	 * 
	 * @param logger the logger this message should be logged to
	 * @param logLevel the level the message will be logged at
	 * @param msgPattern the message to be logged (supports {} for parameter substitution)
	 * @param args the args to be substituted in the msgPattern (last arg can be an exception and will be properly sent to the logger)
	 */
	private void log(Logger logger, Level logLevel, String msgPattern, Object...args) {
		if (logger.isEnabled(logLevel)) {
			Throwable t = null;
			if (args != null) {
				//if an exception wasn't passed in and we have args then see if the last arg is an exception
				if (args.length > 0) {
					Object last = args[args.length-1];
					if (last instanceof Throwable) {
						//should be ok to leave it the array of args the Parameterized Message will ignore it
						t = (Throwable)last;
					}
				}
			}
			if (msgPattern == null) {
				msgPattern = ""; //don't send null, it will throw and exception since we do a replace of newlines
			}
			CFMMessage cfm = new CFMMessage(this, msgPattern, args);
			cfm.setLogLevel(logLevel);
			cfm.setLoggerName(logger.getName());
			cfm.setCfmException(t);
	//		CFMStructuredMessage cfmS = new CFMStructuredMessage(this, msgPattern, args);
	//		cfmS.setLogLevel(logLevel);
	//		cfmS.setCfmException(t);
		
			logger.log(logLevel,cfm,t);
		}
//		logger.log(logLevel,cfmS,t);
	}


	/**
	 * Log the message to the given logger at TRACE level, includes support for logging an exception
	 * 
	 * @param logger the logger this message should be logged to
	 * @param msgPattern the message to be logged (supports {} for parameter substitution)
	 * @param args the args to be substituted in the msgPattern, last arg can be an "extra" exception item and will be sent to the logger 
	 */
	public void trace(Logger logger,String msgPattern, Object...args) {
		log(logger,Level.TRACE,msgPattern,args);
	}
	/**
	 * Log the message to the given logger at DEBUG level, includes support for logging an exception
	 * 
	 * @param logger the logger this message should be logged to
	 * @param msgPattern the message to be logged (supports {} for parameter substitution)
	 * @param args the args to be substituted in the msgPattern, last arg can be an "extra" exception item and will be sent to the logger
	 */
	public void debug(Logger logger, String msgPattern, Object...args) {
		log(logger,Level.DEBUG,msgPattern,args);
	}
	/**
	 * Log the message to the given logger at INFO level, includes support for logging an exception
	 * 
	 * @param logger the logger this message should be logged to
	 * @param msgPattern the message to be logged (supports {} for parameter substitution)
	 * @param args the args to be substituted in the msgPattern, last arg can be an "extra" exception item and will be sent to the logger
	 */
	public void info(Logger logger, String msgPattern, Object...args) {
		log(logger,Level.INFO,msgPattern,args);
	}
	/**
	 * Log the message to the given logger at WARN level, includes support for logging an exception
	 * 
	 * @param logger the logger this message should be logged to
	 * @param msgPattern the message to be logged (supports {} for parameter substitution)
	 * @param args the args to be substituted in the msgPattern, last arg can be an "extra" exception item and will be sent to the logger
	 */
	public void warn(Logger logger, String msgPattern, Object...args) {
		log(logger,Level.WARN,msgPattern,args);
	}
	/**
	 * Log the message to the given logger at ERROR level, includes support for logging an exception
	 * 
	 * @param logger the logger this message should be logged to
	 * @param msgPattern the message to be logged (supports {} for parameter substitution)
	 * @param args the args to be substituted in the msgPattern, last arg can be an "extra" exception item and will be sent to the logger
	 */
	public void error(Logger logger, String msgPattern, Object...args) {
		log(logger,Level.ERROR,msgPattern,args);
	}
	/**
	 * Log the message to the given logger at FATAL level, includes support for logging an exception
	 * 
	 * @param logger the logger this message should be logged to
	 * @param msgPattern the message to be logged (supports {} for parameter substitution)
	 * @param args the args to be substituted in the msgPattern, last arg can be an "extra" exception item and will be sent to the logger
	 */
	public void fatal(Logger logger, String msgPattern, Object...args) {
		log(logger,Level.FATAL,msgPattern,args);
	}

}
