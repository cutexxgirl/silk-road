package io.github.cutexxgirl.silkroad.mixin;

import io.github.cutexxgirl.silkroad.camera.SilkroadRuntime;
import io.github.cutexxgirl.silkroad.compat.PehkuiCompat;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(
            method = "calcOffset",
            at = @At(value = "INVOKE", target = "Lcom/github/exopandora/shouldersurfing/api/util/EntityHelper;getMaxScale(Lnet/minecraft/world/entity/Entity;)F"),
            require = 0,
            remap = false)
    private float silkroad$scaleShoulderSurfingTargetOffset(Entity scaleEntity, Camera camera, BlockGetter level, float partialTick, Entity cameraEntity) {
        double scale = silkroad$getShoulderSurfingMaxScale(scaleEntity);
        double scaled = SilkroadRuntime.INSTANCE.updateShoulderSurfingTargetOffsetScale(scale, cameraEntity, partialTick);
        this.silkroad$targetOffsetScaled = Math.abs(scaled - scale) > 1.0E-4D;
        return (float) scaled;
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

    @Inject(method = "maxZoom", at = @At("HEAD"), cancellable = true, remap = false)
    private static void silkroad$applyPehkuiShoulderSurfingZoom(Camera camera, BlockGetter level, Vec3 cameraOffset, float partialTick, CallbackInfoReturnable<Double> callbackInfo) {
        Entity cameraEntity = camera.getEntity();

        if (cameraEntity == null || Math.abs(PehkuiCompat.getEyeHeightScale(cameraEntity, partialTick) - 1.0F) < 0.0001F) {
            return;
        }

        double distance = cameraOffset.length();
        Vec3 worldOffset = new Vec3(camera.getUpVector()).scale(cameraOffset.y())
                .add(new Vec3(camera.getLeftVector()).scale(cameraOffset.x()))
                .add(new Vec3(camera.getLookVector()).scale(-cameraOffset.z()));
        Vec3 horizontalPush = new Vec3(worldOffset.x, 0.0D, worldOffset.z);

        if (horizontalPush.lengthSqr() > 1.0E-8D) {
            horizontalPush = horizontalPush.normalize().scale(0.3D);
        }

        Vec3 eyePosition = cameraEntity.getEyePosition(partialTick);

        for (int i = 0; i < 8; i++) {
            Vec3 offset = new Vec3(i & 1, i >> 1 & 1, i >> 2 & 1)
                    .scale(2.0D)
                    .subtract(1.0D, 1.0D, 1.0D)
                    .scale(0.15D)
                    .yRot(-camera.getYRot() * Mth.DEG_TO_RAD);
            Vec3 from = eyePosition.add(offset).add(horizontalPush);
            Vec3 to = from.add(worldOffset);
            ClipContext context = new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, cameraEntity);
            HitResult hitResult = level.clip(context);

            if (hitResult.getType() != HitResult.Type.MISS) {
                double newDistance = hitResult.getLocation().distanceTo(eyePosition);

                if (newDistance < distance) {
                    distance = newDistance;
                }
            }
        }

        callbackInfo.setReturnValue(distance);
    }

    @Unique
    private static float silkroad$getShoulderSurfingMaxScale(Entity cameraEntity) {
        Entity entity = cameraEntity;
        float scale = silkroad$getShoulderSurfingEntityScale(entity);

        while (entity.getVehicle() != null) {
            entity = entity.getVehicle();
            scale = Math.max(scale, silkroad$getShoulderSurfingEntityScale(entity));
        }

        return scale;
    }

    @Unique
    private static float silkroad$getShoulderSurfingEntityScale(Entity entity) {
        return entity instanceof LivingEntity living ? living.getScale() : 1.0F;
    }
}
