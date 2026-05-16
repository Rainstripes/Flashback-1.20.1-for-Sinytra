package com.moulberry.flashback.keyframe.impl;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.moulberry.flashback.Interpolation;
import com.moulberry.flashback.keyframe.Keyframe;
import com.moulberry.flashback.keyframe.KeyframeType;
import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.change.KeyframeChangeRoll;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import com.moulberry.flashback.keyframe.types.CameraRollKeyframeType;
import com.moulberry.flashback.spline.CatmullRom;
import com.moulberry.flashback.spline.Hermite;
import imgui.moulberry90.ImGui;
import net.minecraft.client.resources.language.I18n;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Consumer;

public class CameraRollKeyframe extends Keyframe {

    public float roll;

    public CameraRollKeyframe(float roll) {
        this(roll, InterpolationType.getDefault());
    }

    public CameraRollKeyframe(float roll, InterpolationType interpolationType) {
        this.roll = roll;
        this.interpolationType(interpolationType);
    }

    @Override
    public KeyframeType<?> keyframeType() {
        return CameraRollKeyframeType.INSTANCE;
    }

    @Override
    public Keyframe copy() {
        return new CameraRollKeyframe(this.roll, this.interpolationType());
    }

    @Override
    public void renderEditKeyframe(Consumer<Consumer<Keyframe>> update) {
        ImGui.setNextItemWidth(160);
        float[] input = new float[]{this.roll};
        if (ImGui.sliderFloat(I18n.get("flashback.roll"), input, -180.0f, 180.0f, "%.1f")) {
            if (this.roll != input[0]) {
                update.accept(keyframe -> ((CameraRollKeyframe) keyframe).roll = input[0]);
            }
        }
    }

    @Override
    public KeyframeChange createChange() {
        return new KeyframeChangeRoll(this.roll);
    }

    @Override
    public KeyframeChange createSmoothInterpolatedChange(Keyframe p1, Keyframe p2, Keyframe p3, float t0, float t1, float t2, float t3, float amount) {
        float time1 = t1 - t0;
        float time2 = t2 - t0;
        float time3 = t3 - t0;

        float roll = CatmullRom.degrees(this.roll,
            ((CameraRollKeyframe) p1).roll, ((CameraRollKeyframe) p2).roll,
            ((CameraRollKeyframe) p3).roll, time1, time2, time3, amount);

        return new KeyframeChangeRoll(roll);
    }

    @Override
    public KeyframeChange createHermiteInterpolatedChange(Map<Float, Keyframe> keyframes, float amount) {
        double roll = Hermite.degrees(Maps.transformValues(keyframes, k -> (double) ((CameraRollKeyframe) k).roll), amount);
        return new KeyframeChangeRoll((float) roll);
    }

    public static class TypeAdapter implements JsonSerializer<CameraRollKeyframe>, JsonDeserializer<CameraRollKeyframe> {
        @Override
        public CameraRollKeyframe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            float roll = jsonObject.has("roll") ? jsonObject.get("roll").getAsFloat() : 0.0f;
            InterpolationType interpolationType = context.deserialize(jsonObject.get("interpolation_type"), InterpolationType.class);
            return new CameraRollKeyframe(roll, interpolationType);
        }

        @Override
        public JsonElement serialize(CameraRollKeyframe src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("roll", src.roll);
            jsonObject.addProperty("type", "camera_roll");
            jsonObject.add("interpolation_type", context.serialize(src.interpolationType()));
            return jsonObject;
        }
    }
}