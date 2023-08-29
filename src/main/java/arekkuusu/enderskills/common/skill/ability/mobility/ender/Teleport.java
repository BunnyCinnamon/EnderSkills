package arekkuusu.enderskills.common.skill.ability.mobility.ender;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.client.render.skill.TeleportRenderer;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INSTANT;

public class Teleport extends BaseAbility {

    public Teleport() {
        super(LibNames.TELEPORT, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (hasCooldown(skillInfo) || isClientWorld(owner)) return;
        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
        InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
        int level = infoUpgradeable.getLevel();

        //
        double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
        RayTraceHelper.getVecLookedAt(owner, distance).ifPresent(targetVector -> {
            targetVector = new Vec3d(
                    MathHelper.floor(targetVector.x) + 0.5D,
                    MathHelper.floor(targetVector.y) + 0.1D,
                    MathHelper.floor(targetVector.z) + 0.5D
            );

            BlockPos posFloor = new BlockPos(targetVector).up(); //One Bwock fwom bwock tawgeted
            BlockPos posCeiling = posFloor.up(); //One spawce up fow youw wittle head uwu
            if (posFloor.getY() <= 0) return; //Yikes
            if (isSafePos(owner.world, posFloor) && isSafePos(owner.world, posCeiling) && isActionable(owner) && canActivate(owner)) {
                if (infoCooldown.canSetCooldown(owner)) {
                    infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
                }
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setVector(compound, "origin", owner.getPositionVector());
                NBTHelper.setVector(compound, "target", targetVector);
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(INSTANT)
                        .put(compound)
                        .overrides(SkillData.Overrides.EQUAL)
                        .create();
                super.apply(owner, data);
                super.sync(owner, data);
                super.sync(owner);
            }
        });
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        Vec3d vec = NBTHelper.getVector(data.nbt, "target");
        entity.setPositionAndUpdate(vec.x, vec.y, vec.z);
        if (entity.world instanceof WorldServer) {
            SoundHelper.playSound(entity.world, entity.getPosition(), ModSounds.TELEPORT);
            SoundHelper.playSound(entity.world, new BlockPos(vec.x, vec.y, vec.z), ModSounds.TELEPORT);
        }
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) {
            spawnRift(entity, data);
        }
    }

    @SideOnly(Side.CLIENT)
    public void spawnRift(EntityLivingBase entity, SkillData data) {
        Vec3d offset = new Vec3d(0, entity.height / 2D, 0);
        TeleportRenderer.TeleportRift riftOrigin = new TeleportRenderer.TeleportRift(entity, NBTHelper.getVector(data.nbt, "origin").add(offset));
        TeleportRenderer.TeleportRift riftTarget = new TeleportRenderer.TeleportRift(entity, NBTHelper.getVector(data.nbt, "target").add(offset));
        TeleportRenderer.TELEPORT_RIFTS.add(riftOrigin);
        TeleportRenderer.TELEPORT_RIFTS.add(riftTarget);
    }

    public boolean isSafePos(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getCollisionBoundingBox(world, pos) == Block.NULL_AABB;
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_MOBILITY_CONFIG + LibNames.TELEPORT;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
