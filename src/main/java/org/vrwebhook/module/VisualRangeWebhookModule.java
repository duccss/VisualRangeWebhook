package org.vrwebhook.module;

import com.github.rfresh2.EventConsumer;
import com.zenith.Proxy;
import com.zenith.discord.DiscordBot;
import com.zenith.discord.Embed;
import com.zenith.event.module.VisualRangeEnterEvent;
import com.zenith.module.api.Module;
import org.vrwebhook.VisualRangeWebhookPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.rfresh2.EventConsumer.of;
import static com.zenith.Globals.GSON;

public class VisualRangeWebhookModule extends Module {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    @Override
    public boolean enabledSetting() {
        return VisualRangeWebhookPlugin.PLUGIN_CONFIG.enabled;
    }

    @Override
    public List<EventConsumer<?>> registerEvents() {
        return List.of(
            of(VisualRangeEnterEvent.class, this::handleVisualRangeEnterEvent)
        );
    }

    private void handleVisualRangeEnterEvent(VisualRangeEnterEvent event) {
        if (event.isFriend()) return;
        var webhookUrl = VisualRangeWebhookPlugin.PLUGIN_CONFIG.webhookUrl;
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        var embed = Embed.builder()
            .title("Player In Visual Range")
            .errorColor()
            .addField("Player Name", DiscordBot.escape(event.playerEntry().getName()), true)
            .addField("Player UUID", "[%s](https://namemc.com/profile/%s)".formatted(
                event.playerEntry().getProfileId(),
                event.playerEntry().getProfileId()), true)
            .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.playerEntry().getProfileId()).toString())
            .addField("Coordinates", "||[%d, %d, %d]||".formatted(
                (int) event.playerEntity().getX(),
                (int) event.playerEntity().getY(),
                (int) event.playerEntity().getZ()), false);

        try {
            sendWebhookMessage(webhookUrl, null, List.of(embed.toJDAEmbed().toData().toMap()));
            if (VisualRangeWebhookPlugin.PLUGIN_CONFIG.sendEveryoneMessage) {
                sendWebhookMessage(webhookUrl, "@everyone", null);
            }
        } catch (Exception e) {
            debug("Failed to send visual range webhook alert", e);
        }
    }

    private void sendWebhookMessage(String webhookUrl, String content, List<Map<String, Object>> embeds)
        throws URISyntaxException, IOException, InterruptedException {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (content != null && !content.isBlank()) payload.put("content", content);
        if (embeds != null && !embeds.isEmpty()) payload.put("embeds", embeds);
        if ("@everyone".equals(content)) {
            payload.put("allowed_mentions", Map.of("parse", List.of("everyone")));
        }

        var request = HttpRequest.newBuilder()
            .uri(new URI(webhookUrl))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload)))
            .build();

        var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Webhook returned status " + response.statusCode() + ": " + response.body());
        }
    }
}
