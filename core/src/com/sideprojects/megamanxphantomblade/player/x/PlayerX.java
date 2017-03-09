package com.sideprojects.megamanxphantomblade.player.x;

import com.sideprojects.megamanxphantomblade.player.PlayerBase;
import com.sideprojects.megamanxphantomblade.player.TraceColour;

/**
 * Created by buivuhoang on 04/02/17.
 */
public class PlayerX extends PlayerBase {
    protected PlayerX(float x, float y) {
        super(x, y);
        bounds.width = 0.5f;
        bounds.height = 0.7f;
    }

    @Override
    public void createAnimations() {
        animations = new PlayerXAnimation();
    }

    @Override
    public TraceColour getTraceColour() {
        return new TraceColour(0, 0, 1);
    }

    @Override
    public void updatePos() {
        super.updatePos();
        pos.x -= 0.05f;
    }
}
