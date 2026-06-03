package io.github.cutexxgirl.fragmentcamera.camera;

import java.util.UUID;

import io.github.cutexxgirl.fragmentcamera.FragmentCameraConfig;
import io.github.cutexxgirl.fragmentcamera.compat.PehkuiCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public final class FragmentCameraRuntime {
    public static final FragmentCameraRuntime INSTANCE = new FragmentCameraRuntime();

    private final SpringScalar yawSpring = new SpringScalar();
    private final SpringScalar pitchSpring = new SpringScalar();
    private final SpringVec3 anchorXZSpring = new SpringVec3();

    private BlockGetter lastLevel;
    private UUID lastEntityId;
    private boolean lastDetached;
    private boolean lastMirrored;
    private Vec3 lastAnchor;
    private float lastRawYaw;
    private double continuousYaw;
    private double visualY;
    private double lastStableY;
    private double verticalImpulse;
    private double influence;
    private long lastFrameNanos;
    private boolean initialized;

    private FragmentCameraRuntime() {
    }

    public CameraTransform update(Camera camera, BlockGetter level, Entity cameraEntity, boolean detached, boolean mirrored, float partialTick) {
        Vec3 rawPosition = camera.getPosition();
        float rawYRot = camera.getYRot();
        float rawXRot = camera.getXRot();
        float rawRoll = camera.getRoll();

        if (!FragmentCameraConfig.ENABLED.get() || cameraEntity == null || Minecraft.getInstance().isPaused()) {
            reset(level, cameraEntity, rawPosition, rawYRot, rawXRot, detached, mirrored, partialTick);
            return CameraTransform.unchanged(rawPosition, rawYRot, rawXRot, rawRoll);
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean firstPerson = !detached;
        double deltaSeconds = frameDeltaSeconds();

        if (shouldReset(level, cameraEntity, detached, mirrored, partialTick)) {
            reset(level, cameraEntity, rawPosition, rawYRot, rawXRot, detached, mirrored, partialTick);
        }

        if (!firstPerson && isFree3DMovement(cameraEntity, minecraft)) {
            reset(level, cameraEntity, rawPosition, rawYRot, rawXRot, detached, mirrored, partialTick);
            return CameraTransform.unchanged(rawPosition, rawYRot, rawXRot, rawRoll);
        }

        AimingDetector.State aimingState = AimingDetector.getState(minecraft);
        boolean blocked = FragmentCameraConfig.DISABLE_WHILE_AIMING.get() && (aimingState.aiming() || aimingState.freeLooking());
        boolean active = firstPerson
                ? FragmentCameraConfig.FIRST_PERSON_ROTATION_ENABLED.get() || FragmentCameraConfig.FIRST_PERSON_VERTICAL_ENABLED.get()
                : FragmentCameraConfig.THIRD_PERSON_ENABLED.get();
        double targetInfluence = active && !blocked ? 1.0D : 0.0D;
        influence = approachExp(influence, targetInfluence, FragmentCameraConfig.AIM_BLEND_OUT_SPEED.get(), deltaSeconds);

        CameraTransform transform = firstPerson
                ? updateFirstPerson(rawPosition, rawYRot, rawXRot, rawRoll, deltaSeconds)
                : updateThirdPerson(cameraEntity, rawPosition, rawYRot, rawXRot, rawRoll, deltaSeconds, partialTick);

        lastAnchor = getStableAnchor(cameraEntity, partialTick);
        lastDetached = detached;
        lastMirrored = mirrored;
        lastLevel = level;
        lastEntityId = cameraEntity.getUUID();
        initialized = true;
        return transform;
    }

    private CameraTransform updateFirstPerson(Vec3 rawPosition, float rawYRot, float rawXRot, float rawRoll, double deltaSeconds) {
        continuousYaw += Mth.degreesDifference(lastRawYaw, rawYRot);
        lastRawYaw = rawYRot;

        double smoothedYaw = yawSpring.update(
                continuousYaw,
                deltaSeconds,
                FragmentCameraConfig.FIRST_PERSON_ROTATION_FREQUENCY.get(),
                FragmentCameraConfig.FIRST_PERSON_ROTATION_DAMPING.get());
        double smoothedPitch = pitchSpring.update(
                rawXRot,
                deltaSeconds,
                FragmentCameraConfig.FIRST_PERSON_ROTATION_FREQUENCY.get(),
                FragmentCameraConfig.FIRST_PERSON_ROTATION_DAMPING.get());
        visualY = approachExp(
                visualY,
                rawPosition.y,
                FragmentCameraConfig.FIRST_PERSON_VERTICAL_RESPONSE.get(),
                deltaSeconds);

        if (Math.abs(visualY - rawPosition.y) < FragmentCameraConfig.FIRST_PERSON_VERTICAL_SNAP_THRESHOLD.get()) {
            visualY = rawPosition.y;
        }

        double yawOffset = FragmentCameraConfig.FIRST_PERSON_ROTATION_ENABLED.get() ? (smoothedYaw - continuousYaw) * influence : 0.0D;
        double pitchOffset = FragmentCameraConfig.FIRST_PERSON_ROTATION_ENABLED.get() ? (smoothedPitch - rawXRot) * influence : 0.0D;
        double yOffset = FragmentCameraConfig.FIRST_PERSON_VERTICAL_ENABLED.get() ? (visualY - rawPosition.y) * influence : 0.0D;
        Vec3 position = rawPosition.add(0.0D, yOffset, 0.0D);
        return new CameraTransform(position, (float) (rawYRot + yawOffset), (float) (rawXRot + pitchOffset), rawRoll, hasMeaningfulOffset(yawOffset, pitchOffset, yOffset));
    }

    private CameraTransform updateThirdPerson(Entity cameraEntity, Vec3 rawPosition, float rawYRot, float rawXRot, float rawRoll, double deltaSeconds, float partialTick) {
        continuousYaw += Mth.degreesDifference(lastRawYaw, rawYRot);
        lastRawYaw = rawYRot;

        double smoothedYaw = yawSpring.update(
                continuousYaw,
                deltaSeconds,
                FragmentCameraConfig.THIRD_PERSON_ORBIT_FREQUENCY.get(),
                FragmentCameraConfig.THIRD_PERSON_ORBIT_DAMPING.get());
        double smoothedPitch = pitchSpring.update(
                rawXRot,
                deltaSeconds,
                FragmentCameraConfig.THIRD_PERSON_ORBIT_FREQUENCY.get(),
                FragmentCameraConfig.THIRD_PERSON_ORBIT_DAMPING.get());
        double yawOffset = (smoothedYaw - continuousYaw) * influence;
        double pitchOffset = (smoothedPitch - rawXRot) * influence;

        Vec3 stableAnchor = getStableAnchor(cameraEntity, partialTick);
        Vec3 smoothedAnchorXZ = anchorXZSpring.update(
                new Vec3(stableAnchor.x, 0.0D, stableAnchor.z),
                deltaSeconds,
                FragmentCameraConfig.THIRD_PERSON_POSITION_FREQUENCY.get(),
                FragmentCameraConfig.THIRD_PERSON_POSITION_DAMPING.get());
        Vec3 anchorLag = new Vec3(smoothedAnchorXZ.x - stableAnchor.x, 0.0D, smoothedAnchorXZ.z - stableAnchor.z);
        anchorLag = limitLength(anchorLag, FragmentCameraConfig.MAX_LAG_DISTANCE.get()).scale(influence);

        double deltaY = stableAnchor.y - lastStableY;
        if (Math.abs(deltaY) > 0.03D && Math.abs(deltaY) < 1.5D) {
            verticalImpulse -= deltaY;
        }

        verticalImpulse = Mth.clamp(approachExp(verticalImpulse, 0.0D, 18.0D, deltaSeconds), -0.45D, 0.45D);
        lastStableY = stableAnchor.y;

        double yOffset = verticalImpulse * influence + getCrouchCancelY(cameraEntity, partialTick) * influence;
        Vec3 position = rawPosition.add(anchorLag).add(0.0D, yOffset, 0.0D);
        boolean changed = anchorLag.lengthSqr() > 1.0E-8D || hasMeaningfulOffset(yawOffset, pitchOffset, yOffset);
        return new CameraTransform(position, (float) (rawYRot + yawOffset), (float) (rawXRot + pitchOffset), rawRoll, changed);
    }

    private boolean shouldReset(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick) {
        Vec3 anchor = getStableAnchor(entity, partialTick);
        return !initialized
                || lastLevel != level
                || lastEntityId == null
                || !lastEntityId.equals(entity.getUUID())
                || lastDetached != detached
                || lastMirrored != mirrored
                || !entity.isAlive()
                || lastAnchor == null
                || lastAnchor.distanceTo(anchor) > FragmentCameraConfig.RESET_DISTANCE.get();
    }

    private void reset(BlockGetter level, Entity entity, Vec3 rawPosition, float rawYRot, float rawXRot, boolean detached, boolean mirrored, float partialTick) {
        Vec3 anchor = entity == null ? rawPosition : getStableAnchor(entity, partialTick);
        lastLevel = level;
        lastEntityId = entity == null ? null : entity.getUUID();
        lastDetached = detached;
        lastMirrored = mirrored;
        lastAnchor = anchor;
        lastRawYaw = rawYRot;
        continuousYaw = rawYRot;
        visualY = rawPosition.y;
        lastStableY = anchor.y;
        verticalImpulse = 0.0D;
        influence = 0.0D;
        yawSpring.reset(continuousYaw);
        pitchSpring.reset(rawXRot);
        anchorXZSpring.reset(new Vec3(anchor.x, 0.0D, anchor.z));
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

    private static Vec3 limitLength(Vec3 vector, double maxLength) {
        double lengthSqr = vector.lengthSqr();

        if (maxLength <= 0.0D || lengthSqr <= maxLength * maxLength) {
            return vector;
        }

        return vector.normalize().scale(maxLength);
    }

    private static Vec3 getStableAnchor(Entity entity, float partialTick) {
        if (FragmentCameraConfig.IGNORE_CROUCH_HEIGHT_IN_THIRD_PERSON.get() && entity instanceof Player player) {
            Vec3 position = player.getPosition(partialTick);
            return position.add(0.0D, getStandingEyeHeight(player, partialTick), 0.0D);
        }

        return entity.getEyePosition(partialTick);
    }

    private static double getCrouchCancelY(Entity entity, float partialTick) {
        if (!FragmentCameraConfig.IGNORE_CROUCH_HEIGHT_IN_THIRD_PERSON.get() || !(entity instanceof Player player)) {
            return 0.0D;
        }

        return Math.max(0.0D, getStandingEyeHeight(player, partialTick) - player.getEyeHeight());
    }

    private static double getStandingEyeHeight(Player player, float partialTick) {
        double standingEyeHeight = player.getDimensions(Pose.STANDING).eyeHeight();

        if (Math.abs(PehkuiCompat.getEyeHeightScale(player, partialTick) - 1.0F) > 0.0001F) {
            standingEyeHeight = player.getDefaultDimensions(Pose.STANDING).eyeHeight() * PehkuiCompat.getEyeHeightScale(player, partialTick);
        }

        return standingEyeHeight;
    }

    private static boolean isFree3DMovement(Entity entity, Minecraft minecraft) {
        return entity instanceof Player player
                && minecraft.player == player
                && (player.getAbilities().flying
                || player.isFallFlying()
                || player.isSpectator()
                || player.noPhysics
                || player.isPassenger()
                || player.isSwimming());
    }

    private static boolean hasMeaningfulOffset(double yawOffset, double pitchOffset, double yOffset) {
        return Math.abs(yawOffset) > 1.0E-4D || Math.abs(pitchOffset) > 1.0E-4D || Math.abs(yOffset) > 1.0E-5D;
    }
}
