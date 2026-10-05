package me.keano.azurite.modules.tablist;

import lombok.Getter;
import lombok.SneakyThrows;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.board.extra.AnimatedString;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.tablist.adapter.AzuriteTablist;
import me.keano.azurite.modules.tablist.extra.TablistSkin;
import me.keano.azurite.modules.tablist.listener.TablistListener;
import me.keano.azurite.modules.tablist.packet.TablistPacket;
import me.keano.azurite.modules.tablist.task.TablistTask;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.NameThreadFactory;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class TablistManager extends Manager {

    private final Map<UUID, Tablist> tablists;
    private final Map<String, TablistSkin> skins;
    private final Triple<Integer, Integer, TablistSkin> defaultSkins;

    private TablistAdapter adapter;
    private AnimatedString title;

    private final ScheduledExecutorService executor;

    public TablistManager(HCF instance) {
        super(instance);

        this.tablists = new ConcurrentHashMap<>();
        this.skins = new ConcurrentHashMap<>();
        this.defaultSkins = new Triple<>();

        this.load();
        new TablistListener(this);

        this.executor = Executors.newScheduledThreadPool(1, new NameThreadFactory("Azurite - TablistThread"));
        this.executor.scheduleAtFixedRate(new TablistTask(this), 0L, 200L, TimeUnit.MILLISECONDS);
    }

    private void load() {
        this.adapter = new AzuriteTablist(this);
        this.title = new AnimatedString(this,
                getTablistConfig().getStringList("TITLE_CONFIG.CHANGES"),
                getTablistConfig().getInt("TITLE_CONFIG.CHANGER_TICKS"));

        for (String key : getTablistConfig().getConfigurationSection("SKINS").getKeys(false)) {
            String path = "SKINS." + key + ".";
            skins.put(key, new TablistSkin(getTablistConfig().getString(path + "VALUE"), getTablistConfig().getString(path + "SIGNATURE")));
        }

        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 4; col++) {
                String skinName = (col == 0 ?
                        Config.TAB_SKIN_CACHE_LEFT.get(row) : col == 1 ?
                        Config.TAB_SKIN_CACHE_MIDDLE.get(row) : col == 2 ?
                        Config.TAB_SKIN_CACHE_RIGHT.get(row) :
                        Config.TAB_SKIN_CACHE_FAR_RIGHT.get(row));

                TablistSkin skin = skins.get(skinName);
                defaultSkins.put(col, row, skin);
            }
        }
    }

    @Override
    public void disable() {
        executor.shutdownNow();
    }

    @Override
    public void reload() {
        skins.clear();
        defaultSkins.clear();
        this.load();
    }

    @SneakyThrows
    public TablistPacket createPacket(Player player) {
        String path = "me.keano.azurite.modules.tablist.packet.type.TablistPacketV" + Utils.getNMSVer();
        return (TablistPacket) Class.forName(path).getConstructor(TablistManager.class, Player.class).newInstance(this, player);
    }
}