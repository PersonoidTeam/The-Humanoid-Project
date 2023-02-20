package com.personoid.humanoid.utils;

public class Bounds {
    public static Bounds MAX = new Bounds(-1, -1);
    public static Bounds MIN = new Bounds(1, 1);

    private int xz;
    private int y;

    public Bounds(int xz, int y) {
        this.xz = xz;
        this.y = y;
    }

    public Bounds(int xzy) {
        this.xz = xzy;
        this.y = xzy;
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

    public static Bounds of(int xz, int y) {
        return new Bounds(xz, y);
    }

    public static Bounds of(int xzy) {
        return new Bounds(xzy);
    }
}
