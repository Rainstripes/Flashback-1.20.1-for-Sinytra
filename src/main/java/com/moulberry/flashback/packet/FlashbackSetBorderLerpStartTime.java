package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public record FlashbackSetBorderLerpStartTime(long time) implements FabricPacket {
    public static final PacketType<FlashbackSetBorderLerpStartTime> TYPE = PacketType.create(Flashback.createResourceLocation("set_border_lerp_start_time"), FlashbackSetBorderLerpStartTime::new);

    public FlashbackSetBorderLerpStartTime(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readLong());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.time);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
