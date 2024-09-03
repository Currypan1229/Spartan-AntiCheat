package com.vagdedes.spartan.listeners.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.vagdedes.spartan.Register;
import com.vagdedes.spartan.abstraction.protocol.SpartanProtocol;
import com.vagdedes.spartan.compatibility.necessary.protocollib.ProtocolLib;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import com.vagdedes.spartan.listeners.Shared;
import com.vagdedes.spartan.listeners.protocol.async.LagCompensation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class Move extends PacketAdapter {

    public Move() {
        super(
                Register.plugin,
                ListenerPriority.LOWEST,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.GROUND
        );
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        long time = System.currentTimeMillis();
        Player player = event.getPlayer();

        if (ProtocolLib.isTemporary(player)) {
            return;
        }
        SpartanProtocol protocol = SpartanBukkit.getProtocol(player);

        if (System.currentTimeMillis() - protocol.mcClient.lastPacketTime > 4) {
            protocol.mcClient.verifiedPacket++;
        }
        protocol.mcClient.lastPacketTime = time;

        if (!invalidTeleport(protocol.getLocation())
                && protocol.isLoading()) {
            protocol.load();
            return;
        } else if (!invalidTeleport(protocol.getLocation())
                || (invalidTeleport(protocol.getLocation()) && !protocol.isLoading())) {
            LagCompensation.add(player.getEntityId(), protocol.getLocation());
        }
        if (protocol.isLoading()) {
            return;
        }
        movePacket(event, protocol);
    }

    private static void movePacket(PacketEvent event, SpartanProtocol protocol) {
        if (SpartanBukkit.packetsEnabled()) {
            PacketContainer packet = event.getPacket();
            protocol.setOnGround(onGroundPacketLevel(event));
            PacketType type = event.getPacket().getType();

            /*
            if (!protocol.teleportEngine.isEmpty()) {
                for (TeleportData teleportData : protocol.teleportEngine) {
                    Location teleportLocation = protocol.getLocation().clone().add(teleportData.getLocation());
                    Location teleportSilentLocation = teleportData.getLocation();
                    int hl = protocol.getLocation().toVector().hashCode();
                    int ht = teleportLocation.toVector().hashCode();
                    int hSt = teleportSilentLocation.toVector().hashCode();
                    if (hl == ht) {
                        protocol.setLocation(teleportLocation);
                        protocol.teleportEngine.remove(teleportData);
                    } else if (hl == hSt) {
                        protocol.setLocation(teleportSilentLocation);
                        protocol.teleportEngine.remove(teleportData);
                    }
                }
            }

             */

            if (type.equals(PacketType.Play.Client.LOOK)) {
                Location from = protocol.getLocation().clone();
                protocol.getLocation().setYaw(packet.getFloat().read(0));
                protocol.getLocation().setPitch(packet.getFloat().read(1));
                Shared.movement(new PlayerMoveEvent(
                        event.getPlayer(),
                        from,
                        protocol.getLocation()
                ));
            } else if (type.equals(PacketType.Play.Client.POSITION_LOOK)) {
                Location from = protocol.getLocation().clone(),
                        location = readLocation(event);
                protocol.getLocation().setX(location.getX());
                protocol.getLocation().setY(location.getY());
                protocol.getLocation().setZ(location.getZ());
                protocol.getLocation().setYaw(packet.getFloat().read(0));
                protocol.getLocation().setPitch(packet.getFloat().read(1));
                Shared.movement(new PlayerMoveEvent(
                        event.getPlayer(),
                        from,
                        protocol.getLocation()
                ));
            } else if (type.equals(PacketType.Play.Client.POSITION)) {
                Location from = protocol.getLocation().clone(),
                        location = readLocation(event);
                protocol.getLocation().setX(location.getX());
                protocol.getLocation().setY(location.getY());
                protocol.getLocation().setZ(location.getZ());
                Shared.movement(new PlayerMoveEvent(
                        event.getPlayer(),
                        from,
                        protocol.getLocation()
                ));
            }
        }
    }

    public static Location readLocation(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        return new Location(
                event.getPlayer().getWorld(),
                packet.getDoubles().read(0),
                packet.getDoubles().read(1),
                packet.getDoubles().read(2)
        );
    }

    public static boolean onGroundPacketLevel(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        return packet.getBooleans().read(0);
    }

    private static boolean invalidTeleport(Location location) {
        return location.getX() + location.getZ() == 17;
    }

}