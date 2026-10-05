package me.keano.azurite.modules.discord;

import me.keano.azurite.modules.discord.extra.DiscordWebhook;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.Tasks;

import java.io.IOException;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class Discord extends Module<Manager> {

    protected final DiscordWebhook discordWebhook;

    public Discord(Manager manager, String webhookURL) {
        super(manager);
        this.discordWebhook = new DiscordWebhook(webhookURL);
    }

    public void execute() {
        try {

            discordWebhook.execute();

        } catch (IOException e) {
            throw new IllegalArgumentException("[Azurite] Your discord webhook is incorrect!");
        }
    }

    public void executeAsync() {
        Tasks.executeAsync(getManager(), () -> {
            try {

                discordWebhook.execute();

            } catch (IOException e) {
                throw new IllegalArgumentException("[Azurite] Your discord webhook is incorrect!");
            }
        });
    }
}