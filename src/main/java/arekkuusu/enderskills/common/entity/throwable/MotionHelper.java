package arekkuusu.enderskills.common.entity.throwable;

import arekkuusu.enderskills.api.helper.RayTraceHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class MotionHelper {

    public static void forwardMotion(Entity owner, Entity throwable, double distance, int time) {
        Vec3d lookVec = owner.getLookVec();
        Vec3d userVec = owner.getPositionEyes(1F);
        Vec3d position = userVec.addVector(
                lookVec.x * distance,
                lookVec.y * distance,
                lookVec.z * distance
        );
        Vec3d motion = position.subtract(userVec);
        motion = new Vec3d(motion.x / (double) time, motion.y / (double) time, motion.z / (double) time);
        throwable.motionX = motion.x;
        throwable.motionY = motion.y;
        throwable.motionZ = motion.z;
    }

    public static void ayylmaoMotion(Entity owner, Entity throwable, double distance, int time) {
        Vec3d userVec = owner.getPositionEyes(1F);
        Vec3d posLookedAtWeird = RayTraceHelper.getPosLookedAtWeird(owner, distance);
        Vec3d motion = posLookedAtWeird.subtract(userVec);
        motion = new Vec3d(motion.x / (double) time, motion.y / (double) time, motion.z / (double) time);
        throwable.motionX = motion.x;
        throwable.motionY = motion.y;
        throwable.motionZ = motion.z;
    }

    public static void pull(Vec3d pusherVector, Entity target, double push) {
        Vec3d from = target.getPositionVector();
        Vec3d vector = pusherVector.subtract(from).normalize().scale(-1);
        Vec3d to = from.addVector(
                vector.x * push,
                vector.y * push,
                vector.z * push
        );
        moveEntity(from, to, target);
    }

    public static void push(Vec3d pusherVector, Entity target, double push) {
        Vec3d from = target.getPositionVector();
        Vec3d vector = pusherVector.subtract(from).normalize().scale(-1);
        Vec3d to = from.addVector(
                vector.x * push,
                vector.y * push,
                vector.z * push
        );
        moveEntity(to, from, target);
    }

    public static void moveEntity(Vec3d pullerPos, Vec3d pushedPos, Entity pulled) {
        Vec3d distance = pullerPos.subtract(pushedPos);
        Vec3d motion = new Vec3d(distance.x / 10D, distance.y / 10D, distance.z / 10D).scale(-1);
        pulled.motionX = -motion.x;
        pulled.motionY = -motion.y;
        pulled.motionZ = -motion.z;
    }
}
