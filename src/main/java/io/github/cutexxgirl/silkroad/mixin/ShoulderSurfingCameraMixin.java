package io.github.cutexxgirl.silkroad.mixin;

import io.github.cutexxgirl.silkroad.camera.SilkroadRuntime;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera", remap = false)
public abstract class ShoulderSurfingCameraMixin {
    @Shadow(remap = false)
    private Vec3 renderOffset;

    @Shadow(remap = false)
    private double cameraDistance;

    @Unique
    private boolean silkroad$targetOffsetScaled;

    @Inject(method = "calcOffset", at = @At("HEAD"), remap = false)
    private void silkroad$resetShoulderSurfingScaleFlag(Camera camera, BlockGetter level, float partialTick, Entity cameraEntity, CallbackInfoReturnable<Vec3> callbackInfo) {
        this.silkroad$targetOffsetScaled = false;
    }

    @ModifyArg(
            method = "calcOffset",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;", ordinal = 0),
            index = 0,
            require = 0,
            remap = false)
    private double silkroad$scaleShoulderSurfingTargetOffset(double scale, Camera camera, BlockGetter level, float partialTick, Entity cameraEntity) {
        double scaled = SilkroadRuntime.INSTANCE.updateShoulderSurfingTargetOffsetScale(scale, cameraEntity, partialTick);
        this.silkroad$targetOffsetScaled = Math.abs(scaled - scale) > 1.0E-4D;
        return scaled;
    }

    @Inject(method = "calcOffset", at = @At("RETURN"), cancellable = true, remap = false)
    private void silkroad$applyShoulderSurfingLag(Camera camera, BlockGetter level, float partialTick, Entity cameraEntity, CallbackInfoReturnable<Vec3> callbackInfo) {
        Vec3 originalOffset = callbackInfo.getReturnValue();
        Vec3 offset = SilkroadRuntime.INSTANCE.updateShoulderSurfingOffset(camera, level, cameraEntity, originalOffset, partialTick, this.silkroad$targetOffsetScaled);

        if (!offset.equals(originalOffset)) {
            this.renderOffset = offset;
            this.cameraDistance = offset.length();
            callbackInfo.setReturnValue(offset);
        }
    }
}
