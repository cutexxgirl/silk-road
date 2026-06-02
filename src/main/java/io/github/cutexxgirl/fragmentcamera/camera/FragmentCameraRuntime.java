package io.github.cutexxgirl.fragmentcamera.camera;

import java.util.UUID;

import io.github.cutexxgirl.fragmentcamera.FragmentCameraConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FragmentCameraRuntime {
    public static final FragmentCameraRuntime INSTANCE = new FragmentCameraRuntime();

    private final SpringScalar yawSpring = new SpringScalar();
    private final SpringScalar pitchSpring = new SpringScalar();
    private final SpringScalar ySpring = new SpringScalar();
    private final SpringVec3 positionSpring = new SpringVec3();

    private BlockGetter lastLevel;
    private UUID lastEntityId;
    private CameraType lastCameraType;
    private Vec3 lastRawPosition;
    private float lastRawYaw;
    private double continuousYaw;
    private double influence;
    private long lastFrameNanos;
    private boolean initialized;

    private FragmentCameraRuntime() {
    }

    public CameraTransform update(Camera camera, BlockGetter level, Entity cameraEntity) {
        Vec3 rawPosition = camera.getPosition();
        float rawYRot = camera.getYRot();
        float rawXRot = camera.getXRot();
        float rawRoll = camera.getRoll();

        if (!FragmentCameraConfig.ENABLED.get() || cameraEntity == null || Minecraft.getInstance().isPaused()) {
            reset(level, cameraEntity, rawPosition, rawYRot, rawXRot);
            return CameraTransform.unchanged(rawPosition, rawYRot, rawXRot, rawRoll);
        }

        Minecraft minecraft = Minecraft.getInstance();
        CameraType cameraType = minecraft.options.getCameraType();
        boolean firstPerson = cameraType.isFirstPerson();
        double deltaSeconds = frameDeltaSeconds();

        if (shouldReset(level, cameraEntity, cameraType, rawPosition)) {
            reset(level, cameraEntity, rawPosition, rawYRot, rawXRot);
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
                : updateThirdPerson(camera, level, cameraEntity, rawPosition, rawYRot, rawXRot, rawRoll, deltaSeconds);

        lastRawPosition = rawPosition;
        lastCameraType = cameraType;
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
        double smoothedY = ySpring.update(
                rawPosition.y,
                deltaSeconds,
                FragmentCameraConfig.FIRST_PERSON_VERTICAL_FREQUENCY.get(),
                FragmentCameraConfig.FIRST_PERSON_VERTICAL_DAMPING.get());

        double yawOffset = FragmentCameraConfig.FIRST_PERSON_ROTATION_ENABLED.get() ? (smoothedYaw - continuousYaw) * influence : 0.0D;
        double pitchOffset = FragmentCameraConfig.FIRST_PERSON_ROTATION_ENABLED.get() ? (smoothedPitch - rawXRot) * influence : 0.0D;
        double yOffset = FragmentCameraConfig.FIRST_PERSON_VERTICAL_ENABLED.get() ? (smoothedY - rawPosition.y) * influence : 0.0D;
        Vec3 position = rawPosition.add(0.0D, yOffset, 0.0D);
        return new CameraTransform(position, (float) (rawYRot + yawOffset), (float) (rawXRot + pitchOffset), rawRoll, hasMeaningfulOffset(yawOffset, pitchOffset, yOffset));
    }

    private CameraTransform updateThirdPerson(Camera camera, BlockGetter level, Entity cameraEntity, Vec3 rawPosition, float rawYRot, float rawXRot, float rawRoll, double deltaSeconds) {
        Vec3 smoothedPosition = positionSpring.update(
                rawPosition,
                deltaSeconds,
                FragmentCameraConfig.THIRD_PERSON_POSITION_FREQUENCY.get(),
                FragmentCameraConfig.THIRD_PERSON_POSITION_DAMPING.get());
        Vec3 offset = limitLength(smoothedPosition.subtract(rawPosition), FragmentCameraConfig.MAX_LAG_DISTANCE.get()).scale(influence);
        Vec3 position = clampToLineOfSight(level, cameraEntity, rawPosition.add(offset), camera.getPartialTickTime());
        return new CameraTransform(position, rawYRot, rawXRot, rawRoll, offset.lengthSqr() > 1.0E-8D);
    }

    private boolean shouldReset(BlockGetter level, Entity entity, CameraType cameraType, Vec3 rawPosition) {
        return !initialized
                || lastLevel != level
                || lastEntityId == null
                || !lastEntityId.equals(entity.getUUID())
                || lastCameraType != cameraType
                || !entity.isAlive()
                || lastRawPosition == null
                || lastRawPosition.distanceTo(rawPosition) > FragmentCameraConfig.RESET_DISTANCE.get();
    }

    private void reset(BlockGetter level, Entity entity, Vec3 rawPosition, float rawYRot, float rawXRot) {
        lastLevel = level;
        lastEntityId = entity == null ? null : entity.getUUID();
        lastCameraType = Minecraft.getInstance().options.getCameraType();
        lastRawPosition = rawPosition;
        lastRawYaw = rawYRot;
        continuousYaw = rawYRot;
        influence = 0.0D;
        yawSpring.reset(continuousYaw);
        pitchSpring.reset(rawXRot);
        ySpring.reset(rawPosition.y);
        positionSpring.reset(rawPosition);
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
        return Mth.clamp(deltaSeconds, 1.0D / 240.0D, 1.0D / 20.0D);
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

    private static Vec3 clampToLineOfSight(BlockGetter level, Entity entity, Vec3 proposedPosition, float partialTick) {
        Vec3 eyePosition = entity.getEyePosition(partialTick);
        ClipContext context = new ClipContext(eyePosition, proposedPosition, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity);
        HitResult hitResult = level.clip(context);

        if (hitResult.getType() == HitResult.Type.MISS) {
            return proposedPosition;
        }

        Vec3 direction = proposedPosition.subtract(eyePosition);

        if (direction.lengthSqr() < 1.0E-6D) {
            return proposedPosition;
        }

        return hitResult.getLocation().subtract(direction.normalize().scale(0.1D));
    }

    private static boolean hasMeaningfulOffset(double yawOffset, double pitchOffset, double yOffset) {
        return Math.abs(yawOffset) > 1.0E-4D || Math.abs(pitchOffset) > 1.0E-4D || Math.abs(yOffset) > 1.0E-5D;
    }
}
