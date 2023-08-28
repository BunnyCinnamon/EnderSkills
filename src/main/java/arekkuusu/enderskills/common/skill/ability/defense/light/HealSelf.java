package arekkuusu.enderskills.common.skill.ability.defense.light;

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
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INSTANT;

public class HealSelf extends BaseAbility {

    public HealSelf() {
        super(LibNames.HEAL_SELF, new Properties());
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
        double heal = DSLDefaults.getHeal(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setDouble(compound, "heal", heal);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(INSTANT)
                .put(compound)
                .create();
        apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.HEAL_SELF);
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        EnderSkills.getProxy().addToQueue(() -> {
            double heal = NBTHelper.getDouble(data.nbt, "heal");
            ModEffects.OVERHEAL.set(entity, (float) (entity.getMaxHealth() * heal));
        });
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x;
            double posY = vec.y + entity.height + 0.5D;
            double posZ = vec.z;
            EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.1, 0), 1, 50, 0x58DB11, ResourceLibrary.PLUS);
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_DEFENSE_CONFIG + LibNames.HEAL_SELF;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
