package me.keano.azurite.modules.scheduler.extra;

import lombok.Getter;
import me.keano.azurite.modules.scheduler.Schedule;
import me.keano.azurite.utils.Formatter;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class NextSchedule {

    private final List<Schedule> schedules;
    private final String name;
    private final String timeFormatted;
    private final long time;

    public NextSchedule(List<Schedule> schedules, long time) {
        this.schedules = schedules;
        this.name = (schedules != null ? String.join("§7, ", schedules
                .stream()
                .map(Schedule::getName)
                .toArray(String[]::new)) : "");
        this.timeFormatted = (time <= 0L ? "" : Formatter.formatSchedule(time));
        this.time = time;
    }

    public NextSchedule(String name, String timeFormatted) {
        this.schedules = Collections.emptyList();
        this.name = name;
        this.timeFormatted = timeFormatted;
        this.time = 0L;
    }
}