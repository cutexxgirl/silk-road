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
import org.joml.Vector3f;

public final class FragmentCameraRuntime {
    public static final FragmentCameraRuntime INSTANCE = new FragmentCameraRuntime();

    private final SpringScalar yawSpring = new SpringScalar();
    private final SpringScalar pitchSpring = new SpringScalar();
    private final SpringVec3 anchorXZSpring = new SpringVec3();
    private final ThirdPersonCameraRig thirdPersonRig = new ThirdPersonCameraRig();

    private BlockGetter lastLevel;
    private UUID lastEntityId;
    private boolean lastDetached;
    private boolean lastMirrored;
    private Vec3 lastAnchor;
    private float lastRawYaw;
    private double continuousYaw;
    private double visualY;
    private double thirdPersonVisualY;
    private double influence;
    private long lastFrameNanos;
    private boolean initialized;
    private boolean thirdPersonBypassedLastFrame;
    private boolean shoulderSurfingOffsetHandledThisFrame;

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

        if (!firstPerson && shouldBypassThirdPerson(cameraEntity, minecraft)) {
            reset(level, cameraEntity, rawPosition, rawYRot, rawXRot, detached, mirrored, partialTick);
            thirdPersonBypassedLastFrame = true;
            return CameraTransform.unchanged(rawPosition, rawYRot, rawXRot, rawRoll);
        }

        if (!firstPerson && thirdPersonBypassedLastFrame) {
            reset(level, cameraEntity, rawPosition, rawYRot, rawXRot, detached, mirrored, partialTick);
            thirdPersonBypassedLastFrame = false;
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
                : updateThirdPerson(level, cameraEntity, rawPosition, rawYRot, rawXRot, rawRoll, deltaSeconds, partialTick, aimingState.shoulderSurfing());

        lastAnchor = getStableAnchor(cameraEntity, partialTick);
        lastDetached = detached;
        lastMirrored = mirrored;
        lastLevel = level;
        lastEntityId = cameraEntity.getUUID();
        initialized = true;
        shoulderSurfingOffsetHandledThisFrame = false;
        return transform;
    }

    public Vec3 updateShoulderSurfingOffset(Camera camera, BlockGetter level, Entity cameraEntity, Vec3 shoulderOffset, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!FragmentCameraConfig.ENABLED.get()
                || !FragmentCameraConfig.THIRD_PERSON_ENABLED.get()
                || cameraEntity == null
                || minecraft.isPaused()
                || shouldBypassThirdPerson(cameraEntity, minecraft)) {
            reset(level, cameraEntity, camera.getPosition(), camera.getYRot(), camera.getXRot(), true, false, partialTick);
            shoulderSurfingOffsetHandledThisFrame = true;
            return shoulderOffset;
        }

        double deltaSeconds = frameDeltaSeconds();

        if (shouldReset(level, cameraEntity, true, false, partialTick)) {
            reset(level, cameraEntity, camera.getPosition(), camera.getYRot(), camera.getXRot(), true, false, partialTick);
        }

        AimingDetector.State aimingState = AimingDetector.getState(minecraft);
        boolean blocked = FragmentCameraConfig.DISABLE_WHILE_AIMING.get() && (aimingState.aiming() || aimingState.freeLooking());
        double targetInfluence = blocked ? 0.0D : 1.0D;
        influence = approachExp(influence, targetInfluence, FragmentCameraConfig.AIM_BLEND_OUT_SPEED.get(), deltaSeconds);

        Vec3 stableAnchor = getStableAnchor(cameraEntity, partialTick);
        Vec3 anchorLag = updateThirdPersonAnchorLag(stableAnchor, deltaSeconds).scale(influence);
        lastAnchor = stableAnchor;
        lastDetached = true;
        lastMirrored = false;
        lastLevel = level;
        lastEntityId = cameraEntity.getUUID();
        initialized = true;
        shoulderSurfingOffsetHandledThisFrame = true;

        if (anchorLag.lengthSqr() <= 1.0E-8D) {
            return shoulderOffset;
        }

        // Shoulder Surfing stores camera offset in its local left/up/back basis.
        // Feeding the lag there keeps its transparency and pick logic in sync.
        return shoulderOffset.add(worldLagToShoulderOffset(camera, anchorLag));
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

