package com.personoid.humanoid.utils;

public class Bounds {
    private int xz;
    private int y;

    public Bounds(int xz, int y) {
        this.xz = xz;
        this.y = y;
    }

    public int getXz() {
        return xz;
    }

    public int getY() {
        return y;
    }

    public void setXz(int xz) {
        this.xz = xz;
    }

    public void setY(int y) {
        this.y = y;
    }
}
