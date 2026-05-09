package com.ankush.amzplugin.plugin;

import com.ankush.amzplugin.AmazonMusicSourceManager;
import com.ankush.amzplugin.ig.IGSourceManager;
import com.ankush.amzplugin.pd.PandoraProvider;
import com.ankush.amzplugin.plugin.config.AmzPluginConfig;
import com.ankush.amzplugin.plugin.config.InstagramPluginConfig;
import com.ankush.amzplugin.plugin.config.NothingLinkConfig;
import com.ankush.amzplugin.plugin.config.PandoraPluginConfig;
import com.github.topi314.lavasearch.SearchManager;
import com.github.topi314.lavasearch.api.SearchManagerConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("amzPluginService")
public class AmzPlugin implements AudioPlayerManagerConfiguration, SearchManagerConfiguration {
	private static final Logger log = LoggerFactory.getLogger(AmzPlugin.class);
	private AudioPlayerManager manager;
	private AmazonMusicSourceManager amazonMusic;
	private IGSourceManager igSource;
	private PandoraProvider pandoraSource;
	private DiscordWebhookLogger webhook;
	private final AmzPluginConfig config;
	private final InstagramPluginConfig igConfig;
	private final PandoraPluginConfig pdConfig;
	private final NothingLinkConfig nlConfig;

	public AmzPlugin(AmzPluginConfig config, InstagramPluginConfig igConfig, PandoraPluginConfig pdConfig, NothingLinkConfig nlConfig) {
		log.info("Loading NothingLink Plugin...");
		this.config = config;
		this.igConfig = igConfig;
		this.pdConfig = pdConfig;
		this.nlConfig = nlConfig;

		// webhook logger
		if (nlConfig.isWebhookEnabled() && nlConfig.getWebhookUrl() != null && !nlConfig.getWebhookUrl().isEmpty()) {
			this.webhook = new DiscordWebhookLogger(nlConfig.getWebhookUrl());
			log.info("Discord webhook logger enabled");
		}

		StringBuilder sources = new StringBuilder();

		if (config.isEnabled()) {
			this.amazonMusic = new AmazonMusicSourceManager(config.getProviders(), unused -> manager);
			int sl = Math.max(0, Math.min(10, config.getSearchLimit()));
			this.amazonMusic.setSearchLimit(sl);
			log.info("Amazon Music source enabled (searchLimit={})", sl);
			sources.append("✅ Amazon Music (searchLimit=").append(sl).append(")\n");
		} else {
			log.info("Amazon Music source is disabled");
			sources.append("❌ Amazon Music\n");
		}

		if (igConfig.isEnabled()) {
			this.igSource = new IGSourceManager();
			log.info("Instagram source enabled");
			sources.append("✅ Instagram\n");
		} else {
			log.info("Instagram source is disabled");
			sources.append("❌ Instagram\n");
		}

		if (pdConfig.isEnabled()) {
			this.pandoraSource = new PandoraProvider(pdConfig.getProviders(), pdConfig.getTokenApiUrl(),
				pdConfig.getCsrfToken(), pdConfig.isPreferTokenApi(), pdConfig.getSearchLimit(), unused -> manager);
			log.info("Pandora source enabled (searchLimit={})", pdConfig.getSearchLimit());
			sources.append("✅ Pandora (searchLimit=").append(pdConfig.getSearchLimit()).append(")\n");
		} else {
			log.info("Pandora source is disabled");
			sources.append("❌ Pandora\n");
		}

		if (webhook != null) {
			webhook.startup("🎵 NothingLink Plugin Loaded", sources.toString(), "NothingLink v1.0.4");
		}
	}

	@NotNull @Override public AudioPlayerManager configure(@NotNull AudioPlayerManager manager) {
		this.manager = manager;
		StringBuilder registered = new StringBuilder();

		if (amazonMusic != null && config.isEnabled()) {
			log.info("Registering Amazon Music source...");
			manager.registerSourceManager(amazonMusic);
			registered.append("✅ Amazon Music\n");
		}
		if (igSource != null && igConfig.isEnabled()) {
			log.info("Registering Instagram source...");
			manager.registerSourceManager(igSource);
			registered.append("✅ Instagram\n");
		}
		if (pandoraSource != null && pdConfig.isEnabled()) {
			log.info("Registering Pandora source...");
			manager.registerSourceManager(pandoraSource);
			registered.append("✅ Pandora\n");
		}

		if (webhook != null && registered.length() > 0) {
			webhook.info("📡 Sources Registered", registered.toString());
		}

		return manager;
	}

	@NotNull @Override public SearchManager configure(@NotNull SearchManager manager) {
		if (amazonMusic != null && config.isEnabled()) {
			log.info("Registering Amazon Music search...");
			manager.registerSearchManager(amazonMusic);
		}
		return manager;
	}
}
