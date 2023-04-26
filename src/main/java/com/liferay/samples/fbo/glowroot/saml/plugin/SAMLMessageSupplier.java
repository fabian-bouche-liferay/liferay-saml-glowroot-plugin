package com.liferay.samples.fbo.glowroot.saml.plugin;

import java.util.HashMap;
import java.util.Map;

import org.glowroot.agent.plugin.api.Message;
import org.glowroot.agent.plugin.api.MessageSupplier;

public class SAMLMessageSupplier extends MessageSupplier {

	private String emailAddress;
	private String screenName;
	private String firstName;
	private String lastName;
	
	@Override
	public Message get() {
        Map<String, Object> detail = new HashMap<String, Object>();
        if (emailAddress != null) {
            detail.put("SAML Email Address", emailAddress);
        }
        if (screenName != null) {
            detail.put("SAML Screen Name", screenName);
        }
        if (firstName != null) {
            detail.put("SAML First Name", firstName);
        }
        if (lastName != null) {
            detail.put("SAML Last Name", lastName);
        }
        return Message.create("SAML Message", detail);
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
}
