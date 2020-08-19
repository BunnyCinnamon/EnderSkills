package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.entity.ai.FlightMoveHelper;
import arekkuusu.enderskills.common.entity.ai.FlightPathNavigate;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
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
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class EntityVoltaicSentinel extends EntityGolem {

    public static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityVoltaicSentinel.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<Optional<UUID>> FOLLOW_UNIQUE_ID = EntityDataManager.createKey(EntityVoltaicSentinel.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<Float> MAX_HEALTH = EntityDataManager.createKey(EntityVoltaicSentinel.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(EntityVoltaicSentinel.class, DataSerializers.FLOAT);
    public static final DataParameter<Boolean> AGGRO = EntityDataManager.createKey(EntityVoltaicSentinel.class, DataSerializers.BOOLEAN);

    public EntityVoltaicSentinel(World worldIn) {
        super(worldIn);
        this.moveHelper = new FlightMoveHelper(this);
        setSize(1, 1);
    }

    @Override
    public void initEntityAI() {
        this.tasks.addTask(1, new SentinelAIAttack(this));
        this.tasks.addTask(2, new AIFollowFlyingProvider(this, () ->
                getFollowId() != null ? getEntityByUUID(getFollowId()) : null, 2D, 5, 64)
        );
        this.tasks.addTask(4, new EntityAIWanderAvoidWaterFlying(this, 1D));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, Entity.class, 8));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityLivingBase.class, 2, true, false, new AITargetSelector()));
    }

    @Override
    public void entityInit() {
        super.entityInit();
        this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
        this.dataManager.register(FOLLOW_UNIQUE_ID, Optional.absent());
        this.dataManager.register(MAX_HEALTH, 0F);
        this.dataManager.register(DAMAGE, 0F);
        this.dataManager.register(AGGRO, false);
    }

    @Override
    protected boolean canEquipItem(ItemStack stack) {
        return false;
    }

    @Override
    public void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.dataManager.get(MAX_HEALTH));
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.dataManager.get(DAMAGE));
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new FlightPathNavigate(this, worldIn);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!world.isRemote) {
            UUID uuid = getFollowId();
            if (uuid != null) {
                EntityLivingBase owner = getEntityByUUID(uuid);
                if (owner != null) {
                    this.setHomePosAndDistance(owner.getPosition(), 10);
                    if (owner.getDistance(this) > 69) { //uwu
                        teleportTo(owner);
                    }
                } else {
                    setFollowId(getOwnerId());
                }
            } else {
                setFollowId(getOwnerId());
            }
            if (getOwnerId() == null || !SkillHelper.isActiveFrom(getEntityByUUID(getOwnerId()), ModAbilities.VOLTAIC_SENTINEL)) {
                setDead();
            }
        }
    }

    public void teleportTo(EntityLivingBase entity) {
        if (attemptTeleport(entity.posX, entity.posY, entity.posZ)) {
            world.playSound(null, posX, posY, posZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (entityIn instanceof EntityLivingBase) {
            double damage = getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, this, "owner");
            NBTHelper.setDouble(compound, "damage", damage);
            NBTHelper.setDouble(compound, "stun", 20);
            SkillData data = SkillData.of(ModAbilities.ZAP)
                    .by(this)
                    .with(BaseAbility.INSTANT)
                    .put(compound)
                    .overrides(SkillData.Overrides.SAME)
                    .create();
            ModAbilities.ZAP.apply((EntityLivingBase) entityIn, data);
            ModAbilities.ZAP.sync((EntityLivingBase) entityIn, data);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setEntityBoundingBox(AxisAlignedBB bb) {
        super.setEntityBoundingBox(bb);
        this.width = (float) Math.max(Math.abs(bb.maxX - bb.minX), Math.abs(bb.maxZ - bb.minZ));
        this.height = (float) Math.abs(bb.maxY - bb.minY);
    }

    @Override
    protected void setSize(float width, float height) {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        double w = width / 2D;
        double h = height / 2D;
        this.width = width;
        this.height = height;
        setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX - w, axisalignedbb.minY - h, axisalignedbb.minZ - w, axisalignedbb.minX + w, axisalignedbb.minY + h, axisalignedbb.minZ + w));
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
        float f = this.width / 2F;
        float f1 = this.height / 2F;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y - f1, z - (double) f, x + (double) f, y + (double) f1, z + (double) f));
    }

    @Override
    public void resetPositionToBB() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
    }

    @Override
    public float getEyeHeight() {
        return height / 2;
    }

    @Override
    public float getAIMoveSpeed() {
        return super.getAIMoveSpeed();
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
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public void setNoGravity(boolean noGravity) {
        //Yoink!
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        //Yoink!
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        //Yoink!
    }

    @Override
    public int getMaxFallHeight() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int decreaseAirSupply(int air) {
        return air;
    }

    @Nullable
    public EntityLivingBase getEntityByUUID(UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid) && entity instanceof EntityLivingBase)
                return (EntityLivingBase) entity;
        }
        return null;
    }

    @Nullable
    public UUID getFollowId() {
        return this.dataManager.get(FOLLOW_UNIQUE_ID).orNull();
    }

    @SuppressWarnings("Guava")
    public void setFollowId(@Nullable UUID owner) {
        this.dataManager.set(FOLLOW_UNIQUE_ID, Optional.fromNullable(owner));
    }

    @Nullable
    public UUID getOwnerId() {
        return this.dataManager.get(OWNER_UNIQUE_ID).orNull();
    }

    @SuppressWarnings("Guava")
    public void setOwnerId(@Nullable UUID owner) {
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(owner));
    }

    public float getDamage() {
        return this.dataManager.get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.dataManager.set(DAMAGE, damage);
    }

    public boolean getAggro() {
        return this.dataManager.get(AGGRO);
    }

    public void setAggro(boolean aggro) {
        this.dataManager.set(AGGRO, aggro);
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

    public class AITargetSelector implements Predicate<Entity> {

        @Override
        public boolean apply(@Nullable Entity entity) {
            if (getOwnerId() == null || entity == null) return false;
            EntityLivingBase owner = getEntityByUUID(getOwnerId());
            return owner != null && Objects.requireNonNull(TeamHelper.SELECTOR_ENEMY.apply(owner)).test(entity);
        }
    }

    private static class SentinelAIAttack extends EntityAIBase {

        private final EntityVoltaicSentinel sentinel;
        private int tickCounter;

        SentinelAIAttack(EntityVoltaicSentinel sentinel) {
            this.sentinel = sentinel;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase living = sentinel.getAttackTarget();
            return living != null && living.isEntityAlive();
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void startExecuting() {
            if (sentinel.getDistance(sentinel.getAttackTarget()) >= 2) {
                sentinel.getNavigator().clearPath();
                sentinel.getNavigator().tryMoveToEntityLiving(sentinel.getAttackTarget(), 0.25D);
            } else {
                Vec3d vec = RandomPositionGenerator.findRandomTarget(sentinel, 2, 2);
                if (vec != null) {
                    sentinel.getNavigator().clearPath();
                    sentinel.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, 1D);
                }
            }
            sentinel.setAggro(true);
            sentinel.isAirBorne = true;
            tickCounter = 0;
        }

        public void resetTask() {
            sentinel.setAggro(false);
            sentinel.setAttackTarget(null);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void updateTask() {
            EntityLivingBase target = sentinel.getAttackTarget();

            if (!sentinel.canEntityBeSeen(target)) {
                sentinel.setAttackTarget(null);
            } else if (tickCounter++ >= 10) {
                sentinel.attackEntityAsMob(target);
                sentinel.setAttackTarget(null);
            }
            sentinel.getLookHelper().setLookPosition(target.posX, target.posY + (double) target.getEyeHeight(), target.posZ, (float) sentinel.getHorizontalFaceSpeed(), (float) sentinel.getVerticalFaceSpeed());
        }
    }
}
