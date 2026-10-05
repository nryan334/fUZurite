package me.keano.azurite.modules.pvpclass.type.ghost;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class GhostClass extends PvPClass {

    private final Map<Integer, GhostMode> modes;
    private final Map<UUID, GhostData> data;
    private final Map<Integer, UUID> invisible;

    private CustomCooldown quartzCooldown;
    private Material modeItem;
    private int maxSeconds;

    public GhostClass(PvPClassManager manager) {
        super(manager, "Ghost");

        this.modes = new HashMap<>();
        this.data = new HashMap<>();
        this.invisible = new ConcurrentHashMap<>();

        this.load();
    }

    @Override
    public void load() {
        this.quartzCooldown = new CustomCooldown(this, getScoreboardConfig().getString("GHOST_CLASS.QUARTZ"));
        this.modeItem = ItemUtils.getMat(getClassesConfig().getString("GHOST_CLASS.MODE_ITEM"));
        this.maxSeconds = getClassesConfig().getInt("GHOST_CLASS.TOTAL_TIME");

        for (String key : getClassesConfig().getConfigurationSection("GHOST_CLASS.PER_SECOND_MODE").getKeys(false)) {
            modes.put(Integer.parseInt(key), new GhostMode(getManager(), key));
        }
    }

    @Override
    public void handleEquip(Player player) {
        data.put(player.getUniqueId(), new GhostData(getManager()));
    }

    @Override
    public void handleUnequip(Player player) {
        if (invisible.remove(player.getEntityId()) != null) {
            Tasks.executeLater(getManager(), 5L, () -> showArmor(player));
        }

        GhostData ghostData = data.remove(player.getUniqueId());

        if (ghostData != null) {
            for (BukkitTask task : ghostData.getTasks()) task.cancel();
        }
    }

    @Override
    public void reload() {
        modes.clear();
        this.load();
        this.loadEffectsArmor();
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();
        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;
        if (!players.contains(damaged.getUniqueId())) return;

        GhostData ghostData = data.get(damaged.getUniqueId());

        if (getClassesConfig().getBoolean("GHOST_CLASS.SHOW_ARMOR_ON_HIT.ENABLED") &&
                ghostData.getMode().equals(getClassesConfig().getString("GHOST_CLASS.SHOW_ARMOR_ON_HIT.MODE")) &&
                invisible.remove(damaged.getEntityId()) != null) {

            Tasks.executeLater(getManager(), 5L, () -> showArmor(damaged));
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) { // This event gets called before any of the packets are sent
        Player player = e.getPlayer();

        if (invisible.values().remove(player.getUniqueId())) { // Remove old
            invisible.put(player.getEntityId(), player.getUniqueId()); // New
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null) return;
        if (!players.contains(player.getUniqueId())) return;
        if (hand.hasItemMeta() && hand.getItemMeta().hasLore()) return;

        if (hand.getType() == modeItem) {
            if (quartzCooldown.hasCooldown(player)) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.GHOST_CLASS.QUARTZ_COOLDOWN")
                        .replace("%seconds%", quartzCooldown.getRemaining(player))
                );
                return;
            }

            GhostData ghostData = data.get(player.getUniqueId());
            quartzCooldown.applyCooldown(player, getClassesConfig().getInt("GHOST_CLASS.QUARTZ_COOLDOWN"));
            getManager().takeItemInHand(player, 1);

            ghostData.getTasks().add(new BukkitRunnable() {
                @Override
                public void run() {
                    if (!players.contains(player.getUniqueId())) {
                        cancel();
                        return;
                    }

                    if (ghostData.getCounter() > maxSeconds) {
                        ghostData.setCounter(0);
                        cancel();
                        return;
                    }

                    GhostMode mode = modes.get(ghostData.getCounter());
                    ghostData.setCounter(ghostData.getCounter() + 1);

                    if (mode != null) {
                        mode.applyMode(player);
                    }
                }
            }.runTaskTimer(getInstance(), 0L, 20L));
        }
    }

    public void hideArmor(Player player) {
        getInstance().getVersionManager().getVersion().hideArmor(player);
    }

    public void showArmor(Player player) {
        getInstance().getVersionManager().getVersion().showArmor(player);
    }
}