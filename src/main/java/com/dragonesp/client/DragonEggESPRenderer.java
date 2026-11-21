package com.dragonesp.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class DragonEggESPRenderer {

    // ESP Colors - bright magenta/purple for Dragon Egg
    private static final float R = 0.85f;
    private static final float G = 0.0f;
    private static final float B = 1.0f;
    private static final float ALPHA_BOX = 0.35f;
    private static final float ALPHA_LINE = 1.0f;

    // Tracer line color - bright cyan
    private static final float TR = 0.0f;
    private static final float TG = 1.0f;
    private static final float TB = 1.0f;

    // Max search radius
    private static final int SEARCH_RADIUS = 128;

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.gameRenderer == null) return;

        World world = client.world;
        Entity camera = client.getCameraEntity();
        if (camera == null) return;

        // Find all dragon eggs in range
        List<BlockPos> dragonEggs = findDragonEggs(world, client.player.getBlockPos());
        if (dragonEggs.isEmpty()) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d camPos = context.camera().getPos();

        // Setup rendering
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (BlockPos eggPos : dragonEggs) {
            // Offset from camera
            double dx = eggPos.getX() - camPos.x;
            double dy = eggPos.getY() - camPos.y;
            double dz = eggPos.getZ() - camPos.z;

            matrices.push();
            matrices.translate(dx, dy, dz);

            Matrix4f mat = matrices.peek().getPositionMatrix();

            // Draw ESP bounding box
            drawBox(buffer, mat, 0, 0, 0, 1, 1, 1, R, G, B, ALPHA_LINE);

            matrices.pop();
        }

        // Draw tracer lines from screen center to each egg
        for (BlockPos eggPos : dragonEggs) {
            double dx = eggPos.getX() + 0.5 - camPos.x;
            double dy = eggPos.getY() + 0.5 - camPos.y;
            double dz = eggPos.getZ() + 0.5 - camPos.z;

            matrices.push();
            Matrix4f mat = matrices.peek().getPositionMatrix();

            // Tracer: from (0,0,0) which is camera origin to egg center
            // We offset by eye height for screen-center feel
            float eyeY = (float)(client.player.getEyeY() - camPos.y);

            buffer.vertex(mat, 0, eyeY, 0).color(TR, TG, TB, ALPHA_LINE);
            buffer.vertex(mat, (float)dx, (float)dy, (float)dz).color(TR, TG, TB, ALPHA_LINE);

            matrices.pop();
        }

        // End and draw
        try {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } catch (Exception e) {
            // Buffer may be empty
        }

        // Now draw filled box (semi-transparent)
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder fillBuffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (BlockPos eggPos : dragonEggs) {
            double dx = eggPos.getX() - camPos.x;
            double dy = eggPos.getY() - camPos.y;
            double dz = eggPos.getZ() - camPos.z;

            matrices.push();
            matrices.translate(dx, dy, dz);
            Matrix4f mat = matrices.peek().getPositionMatrix();

            drawFilledBox(fillBuffer, mat, 0.02f, 0.02f, 0.02f, 0.98f, 0.98f, 0.98f, R, G, B, ALPHA_BOX);

            matrices.pop();
        }

        try {
            BufferRenderer.drawWithGlobalProgram(fillBuffer.end());
        } catch (Exception e) {
            // empty
        }

        // Restore GL state
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static List<BlockPos> findDragonEggs(World world, BlockPos center) {
        List<BlockPos> found = new ArrayList<>();
        int minX = center.getX() - SEARCH_RADIUS;
        int maxX = center.getX() + SEARCH_RADIUS;
        int minY = Math.max(world.getBottomY(), center.getY() - SEARCH_RADIUS);
        int maxY = Math.min(world.getTopY(), center.getY() + SEARCH_RADIUS);
        int minZ = center.getZ() - SEARCH_RADIUS;
        int maxZ = center.getZ() + SEARCH_RADIUS;

        for (int x = minX; x <= maxX; x += 1) {
            for (int y = minY; y <= maxY; y += 1) {
                for (int z = minZ; z <= maxZ; z += 1) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).getBlock() == Blocks.DRAGON_EGG) {
                        found.add(pos);
                    }
                }
            }
        }
        return found;
    }

    // Draw wireframe box edges
    private static void drawBox(BufferBuilder buf, Matrix4f mat,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float r, float g, float b, float a) {
        // Bottom face
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);

        // Top face
        buf.vertex(mat, x1, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z1).color(r, g, b, a);

        // Vertical edges
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z2).color(r, g, b, a);
    }

    // Draw filled box (6 faces as quads)
    private static void drawFilledBox(BufferBuilder buf, Matrix4f mat,
                                       float x1, float y1, float z1,
                                       float x2, float y2, float z2,
                                       float r, float g, float b, float a) {
        // Bottom
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y1, z2).color(r, g, b, a);
        // Top
        buf.vertex(mat, x1, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z1).color(r, g, b, a);
        // North (z1)
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z1).color(r, g, b, a);
        // South (z2)
        buf.vertex(mat, x1, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z2).color(r, g, b, a);
        // West (x1)
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x1, y1, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x1, y2, z1).color(r, g, b, a);
        // East (x2)
        buf.vertex(mat, x2, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x2, y1, z2).color(r, g, b, a);
    }
}
