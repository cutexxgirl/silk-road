package io.github.cutexxgirl.fragmentcamera.camera;

import java.util.UUID;

import io.github.cutexxgirl.fragmentcamera.FragmentCameraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

public final class PlayerModelSmoother {
    public static final PlayerModelSmoother INSTANCE = new PlayerModelSmoother();

    private UUID lastPlayerId;
    private double lastRawY;
    private double visualY;
    private long lastFrameNanos;
    private boolean initialized;

    private PlayerModelSmoother() {
    }

    public double update(AbstractClientPlayer player, float partialTick) {
        if (!shouldSmooth(player)) {
            reset(player, partialTick);
            return 0.0D;
        }

        double rawY = player.getPosition(partialTick).y;

        if (!initialized || lastPlayerId == null || !lastPlayerId.equals(player.getUUID())) {
            reset(player, partialTick);
            return 0.0D;
        }

        double deltaY = rawY - lastRawY;

        if (player.onGround() && deltaY > 0.0D && deltaY <= 1.1D) {
            visualY = approachExp(visualY, rawY, FragmentCameraConfig.PLAYER_MODEL_VERTICAL_RESPONSE.get(), frameDeltaSeconds());
        } else {
            visualY = rawY;
        }

        lastRawY = rawY;
        return Mth.clamp(visualY - rawY, -FragmentCameraConfig.PLAYER_MODEL_MAX_Y_OFFSET.get(), 0.0D);
    }

    private boolean shouldSmooth(AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        return FragmentCameraConfig.EXPERIMENTAL_PLAYER_MODEL_SMOOTHING.get()
                && minecraft.player == player
                && !minecraft.options.getCameraType().isFirstPerson()
                && !minecraft.isPaused();
    }

    private void reset(AbstractClientPlayer player, float partialTick) {
        double rawY = player.getPosition(partialTick).y;
        lastPlayerId = player.getUUID();
        lastRawY = rawY;
        visualY = rawY;
        initialized = true;
    }

    private double frameDeltaSeconds() {
        long now = System.nanoTime();

        if (lastFrameNanos == 0L) {
            lastFrameNanos = now;
            return 1.0D / 60.0D;
        }

        double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0D;
        lastFrameNanos = now;
        return Mth.clamp(deltaSeconds, 0.0D, 1.0D / 20.0D);
    }

    private static double approachExp(double current, double target, double speed, double deltaSeconds) {
        double step = 1.0D - Math.exp(-Math.max(0.0D, speed) * deltaSeconds);
        return current + (target - current) * step;
    }
}
