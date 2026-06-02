package io.github.cutexxgirl.fragmentcamera.camera;

public final class SpringScalar {
    private double position;
    private double velocity;
    private boolean initialized;

    public double update(double target, double deltaSeconds, double frequency, double dampingRatio) {
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
            double acceleration = (target - position) * stiffness - velocity * damping;
            velocity += acceleration * step;
            position += velocity * step;
            remaining -= step;
        }

        return position;
    }

    public void reset(double value) {
        position = value;
        velocity = 0.0D;
        initialized = true;
    }
}
