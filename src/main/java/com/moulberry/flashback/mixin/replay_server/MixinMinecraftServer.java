package com.moulberry.flashback.mixin.replay_server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @WrapOperation(method = "stopServer", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"))
    private boolean stopServer_skipReplayChunkDrain(Stream<?> instance, Predicate<Object> predicate, Operation<Boolean> original) {
        if ((Object) this instanceof ReplayServer) {
            return false;
        }
        return original.call(instance, predicate);
    }

}
