package arekkuusu.enderskills.common.skill.ability.defense.light;

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
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;
import java.util.List;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INSTANT;

public class HealAura extends BaseAbility implements IScanEntities, IExpand, IFindEntity {

    public HealAura() {
        super(LibNames.HEAL_AURA, new Properties());
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
        double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
        double heal = DSLDefaults.getHeal(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        NBTHelper.setDouble(compound, "heal", heal);
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(INSTANT)
                .put(compound)
                .create();
        EntityPlaceableData spawn = new EntityPlaceableData(owner.world, owner, data, EntityPlaceableData.MIN_TIME);
        spawn.setPosition(owner.posX, owner.posY + owner.height / 2, owner.posZ);
        spawn.setRadius(range);
        spawn.spawnEntity();
        super.sync(owner);

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.HEAL_AURA);
    }

    //* Entity *//
    @Override
    public List<Entity> getScan(Entity source, @Nullable EntityLivingBase owner, SkillData skillData, double size) {
        return source.getEntityWorld().getEntitiesWithinAABB(Entity.class, source.getEntityBoundingBox(), TeamHelper.SELECTOR_ALLY.apply(owner));
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        super.apply(target, skillData);
        super.sync(target, skillData);

        SoundHelper.playSound(target.world, target.getPosition(), ModSounds.LIGHT_HIT);
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase target, SkillData data) {
        if (isClientWorld(target)) return;
        EnderSkills.getProxy().addToQueue(() -> {
            double heal = NBTHelper.getDouble(data.nbt, "heal");
            ModEffects.OVERHEAL.set(target, (float) (target.getMaxHealth() * heal));
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.LIGHT_DEFENSE_CONFIG + LibNames.HEAL_AURA;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
