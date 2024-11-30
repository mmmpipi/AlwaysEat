package com.suppergerrie2.alwayseat.alwayseat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Objects;

@Mod(AlwaysEat.MOD_ID)
public class AlwaysEat {

    public static final String MOD_ID = "salwayseat";
    private static final String PROTOCOL_VERSION = "1";

    public AlwaysEat(ModContainer container, IEventBus busEvent) {

//        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> ServerEvents::new);
        NeoForge.EVENT_BUS.addListener(AlwaysEat::rightClickItemEvent);
        busEvent.addListener(AlwaysEat::register);
        busEvent.addListener(AlwaysEat::configReloadEvent);
        container.registerConfig(ModConfig.Type.SERVER,Config.CONFIG_SPEC);

    }


    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                SyncSettings.TYPE,
                SyncSettings.STREAM_CODEC,
                SyncSettings::handle
        );
    }

    @SubscribeEvent
    public static void configReloadEvent(ModConfigEvent.Reloading event){
        PacketDistributor.sendToAllPlayers(SyncSettings.fromConfig());
    }

    @SubscribeEvent
    public static void rightClickItemEvent(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemstack = event.getItemStack();
        if(itemstack.get(DataComponents.FOOD) == null) return;
        //if(!itemstack.isEdible()) return;

        Player player = event.getEntity();

        if(player.canEat(AlwaysEat.canEatItemWhenFull(itemstack, player))) {
            player.startUsingItem(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
        } else {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    public static boolean canEatItemWhenFull(ItemStack item, LivingEntity livingEntity) {

        String registryName = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.getItem())).toString();

        // If an item is in the uneatable items list always set it to false
        if (Config.UNEATABLE_ITEMS.get().contains(registryName)) {
            return false;
        }

        // In blacklist mode all items except the ones in the list will be set to true
        if (Config.MODE.get() == Config.Mode.BLACKLIST) {
            if (!Config.ITEM_LIST.get().contains(registryName)) {
                return true;
            } else {
                return item.getFoodProperties(livingEntity).canAlwaysEat();
            }
        } else {
            // In whitelist mode only items in the list will be set to true
            if (Config.ITEM_LIST.get().contains(registryName)) {
                return true;
            } else {
                return item.getFoodProperties(livingEntity).canAlwaysEat();
            }
        }
    }


}
