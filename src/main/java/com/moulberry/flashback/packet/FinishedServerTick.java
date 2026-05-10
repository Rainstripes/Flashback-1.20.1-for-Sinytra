package com.moulberry.flashback.packet;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public class FinishedServerTick implements FabricPacket {
    public static final PacketType<FinishedServerTick> TYPE = PacketType.create(Flashback.createResourceLocation("finished_server_tick"), FinishedServerTick::new);
    public static final FinishedServerTick INSTANCE = new FinishedServerTick();

    private FinishedServerTick() {
    }

    public FinishedServerTick(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
