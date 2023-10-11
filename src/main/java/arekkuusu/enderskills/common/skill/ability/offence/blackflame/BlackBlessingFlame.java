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
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.sounds.FireSpiritSound;
import arekkuusu.enderskills.client.sounds.FireSpiritSound2;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INDEFINITE;

public class BlackBlessingFlame extends BaseAbility {

    public BlackBlessingFlame() {
        super(LibNames.BLACK_BLESSING_FLAME, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
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
                double doT = DSLDefaults.getDamageOverTime(this, level);
                double true_dot = DSLDefaults.getTrueDamageOverTime(this, level);
                int time = DSLDefaults.triggerDamageDuration(owner, this, level).getAmount();
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setDouble(compound, "dot", doT);
                NBTHelper.setDouble(compound, "true_dot", true_dot);
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
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) {
            makeSound(entity);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new FireSpiritSound(entity));
        Minecraft.getMinecraft().getSoundHandler().playSound(new FireSpiritSound2(entity));
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) {
            if (owner.world.rand.nextDouble() < 0.5D && ClientProxy.canParticleSpawn()) {
                Vec3d vec = owner.getPositionVector();
                double posX = vec.x + owner.world.rand.nextDouble() - 0.5D;
                double posY = vec.y + owner.world.rand.nextDouble() * owner.height;
                double posZ = vec.z + owner.world.rand.nextDouble() - 0.5D;
                owner.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
                owner.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
                owner.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
                owner.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
                EnderSkills.getProxy().spawnParticle(owner.world, new Vec3d(posX, posY, posZ), new Vec3d((1D - 2D * owner.world.rand.nextDouble()) * 0.05, (1D - 2D * owner.world.rand.nextDouble()) * 0.05, (1D - 2D * owner.world.rand.nextDouble()) * 0.05), 2F, 10, 0xFFFFFF, ResourceLibrary.GLOW_NEGATIVE);
                EnderSkills.getProxy().spawnParticle(owner.world, new Vec3d(posX, posY, posZ), new Vec3d((1D - 2D * owner.world.rand.nextDouble()) * 0.05, (1D - 2D * owner.world.rand.nextDouble()) * 0.05, (1D - 2D * owner.world.rand.nextDouble()) * 0.05), 2F, 10, 0xFFFFFF, ResourceLibrary.GLOW_NEGATIVE);
            }
        } else if (tick % 20 == 0 && (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode)) {
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
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || SkillHelper.isSkillDamage(event.getSource())) return;
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || event.getAmount() <= 0) return;
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        EntityLivingBase target = event.getEntityLiving();
        if (TeamHelper.SELECTOR_ENEMY.apply(attacker).test(target)) {
            SkillHelper.getActiveFrom(attacker, ModAbilities.BLACK_BLESSING_FLAME).ifPresent(data -> {
                if (!SkillHelper.isActive(target, ModEffects.BLACK_FLAME)) {
                    SoundHelper.playSound(target.world, target.getPosition(), ModSounds.FIRE_HIT);
                }
                ModEffects.BLACK_FLAME.set(target, data);
            });
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLACK_FLAME_OFFENCE_CONFIG + LibNames.BLACK_BLESSING_FLAME;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
