package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public class FlashbackClearParticles implements FabricPacket {
    public static final PacketType<FlashbackClearParticles> TYPE = PacketType.create(Flashback.createResourceLocation("clear_particles"), FlashbackClearParticles::new);
    public static final FlashbackClearParticles INSTANCE = new FlashbackClearParticles();

    public FlashbackClearParticles() {
    }

    public FlashbackClearParticles(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
