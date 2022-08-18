package com.inzent.igate.jmeter.sampler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.tcp.sampler.ReadException;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public class InzentTCPSampler extends AbstractSampler implements ThreadListener, Interruptible
{
	private static final long serialVersionUID = 4051085889273598363L;
	private static final Logger log = LoggingManager.getLoggerForClass() ;
	public static final String SERVER = "InzentTCPSampler.server";
	public static final String PORT = "InzentTCPSampler.port";
	public static final String FILENAME = "InzentTCPSampler.filename";
	public static final String NODELAY = "InzentTCPSampler.nodelay";
	public static final String NO_RESPONSE = "InzentTCPSampler.noresponse";
	public static final String TIMEOUT = "InzentTCPSampler.timeout";
	public static final String TIMEOUT_CONNECT = "InzentTCPSampler.ctimeout";
	public static final String REQUEST = "InzentTCPSampler.request";
	public static final String RE_USE_CONNECTION = "InzentTCPSampler.reUseConnection";
	public static final boolean RE_USE_CONNECTION_DEFAULT = true;
	public static final String CLOSE_CONNECTION = "InzentTCPSampler.closeConnection";
	public static final boolean CLOSE_CONNECTION_DEFAULT = false;
	public static final String SO_LINGER = "InzentTCPSampler.soLinger";
	public static final String PACKET_MODE = "InzentTCPSampler.packetMode";
	public static final String PACKET_HEADER_ENCODE = "InzentTCPSampler.packetHeaderEncode";
	public static final String PACKET_HEADER_OFFSET = "InzentTCPSampler.packetHeaderOffset";
	public static final String PACKET_HEADER_LENGTH = "InzentTCPSampler.packetHeaderLength";
	public static final String PACKET_HEADER_COMPENSATION = "InzentTCPSampler.packetHeaderCompensation";	
	
	private static final String ERRKEY = "ERR";
	private static final String TCPKEY = "TCP";
	private static final String STATUS_PREFIX = JMeterUtils.getPropDefault("tcp.status.prefix", "");
	private static final String STATUS_SUFFIX = JMeterUtils.getPropDefault("tcp.status.suffix", "");
	private static final String STATUS_PROPERTIES = JMeterUtils.getPropDefault("tcp.status.properties", "");
	private static final Properties statusProps = new Properties();
	private static final boolean haveStatusProps;

	private transient InzentTCPClient protocolHandler;
	private transient boolean firstSample;
	private volatile transient Socket currentSocket;
	private static final ThreadLocal<Object> tp = new ThreadLocal<Object>()
	{
		protected Map<String, Object> initialValue()
		{
			return new HashMap<String, Object>();
		}
	};
	
	
	static
	{
		boolean hsp = false;
		log.debug("Status prefix=" + STATUS_PREFIX);
		log.debug("Status suffix=" + STATUS_SUFFIX);
		log.debug("Status properties=" + STATUS_PROPERTIES);
		if (STATUS_PROPERTIES.length() > 0) {
			File f = new File(STATUS_PROPERTIES);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				statusProps.load(fis);
				log.debug("Successfully loaded properties");
				hsp = true;
			} catch (FileNotFoundException e) {
				log.debug("Property file not found");
			} catch (IOException e) {
				log.debug("Property file error " + e.toString());
			} finally {
				JOrphanUtils.closeQuietly(fis);
			}

		}

		haveStatusProps = hsp;
	}

	public InzentTCPSampler()
	{
		log.debug("Created " + this);
	}

	@SuppressWarnings("unchecked")
	private String getError() {
		Map<String, Object> cp = (Map<String, Object>)tp.get();
		return (String)cp.get(ERRKEY);
	}

	@SuppressWarnings("unchecked")
	private Socket getSocket(String socketKey) {
		Map<String, Object> cp = (Map<String, Object>)tp.get();
		Socket con = null;
		if (isReUseConnection()) {
			con = (Socket)cp.get(socketKey);
			if (con != null) {
				log.debug(this + " Reusing connection " + con);
			}

		}

		if (con == null) {
			try
			{
				closeSocket(socketKey);
				SocketAddress sockaddr = new InetSocketAddress(getServer(), getPort());
				con = new Socket();
				if (getPropertyAsString(SO_LINGER, "").length() > 0) {
					con.setSoLinger(true, getSoLinger());
				}

				con.connect(sockaddr, getConnectTimeout());
				if (log.isDebugEnabled()) {
					log.debug("Created new connection " + con);
				}

				cp.put(socketKey, con);
			} catch (UnknownHostException e) {
				log.warn("Unknown host for " + getLabel(), e);
				cp.put(ERRKEY, e.toString());
				return null;
			} catch (IOException e) {
				log.warn("Could not create socket for " + getLabel(), e);
				cp.put(ERRKEY, e.toString());
				return null;
			}

		}

		try
		{
			con.setSoTimeout(getTimeout());
			con.setTcpNoDelay(isNoDelay());
			if (log.isDebugEnabled()) {
				log.debug(this + "  Timeout " + getTimeout() + " NoDelay " + isNoDelay());
			}

		} catch (SocketException se) {
			log.warn("Could not set timeout or nodelay for " + getLabel(), se);
			cp.put(ERRKEY, se.toString());
		}

		return con;
	}

	private final String getSocketKey()
	{
		return "TCP#" + getServer() + "#" + getPort() + "#" + getUsername() + "#" + getPassword();
	}

	public String getUsername() {
		return getPropertyAsString("ConfigTestElement.username");
	}

	public String getPassword() {
		return getPropertyAsString("ConfigTestElement.password");
	}

	public void setServer(String newServer) {
		setProperty(SERVER, newServer);
	}

	public String getServer() {
		return getPropertyAsString(SERVER);
	}

	public boolean isReUseConnection() {
		return getPropertyAsBoolean(RE_USE_CONNECTION, true);
	}

	public void setCloseConnection(String close) {
		setProperty(CLOSE_CONNECTION, close, "");
	}

	public boolean isCloseConnection() {
		return getPropertyAsBoolean(CLOSE_CONNECTION, false);
	}

	public void setSoLinger(String soLinger) {
		setProperty(SO_LINGER, soLinger, "");
	}

	public int getSoLinger() {
		return getPropertyAsInt(SO_LINGER);
	}

	public void setPort(String newFilename)
	{
		setProperty(PORT, newFilename);
	}

	public int getPort() {
		return getPropertyAsInt(PORT);
	}

	public void setFilename(String newFilename) {
		setProperty(FILENAME, newFilename);
	}

	public String getFilename() {
		return getPropertyAsString(FILENAME);
	}

	public void setRequestData(String newRequestData) {
		setProperty(REQUEST, newRequestData);
	}

	public String getRequestData() {
		return getPropertyAsString(REQUEST);
	}

	public void setTimeout(String newTimeout) {
		setProperty(TIMEOUT, newTimeout);
	}

	public int getTimeout() {
		return getPropertyAsInt(TIMEOUT);
	}

	public void setConnectTimeout(String newTimeout) {
		setProperty(TIMEOUT_CONNECT, newTimeout, "");
	}

	public int getConnectTimeout() {
		return getPropertyAsInt(TIMEOUT_CONNECT, 0);
	}

	public void setPacketMode(String newPacketMode) {
		setProperty(PACKET_MODE, newPacketMode, "");
	}

	public String getPacketMode() {
		return getPropertyAsString(PACKET_MODE);
	}

	public void setPacketHeaderEncode(String newPacketHeaderEncode) {
		setProperty(PACKET_HEADER_ENCODE, newPacketHeaderEncode, "");
	}

	public String getPacketHeaderEncode() {
		return getPropertyAsString(PACKET_HEADER_ENCODE);
	}

	public void setPacketHeaderOffset(String newValue) {
		setProperty(PACKET_HEADER_OFFSET, newValue);
	}

	public int getPacketHeaderOffset() {
		return getPropertyAsInt(PACKET_HEADER_OFFSET);
	}

	public void setPacketHeaderLength(String newValue) {
		setProperty(PACKET_HEADER_LENGTH, newValue);
	}

	public int getPacketHeaderLength() {
		return getPropertyAsInt(PACKET_HEADER_LENGTH);
	}

	public void setPacketHeaderCompensation(String newValue) {
		setProperty(PACKET_HEADER_COMPENSATION, newValue);
	}

	public int getPacketHeaderCompensation() {
		return getPropertyAsInt(PACKET_HEADER_COMPENSATION);
	}

	public boolean isNoDelay() {
		return getPropertyAsBoolean(NO_RESPONSE, false);
	}

	public boolean getNoResponse() {
		return getPropertyAsBoolean(NO_RESPONSE);
	}
	
	public String getLabel()
	{
		return "tcp://" + getServer() + ":" + getPort();
	}

	private InzentTCPClient getProtocol()
	{
		InzentTCPClient inzentTCPClient = null;
		Class<?> javaClass = InzentTCPClient.class ;

		try {
			inzentTCPClient = (InzentTCPClient)javaClass.newInstance();
			if (getPropertyAsString(PACKET_MODE, "").length() > 0) {
				inzentTCPClient.setPacketMode(getPacketMode().charAt(0));
				log.info("Using PACKET_MODE=" + getPacketMode());
			}

			if (getPropertyAsString(PACKET_HEADER_ENCODE, "").length() > 0) {
				inzentTCPClient.setPacketHeaderEncode(getPacketHeaderEncode().charAt(0));
				log.info("Using PACKET_HEADER_ENCODE=" + getPacketHeaderEncode());
			}

			if (getPropertyAsString(PACKET_HEADER_LENGTH, "").length() > 0) {
				inzentTCPClient.setPacketHeaderLength(getPacketHeaderLength());
				log.info("Using PACKET_HEADER_LENGTH=" + getPacketHeaderLength());
			}
			
			if (getPropertyAsString(PACKET_HEADER_OFFSET, "").length() > 0) {
				inzentTCPClient.setPacketHeaderOffset(getPacketHeaderOffset());
				log.info("Using PACKET_HEADER_OFFSET=" + getPacketHeaderOffset());
			}
			
			if (getPropertyAsString(PACKET_HEADER_COMPENSATION, "").length() > 0) {
				inzentTCPClient.setPacketHeaderCompensation(getPacketHeaderCompensation());
				log.info("Using PACKET_HEADER_COMPENSATION=" + getPacketHeaderCompensation());
			}

			if (log.isDebugEnabled()) {
				log.debug(this + "Created: " + javaClass.getSimpleName() + "@" + Integer.toHexString(inzentTCPClient.hashCode()));
			}

		} catch (Exception e) {
			log.error(this + " Exception creating: " + javaClass.getSimpleName(), e);
		}

		return inzentTCPClient;
	}

	public SampleResult sample(Entry e)
	{
		if (this.firstSample) {
			initSampling();
			this.firstSample = false;
		}

		boolean reUseConnection = isReUseConnection();
		boolean closeConnection = isCloseConnection();
		String socketKey = getSocketKey();
		if (log.isDebugEnabled()) {
			log.debug(getLabel() + " " + getFilename() + " " + getUsername() + " " + getPassword());
		}

		SampleResult res = new SampleResult();
		boolean isSuccessful = false;
		res.setSampleLabel(getName());
		StringBuilder sb = new StringBuilder();
		sb.append("Host: ").append(getServer());
		sb.append(" Port: ").append(getPort());
		sb.append("\n");
		sb.append("Reuse: ").append(reUseConnection);
		sb.append(" Close: ").append(closeConnection);
		sb.append("\n[");
		sb.append("SOLINGER: ").append(getSoLinger());
		sb.append(" PACKET_MODE: ").append(getPacketMode());
		sb.append(" PACKET_HEADER_ENCODE: ").append(getPacketHeaderEncode());
		sb.append(" PACKET_HEADER_OFFSET: ").append(getPacketHeaderOffset());
		sb.append(" PACKET_HEADER_LENGTH: ").append(getPacketHeaderLength());
		sb.append(" PACKET_HEADER_COMPENSATION: ").append(getPacketHeaderCompensation());
		sb.append(" noDelay: ").append(isNoDelay());
		sb.append(" noResponse: ").append(getNoResponse());
		sb.append("]");
		res.setSamplerData(sb.toString());
		res.sampleStart();
		try {
			Socket sock = getSocket(socketKey);
			if (sock == null) {
				res.setResponseCode("500");
				res.setResponseMessage(getError());
			} else if (this.protocolHandler == null) {
				res.setResponseCode("500");
				res.setResponseMessage("Protocol handler not found");
			} else {
				this.currentSocket = sock;
				OutputStream os = sock.getOutputStream();
				String req = getRequestData();
				res.setSamplerData(req);
				this.protocolHandler.write(os, req);
				
				String in = null ;
				if (log.isDebugEnabled()) {
					log.debug(this + "  NO_RESPONSE " + getNoResponse());
				}
				if(getNoResponse()) {
					in = "";
				} else {
					InputStream is = sock.getInputStream();
					in = this.protocolHandler.read(is);
				}
				isSuccessful = setupSampleResult(res, in, null, this.protocolHandler.getCharset());
			}

		} catch (ReadException ex) {
			log.error("", ex);
			isSuccessful = setupSampleResult(res, ex.getPartialResponse(), ex, this.protocolHandler.getCharset());
			closeSocket(socketKey);
		} catch (Exception ex) {
			log.error("", ex);
			isSuccessful = setupSampleResult(res, "", ex, this.protocolHandler.getCharset());
			closeSocket(socketKey);
		} finally {
			this.currentSocket = null;
			res.sampleEnd();
			res.setSuccessful(isSuccessful);
			if ((!reUseConnection) || (closeConnection)) {
				closeSocket(socketKey);
			}

		}

		return res;
	}

	private boolean setupSampleResult(SampleResult sampleResult, String readResponse, Exception exception, String encoding)
	{
		sampleResult.setResponseData(readResponse, encoding);
		sampleResult.setDataType("text");
		if (exception == null) {
			sampleResult.setResponseCodeOK();
			sampleResult.setResponseMessage("OK");
		} else {
			sampleResult.setResponseCode("500");
			sampleResult.setResponseMessage(exception.toString());
		}

		boolean isSuccessful = exception == null;
		if ((!StringUtils.isEmpty(readResponse)) && (STATUS_PREFIX.length() > 0)) {
			int i = readResponse.indexOf(STATUS_PREFIX);
			int j = readResponse.indexOf(STATUS_SUFFIX, i + STATUS_PREFIX.length());
			if ((i != -1) && (j > i)) {
				String rc = readResponse.substring(i + STATUS_PREFIX.length(), j);
				sampleResult.setResponseCode(rc);
				isSuccessful = (isSuccessful) && (checkResponseCode(rc));
				if (haveStatusProps) {
					sampleResult.setResponseMessage(statusProps.getProperty(rc, "Status code not found in properties"));
				} else {
					sampleResult.setResponseMessage("No status property file");
				}

			} else {
				sampleResult.setResponseCode("999");
				sampleResult.setResponseMessage("Status value not found");
				isSuccessful = false;
			}

		}

		return isSuccessful;
	}

	private boolean checkResponseCode(String rc)
	{
		if ((rc.compareTo("400") >= 0) && (rc.compareTo("499") <= 0)) {
			return false;
		}

		if ((rc.compareTo("500") >= 0) && (rc.compareTo("599") <= 0)) {
			return false;
		}

		return true;
	}

	public void threadStarted()
	{
		log.debug("Thread Started");
		this.firstSample = true;
	}

	private void initSampling()
	{
		this.protocolHandler = getProtocol();
		log.debug("Using Protocol Handler: " + (this.protocolHandler == null ? "NONE" : this.protocolHandler.getClass().getName()));
		if (this.protocolHandler != null) {
			this.protocolHandler.setupTest();
		}

	}

	@SuppressWarnings("unchecked")
	private void closeSocket(String socketKey)
	{
		Map<String, Object> cp = (Map<String, Object>)tp.get();
		Socket con = (Socket)cp.remove(socketKey);
		if (con != null) {
			log.debug(this + " Closing connection " + con);
			try {
				con.close();
			} catch (IOException e) {
				log.warn("Error closing socket " + e);
			}

		}

	}

	public void threadFinished()
	{
		log.debug("Thread Finished");
		tearDown();
		if (this.protocolHandler != null) {
			this.protocolHandler.teardownTest();
		}

	}

	@SuppressWarnings("unchecked")
	private void tearDown()
	{
		Map<String, Object> cp = (Map<String, Object>)tp.get();
		for (Map.Entry<String, Object> element : cp.entrySet()) {
			if (((String)element.getKey()).startsWith(TCPKEY)) {
				try {
					((Socket)element.getValue()).close();
				}
				catch (IOException e) {}
			}
		}

		cp.clear();
		tp.remove();
	}

	public boolean applies(ConfigTestElement configElement)
	{
		String guiClass = configElement.getProperty("TestElement.gui_class").getStringValue();
		return "com.inzent.igate.jmeter.control.gui.InzentTCPSamplerGui".equals(guiClass) ;
	}

	public boolean interrupt()
	{
		Socket sock = this.currentSocket;
		if (sock != null) {
			try {
				sock.close();
			}

			catch (IOException e) {}

			return true;
		}

		return false;
	}

}