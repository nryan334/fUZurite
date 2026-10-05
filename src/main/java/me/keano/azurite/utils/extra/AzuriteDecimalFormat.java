package me.keano.azurite.utils.extra;

import lombok.Getter;
import me.keano.azurite.HCF;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class AzuriteDecimalFormat {

    private final DecimalFormat decimalFormat;
    private final Map<Double, String> cache;

    public AzuriteDecimalFormat(HCF instance, String pattern) {
        this.decimalFormat = new DecimalFormat(pattern);
        this.cache = new ConcurrentHashMap<>();
        instance.getDecimalFormats().add(this);
    }

    public String format(double d) {
        String cached = cache.get(d);

        if (cached != null) {
            return cached;
        }

        String formatted = decimalFormat.format(d);
        cache.put(d, formatted);
        return formatted;
    }

    public void clean() {
        cache.clear();
    }
}