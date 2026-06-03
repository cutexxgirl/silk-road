package io.github.cutexxgirl.fragmentcamera.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import io.github.cutexxgirl.fragmentcamera.FragmentCamera;
import net.neoforged.fml.ModList;

public final class ShoulderSurfingCompat {
    private static boolean attempted;
    private static Object instance;
    private static MethodHandle isAimingHandle;
    private static MethodHandle isShoulderSurfingHandle;
    private static MethodHandle isFreeLookingHandle;

    private ShoulderSurfingCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("shouldersurfing");
    }

    public static State getState() {
        if (!isLoaded()) {
            return State.EMPTY;
        }

        initialize();

        if (instance == null) {
            return State.EMPTY;
        }

        try {
            return new State(
                    invokeBoolean(isAimingHandle),
                    invokeBoolean(isShoulderSurfingHandle),
                    invokeBoolean(isFreeLookingHandle));
        } catch (Throwable exception) {
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
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            Method getInstanceMethod = shoulderSurfing.getMethod("getInstance");
            instance = getInstanceMethod.invoke(null);
            Class<?> instanceClass = instance.getClass();
            isAimingHandle = lookup.unreflect(instanceClass.getMethod("isAiming")).bindTo(instance);
            isShoulderSurfingHandle = lookup.unreflect(instanceClass.getMethod("isShoulderSurfing")).bindTo(instance);
            isFreeLookingHandle = lookup.unreflect(instanceClass.getMethod("isFreeLooking")).bindTo(instance);
        } catch (ReflectiveOperationException | LinkageError exception) {
            FragmentCamera.LOGGER.debug("Shoulder Surfing API is not available for FragmentCamera reflection", exception);
            instance = null;
            isAimingHandle = null;
            isShoulderSurfingHandle = null;
            isFreeLookingHandle = null;
        }
    }

    private static boolean invokeBoolean(MethodHandle methodHandle) throws Throwable {
        return methodHandle != null && Boolean.TRUE.equals((Boolean) methodHandle.invoke());
    }

    public record State(boolean aiming, boolean shoulderSurfing, boolean freeLooking) {
        public static final State EMPTY = new State(false, false, false);
    }
}
