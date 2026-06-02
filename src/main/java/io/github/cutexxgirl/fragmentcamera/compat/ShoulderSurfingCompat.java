package io.github.cutexxgirl.fragmentcamera.compat;

import java.lang.reflect.Method;

import io.github.cutexxgirl.fragmentcamera.FragmentCamera;
import net.neoforged.fml.ModList;

public final class ShoulderSurfingCompat {
    private static boolean attempted;
    private static Method getInstanceMethod;
    private static Method isAimingMethod;
    private static Method isShoulderSurfingMethod;
    private static Method isFreeLookingMethod;

    private ShoulderSurfingCompat() {
    }

    public static State getState() {
        if (!ModList.get().isLoaded("shouldersurfing")) {
            return State.EMPTY;
        }

        initialize();

        if (getInstanceMethod == null) {
            return State.EMPTY;
        }

        try {
            Object instance = getInstanceMethod.invoke(null);
            return new State(
                    invokeBoolean(instance, isAimingMethod),
                    invokeBoolean(instance, isShoulderSurfingMethod),
                    invokeBoolean(instance, isFreeLookingMethod));
        } catch (ReflectiveOperationException | LinkageError exception) {
            FragmentCamera.LOGGER.debug("Failed to read Shoulder Surfing state", exception);
            return State.EMPTY;
        }
    }

    private static void initialize() {
        if (attempted) {
            return;
        }

        attempted = true;

        try {
            Class<?> shoulderSurfing = Class.forName("com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing");
            getInstanceMethod = shoulderSurfing.getMethod("getInstance");
            Object instance = getInstanceMethod.invoke(null);
            Class<?> instanceClass = instance.getClass();
            isAimingMethod = instanceClass.getMethod("isAiming");
            isShoulderSurfingMethod = instanceClass.getMethod("isShoulderSurfing");
            isFreeLookingMethod = instanceClass.getMethod("isFreeLooking");
        } catch (ReflectiveOperationException | LinkageError exception) {
            FragmentCamera.LOGGER.debug("Shoulder Surfing API is not available for FragmentCamera reflection", exception);
            getInstanceMethod = null;
            isAimingMethod = null;
            isShoulderSurfingMethod = null;
            isFreeLookingMethod = null;
        }
    }

    private static boolean invokeBoolean(Object instance, Method method) throws ReflectiveOperationException {
        return method != null && Boolean.TRUE.equals(method.invoke(instance));
    }

    public record State(boolean aiming, boolean shoulderSurfing, boolean freeLooking) {
        public static final State EMPTY = new State(false, false, false);
    }
}
