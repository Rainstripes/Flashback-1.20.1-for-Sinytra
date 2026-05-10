package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public class FlashbackClearEntities implements FabricPacket {
    public static final PacketType<FlashbackClearEntities> TYPE = PacketType.create(Flashback.createResourceLocation("clear_entities"), FlashbackClearEntities::new);
    public static final FlashbackClearEntities INSTANCE = new FlashbackClearEntities();

    public FlashbackClearEntities() {
    }

    public FlashbackClearEntities(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
