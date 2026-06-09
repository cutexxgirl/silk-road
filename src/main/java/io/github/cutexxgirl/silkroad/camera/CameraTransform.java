package io.github.cutexxgirl.silkroad.camera;

import net.minecraft.world.phys.Vec3;

public record CameraTransform(Vec3 position, float yRot, float xRot, float roll, boolean changed) {
    public static CameraTransform unchanged(Vec3 position, float yRot, float xRot, float roll) {
        return new CameraTransform(position, yRot, xRot, roll, false);
    }
}
