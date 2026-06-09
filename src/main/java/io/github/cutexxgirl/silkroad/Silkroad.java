package io.github.cutexxgirl.silkroad;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Silkroad.MOD_ID)
public final class Silkroad {
    public static final String MOD_ID = "silkroad";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Silkroad(IEventBus modEventBus, ModContainer modContainer) {
        SilkroadConfig.register();
        LOGGER.info("Silkroad loaded");
    }
}
