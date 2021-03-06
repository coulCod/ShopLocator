package com.example.shoplocator.ui.model;

/**
 * Created by {@author yura.savchuk22@gmail.com} on 21.01.17.
 */

public class ShopCoordinate implements Cloneable {

    private final float x;
    private final float y;

    public ShopCoordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public ShopCoordinate clone() {
        try {
            return (ShopCoordinate) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
