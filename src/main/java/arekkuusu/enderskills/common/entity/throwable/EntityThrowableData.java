package arekkuusu.enderskills.common.entity.throwable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.common.entity.data.IImpact;
import arekkuusu.enderskills.common.entity.data.SkillExtendedData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityThrowableData extends EntityThrowableCustom {

    public static final DataParameter<SkillExtendedData> DATA = EntityDataManager.createKey(EntityThrowableData.class, SkillExtendedData.SERIALIZER);

    public EntityThrowableData(World worldIn) {
        super(worldIn);
    }

    public EntityThrowableData(World worldIn, EntityLivingBase owner, double distance, SkillData data, boolean gravity) {
        super(worldIn, owner, (int) distance);
        setNoGravity(!gravity);
        setData(data);
    }

    @Override
    public void entityInit() {
        super.entityInit();
        this.dataManager.register(DATA, new SkillExtendedData(null));
    }

    @Override
    public void onImpact(RayTraceResult result) {
        if (getData().skill instanceof IImpact) {
            EntityLivingBase thrower = getEntityByUUID(getOwnerId());
            ((IImpact) getData().skill).onImpact(this, thrower, getData().copy(), result);
        }
    }

    public void setData(SkillData data) {
        this.dataManager.set(DATA, new SkillExtendedData(data));
    }

    public SkillData getData() {
        return this.dataManager.get(DATA).data;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setData(new SkillData(compound.getCompoundTag("data")));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("data", getData().serializeNBT());
    }

    public void throwAndSpawn() {
        EntityLivingBase thrower = getEntityByUUID(getOwnerId());
        if (thrower != null) {
            shoot(thrower, thrower.rotationPitch, thrower.rotationYaw, 0F, 3F, 0F);
            thrower.world.spawnEntity(this);
        }
    }

    public static void throwFor(EntityLivingBase owner, double distance, SkillData data, boolean gravity) {
        EntityThrowableData throwable = new EntityThrowableData(owner.world, owner, distance, data, gravity);
        throwable.setOwnerId(owner.getUniqueID());
        throwable.throwAndSpawn();
    }

    public static void throwFor(EntityLivingBase owner, EntityLivingBase target, double distance, SkillData data, boolean gravity) {
        EntityThrowableData throwable = new EntityThrowableData(owner.world, owner, distance, data, gravity);
        throwable.setOwnerId(owner.getUniqueID());
        throwable.setFollowId(target.getUniqueID());
        throwable.throwAndSpawn();
    }

    public static void throwForTarget(EntityLivingBase owner, double distance, SkillData data, boolean gravity) {
        EntityThrowableData throwable = new EntityThrowableData(owner.world, owner, distance, data, gravity);
        throwable.setOwnerId(owner.getUniqueID());
        RayTraceHelper.getEntityLookedAt(owner, distance)
                .filter(e -> e instanceof EntityLivingBase)
                .map(e -> (EntityLivingBase) e)
                .ifPresent(e -> throwable.setFollowId(e.getUniqueID()));
        throwable.throwAndSpawn();
    }
}
