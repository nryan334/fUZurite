package me.keano.azurite.modules.deathban.listener;

import me.keano.azurite.modules.deathban.Deathban;
import me.keano.azurite.modules.deathban.DeathbanManager;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.timers.listeners.playertimers.PvPTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class DeathbanListener extends Module<DeathbanManager> {

    private static final Method RESPAWN_FLAGS = (Utils.isModernVer() ?
            ReflectionUtils.accessMethod(PlayerRespawnEvent.class, "getRespawnFlags") : null
    );

    public DeathbanListener(DeathbanManager manager) {
        super(manager);
    }

    @EventHandler // apply deathban on death
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        // don't override the old one
        if (!getManager().isDeathbanned(player) && !getInstance().isKits()) {
            getManager().applyDeathban(player);
            return;
        }

        // Handle the lowering of the deathban for a kill
        Player killer = player.getKiller();

        if (killer == null) return;
        if (killer == player) return; // if the same from fall damage or something.
        if (!getManager().isDeathbanned(killer)) return; // we don't want non deathbanned players.

        Deathban deathban = getManager().getDeathban(killer);
        int time = getConfig().getInt("DEATHBANS.KILL_TIME");

        deathban.setTime(System.currentTimeMillis() + deathban.getTime() - time * (60 * 1000L));
        killer.sendMessage(getLanguageConfig().getString("DEATHBAN_LISTENER.LOWERED_KILL")
                .replace("%mins%", String.valueOf(time))
        );
    }

    @EventHandler // only teleport onRespawn, not onDeath
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        boolean bypass = player.hasPermission("azurite.deathban.bypass");

        if (Utils.isModernVer()) {
            List<String> flags = ((Set<?>) ReflectionUtils.fetch(RESPAWN_FLAGS, e)).stream().map(Object::toString).collect(Collectors.toList());

            if (flags.contains("END_PORTAL")) {
                if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                    e.setRespawnLocation(getInstance().getWaypointManager().getEndWorldExit().clone().add(0.5, 0, 0.5));
                    player.sendMessage(getLanguageConfig().getString("END_LISTENER.ENTERED"));
                }

                return;
            }
        }

        // if they respawn while deathbanned teleport them back to arena
        if (getManager().isDeathbanned(player) && !bypass && !getInstance().isKits()) {
            e.setRespawnLocation(getManager().getArenaSpawn());
            Tasks.executeLater(getManager(), 20L, () -> getManager().sendSignInfo(player));
            return;
        }

        // Add the pvp timer after deaths.
        PvPTimer pvPTimer = getInstance().getTimerManager().getPvpTimer();
        if (pvPTimer.getSeconds() != 0) Tasks.executeLater(getManager(), 20L, () -> pvPTimer.applyTimer(player));

        // we always respawn here.
        e.setRespawnLocation(getInstance().getWaypointManager().getWorldSpawn());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        Deathban deathban = user.getDeathban();

        if (deathban == null) return;

        // the deathban still exists when expired because they weren't online to get teleported back.
        if (deathban.isExpired()) {
            getManager().removeDeathban(player); // this for players who left but rejoined when it expired.
            return;
        }

        player.teleport(getManager().getArenaSpawn().clone().add(0.5, 0, 0.5)); // teleport them back to the arena
        Tasks.executeLater(getManager(), 20L, () -> getManager().sendSignInfo(player));
    }

    @EventHandler
    public void onInteractSign(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getClickedBlock() == null) return;
        if (!e.getClickedBlock().getType().name().contains("SIGN")) return;
        if (!getManager().isDeathbanned(player)) return;

        getManager().sendSignInfo(player); // resend sign info when interact
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (block == null) return;
        if (!(block.getState() instanceof Sign)) return;
        if (!getManager().isDeathbanned(player)) return;

        Sign sign = (Sign) block.getState();
        List<String> livesLines = getConfig().getStringList("DEATHBANS.SIGNS_CONFIG.LIVES_SIGN");

        if (checkSign(sign.getLines(), livesLines.toArray(new String[0]))) {
            User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

            if (user.getLives() <= 0) {
                player.sendMessage(getLanguageConfig().getString("DEATHBAN_LISTENER.NO_LIVES"));
                return;
            }

            getManager().removeDeathban(player);
            user.setLives(user.getLives() - 1); // decrement lives
            user.save();
        }
    }

    @EventHandler
    public void onSign(SignChangeEvent e) {
        Player player = e.getPlayer();

        if (!player.hasPermission("azurite.customsigns")) return;

        if (e.getLine(0).contains("[lives]")) {
            List<String> livesLines = getConfig().getStringList("DEATHBANS.SIGNS_CONFIG.LIVES_SIGN");

            for (int i = 0; i < livesLines.size(); i++) {
                e.setLine(i, livesLines.get(i));
            }
            return;
        }

        if (e.getLine(0).contains("[deathlocsign]")) {
            getManager().setDeathLocationSign(e.getBlock().getLocation());
            getManager().save();
            player.sendMessage(getLanguageConfig().getString("DEATHBAN_LISTENER.CREATED_DEATHLOCSIGN"));
            return;
        }

        if (e.getLine(0).contains("[killedbysign]")) {
            getManager().setKilledBySign(e.getBlock().getLocation());
            getManager().save();
            player.sendMessage(getLanguageConfig().getString("DEATHBAN_LISTENER.CREATED_KILLEDBYSIGN"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        if (player.isOp()) return;

        if (getManager().isDeathbanned(player)) {
            player.sendMessage(getLanguageConfig().getString("DEATHBAN_LISTENER.CANNOT_USE_COMMAND"));
            e.setCancelled(true);
        }
    }

    private boolean checkSign(String[] array, String[] array2) {
        for (int i = 0; i < array.length; i++) {
            if (!ChatColor.stripColor(array[i]).equals(ChatColor.stripColor(array2[i]))) return false;
        }
        return true;
    }
}