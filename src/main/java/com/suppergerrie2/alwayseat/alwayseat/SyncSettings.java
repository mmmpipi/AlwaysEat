package com.suppergerrie2.alwayseat.alwayseat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record SyncSettings(Config.Mode mode, List<String> itemList, List<String> uneatableList) implements CustomPacketPayload {



    public static final CustomPacketPayload.Type<SyncSettings> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AlwaysEat.MOD_ID, "sync_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncSettings> STREAM_CODEC = StreamCodec.ofMember(
            SyncSettings::encode,SyncSettings::decode
    );

    public static void encode(SyncSettings msg, FriendlyByteBuf friendlyByteBuf) {

        friendlyByteBuf.writeEnum(msg.mode);
        friendlyByteBuf.writeCollection(msg.itemList, FriendlyByteBuf::writeUtf);
        friendlyByteBuf.writeCollection(msg.uneatableList, FriendlyByteBuf::writeUtf);
    }

    public static SyncSettings decode(FriendlyByteBuf friendlyByteBuf) {
        Config.Mode mode = friendlyByteBuf.readEnum(Config.Mode.class);
        List<String> itemList = friendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        List<String> uneatableList = friendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        return new SyncSettings(mode, itemList, uneatableList);
    }

    public static void handle(SyncSettings msg, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Config.MODE.set(msg.mode);
            Config.ITEM_LIST.set(msg.itemList);
            Config.UNEATABLE_ITEMS.set(msg.uneatableList);
        });
    }

    public static SyncSettings fromConfig() {
        return new SyncSettings(Config.MODE.get(),
                asStrings(Config.ITEM_LIST.get()),
                asStrings(Config.UNEATABLE_ITEMS.get()));
    }

    private static List<String> asStrings(List<?> values) {
        return values.stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return SyncSettings.TYPE;
    }
}
