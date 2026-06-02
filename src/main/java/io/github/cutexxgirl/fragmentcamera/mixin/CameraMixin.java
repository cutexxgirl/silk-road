package io.github.cutexxgirl.fragmentcamera.mixin;

import io.github.cutexxgirl.fragmentcamera.camera.CameraTransform;
import io.github.cutexxgirl.fragmentcamera.camera.FragmentCameraRuntime;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
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
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot, float roll);

    @Inject(method = "setup", at = @At("RETURN"))
    private void fragmentcamera$applySpringCamera(BlockGetter level, Entity cameraEntity, boolean detached, boolean mirrored, float partialTick, CallbackInfo callbackInfo) {
        Camera camera = (Camera) (Object) this;
        CameraTransform transform = FragmentCameraRuntime.INSTANCE.update(camera, level, cameraEntity);

        if (transform.changed()) {
            this.setPosition(transform.position());
            this.setRotation(transform.yRot(), transform.xRot(), transform.roll());
        }
    }
}
