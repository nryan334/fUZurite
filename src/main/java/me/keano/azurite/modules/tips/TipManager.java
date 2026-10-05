package me.keano.azurite.modules.tips;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.tips.task.TipTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class TipManager extends Manager {

    private final List<List<String>> tips;
    private final String sound;
    private TipTask task;
    private int counter;

    public TipManager(HCF instance) {
        super(instance);
        this.tips = new ArrayList<>();
        this.sound = getSchedulesConfig().getString("TIPS.SOUND");
        this.counter = 0;
        this.load();
    }

    @Override
    public void reload() {
        tips.clear();
        if (task != null) task.cancel();
        this.counter = 0;
        this.load();
    }

    private void load() {
        if (getSchedulesConfig().getBoolean("TIPS.ENABLED")) {
            for (String key : getSchedulesConfig().getConfigurationSection("TIPS.TIP_TYPES").getKeys(false)) {
                tips.add(getSchedulesConfig().getStringList("TIPS.TIP_TYPES." + key));
            }

            task = new TipTask(this);
        }
    }

    public List<String> getNextTip() {
        if (tips.isEmpty()) return null;
        if (counter > tips.size() - 1) counter = 0;
        List<String> tip = tips.get(counter);
        counter++;
        return tip;
    }
}