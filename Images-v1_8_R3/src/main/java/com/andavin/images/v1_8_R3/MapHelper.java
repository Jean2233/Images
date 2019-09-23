package com.andavin.images.v1_8_R3;

import com.andavin.reflect.Reflection;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.andavin.reflect.Reflection.setFieldValue;
import static java.util.Collections.emptyList;

/**
 * @since September 20, 2019
 * @author Andavin
 */
class MapHelper extends com.andavin.images.MapHelper {

    private static final int DEFAULT_STARTING_ID = 8000;
    private static final Map<UUID, AtomicInteger> MAP_IDS = new HashMap<>(4);
    private static final Field ENTITY_ID = Reflection.findField(Entity.class, "id");

    @Override
    protected MapView getWorldMap(int id) {
        return Bukkit.getMap((short) id);
    }

    @Override
    protected int nextMapId(World world) {
        return MAP_IDS.computeIfAbsent(world.getUID(), __ ->
                new AtomicInteger(DEFAULT_STARTING_ID)).getAndIncrement();
    }

    @Override
    protected void createMap(int frameId, int mapId, Player player, Location location, BlockFace direction, byte[] pixels) {

        ItemStack item = new ItemStack(Items.FILLED_MAP);
        item.setData(mapId);

        EntityItemFrame frame = new EntityItemFrame(((CraftWorld) player.getWorld()).getHandle());
        frame.setItem(item);
        frame.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
        frame.setDirection(CraftBlock.blockFaceToNotch(direction));
        setFieldValue(ENTITY_ID, frame, frameId);

        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(frame, 71, frame.direction.b());
        BlockPosition position = frame.getBlockPosition();
        spawnPacket.a(MathHelper.d((float) (position.getX() * 32)));
        spawnPacket.b(MathHelper.d((float) (position.getY() * 32)));
        spawnPacket.c(MathHelper.d((float) (position.getZ() * 32)));

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(spawnPacket);
        connection.sendPacket(new PacketPlayOutEntityMetadata(frame.getId(), frame.getDataWatcher(), true));
        connection.sendPacket(new PacketPlayOutMap(mapId, (byte) 3, emptyList(), pixels, 0, 0, 128, 128));
    }

    @Override
    protected void destroyMap(Player player, int[] frameIds) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(frameIds));
    }

    @Override
    protected byte[] createPixels(BufferedImage image) {

        int pixelCount = image.getWidth() * image.getHeight();
        int[] pixels = new int[pixelCount];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        byte[] colors = new byte[pixelCount];
        for (int i = 0; i < pixelCount; i++) {
            colors[i] = MapPalette.matchColor(new Color(pixels[i], true));
        }

        return colors;
    }
}