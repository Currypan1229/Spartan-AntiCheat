package com.vagdedes.spartan.objects.profiling;

import com.vagdedes.spartan.compatibility.necessary.bedrock.BedrockCompatibility;
import com.vagdedes.spartan.handlers.stability.ResearchEngine;
import com.vagdedes.spartan.objects.replicates.SpartanPlayer;
import com.vagdedes.spartan.system.SpartanBukkit;
import com.vagdedes.spartan.utils.server.InventoryUtils;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerProfile {

    private UUID uuid;
    private final String name;
    private final ViolationHistory[] violationHistory;
    private final MiningHistory[] miningHistory;
    private final PunishmentHistory punishmentHistory;
    private final PlayerEvidence evidence;
    private boolean bedrockPlayer, bedrockPlayerCheck;
    private ItemStack skull;
    private OfflinePlayer offlinePlayer;
    private final PlayerCombat playerCombat;

    // Separator

    public PlayerProfile(String name) {
        Enums.HackType[] hackTypes = Enums.HackType.values();

        // Separator
        this.uuid = null;
        this.name = name;
        this.punishmentHistory = new PunishmentHistory(this);
        this.playerCombat = new PlayerCombat(this);
        this.evidence = new PlayerEvidence(this);
        this.skull = null;
        this.offlinePlayer = null;

        // Separator
        SpartanPlayer player = this.getSpartanPlayer();

        if (player != null) {
            this.bedrockPlayer = player.isBedrockPlayer();
            this.bedrockPlayerCheck = true;
        } else {
            this.bedrockPlayer = BedrockCompatibility.isPlayer(name);
            this.bedrockPlayerCheck = bedrockPlayer;
        }

        // Separator
        this.violationHistory = new ViolationHistory[hackTypes.length];
        this.miningHistory = new MiningHistory[Enums.MiningOre.values().length];

        for (Enums.HackType hackType : hackTypes) {
            this.violationHistory[hackType.ordinal()] = new ViolationHistory();
        }
        for (Enums.MiningOre ore : Enums.MiningOre.values()) {
            this.miningHistory[ore.ordinal()] = new MiningHistory(ore, 0);
        }
    }

    public PlayerProfile(SpartanPlayer player) {
        Enums.HackType[] hackTypes = Enums.HackType.values();
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.offlinePlayer = player.getPlayer(); // Attention
        this.punishmentHistory = new PunishmentHistory(this);
        this.playerCombat = new PlayerCombat(this);
        this.evidence = new PlayerEvidence(this);
        this.skull = null;
        this.offlinePlayer = null;
        this.bedrockPlayer = player.isBedrockPlayer(); // Attention
        this.bedrockPlayerCheck = true;

        this.violationHistory = new ViolationHistory[hackTypes.length];
        this.miningHistory = new MiningHistory[Enums.MiningOre.values().length];

        for (Enums.HackType hackType : hackTypes) {
            this.violationHistory[hackType.ordinal()] = new ViolationHistory();
        }
        for (Enums.MiningOre ore : Enums.MiningOre.values()) {
            this.miningHistory[ore.ordinal()] = new MiningHistory(ore, 0);
        }
    }

    // Separator

    public UUID getUniqueId() {
        if (uuid == null) {
            SpartanPlayer player = getSpartanPlayer();

            if (player != null) {
                this.uuid = player.getUniqueId();

                if (!bedrockPlayerCheck && BedrockCompatibility.isPlayer(uuid, name)) {
                    this.bedrockPlayerCheck = true;
                    this.bedrockPlayer = true;
                }
            } else {
                OfflinePlayer offlinePlayer = getOfflinePlayer();

                if (offlinePlayer != null) {
                    this.uuid = offlinePlayer.getUniqueId();

                    if (!bedrockPlayerCheck && BedrockCompatibility.isPlayer(uuid, name)) {
                        this.bedrockPlayerCheck = true;
                        this.bedrockPlayer = true;
                    }
                }
            }
        }
        return uuid;
    }

    public String getName() {
        return name;
    }

    public ItemStack getSkull() {
        if (skull == null) {
            OfflinePlayer player = getOfflinePlayer();
            this.skull = player == null ? InventoryUtils.getHead() : InventoryUtils.getSkull(player);
        }
        return skull;
    }

    public ResearchEngine.DataType getDataType() {
        return isBedrockPlayer() ? ResearchEngine.DataType.Bedrock : ResearchEngine.DataType.Java;
    }

    public boolean isBedrockPlayer() {
        if (bedrockPlayer) {
            return true;
        }
        if (!bedrockPlayerCheck) {
            SpartanPlayer player = getSpartanPlayer();

            if (player != null) {
                bedrockPlayerCheck = true;

                if (player.isBedrockPlayer()) {
                    return bedrockPlayer = true;
                }
            }
        }
        return false;
    }

    public OfflinePlayer getOfflinePlayer() {
        if (offlinePlayer == null && name != null) {
            if (this.uuid == null) {
                this.offlinePlayer = Bukkit.getOfflinePlayer(name);
                this.uuid = offlinePlayer.getUniqueId();
            } else {
                this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            }
            if (!bedrockPlayerCheck && !bedrockPlayer
                    && BedrockCompatibility.isPlayer(uuid, name)) {
                this.bedrockPlayerCheck = true;
                this.bedrockPlayer = true;
            }
        }
        return offlinePlayer;
    }

    public SpartanPlayer getSpartanPlayer() {
        return uuid != null ? SpartanBukkit.getPlayer(uuid) :
                name != null ? SpartanBukkit.getPlayer(name) :
                        null;
    }

    public boolean isOnline() {
        return getSpartanPlayer() != null;
    }

    // Separator

    public ViolationHistory[] getViolationHistory() {
        return violationHistory;
    }

    public ViolationHistory getViolationHistory(Enums.HackType hackType) {
        return violationHistory[hackType.ordinal()];
    }

    // Separator

    public PlayerEvidence getEvidence() {
        return evidence;
    }

    // Separator

    public MiningHistory[] getMiningHistory() {
        return miningHistory;
    }

    public MiningHistory getOverallMiningHistory() {
        int mines = 0, days = 0;

        for (MiningHistory miningHistory : getMiningHistory()) {
            mines += miningHistory.getMines();
            days = Math.max(miningHistory.getDays(), days);
        }
        return new MiningHistory(null, mines);
    }

    public MiningHistory getMiningHistory(Enums.MiningOre ore) {
        return miningHistory[ore.ordinal()];
    }

    // Separator

    public boolean isLegitimate() {
        return getEvidence().has(PlayerEvidence.EvidenceType.Legitimate);
    }

    public boolean isHacker() {
        return getEvidence().has(PlayerEvidence.EvidenceType.Hacker);
    }

    public boolean isSuspected() {
        return getEvidence().has(PlayerEvidence.EvidenceType.Suspected);
    }

    public boolean isSuspected(Enums.HackType[] hackTypes) {
        synchronized (evidence.live) {
            if (evidence.has(PlayerEvidence.EvidenceType.Suspected)) {
                for (Enums.HackType hackType : hackTypes) {
                    if (evidence.live.containsKey(hackType)
                            || evidence.historical.containsKey(hackType)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean isSuspected(Enums.HackType hackType) {
        synchronized (evidence.live) {
            return evidence.has(PlayerEvidence.EvidenceType.Suspected)
                    && (evidence.live.containsKey(hackType)
                    || evidence.historical.containsKey(hackType));
        }
    }

    public boolean isSuspectedOrHacker() {
        return isSuspected() || isHacker();
    }

    public boolean isSuspectedOrHacker(Enums.HackType[] hackTypes) {
        return isHacker() || isSuspected(hackTypes);
    }

    public boolean isSuspectedOrHacker(Enums.HackType hackType) {
        return isHacker() || isSuspected(hackType);
    }

    // Separator

    public int getUsefulLogs() {
        int sum = 0;

        for (ViolationHistory violationHistory : getViolationHistory()) {
            sum += (violationHistory.getCount()
                    + getOverallMiningHistory().getMines()
                    + getPunishmentHistory().getOverall());
        }
        return sum;
    }

    public PunishmentHistory getPunishmentHistory() {
        return punishmentHistory;
    }

    public PlayerCombat getCombat() {
        return playerCombat;
    }
}
