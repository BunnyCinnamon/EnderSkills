package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.entity.data.SkillExtendedData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.*;
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
    public static final DataParameter<Float> MAX_HEALTH = EntityDataManager.createKey(EntityStoneGolem.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> MIRROR_DAMAGE = EntityDataManager.createKey(EntityStoneGolem.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(EntityStoneGolem.class, DataSerializers.FLOAT);
    public int growTime = 5 * 20; //Used to make the golem come out of the ground on spawning
    public boolean isGrown; //When the golem is fully out of the ground
    public int attackTimer;
    public BlockPos spawn;

    public EntityStoneGolem(World worldIn) {
        super(worldIn);
        this.setSize(1.4F, 0F);
    }

    @Override
    public void initEntityAI() {
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1D, true));
        this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 1D, 32.0F));
        this.tasks.addTask(6, new AIFollowProvider(this, () -> getOwnerId() != null ? getEntityByUUID(getOwnerId()) : null, 1D, 5, 64));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.tasks.addTask(0, AIOverride.INSTANCE);
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
    protected boolean canEquipItem(ItemStack stack) {
        return false;
    }

    @Override
    public void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(0);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0);
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
        return super.isOnSameTeam(entityIn) || (owner != null && TeamHelper.SELECTOR_ALLY.apply(owner).test(entityIn));
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
        } else if (!isGrown) {
            tasks.removeTask(AIOverride.INSTANCE);
            isGrown = true;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        UUID uuid = getOwnerId();
        if (uuid != null) {
            EntityLivingBase owner = getEntityByUUID(uuid);
            if (owner != null) {
                this.setHomePosAndDistance(owner.getPosition(), 10);
                if (!SkillHelper.isActiveFrom(owner, ModAbilities.ANIMATED_STONE_GOLEM)) {
                    setDead();
                }
                if (owner.getDistance(this) > 69) { //uwu
                    teleportTo(owner);
                }
                if (owner.getLastAttackedEntity() != this && owner.getLastAttackedEntity() != null && TeamHelper.SELECTOR_ENEMY.apply(owner).test(owner.getLastAttackedEntity())) {
                    setAttackTarget(owner.getLastAttackedEntity());
                }
                if (getRevengeTarget() != null && getRevengeTarget().isOnSameTeam(this)) {
                    setRevengeTarget(null);
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

    public void teleportTo(Entity entity) {
        for (int i = 0; i < 16; ++i) {
            double d3 = entity.posX + (world.rand.nextDouble() - 0.5D) * 5;
            double d4 = MathHelper.clamp(entity.posY + (((world.rand.nextDouble()) * 5D) - (5D / 2D)), 0.0D, world.getActualHeight() - 1);
            double d5 = entity.posZ + (world.rand.nextDouble() - 0.5D) * 5;
            if (isRiding()) {
                dismountRidingEntity();
            }

            if (attemptTeleport(d3, d4, d5)) {
                world.playSound(null, posX, posY, posZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking() && player.getUniqueID().equals(getOwnerId())) {
            if(!world.isRemote) setDead();
            return true;
        }
        return super.processInteract(player, hand);
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
                ModAbilities.ANIMATED_STONE_GOLEM.unapply(owner, getData());
                ModAbilities.ANIMATED_STONE_GOLEM.async(owner, getData());
            }
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.attackTimer = 15;
        this.world.setEntityState(this, (byte) 4);
        boolean attacked = false;

        UUID uuid = getOwnerId();
        if (uuid != null) {
            EntityLivingBase owner = getEntityByUUID(uuid);
            if (owner != null) {
                float golemDamage = (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float ownerDamage = (float) owner.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float damage = golemDamage + ownerDamage + (ownerDamage * getMirrorDamage());
                attacked = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                if (entityIn instanceof EntityLivingBase) {
                    SkillData data = getData().copy();
                    ModEffects.STUNNED.apply((EntityLivingBase) entityIn, data);
                    ModEffects.STUNNED.sync((EntityLivingBase) entityIn, data);
                }
                entityIn.motionY += 0.4000000059604645D;
                this.applyEnchantments(this, entityIn);
            }
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
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(damage);
    }

    public void setMaxHealth(float health) {
        this.dataManager.set(MAX_HEALTH, health);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
    }

    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if(world != null && !world.isRemote) {
            setDead();
        }
    }
}