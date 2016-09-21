package com.parrot.fpvtoolbox;

/**
 * Created by fred on 21/09/16.
 */
public class Vector3 {

    public double x;
    public double y;
    public double z;

    public Vector3(float[] values) {
        x = values[0];
        y = values[1];
        z = values[2];
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public double size() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public double dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public void normalize() {
        double size = size();
        x /= size;
        y /= size;
        z /= size;
    }

    public Vector3 getNormalized() {
        double size = size();
        return new Vector3(x / size, y / size, z / size);
    }
}
