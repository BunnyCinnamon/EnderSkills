package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.entity.data.SkillExtendedData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityCrush extends Entity {

    public static final DataParameter<SkillExtendedData> DATA = EntityDataManager.createKey(EntityCrush.class, SkillExtendedData.SERIALIZER);
    public static final DataParameter<Float> SYNC_SIZE = EntityDataManager.createKey(EntityCrush.class, DataSerializers.FLOAT);
    public int warmupDelayTicks;
    public boolean sentSpikeEvent;
    public int lifeTicks;
    public boolean clientSideAttackStarted;
    public float damage;

    public EntityCrush(World worldIn) {
        super(worldIn);
        this.lifeTicks = 22;
        setSize(1F, 0F);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(DATA, new SkillExtendedData(null));
        this.dataManager.register(SYNC_SIZE, 0F);
    }

    @Override
    public void onUpdate() {
        if (width != getSize()) {
            setSize(getSize(), 0);
        }
        super.onUpdate();
        if (this.world.isRemote) {
            if (this.clientSideAttackStarted) {
                --this.lifeTicks;
                if (this.lifeTicks == 14) {
                    for (int i = 0; i < 12; ++i) {
                        double d0 = this.posX + (this.rand.nextDouble() * 2.0D - 1.0D) * (double) this.width * 0.5D;
                        double d1 = this.posY + 0.05D + this.rand.nextDouble() * 1.0D;
                        double d2 = this.posZ + (this.rand.nextDouble() * 2.0D - 1.0D) * (double) this.width * 0.5D;
                        double d3 = (this.rand.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        double d4 = 0.3D + this.rand.nextDouble() * 0.3D;
                        double d5 = (this.rand.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        this.world.spawnParticle(EnumParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
                    }
                }
            }
        } else if (--this.warmupDelayTicks < 0) {
            SkillData data = getData();
            EntityLivingBase owner = SkillHelper.getOwner(data);
            if (this.warmupDelayTicks == -8) {
                for (EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox(), TeamHelper.SELECTOR_ENEMY.apply(owner))) {
                    ModAbilities.CRUSH.apply(target, data);
                }
            }
            if (!this.sentSpikeEvent) {
                this.world.setEntityState(this, (byte) 4);
                this.sentSpikeEvent = true;
            }

            if (--this.lifeTicks < 0) {
                this.setDead();
            }
        }
        if (this.lifeTicks > 12) {
            double amount = getSize() / 10D;
            setEntityBoundingBox(getEntityBoundingBox().expand(0, amount, 0));
        }
    }

    @Nullable
    public EntityLivingBase getEntityByUUID(UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid) && entity instanceof EntityLivingBase)
                return (EntityLivingBase) entity;
        }
        return null;
    }

    private void damage(EntityLivingBase owner, EntityLivingBase target) {
        SkillDamageSource damageSource = new SkillDamageSource("skill", owner);
        SkillDamageEvent event = new SkillDamageEvent(owner, ModAbilities.CRUSH, damageSource, damage);
        MinecraftForge.EVENT_BUS.post(event);
        target.attackEntityFrom(event.getSource(), event.toFloat());
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        super.handleStatusUpdate(id);

        if (id == 4) {
            this.clientSideAttackStarted = true;

            if (!this.isSilent()) {
                this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.EVOCATION_FANGS_ATTACK, this.getSoundCategory(), 1.0F, this.rand.nextFloat() * 0.2F + 0.85F, false);
                this.world.playSound(this.posX, this.posY, this.posZ, ModSounds.CRUSH, this.getSoundCategory(), 1.0F, this.rand.nextFloat() * 0.2F + 0.85F, false);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public float getAnimationProgress(float partialTicks) {
        if (!this.clientSideAttackStarted) {
            return 0.0F;
        } else {
            int i = this.lifeTicks - 2;
            return i <= 0 ? 1.0F : 1.0F - ((float) i - partialTicks) / 20.0F;
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

    public void setData(SkillData data) {
        this.dataManager.set(DATA, new SkillExtendedData(data));
    }

    public SkillData getData() {
        return this.dataManager.get(DATA).data;
    }

    public float getSize() {
        return this.dataManager.get(SYNC_SIZE);
    }

    public void setSize(float size) {
        this.dataManager.set(SYNC_SIZE, size);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }

    public void setYawFrom(EntityLivingBase owner) {
        float f = (float) MathHelper.atan2(owner.posZ - posZ, owner.posX - posX);
        this.rotationYaw = f * (180F / (float) Math.PI);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("data")) {
            setData(new SkillData(compound.getCompoundTag("data")));
        }
        setSize(compound.getFloat("size"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (getData() != null) {
            compound.setTag("data", getData().serializeNBT());
        }
        compound.setFloat("size", getSize());
    }
}
