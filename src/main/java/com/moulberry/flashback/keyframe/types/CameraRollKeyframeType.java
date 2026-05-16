package com.moulberry.flashback.keyframe.types;

import com.moulberry.flashback.keyframe.KeyframeType;
import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.change.KeyframeChangeRoll;
import com.moulberry.flashback.keyframe.impl.CameraRollKeyframe;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import imgui.moulberry90.ImGui;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.Nullable;

public class CameraRollKeyframeType implements KeyframeType<CameraRollKeyframe> {
    public static CameraRollKeyframeType INSTANCE = new CameraRollKeyframeType();

    private CameraRollKeyframeType() {
    }

    @Override
    public Class<? extends KeyframeChange> keyframeChangeType() {
        return KeyframeChangeRoll.class;
    }

    @Override
    public String name() {
        return I18n.get("flashback.camera_roll");
    }

    @Override
    public String id() {
        return "CAMERA_ROLL";
    }

    @Override
    public @Nullable CameraRollKeyframe createDirect() {
        return null;
    }

    @Override
    public KeyframeCreatePopup<CameraRollKeyframe> createPopup() {
        float[] rollKeyframeInput = new float[]{0.0f};
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null && editorState.replayVisuals.overrideRoll) {
            rollKeyframeInput[0] = editorState.replayVisuals.overrideRollAmount;
        }

        return () -> {
            ImGui.sliderFloat(I18n.get("flashback.roll"), rollKeyframeInput, -180.0f, 180.0f, "%.1f");
            if (ImGui.button(I18n.get("flashback.add"))) {
                return new CameraRollKeyframe(rollKeyframeInput[0]);
            }
            ImGui.sameLine();
            if (ImGui.button(I18n.get("gui.cancel"))) {
                ImGui.closeCurrentPopup();
            }
            return null;
        };
    }
}