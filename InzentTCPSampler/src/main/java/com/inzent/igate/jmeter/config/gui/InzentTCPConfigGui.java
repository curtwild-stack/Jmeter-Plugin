package com.inzent.igate.jmeter.config.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.ServerPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.TristateCheckBox;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import com.inzent.igate.jmeter.sampler.InzentTCPSampler;

public class InzentTCPConfigGui extends AbstractConfigGui
{
	private static final long serialVersionUID = 7884991235267049183L;
	private ServerPanel serverPanel;
	private JCheckBox reUseConnection;
	private TristateCheckBox setNoDelay;
	private TristateCheckBox closeConnection;
	private JCheckBox noResponse;
	private JTextField soLinger;
	private JTextField packetMode;
	private JTextField packetHeaderEncode;
	private JTextField packetHeaderOffset;
	private JTextField packetHeaderLength;
	private JTextField packetHeaderCompensation;
	private JSyntaxTextArea requestData;
	private boolean displayName = true;
	public InzentTCPConfigGui() {
		this(true);
	}

	public InzentTCPConfigGui(boolean displayName) {
		this.displayName = displayName;
		init();
	}

	public String getLabelResource()
	{
		throw new IllegalStateException("This shouldn't be called");
	}

	@Override
	public String getStaticLabel() {
		return "TCP(Inzent) Sampler";
	}

	public void configure(TestElement element)
	{
		super.configure(element);
		this.serverPanel.setServer(element.getPropertyAsString(InzentTCPSampler.SERVER));
		this.reUseConnection.setSelected(element.getPropertyAsBoolean(InzentTCPSampler.RE_USE_CONNECTION, true));
		this.noResponse.setSelected(element.getPropertyAsBoolean(InzentTCPSampler.NO_RESPONSE, false));
		this.serverPanel.setPort(element.getPropertyAsString(InzentTCPSampler.PORT));
		this.serverPanel.setResponseTimeout(element.getPropertyAsString(InzentTCPSampler.TIMEOUT));
		this.serverPanel.setConnectTimeout(element.getPropertyAsString(InzentTCPSampler.TIMEOUT_CONNECT));
		this.setNoDelay.setTristateFromProperty(element, InzentTCPSampler.NODELAY);
		this.requestData.setInitialText(element.getPropertyAsString(InzentTCPSampler.REQUEST));
		this.requestData.setCaretPosition(0);
		this.closeConnection.setTristateFromProperty(element, InzentTCPSampler.CLOSE_CONNECTION);
		this.soLinger.setText(element.getPropertyAsString(InzentTCPSampler.SO_LINGER));
		
		this.packetMode.setText(element.getPropertyAsString(InzentTCPSampler.PACKET_MODE));
		this.packetHeaderEncode.setText(element.getPropertyAsString(InzentTCPSampler.PACKET_HEADER_ENCODE));
		this.packetHeaderOffset.setText(element.getPropertyAsString(InzentTCPSampler.PACKET_HEADER_OFFSET));
		this.packetHeaderLength.setText(element.getPropertyAsString(InzentTCPSampler.PACKET_HEADER_LENGTH));
		this.packetHeaderCompensation.setText(element.getPropertyAsString(InzentTCPSampler.PACKET_HEADER_COMPENSATION));
	}

	public TestElement createTestElement()
	{
		ConfigTestElement element = new ConfigTestElement();
		modifyTestElement(element);
		return element;
	}

