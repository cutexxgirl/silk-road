package io.github.cutexxgirl.fragmentcamera.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import io.github.cutexxgirl.fragmentcamera.FragmentCamera;
import io.github.cutexxgirl.fragmentcamera.FragmentCameraConfig;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

public final class PehkuiCompat {
    private static boolean attempted;
    private static MethodHandle thirdPersonScaleHandle;
    private static MethodHandle eyeHeightScaleHandle;

    private PehkuiCompat() {
    }

    public static float getThirdPersonScale(Entity entity, float partialTick) {
        if (!FragmentCameraConfig.PEHKUI_COMPAT_ENABLED.get() || !ModList.get().isLoaded("pehkui")) {
            return 1.0F;
        }

        initialize();
        return invokeScale(thirdPersonScaleHandle, entity, partialTick);
    }

    public static float getEyeHeightScale(Entity entity, float partialTick) {
        if (!FragmentCameraConfig.PEHKUI_COMPAT_ENABLED.get() || !ModList.get().isLoaded("pehkui")) {
            return 1.0F;
        }

        initialize();
        return invokeScale(eyeHeightScaleHandle, entity, partialTick);
    }

    private static void initialize() {
        if (attempted) {
            return;
        }

        attempted = true;

        try {
            Class<?> scaleUtils = Class.forName("virtuoel.pehkui.util.ScaleUtils");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            thirdPersonScaleHandle = lookup.unreflect(scaleUtils.getMethod("getThirdPersonScale", Entity.class, float.class));
            eyeHeightScaleHandle = lookup.unreflect(scaleUtils.getMethod("getEyeHeightScale", Entity.class, float.class));
        } catch (ReflectiveOperationException | LinkageError exception) {
            FragmentCamera.LOGGER.debug("Pehkui API is not available for FragmentCamera reflection", exception);
            thirdPersonScaleHandle = null;
            eyeHeightScaleHandle = null;
        }
    }

    private static float invokeScale(MethodHandle handle, Entity entity, float partialTick) {
        if (handle == null) {
            return 1.0F;
        }

        try {
            Object result = handle.invoke(entity, partialTick);

            if (result instanceof Float scale && Float.isFinite(scale) && scale > 0.0F) {
                return scale;
            }
        } catch (Throwable exception) {
            FragmentCamera.LOGGER.debug("Failed to read Pehkui scale", exception);
        }

        return 1.0F;
    }
}
