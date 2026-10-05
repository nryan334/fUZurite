package me.keano.azurite.modules.discord.type;

import me.keano.azurite.modules.discord.Discord;
import me.keano.azurite.modules.discord.extra.DiscordWebhook;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.entity.Player;

import java.awt.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RequestWebhook extends Discord {

    public RequestWebhook(Manager manager, Player player, String request) {
        super(manager, Config.DISCORD_REQUEST_WEBHOOKURL);
        discordWebhook.setContent(Config.DISCORD_REQUEST_CONTENT
                .replace("%player%", player.getName())
                .replace("%request%", request)
        );
        discordWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setAuthor(Config.DISCORD_REQUEST_AUTHOR
                        .replace("%player%", player.getName())
                        .replace("%request%", request), Config.DISCORD_REQUEST_AUTHOR_URL, Config.DISCORD_REQUEST_AUTHOR_ICON)
                .setDescription(Config.DISCORD_REQUEST_DESCRIPTION
                        .replace("%player%", player.getName())
                        .replace("%request%", request))
                .setColor(Color.decode(Config.DISCORD_REQUEST_COLOR))
                .setFooter(Config.DISCORD_REQUEST_FOOTER, Config.DISCORD_REQUEST_FOOTER_ICON)
                .setThumbnail(Config.DISCORD_REQUEST_THUMBNAIL));
    }
}