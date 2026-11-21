package com.dragonesp.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class DragonEggESPRenderer {
    private static final int HORIZONTAL_RADIUS = 32;
    private static final int VERTICAL_RADIUS = 32;

    public static void render(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        World world = client.world;
        BlockPos center = client.player.getBlockPos();
        Vec3d camPos = client.gameRenderer.getCamera().getCameraPos();

        int minX = center.getX() - HORIZONTAL_RADIUS;
        int maxX = center.getX() + HORIZONTAL_RADIUS;
        int minZ = center.getZ() - HORIZONTAL_RADIUS;
        int maxZ = center.getZ() + HORIZONTAL_RADIUS;

        int minY = Math.max(world.getBottomY(), center.getY() - VERTICAL_RADIUS);
        int maxY = Math.min(world.getTopY(Heightmap.Type.WORLD_SURFACE, center.getX(), center.getZ()) + VERTICAL_RADIUS, 320);

        VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getDebugLineStrip());

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int topY = Math.min(maxY, world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z));
                for (int y = minY; y <= topY; y++) {
                    pos.set(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (state.isOf(Blocks.DRAGON_EGG)) {
                        drawBoxOutline(matrices, buffer, pos, 1.0f, 0.2f, 1.0f, 1.0f);
                    }
                }
            }
        }

        matrices.pop();
        consumers.draw();
    }

    private static void drawBoxOutline(
            MatrixStack matrices,
            VertexConsumer consumer,
            BlockPos pos,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        Box box = new Box(pos);
        MatrixStack.Entry entry = matrices.peek();

        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        line(entry, consumer, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        line(entry, consumer, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
        line(entry, consumer, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
        line(entry, consumer, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);

        line(entry, consumer, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(entry, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        line(entry, consumer, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        line(entry, consumer, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);

        line(entry, consumer, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        line(entry, consumer, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(entry, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        line(entry, consumer, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
    }

    private static void line(
            MatrixStack.Entry entry,
            VertexConsumer consumer,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float red, float green, float blue, float alpha
    ) {
        float dx = (float) (x2 - x1);
        float dy = (float) (y2 - y1);
        float dz = (float) (z2 - z1);
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float nx = len > 0 ? dx / len : 0f;
        float ny = len > 0 ? dy / len : 1f;
        float nz = len > 0 ? dz / len : 0f;

        consumer.vertex(entry, (float) x1, (float) y1, (float) z1)
                .color(red, green, blue, alpha)
                .normal(entry, nx, ny, nz);
        consumer.vertex(entry, (float) x2, (float) y2, (float) z2)
                .color(red, green, blue, alpha)
                .normal(entry, nx, ny, nz);
    }
}
