package com.sideprojects.megamanxphantomblade.renderers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Queue;
import com.sideprojects.megamanxphantomblade.MovingObject;
import com.sideprojects.megamanxphantomblade.physics.player.PlayerState;
import com.sideprojects.megamanxphantomblade.player.PlayerAnimationBase;
import com.sideprojects.megamanxphantomblade.player.PlayerBase;
import com.sideprojects.megamanxphantomblade.renderers.shaders.ChargeShader;
import com.sideprojects.megamanxphantomblade.renderers.shaders.TraceShader;

/**
 * Created by buivuhoang on 23/03/17.
 */
public class PlayerRenderer implements Disposable {
    private PlayerBase player;

    private OrthographicCamera cam;
    private SpriteBatch batch;
    private ShaderProgram traceShader;
    private ShaderProgram damagedShader;
    private ShaderProgram chargeShader;

    // Needed properties to offset the player position properly
    private float mapTileWidth;

    // Keep the last frames of the player to draw a trace
    private Queue<TextureRegion> lastPlayerFrameQueue;
    private Queue<Vector2> lastPlayerPositionQueue;
    // Number of traces to render
    private int numOfTraces = 5;
    // Number of frame skip per trace
    private int traceFrameSkip = 2;
    // Flag to indicate when to start removing traces
    private boolean startRemovingTraces;
    // private count towards number of frame skip
    private int traceFrameSkipCount = 0;

    // Parameters for rendering dash rockets
    private float leftDashRocketPadding;
    private float xDashRocketPadding;
    private float yDashRocketPadding;
    private float yUpDashRocketPadding;

    // invincible state flickering duration
    private float damageFlickerDuration = 0.05f;
    private float chargeFlickerDuration = 0.03f;
    private float flickerStateTime = 0;


    public PlayerRenderer(PlayerBase player, float mapTileWidth, OrthographicCamera cam, SpriteBatch batch, ShaderProgram damagedShader) {
        this.player = player;
        this.cam = cam;
        this.mapTileWidth = mapTileWidth;
        this.batch = batch;

        traceShader = TraceShader.getShaderColor(player.getTraceColour());
        lastPlayerFrameQueue = new Queue<TextureRegion>(numOfTraces);
        lastPlayerPositionQueue = new Queue<Vector2>(numOfTraces);
        startRemovingTraces = false;
        // Calculate dash rocket padding
        leftDashRocketPadding = player.animations.get(PlayerAnimationBase.Type.Dash).getKeyFrame(0).getRegionWidth();
        xDashRocketPadding = player.animations.get(PlayerAnimationBase.Type.Dashrocket).getKeyFrame(0).getRegionWidth() / 5f;
        yDashRocketPadding = player.animations.get(PlayerAnimationBase.Type.Dashrocket).getKeyFrame(0).getRegionHeight() / 7f;
        yUpDashRocketPadding = player.animations.get(PlayerAnimationBase.Type.Updashrocket).getKeyFrame(0).getRegionHeight();

        this.damagedShader = damagedShader;
        chargeShader = ChargeShader.getShader();
    }

    // Pass posX and posY in so we don't have to recalculate them
    public void render(float posX, float posY, float delta) {
        TextureRegion currentFrame = player.currentFrame;
        float originPosX = posX;
        if (player.direction == PlayerBase.RIGHT) {
            // Pad the texture's start x because the engine is drawing from left to right.
            // Without this the animation frames will be misaligned
            posX += mapTileWidth * 0.5f - currentFrame.getRegionWidth() - player.animationPadding.x;
        } else {
            posX += player.animationPadding.x;
        }

        posY += player.animationPadding.y;

        renderPlayerTrace(currentFrame, posX, posY);
        if (player.currentDashRocketFrame != null) {
            if (player.state == PlayerState.Dash) {
                renderPlayerDashRocket(originPosX, posY);
            }
            if (player.state == PlayerState.Updash) {
                renderPlayerUpDashRocket(originPosX, posY);
            }
        }
        if (player.invincible) {
            flickerStateTime += delta;
            if (flickerStateTime >= damageFlickerDuration * 2) {
                flickerStateTime = 0;
            }
            if (flickerStateTime <= damageFlickerDuration) {
                batch.setShader(damagedShader);
            }
        }
        // Only do charging shader if not currently being invincible
        // TODO: Find a way to modify the texture color to do different weapon charges
        if (!player.invincible && player.isCharging) {
            flickerStateTime += delta;
            if (flickerStateTime >= chargeFlickerDuration * 2) {
                flickerStateTime = 0;
            }
            if (flickerStateTime <= chargeFlickerDuration) {
                batch.setShader(chargeShader);
            }
        }
        batch.draw(currentFrame, posX, posY);
        if (player.invincible || player.isCharging) {
            batch.setShader(null);
        }
        renderPlayerAuxiliaryAnimation(originPosX, posY);
    }

    private void renderPlayerDashRocket(float posX, float posY) {
        float y = posY - yDashRocketPadding;
        float x = posX;
        if (player.direction == MovingObject.RIGHT) {
            x -= player.currentDashRocketFrame.getRegionWidth() + xDashRocketPadding;
        } else {
            x += leftDashRocketPadding + xDashRocketPadding;
        }

        batch.draw(player.currentDashRocketFrame, x, y);
    }

    private void renderPlayerUpDashRocket(float posX, float posY) {
        float y = posY - yUpDashRocketPadding;
        batch.draw(player.currentDashRocketFrame, posX, y);
    }

    private void renderPlayerTrace(TextureRegion currentFrame, float posX, float posY) {
        if (traceFrameSkipCount != traceFrameSkip) {
            traceFrameSkipCount++;
        } else {
            if (startRemovingTraces) {
                lastPlayerFrameQueue.removeFirst();
                lastPlayerPositionQueue.removeFirst();
            }
            traceFrameSkipCount = 0;
            // If player is dashing, draw a trace
            if (player.state == PlayerState.Dash || player.state == PlayerState.Updash || player.isJumpDashing) {
                lastPlayerFrameQueue.addLast(currentFrame);
                lastPlayerPositionQueue.addLast(new Vector2(posX, posY));
            }
            if (lastPlayerFrameQueue.size == numOfTraces) {
                startRemovingTraces = true;
            }
        }

        if (player.state == PlayerState.Dash || player.state == PlayerState.Updash || player.isJumpDashing) {
            if (lastPlayerFrameQueue.size != numOfTraces) {
                startRemovingTraces = false;
            }
        } else {
            startRemovingTraces = true;
        }


        if (lastPlayerFrameQueue.size != 0) {
            for (int i = 0; i < lastPlayerFrameQueue.size; i++) {
                TextureRegion frame = lastPlayerFrameQueue.get(i);
                Vector2 position = lastPlayerPositionQueue.get(i);
                batch.setShader(traceShader);
                batch.draw(frame, position.x, position.y);
            }
        } else {
            startRemovingTraces = false;
        }
        batch.setShader(null);
    }

    private void renderPlayerAuxiliaryAnimation(float posX, float posY) {
        Vector2 padding = player.getAuxiliaryAnimationPadding();
        if (padding != null) {
            float x = posX + padding.x;
            float y = posY + padding.y;
            if (player.auxiliaryFrames != null) {
                for (TextureRegion frame : player.auxiliaryFrames.values()) {
                    if (frame != null) {
                        batch.draw(frame, x, y);
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        traceShader.dispose();
        chargeShader.dispose();
    }
}
