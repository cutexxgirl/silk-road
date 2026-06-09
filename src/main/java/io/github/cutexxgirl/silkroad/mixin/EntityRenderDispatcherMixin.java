package io.github.cutexxgirl.silkroad.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cutexxgirl.silkroad.camera.PlayerStepUpVisualSmoother;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", ordinal = 0, shift = Shift.AFTER))
    private <E extends Entity> void silkroad$applyLocalPlayerStepUpSmoothing(
            E entity,
            double x,
            double y,
            double z,
            float rotationYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo callbackInfo) {
        if (entity != Minecraft.getInstance().player) {
            return;
        }

        double offset = PlayerStepUpVisualSmoother.INSTANCE.getRenderYOffset(entity, partialTick);

        if (offset != 0.0D) {
            poseStack.translate(0.0D, offset, 0.0D);
        }
    }
}
