package io.github.cutexxgirl.fragmentcamera.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cutexxgirl.fragmentcamera.camera.PlayerModelSmoother;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Unique
    private double fragmentcamera$modelYOffset;

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void fragmentcamera$translatePlayerModel(AbstractClientPlayer player, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo) {
        fragmentcamera$modelYOffset = PlayerModelSmoother.INSTANCE.update(player, partialTick);

        if (fragmentcamera$modelYOffset != 0.0D) {
            poseStack.translate(0.0D, fragmentcamera$modelYOffset, 0.0D);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void fragmentcamera$restorePlayerModel(AbstractClientPlayer player, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo) {
        if (fragmentcamera$modelYOffset != 0.0D) {
            poseStack.translate(0.0D, -fragmentcamera$modelYOffset, 0.0D);
            fragmentcamera$modelYOffset = 0.0D;
        }
    }
}
