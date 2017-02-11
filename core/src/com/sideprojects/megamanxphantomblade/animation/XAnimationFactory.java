package com.sideprojects.megamanxphantomblade.animation;

import com.badlogic.gdx.graphics.g2d.Animation;

/**
 * Created by buivuhoang on 05/02/17.
 */
public class XAnimationFactory extends AnimationFactory {
    @Override
    protected int[] getAnimationIdle() {
        return new int[] {1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, 1, 3, 4, 3};
    }

    @Override
    protected String getTextureIdleAtlas() {
        return "sprites/x/idle.txt";
    }

    @Override
    protected int[] getAnimationRun() {
        return null;
    }

    @Override
    protected String getTextureIdleRun() {
        return "sprites/x/run.txt";
    }
}