	public void modifyTestElement(TestElement element)
	{
		configureTestElement(element);
		element.setProperty(InzentTCPSampler.SERVER, this.serverPanel.getServer());
		element.setProperty(InzentTCPSampler.RE_USE_CONNECTION, this.reUseConnection.isSelected());
		element.setProperty(InzentTCPSampler.PORT, this.serverPanel.getPort());
		this.setNoDelay.setPropertyFromTristate(element, InzentTCPSampler.NODELAY);
		element.setProperty(InzentTCPSampler.NO_RESPONSE, this.noResponse.isSelected());
		element.setProperty(InzentTCPSampler.TIMEOUT, this.serverPanel.getResponseTimeout());
		element.setProperty(InzentTCPSampler.TIMEOUT_CONNECT, this.serverPanel.getConnectTimeout(), "");
		element.setProperty(InzentTCPSampler.REQUEST, this.requestData.getText());
		this.closeConnection.setPropertyFromTristate(element, InzentTCPSampler.CLOSE_CONNECTION);
		element.setProperty(InzentTCPSampler.SO_LINGER, this.soLinger.getText(), "");
		
		element.setProperty(InzentTCPSampler.PACKET_MODE, this.packetMode.getText(), "");
		element.setProperty(InzentTCPSampler.PACKET_HEADER_ENCODE, this.packetHeaderEncode.getText(), "");
		element.setProperty(InzentTCPSampler.PACKET_HEADER_OFFSET, this.packetHeaderOffset.getText(), "");
		element.setProperty(InzentTCPSampler.PACKET_HEADER_LENGTH, this.packetHeaderLength.getText(), "");
		element.setProperty(InzentTCPSampler.PACKET_HEADER_COMPENSATION, this.packetHeaderCompensation.getText(), "");
	}

	public void clearGui()
	{
		super.clearGui();
		this.serverPanel.clear();
		this.requestData.setInitialText("");
		this.reUseConnection.setSelected(true);
		this.setNoDelay.setSelected(false);
		this.noResponse.setSelected(false);
		this.closeConnection.setSelected(false);
		this.soLinger.setText("");
		
		this.packetMode.setText("");
		this.packetHeaderEncode.setText("");
		this.packetHeaderOffset.setText("");
		this.packetHeaderLength.setText("");
		this.packetHeaderCompensation.setText("");

	}

	private JPanel createNoDelayPanel()
	{
		JLabel label = new JLabel(JMeterUtils.getResString("tcp_nodelay"));
		this.setNoDelay = new TristateCheckBox();
		label.setLabelFor(this.setNoDelay);
		JPanel nodelayPanel = new JPanel(new FlowLayout());
		nodelayPanel.add(label);
		nodelayPanel.add(this.setNoDelay);
		return nodelayPanel;
	}

	private JPanel createNoResponsePanel()
	{
		JLabel label = new JLabel("NO_RESPONSE");
		this.noResponse = new JCheckBox("", false);
		label.setLabelFor(this.noResponse);
		JPanel noResponsePanel = new JPanel(new FlowLayout());
		noResponsePanel.add(label);
		noResponsePanel.add(this.noResponse);
		return noResponsePanel;
	}
	
