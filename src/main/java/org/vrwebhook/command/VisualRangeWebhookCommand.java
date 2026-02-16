package org.vrwebhook.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.api.Command;
import com.zenith.command.api.CommandCategory;
import com.zenith.command.api.CommandContext;
import com.zenith.command.api.CommandUsage;
import com.zenith.discord.Embed;
import org.vrwebhook.module.VisualRangeWebhookModule;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.zenith.Globals.MODULE;
import static com.zenith.command.brigadier.ToggleArgumentType.getToggle;
import static com.zenith.command.brigadier.ToggleArgumentType.toggle;
import static org.vrwebhook.VisualRangeWebhookPlugin.PLUGIN_CONFIG;

public class VisualRangeWebhookCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("visualRangeWebhook")
            .category(CommandCategory.MODULE)
            .description("Webhook notifications for unknown players entering visual range.")
            .usageLines(
                "on/off",
                "webhook <url>",
                "everyoneMention <on/off>"
            )
            .aliases("vrw")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("visualRangeWebhook")
            .then(argument("toggle", toggle()).executes(c -> {
                PLUGIN_CONFIG.enabled = getToggle(c, "toggle");
                MODULE.get(VisualRangeWebhookModule.class).syncEnabledFromConfig();
                c.getSource().getEmbed()
                    .title("Visual Range Webhook " + toggleStrCaps(PLUGIN_CONFIG.enabled))
                    .primaryColor();
            }))
            .then(literal("webhook").then(argument("url", greedyString()).executes(c -> {
                PLUGIN_CONFIG.webhookUrl = getString(c, "url").trim();
                c.getSource().getEmbed()
                    .title("Visual Range Webhook URL Set")
                    .description(PLUGIN_CONFIG.webhookUrl.isBlank()
                        ? "Webhook URL cleared"
                        : "Webhook URL configured")
                    .primaryColor();
                return OK;
            })))
            .then(literal("everyoneMention").then(argument("toggle", toggle()).executes(c -> {
                PLUGIN_CONFIG.sendEveryoneMessage = getToggle(c, "toggle");
                c.getSource().getEmbed()
                    .title("Visual Range @everyone Mention " + toggleStrCaps(PLUGIN_CONFIG.sendEveryoneMessage))
                    .primaryColor();
                return OK;
            })));
    }

    @Override
    public void defaultEmbed(Embed embed) {
        embed
            .primaryColor()
            .addField("Enabled", toggleStr(PLUGIN_CONFIG.enabled))
            .addField("Webhook URL", PLUGIN_CONFIG.webhookUrl.isBlank() ? "Not Set" : "Configured")
            .addField("@everyone Mention", toggleStr(PLUGIN_CONFIG.sendEveryoneMessage));
    }
}
