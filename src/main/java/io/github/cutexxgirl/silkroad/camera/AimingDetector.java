package io.github.cutexxgirl.silkroad.camera;

import io.github.cutexxgirl.silkroad.SilkroadConfig;
import io.github.cutexxgirl.silkroad.compat.ShoulderSurfingCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AimingDetector {
    private AimingDetector() {
    }

    public static State getState(Minecraft minecraft) {
        ShoulderSurfingCompat.State shoulderSurfing = ShoulderSurfingCompat.getState();
        Player player = minecraft.player;
        boolean fallbackAiming = player != null && isFallbackAiming(minecraft, player);
        return new State(shoulderSurfing.aiming() || fallbackAiming, shoulderSurfing.shoulderSurfing(), shoulderSurfing.freeLooking());
    }

    private static boolean isFallbackAiming(Minecraft minecraft, Player player) {
        if (player.isScoping()) {
            return true;
        }

        ItemStack useStack = player.getUseItem();

        if (player.isUsingItem() && !useStack.isEmpty() && !useStack.has(DataComponents.FOOD)) {
            return true;
        }

        if (minecraft.options.keyUse.isDown()) {
            return isAimingCapable(player.getMainHandItem()) || isAimingCapable(player.getOffhandItem());
        }

        return false;
    }

    private static boolean isAimingCapable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.is(Items.TRIDENT) || stack.is(Items.SPYGLASS)) {
            return true;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemId = id.toString();
        String namespace = id.getNamespace();
        return SilkroadConfig.AIMING_ITEM_IDS.get().contains(itemId)
                || SilkroadConfig.AIMING_ITEM_NAMESPACES.get().contains(namespace);
    }

    public record State(boolean aiming, boolean shoulderSurfing, boolean freeLooking) {
    }
}
