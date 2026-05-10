package com.moulberry.flashback.playback;

import com.moulberry.flashback.Flashback;
import net.minecraft.client.Minecraft;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.Entity;

public class TickRateManager {
    public static final float MIN_TICKRATE = 1.0F;

    private static volatile float tickrate = 20.0F;
    static volatile long nanosecondsPerTick = TimeUtil.NANOSECONDS_PER_SECOND / 20L;
    static volatile boolean isFrozen = false;

    private boolean runGameElements = true;
    private final boolean isServerTickRateManager;

    public TickRateManager(boolean isServerTickRateManager) {
        this.isServerTickRateManager = isServerTickRateManager;
        if (this.isServerTickRateManager) {
            setTickRate(20.0f);
            setFrozen(false);
        }
    }

    public static void setTickRate(float tickRate) {
        TickRateManager.tickrate = Math.max(tickRate, MIN_TICKRATE);
        TickRateManager.nanosecondsPerTick = (long) ((double) TimeUtil.NANOSECONDS_PER_SECOND / (double) TickRateManager.tickrate);
    }

    public static void setFrozen(boolean frozen) {
        TickRateManager.isFrozen = frozen;
    }

    public float tickrate() {
        return tickrate;
    }

    public float millisecondsPerTick() {
        return (float) nanosecondsPerTick / (float) TimeUtil.NANOSECONDS_PER_MILLISECOND;
    }

    public boolean runsNormally() {
        return this.runGameElements;
    }

    public void tick() {
        this.runGameElements = !isFrozen && !this.isServerTickRateManager;
    }

    public boolean isEntityFrozen(Entity entity) {
        if (this.isServerTickRateManager) {
            return !(entity instanceof ReplayPlayer);
        }
        if (Flashback.isExporting()) {
            return false;
        }
        return !this.runsNormally() || entity == Minecraft.getInstance().player;
    }
}