package io.github.cutexxgirl.fragmentcamera;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(FragmentCamera.MOD_ID)
public final class FragmentCamera {
    public static final String MOD_ID = "fragmentcamera";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FragmentCamera(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, FragmentCameraConfig.SPEC);
        LOGGER.info("FragmentCamera loaded");
    }
}
