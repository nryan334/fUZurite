package me.keano.azurite.modules.timers.listeners.servertimers;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.timers.Timer;
import me.keano.azurite.modules.timers.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class RebootTimer extends Timer {

    private final Map<Integer, List<String>> messages;
    private RebootTask task;

    public RebootTimer(TimerManager manager) {
        super(
                manager,
                "Reboot",
                "PLAYER_TIMERS.REBOOT",
                ""
        );
        this.messages = new HashMap<>();
        this.task = null;
        this.load();
    }

    @Override
    public void reload() {
        messages.clear();
        this.load();
    }

    private void load() {
        for (String key : getConfig().getConfigurationSection("REBOOT_TIMER.MESSAGES_SECOND").getKeys(false)) {
            int second = Integer.parseInt(key.replace("SECOND_", ""));
            List<String> message = getConfig().getStringList("REBOOT_TIMER.MESSAGES_SECOND." + key);
            messages.put(second, message);
        }
    }

    public boolean isActive() {
        return task != null && task.getRemaining() > 0L;
    }

    public void start(long time) {
        this.task = new RebootTask(getManager(), time);
    }

    public void extend(long time) {
        if (task != null) {
            task.setTime(task.getTime() + time);
            task.setCounter((int) (time / 1000L) - 1);
        }
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Getter
    @Setter
    public class RebootTask extends BukkitRunnable {

        private long time;
        private int counter;

        public RebootTask(TimerManager manager, long time) {
            this.time = System.currentTimeMillis() + time;
            this.counter = (int) (time / 1000L);
            this.runTaskTimer(manager.getInstance(), 0L, 20L);
        }

        @Override
        public void run() {
            counter--;
            List<String> message = messages.get(counter);

            if (message != null) {
                for (String s : message) {
                    Bukkit.broadcastMessage(s);
                }
            }

            if (getRemaining() <= 0L) {
                task = null;
                cancel();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("REBOOT_TIMER.COMMAND"));
            }
        }

        public long getRemaining() {
            return time - System.currentTimeMillis();
        }
    }
}