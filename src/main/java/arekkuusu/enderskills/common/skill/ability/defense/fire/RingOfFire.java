package arekkuusu.enderskills.common.skill.ability.defense.fire;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLEvaluator;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillRangeEvent;
import arekkuusu.enderskills.api.helper.MathUtil;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.client.sounds.RingOfFireSound;
import arekkuusu.enderskills.common.entity.data.*;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableRingOfFire;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INDEFINITE;

public class RingOfFire extends BaseAbility implements IScanEntities, IFindEntity, IExpand, IImpact, ILoopSound {

    public RingOfFire() {
        super(LibNames.RING_OF_FIRE, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (hasCooldown(skillInfo) || isClientWorld(owner)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;

        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
        InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
        int level = infoUpgradeable.getLevel();
        if (infoCooldown.canSetCooldown(owner)) {
            infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
        }

        //
        double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
        double ringRange = RingOfFire.getRingRange(owner, level).getAmount();
        double dot = DSLDefaults.getDamageOverTime(this, level);
        int time = RingOfFire.getRingDuration(owner, level).toInteger();
        int dotTime = DSLDefaults.getDamageDuration(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "ringRange", ringRange);
        NBTHelper.setDouble(compound, "dot", dot);
        NBTHelper.setDouble(compound, "dotDuration", dotTime);
        NBTHelper.setInteger(compound, "time", time);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(INDEFINITE)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityThrowableData.throwFor(owner, range, data, 3F, true);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.FIRE_HIT);
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        if (trace.typeOfHit != RayTraceResult.Type.MISS) {
            Vec3d hitVector = trace.hitVec;

            EntityPlaceableRingOfFire spawn = new EntityPlaceableRingOfFire(owner.world, owner, skillData, skillData.nbt.getInteger("time"));
            spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
            spawn.setRadius(skillData.nbt.getDouble("ringRange"));
            spawn.spreadOnTerrain();
            spawn.spawnEntity();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void makeSound(Entity source) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new RingOfFireSound((EntityPlaceableData) source));
    }

    @Override
    public List<Entity> getScan(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, double size) {
        List<Entity> entities = source.getEntityWorld().getEntitiesWithinAABB(Entity.class, source.getEntityBoundingBox(), TeamHelper.SELECTOR_ENEMY.apply(owner));
        List<BlockPos> terrain = ((EntityPlaceableRingOfFire) source).getTerrainBlocks();
        entities.removeIf(entity -> {
            BlockPos entityPos = entity.getPosition();
            boolean withinHeight = false;
            for (int i = 1; i <= 2; i++) {
                BlockPos pos = entityPos.down(i);
                if (terrain.stream().anyMatch(p -> p.getX() == pos.getX() && p.getZ() == pos.getZ())) {
                    withinHeight = pos.getY() - entity.posY <= skillData.nbt.getDouble("ringRange");
                    break;
                }
            }
            return !withinHeight;
        });
        return entities;
    }

    @Override
    public void onScan(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!MathUtil.fuzzyEqual(target.motionX, 0D)
                || !MathUtil.fuzzyEqual(target.motionY, 0D)
                || !MathUtil.fuzzyEqual(target.motionZ, 0D)) {
            List<BlockPos> terrain = ((EntityPlaceableRingOfFire) source).getTerrainBlocks();
            Vec3d posTarget = target.getPositionVector();
            Vec3d posSource = source.getPositionVector();

            boolean isBorder = false;
            BlockPos targetBlockPos = new BlockPos(posTarget);
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos nextToTarget = targetBlockPos.offset(facing);
                BlockPos perpendicularToTarget = nextToTarget.offset(facing.rotateY());
                boolean isCloseToNonTerrain = terrain.stream().noneMatch(pos -> pos.equals(nextToTarget)
                        || pos.down().equals(nextToTarget)
                        || pos.up().equals(nextToTarget)
                        || pos.equals(perpendicularToTarget)
                        || pos.down().equals(perpendicularToTarget)
                        || pos.up().equals(perpendicularToTarget)
                );
                if (isCloseToNonTerrain) {
                    isBorder = true;
                    break;
                }
            }

            if (isBorder) {
                Vec3d posDiff = posTarget.subtract(posSource);
                Vec3d vecMotion = new Vec3d(target.motionX, target.motionY, target.motionZ);
                if (posDiff.add(vecMotion).lengthVector() > posDiff.lengthVector()) {
                    double speed = 0.05;
                    target.motionZ = -target.motionZ > 0 ? speed : -speed;
                    target.motionX = -target.motionX > 0 ? speed : -speed;
                }
            }
        }
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!SkillHelper.isActive(target, ModEffects.BURNING)) {
            SoundHelper.playSound(target.world, target.getPosition(), ModSounds.FIRE_HIT);
        }
        ModEffects.BURNING.set(target, skillData);
    }
    //* Entity *//

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.FIRE_DEFENSE_CONFIG + LibNames.RING_OF_FIRE;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }

    public static SkillRangeEvent getRingRange(EntityLivingBase entityLivingBase, int level) {
        double original = DSLEvaluator.evaluateDouble(ModAbilities.RING_OF_FIRE, "RING_RANGE", level, 1D);
        return SkillRangeEvent.trigger(entityLivingBase, ModAbilities.RING_OF_FIRE, original);
    }

    public static SkillRangeEvent getRingDuration(EntityLivingBase entityLivingBase, int level) {
        double original = DSLEvaluator.evaluateDouble(ModAbilities.RING_OF_FIRE, "RING_DURATION", level, 1D);
        return SkillRangeEvent.trigger(entityLivingBase, ModAbilities.RING_OF_FIRE, original);
    }
    /*Config Section*/
}
