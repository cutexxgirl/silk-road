package io.github.cutexxgirl.fragmentcamera.camera;

import java.util.UUID;

import io.github.cutexxgirl.fragmentcamera.FragmentCameraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

public final class PlayerModelSmoother {
    public static final PlayerModelSmoother INSTANCE = new PlayerModelSmoother();

    private UUID lastPlayerId;
    private double lastTickY;
    private double visualY;
    private double targetY;
    private long lastFrameNanos;
    private boolean initialized;
    private boolean smoothingActive;
    private boolean wasOnGround;

    private PlayerModelSmoother() {
    }

    public double update(AbstractClientPlayer player, float partialTick) {
        if (!shouldSmooth(player)) {
            reset(player, partialTick);
            return 0.0D;
        }

        double tickY = player.getY();

        if (!initialized || lastPlayerId == null || !lastPlayerId.equals(player.getUUID())) {
            reset(player, partialTick);
            return 0.0D;
        }

        double deltaY = tickY - lastTickY;
        boolean onGround = player.onGround();
        boolean groundedStep = onGround && wasOnGround;

        if (!onGround && !wasOnGround) {
            smoothingActive = false;
            visualY = tickY;
            targetY = tickY;
        } else if (groundedStep && deltaY > 0.03D && deltaY <= 1.1D) {
            if (!smoothingActive) {
                visualY = lastTickY;
            }

            targetY = tickY;
            smoothingActive = true;
        }

        if (smoothingActive) {
            visualY = approachExp(visualY, targetY, FragmentCameraConfig.PLAYER_MODEL_VERTICAL_RESPONSE.get(), frameDeltaSeconds());

            if (Math.abs(visualY - targetY) < 0.003D) {
                visualY = targetY;
                smoothingActive = false;
            }
        } else {
            visualY = tickY;
            targetY = tickY;
        }

        lastTickY = tickY;
        wasOnGround = onGround;
        return Mth.clamp(visualY - tickY, -FragmentCameraConfig.PLAYER_MODEL_MAX_Y_OFFSET.get(), 0.0D);
    }

    private boolean shouldSmooth(AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        return FragmentCameraConfig.EXPERIMENTAL_PLAYER_MODEL_SMOOTHING.get()
                && minecraft.player == player
                && !minecraft.options.getCameraType().isFirstPerson()
                && !minecraft.isPaused()
                && !isFree3DMovement(player);
    }

    private void reset(AbstractClientPlayer player, float partialTick) {
        double tickY = player.getY();
        lastPlayerId = player.getUUID();
        lastTickY = tickY;
        visualY = tickY;
        targetY = tickY;
        smoothingActive = false;
        wasOnGround = player.onGround();
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

    private static boolean isFree3DMovement(AbstractClientPlayer player) {
        return player.getAbilities().flying
                || player.isFallFlying()
                || player.isSpectator()
                || player.noPhysics
                || player.isPassenger()
                || player.isSwimming();
    }
}
