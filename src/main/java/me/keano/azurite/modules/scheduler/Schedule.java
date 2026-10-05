package me.keano.azurite.modules.scheduler;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.scheduler.extra.ScheduleDay;
import org.bukkit.Bukkit;

import java.util.Calendar;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class Schedule extends Module<ScheduleManager> {

    private final String name;
    private final String time;
    private final ScheduleDay day;
    private final List<String> commands;
    private final int minute;
    private final int hour;
    private final int dayDate;
    private final long dayTime;

    public Schedule(ScheduleManager manager, String name, String time, ScheduleDay day, List<String> commands) {
        super(manager);
        this.name = name;
        this.time = time;
        this.commands = commands;
        this.day = day;
        this.dayDate = 0;
        this.hour = (time.contains(":") ? Integer.parseInt(time.split(":")[0]) : 0);
        this.minute = (time.contains(":") ? Integer.parseInt(time.split(":")[1]) : 0);
        this.dayTime = 0L;
    }

    public Schedule(ScheduleManager manager, String name, String time, List<String> commands) {
        super(manager);
        this.name = name;
        this.time = time;
        this.commands = commands;
        this.hour = 0;
        this.dayDate = 0;
        this.day = ScheduleDay.NONE;
        this.minute = Integer.parseInt(time);
        this.dayTime = 0L;
    }

    public Schedule(ScheduleManager manager, String name, String time, int dayDate, List<String> commands) {
        super(manager);
        this.name = name;
        this.time = time;
        this.commands = commands;
        this.day = ScheduleDay.NONE;
        this.dayDate = dayDate;
        this.hour = (time.contains(":") ? Integer.parseInt(time.split(":")[0]) : 0);
        this.minute = (time.contains(":") ? Integer.parseInt(time.split(":")[1]) : 0);
        this.dayTime = calcTime();
    }

    private long calcTime() {
        Calendar calendar = Calendar.getInstance(getManager().getTimeZone());
        calendar.set(Calendar.DAY_OF_MONTH, dayDate);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public void execute() {
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}