	private JPanel createClosePortPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("reuseconnection"));
		this.reUseConnection = new JCheckBox("", true);
		this.reUseConnection.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == 1) {
					InzentTCPConfigGui.this.closeConnection.setEnabled(true);
				} else {
					InzentTCPConfigGui.this.closeConnection.setEnabled(false);
				}

			}

		});
		label.setLabelFor(this.reUseConnection);
		JPanel closePortPanel = new JPanel(new FlowLayout());
		closePortPanel.add(label);
		closePortPanel.add(this.reUseConnection);
		return closePortPanel;
	}

	private JPanel createCloseConnectionPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("closeconnection"));
		this.closeConnection = new TristateCheckBox("", false);
		label.setLabelFor(this.closeConnection);
		JPanel closeConnectionPanel = new JPanel(new FlowLayout());
		closeConnectionPanel.add(label);
		closeConnectionPanel.add(this.closeConnection);
		return closeConnectionPanel;
	}

	private JPanel createSoLingerOption() {
		JLabel label = new JLabel(JMeterUtils.getResString("solinger"));
		this.soLinger = new JTextField(5);
		this.soLinger.setMaximumSize(new Dimension(this.soLinger.getPreferredSize()));
		label.setLabelFor(this.soLinger);
		JPanel soLingerPanel = new JPanel(new FlowLayout());
		soLingerPanel.add(label);
		soLingerPanel.add(this.soLinger);
		return soLingerPanel;
	}

	private JPanel createRequestPanel() {
		JLabel reqLabel = new JLabel(JMeterUtils.getResString("tcp_request_data"));
		this.requestData = new JSyntaxTextArea(15, 80);
		this.requestData.setLanguage("text");
		reqLabel.setLabelFor(this.requestData);
		JPanel reqDataPanel = new JPanel(new BorderLayout(5, 0));
		reqDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
		reqDataPanel.add(reqLabel, "West");
		reqDataPanel.add(new JTextScrollPane(this.requestData), "Center");
		return reqDataPanel;
	}
	
	private JPanel createPacketModeOption() {
		JLabel label = new JLabel("PACKET_MODE");
		this.packetMode = new JTextField(2);
		this.packetMode.setMaximumSize(new Dimension(this.packetMode.getPreferredSize()));
		label.setLabelFor(this.packetMode);
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(label);
		panel.add(this.packetMode);
		return panel;
	}
	
	private JPanel createPacketHeaderEncodeOption() {
		JLabel label = new JLabel("PACKET_HEADER_ENCODE");
		this.packetHeaderEncode = new JTextField(2);
		this.packetHeaderEncode.setMaximumSize(new Dimension(this.packetHeaderEncode.getPreferredSize()));
		label.setLabelFor(this.packetHeaderEncode);
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(label);
		panel.add(this.packetHeaderEncode);
		return panel;
	}
	
	private JPanel createPacketHeaderOffsetOption() {
		JLabel label = new JLabel("PACKET_HEADER_OFFSET");
		this.packetHeaderOffset = new JTextField(2);
		this.packetHeaderOffset.setMaximumSize(new Dimension(this.packetHeaderOffset.getPreferredSize()));
		label.setLabelFor(this.packetHeaderOffset);
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(label);
		panel.add(this.packetHeaderOffset);
		return panel;
	}
	
	private JPanel createPacketHeaderLengthOption() {
		JLabel label = new JLabel("PACKET_HEADER_LENGTH");
		this.packetHeaderLength = new JTextField(2);
		this.packetHeaderLength.setMaximumSize(new Dimension(this.packetHeaderLength.getPreferredSize()));
		label.setLabelFor(this.packetHeaderLength);
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(label);
		panel.add(this.packetHeaderLength);
		return panel;
	}
	
	private JPanel createPacketHeaderCompensationOption() {
		JLabel label = new JLabel("PACKET_HEADER_COMPENSATION");
		this.packetHeaderCompensation = new JTextField(2);
		this.packetHeaderCompensation.setMaximumSize(new Dimension(this.packetHeaderCompensation.getPreferredSize()));
		label.setLabelFor(this.packetHeaderCompensation);
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(label);
		panel.add(this.packetHeaderCompensation);
		return panel;
	}
	
	private void init()
	{
		setLayout(new BorderLayout(0, 5));
		this.serverPanel = new ServerPanel();
		if (this.displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), "North");
		}

		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(this.serverPanel);
		HorizontalPanel optionsPanel = new HorizontalPanel();
		optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
		optionsPanel.add(createClosePortPanel());
		optionsPanel.add(createCloseConnectionPanel());
		optionsPanel.add(createNoDelayPanel());
		optionsPanel.add(createNoResponsePanel());
		optionsPanel.add(createSoLingerOption());
		mainPanel.add(optionsPanel);
		
		HorizontalPanel packetOptionPanel = new HorizontalPanel();
		packetOptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
		packetOptionPanel.add(createPacketModeOption());
		packetOptionPanel.add(createPacketHeaderEncodeOption());
		packetOptionPanel.add(createPacketHeaderOffsetOption());
		packetOptionPanel.add(createPacketHeaderLengthOption());
		packetOptionPanel.add(createPacketHeaderCompensationOption());
		mainPanel.add(packetOptionPanel);

		mainPanel.add(createRequestPanel());
		add(mainPanel, "Center");
	}

}