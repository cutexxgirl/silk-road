package io.github.cutexxgirl.silkroad;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = Silkroad.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Silkroad.MOD_ID, value = Dist.CLIENT)
public final class SilkroadClient {
    public SilkroadClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Silkroad.LOGGER.info("Silkroad client setup complete");
    }
}
