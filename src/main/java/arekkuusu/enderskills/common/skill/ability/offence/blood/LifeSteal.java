package arekkuusu.enderskills.common.skill.ability.offence.blood;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INDEFINITE;

public class LifeSteal extends BaseAbility {

    public LifeSteal() {
        super(LibNames.LIFE_STEAL, new Properties());
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
                double heal = DSLDefaults.getHeal(this, level);
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setEntity(compound, owner, "owner");
                NBTHelper.setDouble(compound, "heal", heal);
                SkillData data = SkillData.of(this)
                        .by(owner)
                        .with(INDEFINITE)
                        .put(compound)
                        .overrides(SkillData.Overrides.EQUAL)
                        .create();
                super.apply(owner, data);
                super.sync(owner, data);
                super.sync(owner);

                SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.LIFE_STEAL);
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
        if (isClientWorld(owner)) {
            for (int i = 0; i < 4; i++) {
                Vec3d vec = owner.getPositionVector();
                double posX = vec.x + owner.world.rand.nextDouble() - 0.5D;
                double posY = vec.y + owner.world.rand.nextDouble() * owner.height;
                double posZ = vec.z + owner.world.rand.nextDouble() - 0.5D;
                EnderSkills.getProxy().spawnParticle(owner.world, new Vec3d(posX, posY, posZ), new Vec3d(0, -0.1, 0), 2F, 50, 0x8A0303, ResourceLibrary.PLUS);
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
        if (isClientWorld(event.getEntityLiving())) return;
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || source instanceof SkillDamageSource || event.getAmount() <= 0)
            return;
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        EntityLivingBase attacked = event.getEntityLiving();
        if (TeamHelper.SELECTOR_ENEMY.apply(attacker).test(attacked)) {
            SkillHelper.getActiveFrom(attacker, this).ifPresent(data -> {
                double heal = NBTHelper.getDouble(data.nbt, "heal");
                attacker.heal((float) (attacker.getMaxHealth() * heal));
                {
                    Vec3d vec = attacked.getPositionVector();
                    double posX = vec.x;
                    double posY = vec.y + attacked.height + 0.5D;
                    double posZ = vec.z;
                    EnderSkills.getProxy().spawnParticle(attacked.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 3, 50, 0x8A0303, ResourceLibrary.MINUS);
                }
                {
                    Vec3d vec = attacker.getPositionVector();
                    double posX = vec.x;
                    double posY = vec.y + attacker.height + 0.5D;
                    double posZ = vec.z;
                    EnderSkills.getProxy().spawnParticle(attacker.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 3, 50, 0x8A0303, ResourceLibrary.PLUS);
                }

                SoundHelper.playSound(attacker.world, attacker.getPosition(), ModSounds.LIFE_STEAL_ATTACK);
            });
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLOOD_OFFENCE_CONFIG + LibNames.LIFE_STEAL;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
