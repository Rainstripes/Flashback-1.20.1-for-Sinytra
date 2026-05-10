package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public class FlashbackForceClientTick implements FabricPacket {
    public static final PacketType<FlashbackForceClientTick> TYPE = PacketType.create(Flashback.createResourceLocation("force_client_tick"), FlashbackForceClientTick::new);
    public static final FlashbackForceClientTick INSTANCE = new FlashbackForceClientTick();

    public FlashbackForceClientTick() {
    }

    public FlashbackForceClientTick(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
