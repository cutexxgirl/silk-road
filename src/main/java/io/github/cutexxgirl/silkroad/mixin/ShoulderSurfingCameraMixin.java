package io.github.cutexxgirl.silkroad.mixin;

import io.github.cutexxgirl.silkroad.camera.SilkroadRuntime;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera", remap = false)
public abstract class ShoulderSurfingCameraMixin {
    @Shadow(remap = false)
    private Vec3 renderOffset;

    @Shadow(remap = false)
    private double cameraDistance;

    @Inject(method = "calcOffset", at = @At("RETURN"), cancellable = true, remap = false)
    private void silkroad$applyShoulderSurfingLag(Camera camera, BlockGetter level, float partialTick, Entity cameraEntity, CallbackInfoReturnable<Vec3> callbackInfo) {
        Vec3 originalOffset = callbackInfo.getReturnValue();
        Vec3 offset = SilkroadRuntime.INSTANCE.updateShoulderSurfingOffset(camera, level, cameraEntity, originalOffset, partialTick);

        if (!offset.equals(originalOffset)) {
            this.renderOffset = offset;
            this.cameraDistance = offset.length();
            callbackInfo.setReturnValue(offset);
        }
    }
}
