package com.vagdedes.spartan.listeners.bukkit;

import com.vagdedes.spartan.abstraction.player.SpartanPlayer;
import com.vagdedes.spartan.abstraction.protocol.SpartanProtocol;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Event_Death implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Event(PlayerDeathEvent e) {
        event(e.getEntity(), false);
    }

    public static void event(Player player, boolean packets) {
        SpartanProtocol protocol = SpartanBukkit.getProtocol(player);
        protocol.timerBalancer.addBalance(50);

        if (protocol.packetsEnabled() == packets) {
            SpartanPlayer p = protocol.spartanPlayer;

            // Detections
            p.getExecutor(Enums.HackType.AutoRespawn).handle(false, null);
            p.getExecutor(Enums.HackType.ImpossibleInventory).handle(false, null);
            // Objects
            p.resetData(true);
            p.movement.setDetectionLocation();
        }
    }

}
