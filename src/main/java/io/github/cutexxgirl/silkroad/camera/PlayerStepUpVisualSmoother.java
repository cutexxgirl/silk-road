package io.github.cutexxgirl.silkroad.camera;

import java.util.UUID;

import io.github.cutexxgirl.silkroad.SilkroadConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class PlayerStepUpVisualSmoother {
    public static final PlayerStepUpVisualSmoother INSTANCE = new PlayerStepUpVisualSmoother();

    private UUID lastEntityId;
    private int lastTick = Integer.MIN_VALUE;
    private double lastTickY;
    private double visualY;
    private boolean smoothingActive;
    private boolean lastOnGround;
    private long lastFrameNanos;

    private PlayerStepUpVisualSmoother() {
    }

    public double getRenderYOffset(Entity entity, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        if (entity != minecraft.player) {
            return 0.0D;
        }

        LocalPlayer player = minecraft.player;
        double rawY = entity.getPosition(partialTick).y;

        if (shouldReset(entity, player, minecraft)) {
            reset(entity, rawY);
            return 0.0D;
        }

        UUID entityId = entity.getUUID();

        if (lastEntityId == null || !lastEntityId.equals(entityId)) {
            reset(entity, rawY);
            return 0.0D;
        }

        updateTickState(player, rawY);

        if (!smoothingActive) {
            visualY = rawY;
            return 0.0D;
        }

        visualY = approachExp(visualY, rawY, SilkroadConfig.THIRD_PERSON_STEP_UP_RESPONSE.get(), frameDeltaSeconds());

        if (Math.abs(visualY - rawY) < SilkroadConfig.THIRD_PERSON_STEP_UP_SNAP_THRESHOLD.get()) {
            visualY = rawY;
            smoothingActive = false;
            return 0.0D;
        }

        return visualY - rawY;
    }

    private void updateTickState(LocalPlayer player, double rawY) {
        if (player.tickCount == lastTick) {
            return;
        }

        double tickY = player.getY();
        double dy = tickY - lastTickY;

        if (Math.abs(dy) > SilkroadConfig.THIRD_PERSON_STEP_UP_RESET_DISTANCE.get()) {
            reset(player, rawY);
            return;
        }

        boolean hasVerticalMovement = Math.abs(dy) > 1.0E-5D;
        boolean onGround = player.onGround();
        boolean stepUp = onGround
                && lastOnGround
                && dy > SilkroadConfig.THIRD_PERSON_STEP_UP_MIN_HEIGHT.get()
                && dy <= SilkroadConfig.THIRD_PERSON_STEP_UP_MAX_HEIGHT.get()
                && player.getDeltaMovement().y <= 0.08D;

        if (stepUp) {
            smoothingActive = true;
        } else if (hasVerticalMovement) {
            visualY = rawY;
            smoothingActive = false;
        }

        lastTickY = tickY;
        lastTick = player.tickCount;
        lastOnGround = onGround;
    }

    private boolean shouldReset(Entity entity, LocalPlayer player, Minecraft minecraft) {
        return entity == null
                || player == null
                || !SilkroadConfig.ENABLED.get()
                || !SilkroadConfig.THIRD_PERSON_STEP_UP_SMOOTHING_ENABLED.get()
                || minecraft.isPaused()
                || minecraft.options.getCameraType().isFirstPerson()
                || !player.isAlive()
                || player.isSpectator()
                || player.noPhysics
                || player.isPassenger()
                || player.isSwimming()
                || player.getAbilities().flying;
    }

    private void reset(Entity entity, double rawY) {
        lastEntityId = entity == null ? null : entity.getUUID();
        lastTick = entity == null ? Integer.MIN_VALUE : entity.tickCount;
        lastTickY = entity == null ? rawY : entity.getY();
        visualY = rawY;
        smoothingActive = false;
        lastOnGround = entity != null && entity.onGround();
        lastFrameNanos = 0L;
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
