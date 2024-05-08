package com.vagdedes.spartan.listeners;

import com.vagdedes.spartan.abstraction.replicates.SpartanPlayer;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import com.vagdedes.spartan.functionality.tracking.TridentUse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerRiptideEvent;

public class EventsHandler_1_13 implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void SwimEvent(EntityToggleSwimEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof Player) {
            Player n = (Player) entity;
            SpartanPlayer p = SpartanBukkit.getPlayer(n);

            if (p == null) {
                return;
            }
            // Objects
            p.movement.setSwimming(
                    e.isCancelled() ? n.isSwimming() : n.isSwimming() || e.isSwimming(),
                    0
            );
        }
    }

    @EventHandler
    private void TridentShoot(PlayerRiptideEvent e) {
        SpartanPlayer p = SpartanBukkit.getPlayer(e.getPlayer());

        if (p == null) {
            return;
        }
        // Handlers
        TridentUse.run(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void PotionEffect(EntityPotionEffectEvent e) {
        if (e.getAction() == EntityPotionEffectEvent.Action.ADDED
                && e.getCause() == EntityPotionEffectEvent.Cause.PLUGIN) { // Attention for new methods
            Entity entity = e.getEntity();

            if (entity instanceof Player) {
                Player n = (Player) entity;
                SpartanPlayer p = SpartanBukkit.getPlayer(n);

                if (p == null) {
                    return;
                }
                // Objects
                p.setStoredPotionEffects(n.getActivePotionEffects());
            }
        }
    }
}