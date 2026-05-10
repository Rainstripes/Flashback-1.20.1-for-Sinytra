package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public record FlashbackRawCustomPayload(byte[] packetBytes, boolean configPhase) implements FabricPacket {
    public static final PacketType<FlashbackRawCustomPayload> TYPE = PacketType.create(Flashback.createResourceLocation("raw_custom_payload"), FlashbackRawCustomPayload::new);

    public FlashbackRawCustomPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readByteArray(), friendlyByteBuf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByteArray(this.packetBytes());
        friendlyByteBuf.writeBoolean(this.configPhase());
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
