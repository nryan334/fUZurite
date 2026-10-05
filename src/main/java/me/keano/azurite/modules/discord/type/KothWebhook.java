package me.keano.azurite.modules.discord.type;

import me.keano.azurite.modules.discord.Discord;
import me.keano.azurite.modules.discord.extra.DiscordWebhook;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Formatter;

import java.awt.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothWebhook extends Discord {

    public KothWebhook(Manager manager, Koth koth) {
        super(manager, Config.DISCORD_KOTH_WEBHOOKURL);
        discordWebhook.setContent(Config.DISCORD_KOTH_CONTENT
                .replace("%koth%", koth.getName())
                .replace("%time%", Formatter.getRemaining(koth.getMinutes(), false))
                .replace("%points%", String.valueOf(koth.getPointsReward()))
        );
        discordWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setAuthor(Config.DISCORD_KOTH_AUTHOR
                        .replace("%koth%", koth.getName())
                        .replace("%time%", Formatter.getRemaining(koth.getMinutes(), false))
                        .replace("%points%", String.valueOf(koth.getPointsReward())), Config.DISCORD_KOTH_AUTHOR_URL, Config.DISCORD_KOTH_AUTHOR_ICON)
                .setDescription(Config.DISCORD_KOTH_DESCRIPTION
                        .replace("%koth%", koth.getName())
                        .replace("%time%", Formatter.getRemaining(koth.getMinutes(), false))
                        .replace("%points%", String.valueOf(koth.getPointsReward())))
                .setColor(Color.decode(Config.DISCORD_KOTH_COLOR))
                .setFooter(Config.DISCORD_KOTH_FOOTER, Config.DISCORD_KOTH_FOOTER_ICON)
                .setThumbnail(Config.DISCORD_KOTH_THUMBNAIL));
    }
}