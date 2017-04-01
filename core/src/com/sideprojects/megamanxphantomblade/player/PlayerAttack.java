package com.sideprojects.megamanxphantomblade.player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.sideprojects.megamanxphantomblade.Damage;
import com.sideprojects.megamanxphantomblade.MovingObject;

/**
 * Created by buivuhoang on 27/03/17.
 */
public abstract class PlayerAttack extends MovingObject {
    public int direction;
    public Damage damage;
    public TextureRegion currentFrame;
    public TextureRegion muzzleFrame;
    public boolean shouldBeRemoved;
    public Vector2 muzzlePos;

    public PlayerAttack(Damage damage, int direction) {
        this.damage = damage;
        this.direction = direction;
        shouldBeRemoved = false;
    }

    public abstract void update(PlayerBase player, float delta);
}
