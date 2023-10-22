package arekkuusu.enderskills.common.skill.ability.offence.blackflame;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.block.ModBlocks;
import arekkuusu.enderskills.common.block.tile.TileFire;
import arekkuusu.enderskills.common.entity.data.*;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableGleamFlash;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.effect.BlackFlame;
import arekkuusu.enderskills.common.sound.ModSounds;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlackFlareFlame extends BaseAbility implements IImpact, IScanEntities, IExpand, IFindEntity, IFlash {

    public BlackFlareFlame() {
        super(LibNames.BLACK_FLARE_FLAME, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
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
        double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
        double range = DSLDefaults.triggerSize(owner, this, level).getAmount();
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        double damage = DSLDefaults.getDamage(this, level);
        double true_damage = DSLDefaults.getTrueDamage(this, level);
        int dotDuration = DSLDefaults.triggerDamageDuration(owner, this, level).getAmount();
        double dot = DSLDefaults.getDamageOverTime(this, level);
        double true_dot = DSLDefaults.getTrueDamageOverTime(this, level);
        double delay = DSLDefaults.getDelay(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "damage", damage);
        NBTHelper.setDouble(compound, "true_damage", true_damage);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setInteger(compound, "time", time);
        NBTHelper.setDouble(compound, "dot", dot);
        NBTHelper.setDouble(compound, "true_dot", true_dot);
        NBTHelper.setInteger(compound, "dotDuration", dotDuration);
        NBTHelper.setDouble(compound, "distance", distance);
        NBTHelper.setDouble(compound, "delay", delay);

        SkillData data = SkillData.of(this)
                .put(compound)
                .with(time)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        EntityThrowableData.throwFor(owner, distance, data, 0.3F, true);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.FIREBALL);
    }

    //* Entity *//
    @Override
    public void onImpact(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, RayTraceResult trace) {
        double radius = skillData.nbt.getDouble("range");
        Vec3d hitVector = trace.hitVec;
        if (trace.typeOfHit == RayTraceResult.Type.ENTITY) {
            hitVector = new Vec3d(hitVector.x, hitVector.y + trace.entityHit.getEyeHeight(), hitVector.z);
        }
        EntityPlaceableGleamFlash spawn = new EntityPlaceableGleamFlash(source.world, owner, skillData, EntityPlaceableData.MIN_TIME);
        spawn.setPosition(hitVector.x, hitVector.y, hitVector.z);
        spawn.setRadius(radius);
        spawn.growTicks = 5;
        source.world.spawnEntity(spawn); //MANIFEST B L O O D!!
    }

    @Override
    public void onFlash(Entity source, @Nullable EntityLivingBase owner, SkillData skillData) {
        double radius = skillData.nbt.getDouble("range");
        int time = skillData.nbt.getInteger("time");
        this.doExplosionA(source.world, (float) ((float) Math.max(radius, 10F) + (radius / 10F)), source.posX, source.posY, source.posZ, owner, time);
    }

    public void doExplosionA(World world, float size, double x, double y, double z, EntityLivingBase exploder, int time) {
        Explosion explosion = new Explosion(world, exploder, x, y, z, 1F, true, false);
        Set<BlockPos> set = Sets.<BlockPos>newHashSet();
        int i = 16;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = size * (0.7F + world.rand.nextFloat() * 0.6F);
                        double d4 = x;
                        double d6 = y;
                        double d8 = z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            IBlockState iblockstate = world.getBlockState(blockpos);

                            if (iblockstate.getMaterial() != Material.AIR) {
                                float f2 = exploder != null ? exploder.getExplosionResistance(explosion, world, blockpos, iblockstate) : iblockstate.getBlock().getExplosionResistance(world, blockpos, (Entity) null, explosion);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (exploder == null || exploder.canExplosionDestroyBlock(explosion, world, blockpos, iblockstate, f))) {
                                set.add(blockpos);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d6 += d1 * 0.30000001192092896D;
                            d8 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        List<BlockPos> affectedBlockPositions = new ArrayList<>(set);

        world.playSound((EntityPlayer) null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);

        if (size >= 2.0F) {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, x, y, z, 1.0D, 0.0D, 0.0D);
        } else {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, x, y, z, 1.0D, 0.0D, 0.0D);
        }

        for (BlockPos blockpos1 : affectedBlockPositions) {
            if (world.getBlockState(blockpos1).getMaterial() == Material.AIR && world.getBlockState(blockpos1.down()).isFullBlock() && world.rand.nextInt(6) == 0) {
                world.setBlockState(blockpos1, ModBlocks.BLACK_FIRE_FLAME.getDefaultState());
                TileFire tileEntity = (TileFire) world.getTileEntity(blockpos1);
                tileEntity.setOwnerTime(exploder.getUniqueID(), time);
            }
        }
    }

    @Override
    public AxisAlignedBB expand(Entity source, AxisAlignedBB bb, float amount) {
        return bb.grow(amount);
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        if (!target.world.isRemote) {
            ModEffects.BLACK_FLAME.set(target, skillData);
            super.apply(target, skillData);

            SoundHelper.playSound(source.world, source.getPosition(), ModSounds.FIRE_HIT);
        }
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double damage = data.nbt.getDouble("damage");
        BlackFlame.dealTrueDamage(this, entity, owner, damage);
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) return;
        EntityLivingBase owner = SkillHelper.getOwner(data);
        double true_damage = data.nbt.getDouble("true_damage");
        int time = data.nbt.getInteger("time");
        BlackFlame.dealTrueDamage(this, entity, owner, true_damage, time);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entityLiving = event.getEntityLiving();
        Optional<BlockPos> flammableWithin = getFlammableWithin(entityLiving.world, entityLiving.getEntityBoundingBox().shrink(0.001D));
        if (flammableWithin.isPresent()) {
            BlockPos pooledMutableBlockPos = flammableWithin.get();
            TileFire tileEntity = (TileFire) entityLiving.world.getTileEntity(pooledMutableBlockPos);
            EntityLivingBase owner = tileEntity.getEntityLivingBase();

            if (owner != null) {
                if (owner == entityLiving) return;
                if (!TeamHelper.SELECTOR_ENEMY.apply(owner).apply(entityLiving)) return;
            }

            int level = Capabilities.get(owner).flatMap(a -> a.getOwned(ModAbilities.BLACK_FLARE_FLAME)).map(a -> ((AbilityInfo) a).getLevel()).orElse(0);

            NBTTagCompound compound = new NBTTagCompound();
            double dot = DSLDefaults.getDamageOverTime(ModEffects.BLACK_FLAME, level);
            double true_dot = DSLDefaults.getTrueDamageOverTime(ModEffects.BLACK_FLAME, level);
            int dotDuration = DSLDefaults.triggerDamageDuration(owner, ModEffects.BLACK_FLAME, level).getAmount();
            NBTHelper.setDouble(compound, "dot", dot);
            NBTHelper.setDouble(compound, "true_dot", true_dot);
            NBTHelper.setInteger(compound, "dotDuration", dotDuration);

            if (owner != null) {
                NBTHelper.setEntity(compound, owner, "owner");
            }

            SkillData data = SkillData.of(ModEffects.BLACK_FLAME)
                    .put(compound)
                    .create();
            if (!SkillHelper.isActive(entityLiving, ModEffects.BLACK_FLAME)) {
                SoundHelper.playSound(entityLiving.world, entityLiving.getPosition(), ModSounds.FIRE_HIT);
            }
            ModEffects.BLACK_FLAME.set(entityLiving, data);
        }
    }

    public Optional<BlockPos> getFlammableWithin(World world, AxisAlignedBB bb) {
        int j2 = MathHelper.floor(bb.minX);
        int k2 = MathHelper.ceil(bb.maxX);
        int l2 = MathHelper.floor(bb.minY);
        int i3 = MathHelper.ceil(bb.maxY);
        int j3 = MathHelper.floor(bb.minZ);
        int k3 = MathHelper.ceil(bb.maxZ);

        if (world.isAreaLoaded(new BlockPos(j2, l2, j3), new BlockPos(k2, i3, k3), true)) {
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

            for (int l3 = j2; l3 < k2; ++l3) {
                for (int i4 = l2; i4 < i3; ++i4) {
                    for (int j4 = j3; j4 < k3; ++j4) {
                        BlockPos.PooledMutableBlockPos pos = blockpos$pooledmutableblockpos.setPos(l3, i4, j4);
                        Block block = world.getBlockState(pos).getBlock();

                        if (block == ModBlocks.BLACK_FIRE_FLAME) {
                            BlockPos value = pos.toImmutable();
                            blockpos$pooledmutableblockpos.release();
                            return Optional.of(value);
                        }
                    }
                }
            }

            blockpos$pooledmutableblockpos.release();
        }

        return Optional.empty();
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLACK_FLAME_OFFENCE_CONFIG + LibNames.BLACK_FLARE_FLAME;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
