package me.keano.azurite.modules.scheduler;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.scheduler.extra.NextSchedule;
import me.keano.azurite.modules.scheduler.extra.ScheduleDay;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class ScheduleManager extends Manager {

    private final Triple<Integer, Long, List<Schedule>> normal; // Day, Time -> Schedules
    private final Map<Long, List<Schedule>> daily; // Time -> Schedules
    private final Map<Integer, List<Schedule>> hourly; // Hour -> Schedules
    private final Map<Long, List<Schedule>> kothSchedules;

    // These can be cached
    private Calendar calendar;
    private TimeZone timeZone;
    private NextSchedule nextSchedule;
    private int oldMin;

    public ScheduleManager(HCF instance) {
        super(instance);

        this.normal = new Triple<>(new LinkedHashMap<>());
        this.daily = new LinkedHashMap<>();
        this.hourly = new LinkedHashMap<>();
        this.kothSchedules = new LinkedHashMap<>();
        this.timeZone = TimeZone.getTimeZone(Config.SCHEDULE_TIMEZONE);
        this.calendar = Calendar.getInstance(timeZone);

        this.load();
        Tasks.executeScheduled(this, 20, this::tick);
    }

    @Override
    public void reload() {
        normal.clear();
        daily.clear();
        hourly.clear();
        kothSchedules.clear();
        this.timeZone = TimeZone.getTimeZone(Config.SCHEDULE_TIMEZONE);
        this.calendar = Calendar.getInstance(timeZone);
        this.load();
    }

    public NextSchedule getNextSchedule() {
        if (nextSchedule == null || nextSchedule.getSchedules() == null || nextSchedule.getSchedules().isEmpty()) {
            return new NextSchedule(Config.SCHEDULE_NONE_NAME, Config.SCHEDULE_NONE_TIME);
        }

        return nextSchedule;
    }

    // TODO: clean up schedules completely this shit is filthy
    private void load() {
        for (String s : getSchedulesConfig().getStringList("SCHEDULES.HOURLY")) {
            String[] split = s.split(", ");
            Schedule schedule = new Schedule(this, split[0], split[1], Arrays.asList(split[2].split(";")));
            hourly.putIfAbsent(schedule.getMinute(), new ArrayList<>());
            hourly.get(schedule.getMinute()).add(schedule);
        }

        for (String s : getSchedulesConfig().getStringList("SCHEDULES.DAILY")) {
            String[] split = s.split(", ");
            Schedule schedule = new Schedule(this, split[0], split[1], ScheduleDay.NONE, Arrays.asList(split[2].split(";")));
            long time = toLong(schedule.getHour(), schedule.getMinute());
            daily.putIfAbsent(time, new ArrayList<>());
            daily.get(time).add(schedule);
        }

        for (String s : getSchedulesConfig().getStringList("SCHEDULES.NORMAL")) {
            String[] split = s.split(", ");
            Schedule schedule = new Schedule(this, split[0], split[1], ScheduleDay.valueOf(split[2]), Arrays.asList(split[3].split(";")));
            long time = toLong(schedule.getHour(), schedule.getMinute());
            int day = schedule.getDay().ordinal();
            if (normal.get(day, time) == null) normal.put(day, time, new ArrayList<>());
            normal.get(day, time).add(schedule);
        }

        for (String s : getSchedulesConfig().getStringList("KOTH_SCHEDULES")) {
            String[] split = s.split(", ");
            boolean daily = split[2].equalsIgnoreCase("DAILY");

            if (daily) {
                for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                    Schedule schedule = new Schedule(this, split[0], split[1], i, Arrays.asList(split[3].split(";")));
                    kothSchedules.putIfAbsent(schedule.getDayTime(), new ArrayList<>());
                    kothSchedules.get(schedule.getDayTime()).add(schedule);
                }
                continue;
            }

            try {

                ScheduleDay scheduleDay = ScheduleDay.valueOf(split[2].toUpperCase());

                for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                    if (dayOfWeek == scheduleDay.getNumber()) {
                        Schedule schedule = new Schedule(this, split[0], split[1], i, Arrays.asList(split[3].split(";")));
                        kothSchedules.putIfAbsent(schedule.getDayTime(), new ArrayList<>());
                        kothSchedules.get(schedule.getDayTime()).add(schedule);
                    }
                }

            } catch (IllegalArgumentException e) {
                Schedule schedule = new Schedule(this, split[0], split[1], Integer.parseInt(split[2]), Arrays.asList(split[3].split(";")));
                kothSchedules.putIfAbsent(schedule.getDayTime(), new ArrayList<>());
                kothSchedules.get(schedule.getDayTime()).add(schedule);
            }
        }
    }

    private void tick() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        int min = calendar.get(Calendar.MINUTE);
        this.nextSchedule = checkNextSchedule(calendar.getTimeInMillis());

        if (oldMin == min) return;

        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // These are needed otherwise the time might be slight off for koth schedules
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        this.oldMin = min;
        long time = toLong(hour, min);

        List<Schedule> normalSchedule = normal.get(day, time);
        List<Schedule> dailySchedule = daily.get(time);
        List<Schedule> hourlySchedule = hourly.get(min);
        List<Schedule> kothSchedule = kothSchedules.get(calendar.getTimeInMillis());

        if (normalSchedule != null) normalSchedule.forEach(Schedule::execute);
        if (dailySchedule != null) dailySchedule.forEach(Schedule::execute);
        if (hourlySchedule != null) hourlySchedule.forEach(Schedule::execute);

        if (kothSchedule != null) {
            int maxPlayers = getSchedulesConfig().getInt("SCHEDULE_CONFIG.KOTH_SCHEDULES_PLAYERS_REQUIRED");

            if (maxPlayers > 0 && Bukkit.getOnlinePlayers().size() < maxPlayers) {
                Bukkit.broadcastMessage(Config.SCHEDULE_NO_PLAYERS_START
                        .replace("%amount%", String.valueOf(maxPlayers))
                );
                return;
            }

            kothSchedule.forEach(Schedule::execute);
        }
    }

    private NextSchedule checkNextSchedule(long currentTime) {
        long closest = 0L;

        for (Map.Entry<Long, List<Schedule>> entry : kothSchedules.entrySet()) {
            long time = entry.getKey();

            // Don't check if the time is already passed
            if (time < currentTime) continue;

            // Init first
            if (closest == 0L) {
                closest = time;
                continue;
            }

            // if time is less than the current closest we know its next
            if ((time - currentTime) < (closest - currentTime)) {
                closest = time;
            }
        }

        return new NextSchedule(kothSchedules.get(closest), closest - currentTime);
    }

    private long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - Integer.MIN_VALUE;
    }
}