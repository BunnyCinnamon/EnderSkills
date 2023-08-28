package arekkuusu.enderskills.common.skill.ability.defense.fire;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.sounds.WarmHeartSound;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WarmHeart extends BaseAbility {

    public WarmHeart() {
        super(LibNames.WARM_HEART, new Properties());
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
        int time = DSLDefaults.triggerHealDuration(owner, this, level).getAmount();
        double heal = DSLDefaults.getHeal(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "heal", heal);
        NBTHelper.setDouble(compound, "time", time);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        super.apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        if (isClientWorld(owner)) {
            makeSound(owner);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new WarmHeartSound(entity));
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) {
            float particleProgress = Math.min(((float) tick / ((float) data.time / 3F)), 1F);
            for (int i = 0; i < 3; i++) {
                if (entity.world.rand.nextDouble() < 0.9D * particleProgress && ClientProxy.canParticleSpawn()) {
                    Vec3d vec = entity.getPositionVector();
                    double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
                    double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
                    double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
                    float particleScale = 0.25F + 2F * particleProgress;
                    Vector speedVec = Vector.Up.rotateRandom(entity.world.rand, 70F).multiply(0.15D * entity.world.rand.nextDouble());
                    EnderSkills.getProxy().spawnParticleLuminescence(entity.world, new Vec3d(posX, posY, posZ), speedVec.toVec3d(), particleScale, 30, ResourceLibrary.GLOW);
                    if (ClientProxy.canParticleSpawn()) {
                        double motionX = (entity.world.rand.nextDouble() - 0.5D) * 0.25D;
                        double motionZ = (entity.world.rand.nextDouble() - 0.5D) * 0.25D;
                        entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, motionX, 0.01D, motionZ);
                    }
                }
            }
            {
                Vec3d vec = entity.getPositionVector();
                double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
                double posY = vec.y + entity.world.rand.nextDouble() * entity.height;
                double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
                EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.1, 0), 1, 50, 0x58DB11, ResourceLibrary.PLUS);
            }
        } else EnderSkills.getProxy().addToQueue(() -> {
            double heal = NBTHelper.getDouble(data.nbt, "heal");
            double time = NBTHelper.getDouble(data.nbt, "time");
            ModEffects.OVERHEAL.set(entity, (float) (entity.getMaxHealth() * (heal / time)));
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.FIRE_DEFENSE_CONFIG + LibNames.WARM_HEART;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
