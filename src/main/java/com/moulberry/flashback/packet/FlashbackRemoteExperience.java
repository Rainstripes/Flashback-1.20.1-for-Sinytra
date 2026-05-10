package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public record FlashbackRemoteExperience(int entityId, float experienceProgress, int totalExperience, int experienceLevel) implements FabricPacket {
    public static final PacketType<FlashbackRemoteExperience> TYPE = PacketType.create(Flashback.createResourceLocation("remote_experience"), FlashbackRemoteExperience::new);

    public FlashbackRemoteExperience(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readFloat(), friendlyByteBuf.readVarInt(), friendlyByteBuf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeFloat(this.experienceProgress);
        friendlyByteBuf.writeVarInt(this.totalExperience);
        friendlyByteBuf.writeVarInt(this.experienceLevel);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
