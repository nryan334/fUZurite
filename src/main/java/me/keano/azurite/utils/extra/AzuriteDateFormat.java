package me.keano.azurite.utils.extra;

import lombok.Getter;
import me.keano.azurite.HCF;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class AzuriteDateFormat {

    private final SimpleDateFormat dateFormat;
    private final Map<Long, String> cache;

    public AzuriteDateFormat(HCF instance, String pattern) {
        this.dateFormat = new SimpleDateFormat(pattern);
        this.cache = new ConcurrentHashMap<>();
        instance.getDateFormats().add(this);
    }

    public String format(Date date) {
        String cached = cache.get(date.getTime());

        if (cached != null) {
            return cached;
        }

        String formatted = dateFormat.format(date);
        cache.put(date.getTime(), formatted);
        return formatted;
    }

    public void clean() {
        cache.clear();
    }
}