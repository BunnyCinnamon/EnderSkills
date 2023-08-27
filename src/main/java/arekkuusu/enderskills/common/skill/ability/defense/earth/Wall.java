package arekkuusu.enderskills.common.skill.ability.defense.earth;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.client.gui.data.SkillAdvancement;
import arekkuusu.enderskills.common.entity.EntityWall;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Config;

public class Wall extends BaseAbility {

    public Wall() {
        super(LibNames.WALL, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (hasCooldown(skillInfo) || isClientWorld(owner)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;

        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
        InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
        int level = infoUpgradeable.getLevel();

        double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
        RayTraceHelper.getFloorLookedAt(owner, distance, distance).map(BlockPos::up).ifPresent(pos -> {
            if (infoCooldown.canSetCooldown(owner)) {
                infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
            }

            //
            EnumFacing cardinal = owner.getHorizontalFacing();
            int height = DSLDefaults.triggerHeight(owner, this, level).toInteger();
            int width = DSLDefaults.triggerWidth(owner, this, level).toInteger();
            int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
            float force = DSLDefaults.getForce(this, level);
            EntityWall wall = new EntityWall(owner.world, SkillData.of(this).by(owner).create());
            wall.setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            wall.create(pos, cardinal, width, height, time);
            wall.setLaunch(force);
            wall.spawnEntity(); //MANIFEST W A L L!!
            super.sync(owner);

            SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.WALL_UP);
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.EARTH_DEFENSE_CONFIG + LibNames.WALL;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
