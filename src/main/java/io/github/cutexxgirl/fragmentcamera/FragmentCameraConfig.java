package io.github.cutexxgirl.fragmentcamera;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FragmentCameraConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable camera movement smoothing.")
            .define("enabled", true);

    public static final ModConfigSpec.BooleanValue FIRST_PERSON_ROTATION_ENABLED = BUILDER
            .comment("Enable visual first-person yaw and pitch smoothing. This does not change player rotation.")
            .define("firstPersonRotationEnabled", true);

    public static final ModConfigSpec.BooleanValue FIRST_PERSON_VERTICAL_ENABLED = BUILDER
            .comment("Enable first-person vertical spring movement for jumps and falls.")
            .define("firstPersonVerticalEnabled", true);

    public static final ModConfigSpec.BooleanValue THIRD_PERSON_ENABLED = BUILDER
            .comment("Enable third-person camera position lag.")
            .define("thirdPersonEnabled", true);

    public static final ModConfigSpec.BooleanValue DISABLE_WHILE_AIMING = BUILDER
            .comment("Smoothly disable FragmentCamera offsets while aiming.")
            .define("disableWhileAiming", true);

    public static final ModConfigSpec.DoubleValue AIM_BLEND_OUT_SPEED = BUILDER
            .comment("How quickly camera offsets fade out while aiming.")
            .defineInRange("aimBlendOutSpeed", 16.0D, 1.0D, 60.0D);

    public static final ModConfigSpec.DoubleValue FIRST_PERSON_ROTATION_FREQUENCY = BUILDER
            .comment("First-person rotation spring frequency. Higher values are more responsive.")
            .defineInRange("firstPersonRotationFrequency", 7.0D, 0.1D, 30.0D);

    public static final ModConfigSpec.DoubleValue FIRST_PERSON_ROTATION_DAMPING = BUILDER
            .comment("First-person rotation spring damping ratio.")
            .defineInRange("firstPersonRotationDamping", 0.9D, 0.1D, 3.0D);

    public static final ModConfigSpec.DoubleValue FIRST_PERSON_VERTICAL_FREQUENCY = BUILDER
            .comment("First-person vertical spring frequency. Lower values give a heavier jump spring.")
            .defineInRange("firstPersonVerticalFrequency", 6.0D, 0.1D, 30.0D);

    public static final ModConfigSpec.DoubleValue FIRST_PERSON_VERTICAL_DAMPING = BUILDER
            .comment("First-person vertical spring damping ratio.")
            .defineInRange("firstPersonVerticalDamping", 0.75D, 0.1D, 3.0D);

    public static final ModConfigSpec.DoubleValue THIRD_PERSON_POSITION_FREQUENCY = BUILDER
            .comment("Third-person position spring frequency. Lower values make the camera lag farther behind.")
            .defineInRange("thirdPersonPositionFrequency", 4.0D, 0.1D, 30.0D);

    public static final ModConfigSpec.DoubleValue THIRD_PERSON_POSITION_DAMPING = BUILDER
            .comment("Third-person position spring damping ratio.")
            .defineInRange("thirdPersonPositionDamping", 0.85D, 0.1D, 3.0D);

    public static final ModConfigSpec.DoubleValue MAX_LAG_DISTANCE = BUILDER
            .comment("Maximum distance the camera spring can lag from the target position.")
            .defineInRange("maxLagDistance", 2.5D, 0.0D, 16.0D);

    public static final ModConfigSpec.DoubleValue RESET_DISTANCE = BUILDER
            .comment("Reset springs when the target camera position jumps farther than this many blocks.")
            .defineInRange("resetDistance", 8.0D, 1.0D, 128.0D);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> AIMING_ITEM_NAMESPACES = BUILDER
            .comment("Item namespaces treated as aiming-capable while the use key is held.")
            .defineListAllowEmpty("aimingItemNamespaces", List.of("irons_spellbooks", "epicfight"), () -> "", FragmentCameraConfig::isString);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> AIMING_ITEM_IDS = BUILDER
            .comment("Full item ids treated as aiming-capable while the use key is held.")
            .defineListAllowEmpty("aimingItemIds", List.of(), () -> "", FragmentCameraConfig::isString);

    static final ModConfigSpec SPEC = BUILDER.build();

    private FragmentCameraConfig() {
    }

    private static boolean isString(Object value) {
        return value instanceof String;
    }
}
