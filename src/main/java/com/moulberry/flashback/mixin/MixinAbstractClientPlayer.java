package com.moulberry.flashback.mixin;

import com.mojang.authlib.GameProfile;
import com.moulberry.flashback.FilePlayerSkin;
import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends Player {

    @Shadow
    public abstract @Nullable PlayerInfo getPlayerInfo();

    @Unique
    private @Nullable PlayerInfo fallbackPlayerInfo;

    public MixinAbstractClientPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void postInit(ClientLevel clientLevel, GameProfile gameProfile, CallbackInfo ci) {
        if (Flashback.isInReplay()) {
            if (Minecraft.getInstance().getConnection() != null) {
                try {
                    this.getPlayerInfo();
                } catch (Exception ignored) {}
            }
            this.fallbackPlayerInfo = new PlayerInfo(gameProfile, false);
        }
    }

    @Inject(method = "getPlayerInfo", at = @At("RETURN"), cancellable = true)
    public void getPlayerInfo(CallbackInfoReturnable<PlayerInfo> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(this.fallbackPlayerInfo);
        }
    }

    @Unique
    private PlayerInfo skinOverridePlayerInfo = null;

    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true, require = 0)
    public void getSkinTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            FilePlayerSkin filePlayerSkin = editorState.skinOverrideFromFile.get(this.getUUID());
            if (filePlayerSkin != null) {
                cir.setReturnValue(filePlayerSkin.getTextureLocation());
                return;
            }

            GameProfile skinOverride = editorState.skinOverride.get(this.uuid);
            if (skinOverride != null) {
                if (skinOverridePlayerInfo == null || skinOverridePlayerInfo.getProfile() != skinOverride) {
                    skinOverridePlayerInfo = new PlayerInfo(skinOverride, false);
                }
                cir.setReturnValue(skinOverridePlayerInfo.getSkinLocation());
            }
        }
    }

    @Inject(method = "getModelName", at = @At("HEAD"), cancellable = true, require = 0)
    public void getModelName(CallbackInfoReturnable<String> cir) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            FilePlayerSkin filePlayerSkin = editorState.skinOverrideFromFile.get(this.getUUID());
            if (filePlayerSkin != null) {
                cir.setReturnValue(filePlayerSkin.getModelName());
                return;
            }

            GameProfile skinOverride = editorState.skinOverride.get(this.uuid);
            if (skinOverride != null) {
                if (skinOverridePlayerInfo == null || skinOverridePlayerInfo.getProfile() != skinOverride) {
                    skinOverridePlayerInfo = new PlayerInfo(skinOverride, false);
                }
                cir.setReturnValue(skinOverridePlayerInfo.getModelName());
            }
        }
    }

}
