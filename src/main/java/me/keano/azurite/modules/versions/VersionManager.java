package me.keano.azurite.modules.versions;

import lombok.Getter;
import lombok.SneakyThrows;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Logger;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class VersionManager extends Manager {

    @Getter
    private final Version version;

    public VersionManager(HCF instance) {
        super(instance);
        this.version = setVersion();
    }

    @SneakyThrows
    public Version setVersion() {
        String path = "me.keano.azurite.modules.versions.type.Version" + Utils.getNMSVer();

        try {

            return (Version) Class.forName(path).getConstructor(VersionManager.class).newInstance(this);

        } catch (ClassNotFoundException e) {
            Bukkit.getServer().shutdown();
            Logger.print(
                    Logger.LINE_CONSOLE,
                    "- &dAzurite HCF",
                    "- &cThis version is not supported.",
                    Logger.LINE_CONSOLE
            );
            return null;
        }
    }
}