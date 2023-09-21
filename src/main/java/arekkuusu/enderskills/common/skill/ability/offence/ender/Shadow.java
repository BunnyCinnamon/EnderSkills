package arekkuusu.enderskills.common.skill.ability.offence.ender;

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
import arekkuusu.enderskills.common.entity.EntityShadow;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Optional;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INDEFINITE;

public class Shadow extends BaseAbility {

    public Shadow() {
        super(LibNames.SHADOW, new Properties());
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
                float mirror = DSLDefaults.getDamageMimicry(this, level);
                EntityShadow shadow = new EntityShadow(owner.world);
                shadow.setPosition(owner.posX, owner.posY, owner.posZ);
                shadow.setOwnerId(owner.getUniqueID());
                shadow.setMirrorDamage(mirror);
                shadow.spawn();
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setEntity(compound, shadow, "shadow");
                NBTHelper.setFloat(compound, "mirror", mirror);
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(INDEFINITE)
                        .put(compound)
                        .overrides(SkillData.Overrides.EQUAL)
                        .create();
                apply(owner, data);
                super.sync(owner, data);
                super.sync(owner);

                SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.SHADOW);
            }
        } else {
            SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                super.unapply(owner, data);
                super.async(owner, data);
            });
        }
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        EntityShadow shadow = NBTHelper.getEntity(EntityShadow.class, data.nbt, "shadow");
        if (shadow != null) {
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
            super.unapply(owner, data);
            super.async(owner, data);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        DamageSource source = event.getSource();
        if (source.getDamageType().equals("shadow") || source.getDamageType().equals(DAMAGE_DOT_TYPE)) return;
        if (!(source.getTrueSource() instanceof EntityLivingBase) || event.getAmount() <= 0) return;
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        SkillHelper.getActiveFrom(attacker, this).ifPresent(data -> {
            Optional.ofNullable(NBTHelper.getEntity(EntityShadow.class, data.nbt, "shadow")).ifPresent(shadow -> {
                float mirror = NBTHelper.getFloat(data.nbt, "mirror");
                shadow.addAttack(event.getEntityLiving(), event.getAmount() + (event.getAmount() * mirror));
            });
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_OFFENCE_CONFIG + LibNames.SHADOW;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
