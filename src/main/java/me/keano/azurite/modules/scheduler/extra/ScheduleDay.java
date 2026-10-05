package me.keano.azurite.modules.scheduler.extra;

import lombok.Getter;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public enum ScheduleDay {

    SUNDAY("Sunday", 1),
    MONDAY("Monday", 2),
    TUESDAY("Tuesday", 3),
    WEDNESDAY("Wednesday", 4),
    THURSDAY("Thursday", 5),
    FRIDAY("Friday", 6),
    SATURDAY("Saturday", 7),
    NONE("None", 0);

    private final String name;
    private final int number;

    ScheduleDay(String name, int number) {
        this.name = name;
        this.number = number;
    }
}