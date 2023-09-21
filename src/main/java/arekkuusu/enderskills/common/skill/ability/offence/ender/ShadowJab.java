package arekkuusu.enderskills.common.skill.ability.offence.ender;

import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.client.sounds.ShadowJabSound;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShadowJab extends BaseAbility {

    public ShadowJab() {
        super(LibNames.SHADOW_JAB, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
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
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
        double damage = DSLDefaults.getDamage(this, level);
        double dot = DSLDefaults.getDamageOverTime(this, level);
        int dotDuration = DSLDefaults.triggerDamageDuration(owner, this, level).getAmount();

        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setNBT(compound, "list", new NBTTagList());
        NBTHelper.setDouble(compound, "dot", dot);
        NBTHelper.setDouble(compound, "dotDuration", dotDuration);
        NBTHelper.setDouble(compound, "range", range);
        NBTHelper.setDouble(compound, "damage", damage);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) {
            makeSound(entity);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new ShadowJabSound(entity));
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        NBTHelper.getNBTList(data.nbt, "list").ifPresent(list -> {
            double range = data.nbt.getDouble("range");
            List<UUID> uuids = new ArrayList<>();
            list.iterator().forEachRemaining(nbt -> {
                uuids.add(((NBTTagCompound) nbt).getUniqueId("uuid"));
            });
            RayTraceHelper.getEntitiesInCone(owner, range, 40, TeamHelper.SELECTOR_ENEMY.apply(owner)).forEach(target -> {
                if (target instanceof EntityLivingBase) {
                    if (!uuids.contains(target.getUniqueID())) {
                        //Apply initial damage
                        double damage = data.nbt.getDouble("damage");
                        SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
                        SkillDamageEvent event = new SkillDamageEvent(owner, this, source, damage);
                        MinecraftForge.EVENT_BUS.post(event);
                        target.attackEntityFrom(event.getSource(), event.toFloat());
                        // Add entity to exception list
                        NBTTagCompound nbt = new NBTTagCompound();
                        nbt.setUniqueId("uuid", target.getUniqueID());
                        list.appendTag(nbt);

                        if (owner.world instanceof WorldServer) {
                            ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.VOID_HIT, SoundCategory.PLAYERS, 1.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
                        }
                    }
                    //Add effect
                    ModEffects.VOIDED.set((EntityLivingBase) target, data);
                    ModEffects.SLOWED.set((EntityLivingBase) target, data, 0.6D);
                }
            });
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_OFFENCE_CONFIG + LibNames.SHADOW_JAB;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
