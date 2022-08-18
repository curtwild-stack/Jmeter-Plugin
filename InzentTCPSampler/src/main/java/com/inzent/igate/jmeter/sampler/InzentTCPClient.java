package com.inzent.igate.jmeter.sampler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.tcp.sampler.AbstractTCPClient;
import org.apache.jmeter.protocol.tcp.sampler.ReadException;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class InzentTCPClient extends AbstractTCPClient
{
	private static final Logger log = LoggingManager.getLoggerForClass() ;
	private static final String charset = JMeterUtils.getPropDefault("tcp.charset", Charset.defaultCharset().name());
	protected char packetMode;
	protected char packetHeaderEncode;
	protected int packetHeaderOffset;
	protected int packetHeaderLength;
	protected int packetHeaderCompensation;	
	
	public InzentTCPClient()
	{
		setCharset(charset);
		String configuredCharset = JMeterUtils.getProperty("tcp.charset");
		if (StringUtils.isEmpty(configuredCharset)) {
			log.info("Using platform default charset:" + charset);
		} else {
			log.info("Using charset:" + configuredCharset);
		}

	}

	
	public char getPacketMode() {
		return packetMode;
	}


	public void setPacketMode(char packetMode) {
		this.packetMode = packetMode;
	}


	public char getPacketHeaderEncode() {
		return packetHeaderEncode;
	}


	public void setPacketHeaderEncode(char packetHeaderEncode) {
		this.packetHeaderEncode = packetHeaderEncode;
	}


	public int getPacketHeaderOffset() {
		return packetHeaderOffset;
	}


	public void setPacketHeaderOffset(int packetHeaderOffset) {
		this.packetHeaderOffset = packetHeaderOffset;
	}


	public int getPacketHeaderLength() {
		return packetHeaderLength;
	}


	public void setPacketHeaderLength(int packetHeaderLength) {
		this.packetHeaderLength = packetHeaderLength;
	}


	public int getPacketHeaderCompensation() {
		return packetHeaderCompensation;
	}


	public void setPacketHeaderCompensation(int packetHeaderCompensation) {
		this.packetHeaderCompensation = packetHeaderCompensation;
	}


	public void write(OutputStream os, String s)
			throws IOException
	{
		os.write(s.getBytes(charset));
		os.flush();
	}

	public void write(OutputStream os, InputStream is)
			throws IOException
	{
		byte[] buff = new byte['?'];
		while (is.read(buff) > 0) {
			os.write(buff);
			os.flush();
		}

	}

	public String read(InputStream is) throws ReadException
	{
		ByteArrayOutputStream w = new ByteArrayOutputStream();
		try {
			switch (this.packetMode)
			{
			case 'L':
				if(this.packetHeaderOffset > 0) {
					byte[] aryBeforePacketHeader = new byte[this.packetHeaderOffset] ;
					is.read(aryBeforePacketHeader, 0, aryBeforePacketHeader.length) ;
					w.write(aryBeforePacketHeader, 0, aryBeforePacketHeader.length);
					log.info("Read:[" + new String(aryBeforePacketHeader, charset) + "]");
				}
				
				if(this.packetHeaderLength <= 0)
					this.packetHeaderLength = 8;
				byte[] aryPacketHeader = new byte[this.packetHeaderLength] ;
				int n = is.read(aryPacketHeader, 0, aryPacketHeader.length);
				log.info("Read: " + n + "\n" + new String(aryPacketHeader));
				w.write(aryPacketHeader, 0, aryPacketHeader.length);
				int remainPacketLength = Integer.parseInt(new String(aryPacketHeader, charset)) + this.packetHeaderCompensation;
				
				int x = 0;
				byte[] buffer = new byte[64] ;
				while ((x = is.read(buffer)) > -1) {
					w.write(buffer, 0, x);
					remainPacketLength -= x;
					if (remainPacketLength <= 0 ) {
						break;
					}
				}
				break;
				
			default:
			}

			if (log.isDebugEnabled()) {
				log.debug("Read: " + w.size() + "\n" + w.toString());
			}

			return w.toString(charset);
		} catch (Exception e) {
			throw new ReadException("Error reading from server, bytes read: " + w.size(), e, w.toString());
		}

	}
}