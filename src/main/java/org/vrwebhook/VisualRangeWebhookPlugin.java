package org.vrwebhook;

import com.zenith.plugin.api.Plugin;
import com.zenith.plugin.api.PluginAPI;
import com.zenith.plugin.api.ZenithProxyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.vrwebhook.command.VisualRangeWebhookCommand;
import org.vrwebhook.module.VisualRangeWebhookModule;

@Plugin(
    id = BuildConstants.PLUGIN_ID,
    version = BuildConstants.VERSION,
    description = "Visual Range Webhook Plugin",
    url = "https://github.com/duccss/VisualRangeWebhook/",
    authors = {"duccss"},
    mcVersions = {BuildConstants.MC_VERSION} // to indicate any MC version: @Plugin(mcVersions = "*")
)
public class VisualRangeWebhookPlugin implements ZenithProxyPlugin {

    public static VisualRangeWebhookConfig PLUGIN_CONFIG;
    public static ComponentLogger LOG;

    @Override
    public void onLoad(PluginAPI pluginAPI) {
        LOG = pluginAPI.getLogger();
        LOG.info("Visual Range Webhook Plugin loading...");
        PLUGIN_CONFIG = pluginAPI.registerConfig(BuildConstants.PLUGIN_ID, VisualRangeWebhookConfig.class);
        pluginAPI.registerModule(new VisualRangeWebhookModule());
        pluginAPI.registerCommand(new VisualRangeWebhookCommand());
        LOG.info("Visual Range Webhook Plugin loaded!");
    }
}
