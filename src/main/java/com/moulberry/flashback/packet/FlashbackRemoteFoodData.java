package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public record FlashbackRemoteFoodData(int entityId, int foodLevel, float saturationLevel) implements FabricPacket {
    public static final PacketType<FlashbackRemoteFoodData> TYPE = PacketType.create(Flashback.createResourceLocation("remote_food_data"), FlashbackRemoteFoodData::new);

    public FlashbackRemoteFoodData(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readVarInt(), friendlyByteBuf.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeVarInt(this.foodLevel);
        friendlyByteBuf.writeFloat(this.saturationLevel);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
