package com.ankush.amzplugin.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends log messages to a Discord channel via webhook.
 */
public class DiscordWebhookLogger {

    private static final Logger log = LoggerFactory.getLogger(DiscordWebhookLogger.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String webhookUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NothingLink-Webhook");
        t.setDaemon(true);
        return t;
    });

    public DiscordWebhookLogger(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    /**
     * Send an info embed (green).
     */
    public void info(String title, String description) {
        sendEmbed(title, description, 0x2ECC71, null);
    }

    /**
     * Send a warning embed (yellow).
     */
    public void warn(String title, String description) {
        sendEmbed(title, description, 0xF1C40F, null);
    }

    /**
     * Send an error embed (red).
     */
    public void error(String title, String description) {
        sendEmbed(title, description, 0xE74C3C, null);
    }

    /**
     * Send a startup summary embed (blue).
     */
    public void startup(String title, String description, String footer) {
        sendEmbed(title, description, 0x3498DB, footer);
    }

    private void sendEmbed(String title, String description, int color, String footer) {
        executor.submit(() -> {
            try {
                ObjectNode root = JSON.createObjectNode();
                root.putNull("content");
                ArrayNode embeds = root.putArray("embeds");
                ObjectNode embed = embeds.addObject();
                embed.put("title", title);
                if (description != null && !description.isEmpty())
                    embed.put("description", description);
                embed.put("color", color);
                embed.put("timestamp", Instant.now().toString());
                if (footer != null && !footer.isEmpty()) {
                    ObjectNode ft = embed.putObject("footer");
                    ft.put("text", footer);
                }

                String body = JSON.writeValueAsString(root);
                HttpRequest req = HttpRequest.newBuilder(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 400) {
                    log.debug("Webhook returned HTTP {}", resp.statusCode());
                }
            } catch (Exception e) {
                log.debug("Webhook send failed: {}", e.getMessage());
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
