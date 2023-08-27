package arekkuusu.enderskills.common.skill.ability.defense.earth;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDurationEvent;
import arekkuusu.enderskills.api.event.SkillRangeEvent;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.common.entity.EntityWall;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;

public class Dome extends BaseAbility {

    public Dome() {
        super(LibNames.DOME, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((InfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        BlockPos pos = owner.getPosition().down();
        IBlockState state = owner.world.getBlockState(pos);
        pos = pos.up();
        if (state.isOpaqueCube() && state.isFullBlock() && isActionable(owner) && canActivate(owner)) {
            InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
            InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
            int level = infoUpgradeable.getLevel();
            if (infoCooldown.canSetCooldown(owner)) {
                infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
            }

            //
            int height = DSLDefaults.triggerHeight(owner, this, level).toInteger();
            int size = DSLDefaults.triggerWidth(owner, this, level).toInteger();
            int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
            float force = DSLDefaults.getForce(this, level);

            EntityWall wall = new EntityWall(owner.world, SkillData.of(this).by(owner).create());
            wall.setPosition(pos.getX() + 0.5D, pos.getY() - size, pos.getZ() + .5);
            wall.setLaunch(force);

            int width = 1 + size * 2;
            wall.create(pos.add(size, 0, 0), EnumFacing.EAST, width, height, time);
            wall.create(pos.add(-size, 0, 0), EnumFacing.WEST, width, height, time);
            wall.create(pos.add(0, 0, size), EnumFacing.NORTH, width, height, time);
            wall.create(pos.add(0, 0, -size), EnumFacing.SOUTH, width, height, time);
            wall.spawnEntity(); //MANIFEST W A L L!!
            super.sync(owner);

            SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.DOME_UP);
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.EARTH_DEFENSE_CONFIG + LibNames.DOME;

    @Config(modid = LibMod.MOD_ID, name = Dome.CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(Dome.CONFIG_FILE);
    }
    /*Config Section*/
}
