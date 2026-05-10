package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public record FlashbackRemoteSelectHotbarSlot(int entityId, int slot) implements FabricPacket {
    public static final PacketType<FlashbackRemoteSelectHotbarSlot> TYPE = PacketType.create(Flashback.createResourceLocation("remote_select_hotbar_slot"), FlashbackRemoteSelectHotbarSlot::new);

    public FlashbackRemoteSelectHotbarSlot(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readByte());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeByte(this.slot);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
