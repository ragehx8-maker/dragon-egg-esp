package com.dragonesp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonEggESPClient implements ClientModInitializer {

    public static final String MOD_ID = "dragonesp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Dragon Egg ESP Mod loaded!");

        // Register world render event for ESP drawing
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null) {
                DragonEggESPRenderer.render(context);
            }
        });
    }
}
