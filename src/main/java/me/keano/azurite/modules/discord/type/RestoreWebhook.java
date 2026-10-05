package me.keano.azurite.modules.discord.type;

import me.keano.azurite.modules.discord.Discord;
import me.keano.azurite.modules.discord.extra.DiscordWebhook;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.utils.Formatter;
import org.bukkit.entity.Player;

import java.awt.*;

public class RestoreWebhook extends Discord {

    public RestoreWebhook(Manager manager, Player player, Player target, StoredInventory storedInventory) {
        super(manager, Config.DISCORD_RESTORE_WEBHOOKURL);
        discordWebhook.setContent(Config.DISCORD_RESTORE_CONTENT
                .replace("%player%", player.getName())
                .replace("%target%", target.getName())
        );
        discordWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setAuthor(Config.DISCORD_RESTORE_AUTHOR
                        .replace("%player%", player.getName())
                        .replace("%target%", target.getName())
                        .replace("%date%", Formatter.formatDate(storedInventory.getDate())), Config.DISCORD_RESTORE_AUTHOR_URL, Config.DISCORD_RESTORE_AUTHOR_ICON)
                .setDescription(Config.DISCORD_RESTORE_DESCRIPTION
                        .replace("%player%", player.getName())
                        .replace("%target%", target.getName())
                        .replace("%date%", Formatter.formatDate(storedInventory.getDate())))
                .setColor(Color.decode(Config.DISCORD_RESTORE_COLOR))
                .setFooter(Config.DISCORD_RESTORE_FOOTER, Config.DISCORD_RESTORE_FOOTER_ICON)
                .setThumbnail(Config.DISCORD_RESTORE_THUMBNAIL));
    }
}
