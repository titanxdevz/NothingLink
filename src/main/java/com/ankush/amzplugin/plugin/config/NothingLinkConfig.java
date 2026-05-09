package com.ankush.amzplugin.plugin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "plugins.nothinglink")
@Component("nothingLinkConfig")
public class NothingLinkConfig {
	private String webhookUrl = "";
	private boolean webhookEnabled = false;

	public String getWebhookUrl() { return webhookUrl; }
	public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
	public boolean isWebhookEnabled() { return webhookEnabled; }
	public void setWebhookEnabled(boolean webhookEnabled) { this.webhookEnabled = webhookEnabled; }
}
