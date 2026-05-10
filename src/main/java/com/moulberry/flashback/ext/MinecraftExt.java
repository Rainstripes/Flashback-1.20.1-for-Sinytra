package com.moulberry.flashback.ext;

import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import com.moulberry.flashback.playback.ReplayTimer;

import java.nio.file.Path;
import java.util.UUID;

public interface MinecraftExt {

    record StartReplayServerInfo(UUID playbackUUID, Path path) {}

    void flashback$applyKeyframes();
    void flashback$startReplayServer(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem stem, StartReplayServerInfo info);
    ReplayTimer flashback$getReplayTimer();
    float flashback$getLocalPlayerPartialTick(float originalPartialTick);
    boolean flashback$overridingLocalPlayerTimer();

}
