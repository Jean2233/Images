package com.andavin.images.v1_9_R2;

import net.minecraft.server.v1_9_R2.PlayerConnection;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @since September 21, 2019
 * @author Andavin
 */
class PacketListener extends com.andavin.images.PacketListener {

    @Override
    public void setEntityListener(Player player, ImageListener listener) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.networkManager.setPacketListener(new PlayerConnectionProxy(connection, listener));
    }
}