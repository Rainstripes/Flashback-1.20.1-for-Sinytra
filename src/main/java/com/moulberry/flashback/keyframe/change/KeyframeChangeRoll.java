package com.moulberry.flashback.keyframe.change;

import com.moulberry.flashback.Interpolation;
import com.moulberry.flashback.keyframe.handler.KeyframeHandler;

public record KeyframeChangeRoll(float roll) implements KeyframeChange {
    @Override
    public void apply(KeyframeHandler keyframeHandler) {
        keyframeHandler.applyRoll(this.roll);
    }

    @Override
    public KeyframeChange interpolate(KeyframeChange to, double amount) {
        KeyframeChangeRoll other = (KeyframeChangeRoll) to;
        return new KeyframeChangeRoll(Interpolation.linearAngle(this.roll, other.roll, (float) amount));
    }
}