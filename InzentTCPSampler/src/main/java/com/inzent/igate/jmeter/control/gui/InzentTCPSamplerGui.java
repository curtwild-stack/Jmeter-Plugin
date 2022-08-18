package com.inzent.igate.jmeter.control.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import com.inzent.igate.jmeter.config.gui.InzentTCPConfigGui;
import com.inzent.igate.jmeter.sampler.InzentTCPSampler;

public class InzentTCPSamplerGui extends AbstractSamplerGui
{
	private static final long serialVersionUID = -1897133468901809534L;
	private InzentTCPConfigGui tcpDefaultPanel;
	public InzentTCPSamplerGui()
	{
		init();
	}

	public void configure(TestElement element)
	{
		super.configure(element);
		this.tcpDefaultPanel.configure(element);
	}

	public TestElement createTestElement()
	{
		InzentTCPSampler sampler = new InzentTCPSampler();
		modifyTestElement(sampler);
		return sampler;
	}

	public void modifyTestElement(TestElement sampler)
	{
		sampler.clear();
		sampler.addTestElement(this.tcpDefaultPanel.createTestElement());
		configureTestElement(sampler);
	}

	public void clearGui()
	{
		super.clearGui();
		this.tcpDefaultPanel.clearGui();
	}

	public String getLabelResource()
	{
		throw new IllegalStateException("This shouldn't be called");
	}

	@Override
	public String getStaticLabel() {
		return "TCP(Inzent) Sampler";
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());
		add(makeTitlePanel(), "North");
		VerticalPanel mainPanel = new VerticalPanel();
		this.tcpDefaultPanel = new InzentTCPConfigGui(false);
		mainPanel.add(this.tcpDefaultPanel);
		add(mainPanel, "Center");
	}

}