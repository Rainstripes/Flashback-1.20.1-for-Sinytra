package com.moulberry.flashback.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.combo_options.GlowingOverride;
import com.moulberry.flashback.configuration.FlashbackConfigV1;
import com.moulberry.flashback.exporting.ExportJob;
import com.moulberry.flashback.exporting.ExportJobQueue;
import com.moulberry.flashback.keyframe.handler.MinecraftKeyframeHandler;
import com.moulberry.flashback.playback.ReplayTimer;
import com.moulberry.flashback.playback.TickRateManager;
import com.moulberry.flashback.sound.FlashbackAudioManager;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import com.moulberry.flashback.exporting.PerfectFrames;
import com.moulberry.flashback.playback.ReplayServer;
import com.moulberry.flashback.ext.MinecraftExt;
import com.moulberry.flashback.editor.ui.ReplayUI;
import com.moulberry.flashback.visuals.AccurateEntityPositionHandler;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.Connection;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements MinecraftExt {
    @Shadow
    @Final
    private AtomicReference<StoringChunkProgressListener> progressListener;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Shadow
    @Final
    private Queue<Runnable> progressTasks;

    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public Entity cameraEntity;

    @Shadow
    @Final
    public Timer timer;

    @Shadow @Final public Options options;

    @Shadow @Final public LevelRenderer levelRenderer;

    @Shadow
    public abstract void doWorldLoad(String string, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, boolean bl);

    @Inject(method="<init>", at=@At("RETURN"))
    public void init(GameConfig gameConfig, CallbackInfo ci) {
        ReplayUI.init();
    }

    @Inject(method = "pauseGame", at = @At("HEAD"), cancellable = true)
    public void pauseGame(boolean bl, CallbackInfo ci) {
        if (Flashback.EXPORT_JOB != null) {
            ci.cancel();
        }
    }

    @Inject(method = "renderNames", at = @At("HEAD"), cancellable = true)
    private static void renderNames(CallbackInfoReturnable<Boolean> cir) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null && !editorState.replayVisuals.renderNametags) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            GlowingOverride glowingOverride = editorState.glowingOverride.get(entity.getUUID());

            if (glowingOverride == GlowingOverride.FORCE_GLOW) {
                cir.setReturnValue(true);
            } else if (glowingOverride == GlowingOverride.FORCE_NO_GLOW) {
                cir.setReturnValue(false);
            }
        }
    }

    @WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;updateSource(Lnet/minecraft/client/Camera;)V"))
    public void runTick_updateSource(SoundManager instance, Camera camera, Operation<Void> original) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            Camera audioCamera = editorState.getAudioCamera();
            if (audioCamera != null) {
                original.call(instance, audioCamera);
                return;
            }
        }

        original.call(instance, camera);
    }

    @Inject(method = "runTick", at=@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(II)V", shift = At.Shift.AFTER))
    public void afterMainBlit(boolean bl, CallbackInfo ci) {
        if (!RenderSystem.isOnRenderThread()) return;
        ReplayUI.drawOverlay();
    }

    @Unique
    private boolean inReplayLast = false;

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        if (Flashback.RECORDER != null) {
            Flashback.RECORDER.endTick(false);
        }

        EditorStateManager.saveIfNeeded();

        ReplayServer replayServer = Flashback.getReplayServer();

        boolean inReplay = replayServer != null;
        if (inReplay != inReplayLast) {
            inReplayLast = inReplay;
            if (inReplay) {
                Minecraft.getInstance().options.hideGui = false;
            } else {
                EditorStateManager.reset();
            }
        }

        FlashbackConfigV1 config = Flashback.getConfig();
        if (inReplay && !config.advanced.disableThirdPersonCancel) {
            // Force camera type to first person
            if (ReplayUI.isActive() && this.player != null && this.cameraEntity == this.player && this.options.getCameraType() != CameraType.FIRST_PERSON) {
                this.options.setCameraType(CameraType.FIRST_PERSON);
                this.levelRenderer.needsUpdate();

                ReplayUI.setInfoOverlay("Forced perspective to First-Person");
            }
        }
    }

    @Unique
    private final ReplayTimer replayTimer = new ReplayTimer(0, new TickRateManager(false));

    @Override
    public ReplayTimer flashback$getReplayTimer() {
        return this.replayTimer;
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    public void disconnectHead(Screen screen, CallbackInfo ci) {
        Flashback.resetReplayCameraToPlayer();
        try {
            if (Flashback.getConfig().recordingControls.automaticallyFinish && Flashback.RECORDER != null) {
                Flashback.finishRecordingReplay();
            }
        } catch (Exception e) {
            Flashback.LOGGER.error("Failed to finish replay on disconnect", e);
        }
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"))
    public void disconnectReturn(Screen screen, CallbackInfo ci) {
        Flashback.updateIsInReplay();
    }

    @Unique
    private final Timer localPlayerTimer = new Timer(20.0f, 0);

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runAllTasks()V", shift = At.Shift.AFTER))
    public void runTick_runAllTasks(boolean runTick, CallbackInfo ci, @Local LocalIntRef i) {
        if (ExportJobQueue.drainingQueue) {
            if (ExportJobQueue.queuedJobs.isEmpty()) {
                ExportJobQueue.drainingQueue = false;
            } else if (Flashback.EXPORT_JOB == null) {
                Flashback.EXPORT_JOB = new ExportJob(ExportJobQueue.queuedJobs.remove(0));
            }
        }

        if (Flashback.EXPORT_JOB != null && !ReplayUI.isActive()) {
            try {
                PerfectFrames.enable();
                Flashback.EXPORT_JOB.run();
            } finally {
                PerfectFrames.disable();
                Flashback.EXPORT_JOB = null;
            }
        }

        if (Flashback.isInReplay()) {
            i.set(replayTimer.advanceTime(Util.getMillis()));
            timer.tickDelta = replayTimer.tickDelta;
            if (!replayTimer.manager.runsNormally()) {
                timer.partialTick = 1.0F;
            } else {
                timer.partialTick = replayTimer.partialTick;
            }

            int localPlayerTicks = localPlayerTimer.advanceTime(Util.getMillis());
            if (this.level != null && this.player != null && !this.player.isPassenger() && !this.player.isRemoved()) {
                localPlayerTicks = Math.min(10, localPlayerTicks);
                for (int j = 0; j < localPlayerTicks; j++) {
                    this.level.guardEntityTick(this.level::tickNonPassenger, this.player);
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick_tickRateManager(CallbackInfo ci) {
        if (Flashback.isInReplay()) {
            this.replayTimer.manager.tick();
            if (!replayTimer.manager.runsNormally()) {
                timer.partialTick = 1.0F;
            } else {
                timer.partialTick = replayTimer.partialTick;
            }
        }
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;tick()V"))
    public boolean tick_levelRenderer(LevelRenderer instance) {
        return !Flashback.isInReplay() || this.replayTimer.manager.runsNormally();
    }

    @Override
    public boolean flashback$overridingLocalPlayerTimer() {
        return !Flashback.isExporting() && this.level != null && this.player != null && !this.player.isPassenger() && !this.player.isRemoved();
    }

    @Override
    public float flashback$getLocalPlayerPartialTick(float originalPartialTick) {
        if (this.cameraEntity != this.player || !this.flashback$overridingLocalPlayerTimer()) {
            return originalPartialTick;
        }
        return this.localPlayerTimer.partialTick;
    }

    @Unique
    private final AtomicBoolean applyKeyframes = new AtomicBoolean(false);

    @Override
    public void flashback$applyKeyframes() {
        this.applyKeyframes.set(true);
    }

    @Inject(method = "runTick", at = @At(
        value = "INVOKE_STRING",
        target = "Lcom/mojang/blaze3d/platform/Window;setErrorSection(Ljava/lang/String;)V",
        args = "ldc=Render"
    ), cancellable = true)
    public void runTick_setErrorSection(boolean bl, CallbackInfo ci) {
        ReplayServer replayServer = Flashback.getReplayServer();
        if (replayServer == null) {
            FlashbackAudioManager.stopAll();
            return;
        }

        LocalPlayer player = this.player;
        if (Flashback.RECORDER != null && player != null) {
            Flashback.RECORDER.trackPartialPosition(player, this.timer.partialTick);
        }

        AccurateEntityPositionHandler.apply(this.level, this.timer.partialTick);

        boolean paused = replayServer.replayPaused;
        boolean forceApplyKeyframes = this.applyKeyframes.compareAndSet(true, false);
        if (paused) {
            FlashbackAudioManager.pauseAll();
        }
        if (!paused || forceApplyKeyframes) {
            if (!paused) {
                FlashbackAudioManager.startHandling();
            }

            try {
                EditorState editorState = EditorStateManager.get(replayServer.getMetadata().replayIdentifier);
                editorState.applyKeyframes(new MinecraftKeyframeHandler((Minecraft) (Object) this), (float) replayServer.getPartialReplayTick());
            } finally {
                if (!paused) {
                    FlashbackAudioManager.finishHandling();
                }
            }
        }
        if (!replayServer.doClientRendering()) {
            ci.cancel();
        }
    }

    @Unique
    private final ThreadLocal<StartReplayServerInfo> info = new ThreadLocal<>();

    @WrapOperation(method = "doWorldLoad", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;spin(Ljava/util/function/Function;)Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer doWorldLoad_spin(Function<Thread, MinecraftServer> function, Operation<MinecraftServer> original,
            @Local(argsOnly = true) LevelStorageSource.LevelStorageAccess levelStorageAccess, @Local(argsOnly = true) PackRepository packRepository, @Local(argsOnly = true) WorldStem stem,
            @Local Services services) {
        StartReplayServerInfo info = this.info.get();
        if (info != null) {
            function = thread -> new ReplayServer(thread, (Minecraft) (Object) this,
                levelStorageAccess, packRepository, stem, services, i -> {
                StoringChunkProgressListener storingChunkProgressListener = new StoringChunkProgressListener(i);
                this.progressListener.set(storingChunkProgressListener);
                return ProcessorChunkProgressListener.createStarted(storingChunkProgressListener, this.progressTasks::add);
            }, info.playbackUUID(), info.path());
        }
        return original.call(function);
    }

    @Inject(method = "doWorldLoad", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;singleplayerServer:Lnet/minecraft/client/server/IntegratedServer;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    public void afterSetSingleplayerServer(CallbackInfo ci) {
        Flashback.updateIsInReplay();
    }

    @Override
    public void flashback$startReplayServer(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem stem, StartReplayServerInfo info) {
        this.info.set(info);
        try {
            this.doWorldLoad("idk", levelStorageAccess, packRepository, stem, false);
        } finally {
            this.info.remove();
        }
    }

}
