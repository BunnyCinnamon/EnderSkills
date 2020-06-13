package arekkuusu.enderskills.api.util;

import net.minecraft.util.math.MathHelper;

public class Quat {

    public final double x;
    public final double y;
    public final double z;
    public final double w;

    public Quat(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quat multiply(Quat a) {
        double newX = a.w * x + a.x * w + a.y * z - a.z * y;
        double newY = a.w * y + a.y * w + a.z * x - a.x * z;
        double newZ = a.w * z + a.z * w + a.x * y - a.y * x;
        double newW = a.w * w - a.x * x - a.y * y - a.z * z;
        return new Quat(newX, newY, newZ, newW);
    }

    public static Quat fromAxisAngleRad(Vector v, float angle) {
        float halfAngle = (angle * 0.5F);
        float sinHalfAngle = MathHelper.sin(halfAngle);
        return new Quat(v.x * sinHalfAngle, v.y * sinHalfAngle, v.z * sinHalfAngle, MathHelper.cos(halfAngle));
    }
}
