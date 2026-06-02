package io.github.cutexxgirl.fragmentcamera;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FragmentCameraConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable camera movement smoothing.")
            .define("enabled", true);

    public static final ModConfigSpec.DoubleValue SMOOTHING_STRENGTH = BUILDER
            .comment("Camera smoothing strength. Higher values should feel smoother but less responsive.")
            .defineInRange("smoothingStrength", 0.65D, 0.0D, 1.0D);

    static final ModConfigSpec SPEC = BUILDER.build();

    private FragmentCameraConfig() {
    }
}