    private CameraTransform updateThirdPerson(BlockGetter level, Entity cameraEntity, Vec3 rawPosition, float rawYRot, float rawXRot, float rawRoll, double deltaSeconds, float partialTick, boolean shoulderSurfing) {
        Vec3 stableAnchor = getStableAnchor(cameraEntity, partialTick);
        Vec3 vanillaEye = cameraEntity.getEyePosition(partialTick);
        Vec3 rawCameraOffset = rawPosition.subtract(stableAnchor);

        if (shoulderSurfing && shoulderSurfingOffsetHandledThisFrame) {
            return CameraTransform.unchanged(rawPosition, rawYRot, rawXRot, rawRoll);
        }

        Vec3 anchorLag = updateThirdPersonAnchorLag(stableAnchor, deltaSeconds).scale(influence);

        if (shoulderSurfing) {
            Vec3 position = rawPosition.add(anchorLag);
            return new CameraTransform(position, rawYRot, rawXRot, rawRoll, anchorLag.lengthSqr() > 1.0E-8D);
        }

        if (FragmentCameraConfig.EXPERIMENTAL_THIRD_PERSON_RIG_ENABLED.get()) {
            return thirdPersonRig.update(level, cameraEntity, stableAnchor, rawPosition, rawYRot, rawXRot, rawRoll, anchorLag, partialTick);
        }

        rawCameraOffset = applyPehkuiThirdPersonScale(rawCameraOffset, cameraEntity, partialTick);
        Vec3 position = stableAnchor.add(rawCameraOffset).add(anchorLag);
        return new CameraTransform(position, rawYRot, rawXRot, rawRoll, anchorLag.lengthSqr() > 1.0E-8D || stableAnchor.distanceToSqr(vanillaEye) > 1.0E-8D);
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
        thirdPersonVisualY = anchor.y;
        influence = 0.0D;
        yawSpring.reset(continuousYaw);
        pitchSpring.reset(rawXRot);
        anchorXZSpring.reset(new Vec3(anchor.x, 0.0D, anchor.z));
        thirdPersonBypassedLastFrame = false;
        shoulderSurfingOffsetHandledThisFrame = false;
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

    private Vec3 updateThirdPersonAnchorLag(Vec3 stableAnchor, double deltaSeconds) {
        Vec3 smoothedAnchorXZ = anchorXZSpring.update(
                new Vec3(stableAnchor.x, 0.0D, stableAnchor.z),
                deltaSeconds,
                FragmentCameraConfig.THIRD_PERSON_POSITION_FREQUENCY.get(),
                FragmentCameraConfig.THIRD_PERSON_POSITION_DAMPING.get());
        thirdPersonVisualY = approachExp(
                thirdPersonVisualY,
                stableAnchor.y,
                FragmentCameraConfig.THIRD_PERSON_VERTICAL_RESPONSE.get(),
                deltaSeconds);

        if (Math.abs(thirdPersonVisualY - stableAnchor.y) < FragmentCameraConfig.THIRD_PERSON_VERTICAL_SNAP_THRESHOLD.get()) {
            thirdPersonVisualY = stableAnchor.y;
        }

        Vec3 smoothedAnchor = new Vec3(smoothedAnchorXZ.x, thirdPersonVisualY, smoothedAnchorXZ.z);
        return limitLength(smoothedAnchor.subtract(stableAnchor), FragmentCameraConfig.MAX_LAG_DISTANCE.get());
    }

    private static Vec3 worldLagToShoulderOffset(Camera camera, Vec3 worldLag) {
        Vec3 left = vectorToVec3(camera.getLeftVector());
        Vec3 up = vectorToVec3(camera.getUpVector());
        Vec3 look = vectorToVec3(camera.getLookVector());
        return new Vec3(worldLag.dot(left), worldLag.dot(up), -worldLag.dot(look));
    }

    private static Vec3 vectorToVec3(Vector3f vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    private static Vec3 getStableAnchor(Entity entity, float partialTick) {
        if (FragmentCameraConfig.IGNORE_CROUCH_HEIGHT_IN_THIRD_PERSON.get() && entity instanceof Player player) {
            Vec3 position = player.getPosition(partialTick);
            double standingEyeHeight = player.getDimensions(Pose.STANDING).eyeHeight();

            if (Math.abs(PehkuiCompat.getEyeHeightScale(player, partialTick) - 1.0F) > 0.0001F) {
                standingEyeHeight = player.getDefaultDimensions(Pose.STANDING).eyeHeight() * PehkuiCompat.getEyeHeightScale(player, partialTick);
            }

            return position.add(0.0D, standingEyeHeight, 0.0D);
        }

        return entity.getEyePosition(partialTick);
    }

    private static boolean shouldBypassThirdPerson(Entity entity, Minecraft minecraft) {
        return entity instanceof Player player
                && minecraft.player == player
                && (player.isSpectator()
                || player.noPhysics
                || player.isPassenger()
                || player.isSwimming());
    }

    private static Vec3 applyPehkuiThirdPersonScale(Vec3 rawCameraOffset, Entity entity, float partialTick) {
        float scale = PehkuiCompat.getThirdPersonScale(entity, partialTick);

        if (Math.abs(scale - 1.0F) < 0.0001F) {
            return rawCameraOffset;
        }

        return rawCameraOffset.scale(scale);
    }

    private static boolean hasMeaningfulOffset(double yawOffset, double pitchOffset, double yOffset) {
        return Math.abs(yawOffset) > 1.0E-4D || Math.abs(pitchOffset) > 1.0E-4D || Math.abs(yOffset) > 1.0E-5D;
    }
}
