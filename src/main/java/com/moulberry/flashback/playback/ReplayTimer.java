package com.moulberry.flashback.playback;

import net.minecraft.client.Timer;

public class ReplayTimer extends Timer {
    public final TickRateManager manager;

    public ReplayTimer(long lastMs, TickRateManager manager) {
        super(manager.tickrate(), lastMs);
        this.manager = manager;
    }

    @Override
    public int advanceTime(long currentMs) {
        this.msPerTick = manager.millisecondsPerTick();
        return super.advanceTime(currentMs);
    }

}
