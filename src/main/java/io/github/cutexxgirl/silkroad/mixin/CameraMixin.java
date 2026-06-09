package io.github.cutexxgirl.silkroad.mixin;

import io.github.cutexxgirl.silkroad.SilkroadConfig;
import io.github.cutexxgirl.silkroad.camera.CameraTransform;
import io.github.cutexxgirl.silkroad.camera.SilkroadRuntime;
import io.github.cutexxgirl.silkroad.compat.PehkuiCompat;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 900)
public abstract class CameraMixin {
    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot, float roll);

    @Inject(method = "setup", at = @At("HEAD"))
    private void silkroad$ignoreThirdPersonCrouchHeight(BlockGetter level, Entity cameraEntity, boolean detached, boolean mirrored, float partialTick, CallbackInfo callbackInfo) {
        if (!detached || !SilkroadConfig.ENABLED.get() || !SilkroadConfig.IGNORE_CROUCH_HEIGHT_IN_THIRD_PERSON.get() || !(cameraEntity instanceof Player player)) {
            return;
        }

        float standingEyeHeight = player.getDimensions(Pose.STANDING).eyeHeight();
        float eyeScale = PehkuiCompat.getEyeHeightScale(player, partialTick);

        if (Math.abs(eyeScale - 1.0F) > 0.0001F) {
            standingEyeHeight = player.getDefaultDimensions(Pose.STANDING).eyeHeight() * eyeScale;
        }

        this.eyeHeight = standingEyeHeight;
        this.eyeHeightOld = standingEyeHeight;
    }

    @Inject(method = "setup", at = @At("RETURN"))
    private void silkroad$applySpringCamera(BlockGetter level, Entity cameraEntity, boolean detached, boolean mirrored, float partialTick, CallbackInfo callbackInfo) {
        Camera camera = (Camera) (Object) this;
        CameraTransform transform = SilkroadRuntime.INSTANCE.update(camera, level, cameraEntity, detached, mirrored, partialTick);

        if (transform.changed()) {
            this.setPosition(transform.position());
            this.setRotation(transform.yRot(), transform.xRot(), transform.roll());
        }
    }

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V", ordinal = 0))
    private void silkroad$applyThirdPersonRotationBeforeMove(BlockGetter level, Entity cameraEntity, boolean detached, boolean mirrored, float partialTick, CallbackInfo callbackInfo) {
        Camera camera = (Camera) (Object) this;
        CameraTransform transform = SilkroadRuntime.INSTANCE.prepareThirdPersonRotation(camera, level, cameraEntity, detached, mirrored, partialTick);

        if (transform.changed()) {
            this.setRotation(transform.yRot(), transform.xRot(), transform.roll());
        }
    }
}
