package com.dragonesp.client.mixin;

import com.dragonesp.client.DragonEggESPRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void dragonesp$onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        DragonEggESPRenderer.render(new MatrixStack(), 0.0f);
    }
}
