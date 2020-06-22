package arekkuusu.enderskills.client.gui.data;

public class SkillAdvancementConditionAltar extends SkillAdvancementCondition {

    public static double ALTAR_JUICE = 0D;
    public static boolean IS_ULTIMATE = false;
    public static final double LEVEL_0 = 0.1641791044776119D;
    public static final double LEVEL_1 = 0.4701492537313433D;
    public static final double LEVEL_2 = 0.7388059701492537D;
    public static final double LEVEL_3 = 1;

    private final double amount;

    public SkillAdvancementConditionAltar(double amount) {
        this.amount = amount;
    }

    public boolean canUpgrade() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUpgraded() {
        return ALTAR_JUICE >= amount || IS_ULTIMATE;
    }
}
