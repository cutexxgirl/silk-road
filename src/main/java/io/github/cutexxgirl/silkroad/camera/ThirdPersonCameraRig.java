package io.github.cutexxgirl.silkroad.camera;

import io.github.cutexxgirl.silkroad.SilkroadConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ThirdPersonCameraRig {
    public CameraTransform update(BlockGetter level, Entity cameraEntity, Vec3 stableAnchor, Vec3 rawPosition, float rawYRot, float rawXRot, float rawRoll, Vec3 anchorLag, float partialTick) {
        double rawDistance = rawPosition.distanceTo(stableAnchor);
        double distance = clamp(
                rawDistance,
                SilkroadConfig.THIRD_PERSON_RIG_MIN_DISTANCE.get(),
                SilkroadConfig.THIRD_PERSON_RIG_MAX_DISTANCE.get());
        Vec3 anchor = stableAnchor.add(anchorLag);
        Vec3 desiredPosition = anchor.subtract(forwardFromYawPitch(rawYRot, rawXRot).scale(distance));
        Vec3 position = clipToWorld(level, cameraEntity, anchor, desiredPosition);
        boolean changed = position.distanceToSqr(rawPosition) > 1.0E-8D || anchorLag.lengthSqr() > 1.0E-8D;
        return new CameraTransform(position, rawYRot, rawXRot, rawRoll, changed);
    }

    private static Vec3 clipToWorld(BlockGetter level, Entity entity, Vec3 anchor, Vec3 desiredPosition) {
        HitResult hit = level.clip(new ClipContext(anchor, desiredPosition, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));

        if (hit.getType() == HitResult.Type.MISS) {
            return desiredPosition;
        }

        Vec3 hitPosition = hit.getLocation();
        Vec3 fromAnchor = hitPosition.subtract(anchor);
        double distance = fromAnchor.length();
        double padding = SilkroadConfig.THIRD_PERSON_RIG_COLLISION_PADDING.get();

        if (distance <= padding) {
            return anchor;
        }

        return anchor.add(fromAnchor.normalize().scale(distance - padding));
    }

    private static Vec3 forwardFromYawPitch(float yRot, float xRot) {
        double yaw = Math.toRadians(yRot);
        double pitch = Math.toRadians(xRot);
        double cosPitch = Math.cos(pitch);
        return new Vec3(-Math.sin(yaw) * cosPitch, -Math.sin(pitch), Math.cos(yaw) * cosPitch).normalize();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
