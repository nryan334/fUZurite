package me.keano.azurite.modules.nametags;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.nametags.packet.NametagPacket;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class Nametag extends Module<NametagManager> {

    private final Player player;
    private final NametagPacket packet;
    private final Set<String> trackedPlayers;
    private int protocolVersion;

    public Nametag(NametagManager manager, Player player) {
        super(manager);
        this.player = player;
        this.packet = manager.createPacket(player);
        this.trackedPlayers = new CopyOnWriteArraySet<>();
        this.protocolVersion = Utils.getProtocolVersion(player);
        Tasks.executeLater(getManager(), 5L, () -> protocolVersion = Utils.getProtocolVersion(player));
    }

    public void delete() {
        packet.delete();
    }
}