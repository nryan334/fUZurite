package me.keano.azurite.modules.hooks.tags;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.hooks.tags.type.*;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TagHook extends Manager implements Tag {

    private Tag tag;

    public TagHook(HCF instance) {
        super(instance);
        this.load();
    }

    private void load() {
        if (Utils.verifyPlugin("AquaCore", getInstance())) {
            tag = new AquaCoreTag();

        } else if (Utils.verifyPlugin("Azurite-Core", getInstance())) {
            tag = new AzuriteTag(getInstance());

        } else if (Utils.verifyPlugin("Mizu", getInstance())) {
            tag = new MizuTag();

        } else if (Utils.verifyPlugin("Basic", getInstance())) {
            tag = new CoreTag();

        } else if (Utils.verifyPlugin("DeluxeTags", getInstance())) {
            tag = new DeluxeTag();

        } else if (Utils.verifyPlugin("HestiaCore", getInstance())) {
            tag = new HestiaTag();

        } else if (Utils.verifyPlugin("Phoenix", getInstance()) || Utils.verifyPlugin("pxLoader", getInstance())) {
            tag = new PhoenixTag();

        } else {
            tag = new NoneTag();
        }
    }

    @Override
    public String getTag(Player player) {
        return tag.getTag(player);
    }
}