package arekkuusu.enderskills.client.gui.data;

import net.minecraft.entity.EntityLivingBase;

import java.util.List;

public interface ISkillAdvancement {
    void addDescription(List<String> description);
    boolean canUpgrade(EntityLivingBase entity);
    void onUpgrade(EntityLivingBase entity);
    Requirement getRequirement(EntityLivingBase entity);

    interface Requirement {
        int getLevels();
        int getXp();
    }

    class DefaultRequirement implements Requirement {

        public final int tokens;
        public final int xp;

        public DefaultRequirement(int tokens, int xp) {
            this.tokens = tokens;
            this.xp = xp;
        }

        @Override
        public int getLevels() {
            return tokens;
        }

        @Override
        public int getXp() {
            return xp;
        }
    }
}
