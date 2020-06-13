package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.entity.data.SkillExtendedData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityStoneGolem extends EntityGolem {

    public static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityStoneGolem.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<SkillExtendedData> DATA = EntityDataManager.createKey(EntityStoneGolem.class, SkillExtendedData.SERIALIZER);
    private static final DataParameter<Float> MAX_HEALTH = EntityDataManager.createKey(EntityStoneGolem.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> MIRROR_DAMAGE = EntityDataManager.createKey(EntityStoneGolem.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(EntityStoneGolem.class, DataSerializers.FLOAT);
    public int growTime = 5 * 20; //Used to make the golem 'grow' on spawning
    public int attackTimer;
    public BlockPos spawn;

    public EntityStoneGolem(World worldIn) {
        super(worldIn);
        this.setSize(1.4F, 0F/*2.7F*/);
    }

    @Override
    public void initEntityAI() {
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9D, 32.0F));
        this.tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 0.3D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
    }

    @Override
    public void entityInit() {
        super.entityInit();
        this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
        this.dataManager.register(MAX_HEALTH, 0F);
        this.dataManager.register(DATA, new SkillExtendedData(null));
        this.dataManager.register(MIRROR_DAMAGE, 0F);
        this.dataManager.register(DAMAGE, 0F);
    }

    @Override
    public void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.dataManager.get(MAX_HEALTH));
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        EntityLivingBase owner = null;
        UUID uuid = getOwnerId();
        if (uuid != null) {
            owner = getEntityByUUID(uuid);
        }
        return super.isOnSameTeam(entityIn) || (owner != null && owner == entityIn);
    }

    @Override
    public Team getTeam() {
        EntityLivingBase owner = null;
        UUID uuid = getOwnerId();
        if (uuid != null) {
            owner = getEntityByUUID(uuid);
        }
        return owner != null ? owner.getTeam() : super.getTeam();
    }

    @Override
    public int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (ticksExisted < growTime) {
            float progress = (float) ticksExisted / (float) growTime;
            this.height = 2.7F * progress;
            this.setEntityBoundingBox(getEntityBoundingBox().expand(0, 2.7F / growTime, 0));
            if (world.isRemote) {
                int i = MathHelper.floor(this.posX);
                int j = MathHelper.floor(this.posY - 0.20000000298023224D);
                int k = MathHelper.floor(this.posZ);
                IBlockState iblockstate = this.world.getBlockState(new BlockPos(i, j, k));

                if (iblockstate.getMaterial() != Material.AIR) {
                    this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double) this.rand.nextFloat() - 0.5D) * (double) this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double) this.rand.nextFloat() - 0.5D) * (double) this.width, 4.0D * ((double) this.rand.nextFloat() - 0.5D), 1.5D, ((double) this.rand.nextFloat() - 0.5D) * 4.0D, Block.getStateId(iblockstate));
                }
            }
        }
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();

        UUID uuid = getOwnerId();
        if (uuid != null) {
            EntityLivingBase owner = getEntityByUUID(uuid);
            if (owner != null) {
                this.setHomePosAndDistance(owner.getPosition(), 10);
                if (ticksExisted % 20 == 0) { //Check if skill is still active every 2 seconds
                    if (!SkillHelper.isActiveOwner(owner, ModAbilities.ANIMATED_STONE_GOLEM)) {
                        setDead();
                    }
                }
                if (owner.getDistance(this) > 10) {
                    this.setPositionAndUpdate(owner.posX, owner.posY, owner.posZ);
                }
                if (owner.getLastAttackedEntity() != this) {
                    setAttackTarget(owner.getLastAttackedEntity());
                }
            } else {
                setDead();
            }
        } else {
            setDead();
        }
        if (this.attackTimer > 0) {
            --this.attackTimer;
        }
        if (this.motionX * this.motionX + this.motionZ * this.motionZ > 2.500000277905201E-7D && this.rand.nextInt(5) == 0) {
            int i = MathHelper.floor(this.posX);
            int j = MathHelper.floor(this.posY - 0.20000000298023224D);
            int k = MathHelper.floor(this.posZ);
            IBlockState iblockstate = this.world.getBlockState(new BlockPos(i, j, k));

            if (iblockstate.getMaterial() != Material.AIR) {
                this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double) this.rand.nextFloat() - 0.5D) * (double) this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double) this.rand.nextFloat() - 0.5D) * (double) this.width, 4.0D * ((double) this.rand.nextFloat() - 0.5D), 0.5D, ((double) this.rand.nextFloat() - 0.5D) * 4.0D, Block.getStateId(iblockstate));
            }
        }
    }

    @Override
    protected void setSize(float width, float height) {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        double w = width / 2D;
        double h = height;
        this.width = width;
        this.height = height;
        setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX - w, axisalignedbb.minY, axisalignedbb.minZ - w, axisalignedbb.minX + w, axisalignedbb.minY + h, axisalignedbb.minZ + w));
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
        float f = this.width / 2F;
        float f1 = this.height;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y, z - (double) f, x + (double) f, y + (double) f1, z + (double) f));
    }

    @Override
    public void resetPositionToBB() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = axisalignedbb.minY;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
    }

    @Override
    public void setDead() {
        super.setDead();
        UUID uuid = getOwnerId();
        if (uuid != null) {
            EntityLivingBase owner = getEntityByUUID(uuid);
            if (owner != null) {
                SkillHelper.getActiveOwner(owner, ModAbilities.ANIMATED_STONE_GOLEM, holder -> {
                    ModAbilities.ANIMATED_STONE_GOLEM.unapply(owner, holder.data);
                    ModAbilities.ANIMATED_STONE_GOLEM.async(owner, holder.data);
                });
            }
        }
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        this.attackTimer = 15;
        this.world.setEntityState(this, (byte) 4);
        boolean attacked = false;

        UUID uuid = getOwnerId();
        if (uuid != null) {
            EntityLivingBase owner = getEntityByUUID(uuid);
            if (owner != null) {
                float ownerDamage = (float) owner.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float damage = getDamage() + ownerDamage + (ownerDamage * getMirrorDamage());
                attacked = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
            }
        }

        if (attacked) {
            if (entityIn instanceof EntityLivingBase) {
                ModAbilities.ANIMATED_STONE_GOLEM.apply((EntityLivingBase) entityIn, getData().copy());
                ModAbilities.ANIMATED_STONE_GOLEM.sync((EntityLivingBase) entityIn, getData().copy());
            }
            entityIn.motionY += 0.4000000059604645D;
            this.applyEnchantments(this, entityIn);
        }

        this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0F, 1.0F);
        return attacked;
    }

    @Nullable
    public EntityLivingBase getEntityByUUID(UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid) && entity instanceof EntityLivingBase)
                return (EntityLivingBase) entity;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 4) {
            this.attackTimer = 10;
            this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0F, 1.0F);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @SideOnly(Side.CLIENT)
    public int getAttackTimer() {
        return this.attackTimer;
    }

    public SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_IRONGOLEM_HURT;
    }

    public SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRONGOLEM_DEATH;
    }

    public void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(SoundEvents.ENTITY_IRONGOLEM_STEP, 1.0F, 1.0F);
    }

    @Nullable
    public UUID getOwnerId() {
        return this.dataManager.get(OWNER_UNIQUE_ID).orNull();
    }

    @SuppressWarnings("Guava")
    public void setOwnerId(@Nullable UUID owner) {
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(owner));
    }

    public void setData(SkillData data) {
        this.dataManager.set(DATA, new SkillExtendedData(data));
    }

    public SkillData getData() {
        return this.dataManager.get(DATA).data;
    }

    public float getMirrorDamage() {
        return this.dataManager.get(MIRROR_DAMAGE);
    }

    public void setMirrorDamage(float damage) {
        this.dataManager.set(MIRROR_DAMAGE, damage);
    }

    public float getDamage() {
        return this.dataManager.get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.dataManager.set(DAMAGE, damage);
    }

    public void setMaxHealth(float health) {
        this.dataManager.set(MAX_HEALTH, health);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
    }

    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat("mirror", getMirrorDamage());
        if (this.getOwnerId() == null) {
            compound.setString("OwnerUUID", "");
        } else {
            compound.setString("OwnerUUID", this.getOwnerId().toString());
        }
    }

    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setMirrorDamage(compound.getFloat("mirror"));
        String s;
        if (compound.hasKey("OwnerUUID", 8)) {
            s = compound.getString("OwnerUUID");
        } else {
            String s1 = compound.getString("Owner");
            s = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), s1);
        }

        if (!s.isEmpty()) {
            try {
                this.setOwnerId(UUID.fromString(s));
            } catch (Throwable ignored) {
            }
        }
    }
}