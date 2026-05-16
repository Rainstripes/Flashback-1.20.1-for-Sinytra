package com.moulberry.flashback.keyframe.handler;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.keyframe.change.*;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.Set;

public record MinecraftKeyframeHandler(Minecraft minecraft) implements KeyframeHandler {

    private static final Set<Class<? extends KeyframeChange>> supportedChanges = Set.of(
            KeyframeChangeCameraPosition.class, KeyframeChangeCameraPositionOrbit.class, KeyframeChangeTrackEntity.class,
            KeyframeChangeFov.class, KeyframeChangeTimeOfDay.class, KeyframeChangeCameraShake.class, KeyframeChangeRoll.class
    );

    @Override
    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    @Override
    public boolean supportsKeyframeChange(Class<? extends KeyframeChange> clazz) {
        return supportedChanges.contains(clazz);
    }

    @Override
    public void applyCameraPosition(Vector3d position, double yaw, double pitch, double roll) {
        LocalPlayer player = this.minecraft.player;
        if (player != null) {
            if (this.minecraft.cameraEntity != this.minecraft.player) {
                Flashback.spectateEntityInReplay(null);
            }

            player.moveTo(position.x, position.y, position.z, (float) yaw, (float) pitch);

            EditorState editorState = EditorStateManager.getCurrent();
            if (editorState != null) {
                this.applyRollVisual(editorState, (float) roll);
            }

            player.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public void applyFov(float fov) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            editorState.replayVisuals.setFov(fov);
        }
    }

    @Override
    public void applyTimeOfDay(int timeOfDay) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            editorState.replayVisuals.overrideTimeOfDay = timeOfDay;
        }
    }

    @Override
    public void applyCameraShake(float frequencyX, float amplitudeX, float frequencyY, float amplitudeY) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            editorState.replayVisuals.setCameraShake(frequencyX, amplitudeX, frequencyY, amplitudeY);
        }
    }

    @Override
    public void applyRoll(float roll) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            this.applyRollVisual(editorState, roll);
        }
    }

    private void applyRollVisual(EditorState editorState, float roll) {
        if (roll > -0.01f && roll < 0.01f) {
            editorState.replayVisuals.overrideRoll = false;
            editorState.replayVisuals.overrideRollAmount = 0.0f;
        } else {
            editorState.replayVisuals.overrideRoll = true;
            editorState.replayVisuals.overrideRollAmount = roll;
        }
    }
}
