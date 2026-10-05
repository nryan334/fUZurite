package me.keano.azurite.modules.hooks.ranks;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.hooks.ranks.type.*;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RankHook extends Manager implements Rank {

    private Rank rank;

    public RankHook(HCF instance) {
        super(instance);
        this.load();
    }

    private void load() {
        if (Utils.verifyPlugin("AquaCore", getInstance())) {
            rank = new AquaCoreRank();

        } else if (Utils.verifyPlugin("Azurite-Core", getInstance())) {
            rank = new AzuriteHook(getInstance());

        } else if (Utils.verifyPlugin("Zoot", getInstance())) {
            rank = new ZootRank();

        } else if (Utils.verifyPlugin("Zoom", getInstance())) {
            rank = new ZoomRank();

        } else if (Utils.verifyPlugin("Mizu", getInstance())) {
            rank = new MizuRank();

        } else if (Utils.verifyPlugin("Atom", getInstance())) {
            rank = new AtomRank();

        } else if (Utils.verifyPlugin("Basic", getInstance())) {
            rank = new CoreRank();

        } else if (Utils.verifyPlugin("ZPermissions", getInstance())) {
            rank = new ZPermissionRank();

        } else if (Utils.verifyPlugin("HestiaCore", getInstance())) {
            rank = new HestiaRank();

        } else if (Utils.verifyPlugin("Phoenix", getInstance()) || Utils.verifyPlugin("pxLoader", getInstance())) {
            rank = new PhoenixRank();

        } else if (Utils.verifyPlugin("Alchemist", getInstance())) {
            rank = new AlchemistRank();

        } else if (Utils.verifyPlugin("Holiday", getInstance())) {
            rank = new HolidayRank();

        } else if (Utils.verifyPlugin("Akuma", getInstance())) {
            rank = new AkumaRank();

        } else if (Utils.verifyPlugin("Helium", getInstance())) {
            rank = new HeliumRank();

        } else if (Utils.verifyPlugin("Vault", getInstance())) {
            rank = new VaultRank();

        } else {
            rank = new NoneRank();
        }
    }

    @Override
    public String getRankName(Player player) {
        return rank.getRankName(player);
    }

    @Override
    public String getRankPrefix(Player player) {
        return rank.getRankPrefix(player);
    }

    @Override
    public String getRankSuffix(Player player) {
        return rank.getRankSuffix(player);
    }

    @Override
    public String getRankColor(Player player) {
        return rank.getRankColor(player);
    }
}