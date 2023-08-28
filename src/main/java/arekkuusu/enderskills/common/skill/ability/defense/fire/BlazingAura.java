package arekkuusu.enderskills.common.skill.ability.defense.fire;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.configuration.parser.DSLParser;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.sounds.BlazingAuraSound;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INDEFINITE;

public class BlazingAura extends BaseAbility {

    public BlazingAura() {
        super(LibNames.BLAZING_AURA, new Properties());
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner) || !isActionable(owner)) return;

        if (!SkillHelper.isActiveFrom(owner, this)) {
            if (hasNoCooldown(skillInfo) && canActivate(owner)) {
                InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
                InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
                int level = infoUpgradeable.getLevel();
                if (infoCooldown.canSetCooldown(owner)) {
                    infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
                }

                //
                double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
                double dot = DSLDefaults.getDamageOverTime(this, level);
                int time = DSLDefaults.triggerDamageDuration(owner, this, level).getAmount();
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setDouble(compound, "range", range);
                NBTHelper.setDouble(compound, "dot", dot);
                NBTHelper.setInteger(compound, "dotDuration", time);
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(INDEFINITE)
                        .put(compound)
                        .overrides(SkillData.Overrides.EQUAL)
                        .create();
                super.apply(owner, data);
                super.sync(owner, data);
                super.sync(owner);
            }
        } else {
            SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                super.unapply(owner, data);
                super.async(owner, data);
            });
        }
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        if (isClientWorld(owner)) {
            makeSound(owner);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new BlazingAuraSound(entity));
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        double distance = NBTHelper.getDouble(data.nbt, "range") * MathHelper.clamp(((double) tick / 10D), 0D, 1D);
        if (!isClientWorld(owner)) {
            Vec3d pos = owner.getPositionVector();
            pos = new Vec3d(pos.x, pos.y + owner.height / 2, pos.z);
            Vec3d min = pos.subtract(0.5D, 0.5D, 0.5D);
            Vec3d max = pos.addVector(0.5D, 0.5D, 0.5D);
            AxisAlignedBB bb = new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
            owner.world.getEntitiesWithinAABB(EntityLivingBase.class, bb.grow(distance), TeamHelper.SELECTOR_ENEMY.apply(owner)).forEach(target -> {
                if (!SkillHelper.isActive(target, ModEffects.BURNING)) {

                    SoundHelper.playSound(target.world, target.getPosition(), ModSounds.FIRE_HIT);
                    ModEffects.BURNING.set(target, data);
                }
            });
            if (tick % 20 == 0 && (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode)) {
                Capabilities.endurance(owner).ifPresent(capability -> {
                    int level = Capabilities.get(owner).flatMap(a -> a.getOwned(this)).map(a -> ((AbilityInfo) a).getLevel()).orElse(0);
                    int drain = ModAttributes.ENDURANCE.getEnduranceDrain(this, level);
                    if (capability.getEndurance() - drain >= 0) {
                        capability.setEndurance(capability.getEndurance() - drain);
                        capability.setEnduranceDelay(30);
                        if (owner instanceof EntityPlayerMP) {
                            PacketHelper.sendEnduranceSync((EntityPlayerMP) owner);
                        }
                    } else {
                        super.unapply(owner, data);
                        super.async(owner, data);
                    }
                });
            }
        } else {
            for (int i = 0; i < 6; i++) {
                if (owner.world.rand.nextDouble() < 0.4D && ClientProxy.canParticleSpawn()) {
                    Vec3d vec = owner.getPositionVector();
                    double posX = vec.x + distance * (owner.world.rand.nextDouble() - 0.5);
                    double posY = (vec.y + owner.height / 2) + distance * (owner.world.rand.nextDouble() - 0.5);
                    double posZ = vec.z + distance * (owner.world.rand.nextDouble() - 0.5);
                    double motionX = (owner.world.rand.nextDouble() - 0.5D) * 0.25D;
                    double motionZ = (owner.world.rand.nextDouble() - 0.5D) * 0.25D;
                    owner.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, motionX, 0.01D, motionZ);
                }
            }
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.FIRE_DEFENSE_CONFIG + LibNames.BLAZING_AURA;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
