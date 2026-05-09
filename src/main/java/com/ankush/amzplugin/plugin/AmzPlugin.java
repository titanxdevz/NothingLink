package com.ankush.amzplugin.plugin;

import com.ankush.amzplugin.AmazonMusicSourceManager;
import com.ankush.amzplugin.ig.IGSourceManager;
import com.ankush.amzplugin.pd.PandoraProvider;
import com.ankush.amzplugin.plugin.config.AmzPluginConfig;
import com.ankush.amzplugin.plugin.config.InstagramPluginConfig;
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
	private final AmzPluginConfig config;
	private final InstagramPluginConfig igConfig;
	private final PandoraPluginConfig pdConfig;

	public AmzPlugin(AmzPluginConfig config, InstagramPluginConfig igConfig, PandoraPluginConfig pdConfig) {
		log.info("Loading NothingLink Plugin...");
		this.config = config;
		this.igConfig = igConfig;
		this.pdConfig = pdConfig;

		if (config.isEnabled()) {
			this.amazonMusic = new AmazonMusicSourceManager(config.getProviders(), unused -> manager);
			int sl = Math.max(0, Math.min(10, config.getSearchLimit()));
			this.amazonMusic.setSearchLimit(sl);
			log.info("Amazon Music source enabled (searchLimit={})", sl);
		} else { log.info("Amazon Music source is disabled"); }

		if (igConfig.isEnabled()) {
			this.igSource = new IGSourceManager();
			log.info("Instagram source enabled");
		} else { log.info("Instagram source is disabled"); }

		if (pdConfig.isEnabled()) {
			this.pandoraSource = new PandoraProvider(pdConfig.getProviders(), pdConfig.getTokenApiUrl(),
				pdConfig.getCsrfToken(), pdConfig.isPreferTokenApi(), pdConfig.getSearchLimit(), unused -> manager);
			log.info("Pandora source enabled (searchLimit={})", pdConfig.getSearchLimit());
		} else { log.info("Pandora source is disabled"); }
	}

	@NotNull @Override public AudioPlayerManager configure(@NotNull AudioPlayerManager manager) {
		this.manager = manager;
		if (amazonMusic != null && config.isEnabled()) { log.info("Registering Amazon Music source..."); manager.registerSourceManager(amazonMusic); }
		if (igSource != null && igConfig.isEnabled()) { log.info("Registering Instagram source..."); manager.registerSourceManager(igSource); }
		if (pandoraSource != null && pdConfig.isEnabled()) { log.info("Registering Pandora source..."); manager.registerSourceManager(pandoraSource); }
		return manager;
	}

	@NotNull @Override public SearchManager configure(@NotNull SearchManager manager) {
		if (amazonMusic != null && config.isEnabled()) { log.info("Registering Amazon Music search..."); manager.registerSearchManager(amazonMusic); }
		return manager;
	}
}
