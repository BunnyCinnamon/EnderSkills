package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityWisp extends EntityThrowableData {

    static final List<String> aaaaaaaa = new ArrayList<>();
    static synchronized void add(String a) {
        aaaaaaaa.add(a);
    }
    static synchronized void rem(String a) {
        aaaaaaaa.remove(a);
    }
    static synchronized boolean check(String a){
        return !aaaaaaaa.contains(a);
    }
    public int tickDelay;
    public String uuid;
    float fov = 45;

    public EntityWisp(World worldIn) {
        super(worldIn);
    }

    public EntityWisp(World worldIn, EntityLivingBase owner, double distance, SkillData data, boolean gravity, UUID uuid) {
        super(worldIn, owner, distance, data, gravity);
        this.rotationYaw = owner.rotationYaw;
        this.rotationPitch = owner.rotationPitch;
        this.uuid = uuid.toString();
    }

    @Override
    public void onUpdate() {
        if (getFollowId() != null) {
            if(!world.isRemote) {
                if(rand.nextDouble() < 0.6D) {
                    this.motionX += (rand.nextDouble() * 2 - 1) * 0.25D;
                    this.motionY += (rand.nextDouble() * 2 - 1) * 0.25D;
                    this.motionZ += (rand.nextDouble() * 2 - 1) * 0.25D;
                }
            }
            super.onUpdate();
        } else {
            if(!world.isRemote) {
                if (tickDelay > getData().nbt.getInteger("delay")) {
                    EntityLivingBase owner = getEntityByUUID(getOwnerId());
                    if(owner != null && getFollowId() == null) {
                        Vec3d vector = startVector.add(getLookVec().normalize().scale(-1).scale(3));
                        List<Entity> list = RayTraceHelper.getEntitiesInCone2(this, vector, getDistance() + 3, fov, TeamHelper.getEnemyTeamPredicate(owner));
                        if (!list.isEmpty()) {
                            Entity target = list.get(0);
                            for (Entity entity : list) {
                                if (entity.getDistance(owner) < target.getDistance(owner)) {
                                    target = entity;
                                }
                            }
                            if(target instanceof EntityLivingBase) {
                                setFollowId(target.getUniqueID());
                                if (world instanceof WorldServer && check(uuid)) {
                                    add(uuid);
                                    ((WorldServer) world).playSound(null, posX, posY, posZ, ModSounds.BARRAGE_WHISPS_RELEASE, SoundCategory.PLAYERS, 1.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
                                }
                            }
                        }
                    }
                    this.motionX = 0;
                    this.motionY = 0;
                    this.motionZ = 0;
                    if(tickDelay > getData().nbt.getInteger("delay") + 5 * 20) setDead();
                    if(tickDelay % 20 == 0) {
                        fov += 45;
                        rem(uuid);
                    }
                } else {
                    move(MoverType.SELF, motionX, motionY, motionZ);
                    markVelocityChanged();
                }
            }
            this.tickDelay++;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setString("uuidaaa", uuid);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        uuid = compound.getString("uuidaaa");
    }
}
