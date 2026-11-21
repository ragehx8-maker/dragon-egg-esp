package com.dragonesp.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class DragonEggESPRenderer {
    private static final int HORIZONTAL_RADIUS = 32;
    private static final int VERTICAL_RADIUS = 32;

    public static List<Box> scanDragonEggs(MinecraftClient client) {
        List<Box> targets = new ArrayList<>();
        if (client == null || client.player == null || client.world == null) {
            return targets;
        }

        World world = client.world;
        BlockPos center = client.player.getBlockPos();

        int minX = center.getX() - HORIZONTAL_RADIUS;
        int maxX = center.getX() + HORIZONTAL_RADIUS;
        int minZ = center.getZ() - HORIZONTAL_RADIUS;
        int maxZ = center.getZ() + HORIZONTAL_RADIUS;

        int minY = Math.max(world.getBottomY(), center.getY() - VERTICAL_RADIUS);
        int maxY = Math.min(world.getTopY(Heightmap.Type.WORLD_SURFACE, center.getX(), center.getZ()) + VERTICAL_RADIUS, 320);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int topY = Math.min(maxY, world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z));
                for (int y = minY; y <= topY; y++) {
                    pos.set(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (state.isOf(Blocks.DRAGON_EGG)) {
                        targets.add(new Box(pos));
                    }
                }
            }
        }

        return targets;
    }

    public static void render(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null || client.gameRenderer == null) {
            return;
        }

        Vec3d camPos = client.gameRenderer.getCamera().getCameraPos();
        List<Box> targets = scanDragonEggs(client);
        if (targets.isEmpty() || camPos == null) {
            return;
        }

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        matrices.pop();
    }
}
