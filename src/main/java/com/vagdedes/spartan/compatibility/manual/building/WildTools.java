package com.vagdedes.spartan.compatibility.manual.building;

import com.bgsoftware.wildtools.api.events.ToolUseEvent;
import com.vagdedes.spartan.configuration.Compatibility;
import com.vagdedes.spartan.handlers.identifiers.simple.CheckProtection;
import com.vagdedes.spartan.objects.replicates.SpartanPlayer;
import com.vagdedes.spartan.system.SpartanBukkit;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WildTools implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Event(ToolUseEvent e) {
        SpartanPlayer p = SpartanBukkit.getPlayer(e.getPlayer());

        if (p == null) {
            return;
        }
        Compatibility.CompatibilityType compatibilityType = Compatibility.CompatibilityType.WildTools;

        if (compatibilityType.isFunctional()) {
            CheckProtection.evadeCommonFalsePositives(p, compatibilityType,
                    new Enums.HackType[]{
                            Enums.HackType.FastBreak,
                            Enums.HackType.BlockReach,
                            Enums.HackType.GhostHand,
                    }, 40);
        }
    }
}
