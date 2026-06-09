package io.github.cutexxgirl.silkroad.camera;

import net.minecraft.world.phys.Vec3;

public final class SpringVec3 {
    private Vec3 position = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private boolean initialized;

    public Vec3 update(Vec3 target, double deltaSeconds, double frequency, double dampingRatio) {
        if (!initialized) {
            reset(target);
            return position;
        }

        double angularFrequency = Math.max(0.001D, frequency) * Math.PI * 2.0D;
        double stiffness = angularFrequency * angularFrequency;
        double damping = 2.0D * Math.max(0.0D, dampingRatio) * angularFrequency;

        double remaining = Math.max(0.0D, deltaSeconds);

        while (remaining > 0.0D) {
            double step = Math.min(remaining, 1.0D / 240.0D);
            Vec3 acceleration = target.subtract(position).scale(stiffness).subtract(velocity.scale(damping));
            velocity = velocity.add(acceleration.scale(step));
            position = position.add(velocity.scale(step));
            remaining -= step;
        }

        return position;
    }

    public void reset(Vec3 value) {
        position = value;
        velocity = Vec3.ZERO;
        initialized = true;
    }
}
