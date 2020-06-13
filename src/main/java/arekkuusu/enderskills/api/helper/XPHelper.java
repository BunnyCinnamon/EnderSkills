package arekkuusu.enderskills.api.helper;

import net.minecraft.entity.player.EntityPlayer;

public class XPHelper {

    public static int takeXP(EntityPlayer player, int xp) {
        int total = getXPTotal(player);
        int taken = Math.min(xp, total);
        total -= taken;
        setXP(player, total);
        return taken;
    }

    public static void giveXP(EntityPlayer player, int xp) {
        setXP(player, getXPTotal(player) + xp);
    }

    public static void setXP(EntityPlayer player, int xp) {
        player.experienceLevel = Math.max(getLevelFromXPValue(xp), 0);
        player.experience = Math.max(getLevelProgressFromXPValue(xp), 0F);
        player.experienceTotal = xp;
    }

    public static int getXPTotal(int level, double current) {
        return (int) (getXPValueFromLevel(level) + getXPValueToNextLevel(level) * current);
    }

    public static int getXPTotal(EntityPlayer player) {
        return (int) (getXPValueFromLevel(player.experienceLevel) + (getXPValueToNextLevel(player.experienceLevel) * player.experience));
    }

    public static int getLevelFromXPValue(int value) {
        int level;
        if (value >= getXPValueFromLevel(30)) {
            level = (int) (.07142857142857142 * (Math.sqrt(56D * value - 32511D) + 303D));
        } else if (value >= getXPValueFromLevel(15)) {
            level = (int) (.16666666666666666 * (Math.sqrt(24D * value - 5159D) + 59D));
        } else {
            level = (int) (value / 17D);
        }
        return level;
    }

    public static float getLevelProgressFromXPValue(int value) {
        if (value == 0) return 0F;
        int level = getLevelFromXPValue(value);
        float needed = getXPValueFromLevel(level);
        float next = getXPValueToNextLevel(level);
        float difference = value - needed;
        return difference / next;
    }

    public static int getXPValueFromLevel(int level) {
        int val;
        if (level >= 30) {
            val = (int) (3.5 * Math.pow(level, 2D) - 151.5 * level + 2220D);
        } else if (level >= 15) {
            val = (int) (1.5D * Math.pow(level, 2D) - 29.5 * level + 360D);
        } else {
            val = 17 * level;
        }
        return val;
    }

    public static int getXPValueToNextLevel(int level) {
        int val;
        if (level >= 30) {
            val = 7 * level - 148;
        } else if (level >= 15) {
            val = 3 * level - 28;
        } else {
            val = 17;
        }
        return val;
    }
}
