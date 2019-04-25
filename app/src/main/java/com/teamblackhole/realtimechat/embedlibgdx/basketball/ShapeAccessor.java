package com.teamblackhole.realtimechat.embedlibgdx.basketball;

import com.badlogic.gdx.physics.box2d.Shape;

import aurelienribon.tweenengine.TweenAccessor;

public class ShapeAccessor implements TweenAccessor<Shape> {

    public static final int TYPE_RADIUS = 1;

    @Override
    public int getValues(Shape target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case TYPE_RADIUS:
                returnValues[0] = target.getRadius();
                return 1;
            default:
                assert false;
                return -1;
        }
    }

    @Override
    public void setValues(Shape target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case TYPE_RADIUS:
                target.setRadius(newValues[0]);
                break;
            default:
                assert false;
                break;
        }
    }
}
