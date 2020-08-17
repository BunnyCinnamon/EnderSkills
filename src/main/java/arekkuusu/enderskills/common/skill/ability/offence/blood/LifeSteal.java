package arekkuusu.enderskills.common.skill.ability.offence.blood;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LifeSteal extends BaseAbility implements ISkillAdvancement {

    public LifeSteal() {
        super(LibNames.LIFE_STEAL, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner) || !isActionable(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        Capabilities.get(owner).ifPresent(capability -> {
            if (!SkillHelper.isActiveFrom(owner, this)) {
                if (!((IInfoCooldown) skillInfo).hasCooldown() && canActivate(owner)) {
                    if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                        abilityInfo.setCooldown(getCooldown(abilityInfo));
                    }
                    float heal = getHeal(abilityInfo);
                    NBTTagCompound compound = new NBTTagCompound();
                    NBTHelper.setEntity(compound, owner, "owner");
                    NBTHelper.setFloat(compound, "heal", heal);
                    SkillData data = SkillData.of(this)
                            .by(owner)
                            .with(INDEFINITE)
                            .put(compound)
                            .overrides(SkillData.Overrides.EQUAL)
                            .create();
                    apply(owner, data);
                    sync(owner, data);
                    sync(owner);

                    if (owner.world instanceof WorldServer) {
                        ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.LIFE_STEAL, SoundCategory.PLAYERS, 1.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
                    }
                }
            } else {
                SkillHelper.getActiveFrom(owner, this).ifPresent(data -> {
                    unapply(owner, data);
                    async(owner, data);
                });
            }
        });
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner)) return;
        if (tick % 20 == 0 && (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode)) {
            Capabilities.endurance(owner).ifPresent(capability -> {
                int drain = ModAttributes.ENDURANCE.getEnduranceDrain(this);
                if (capability.getEndurance() - drain >= 0) {
                    capability.setEndurance(capability.getEndurance() - drain);
                    capability.setEnduranceDelay(30);
                } else {
                    unapply(owner, data);
                    async(owner, data);
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
                float heal = NBTHelper.getFloat(data.nbt, "heal");
                attacker.heal(attacker.getMaxHealth() * heal);
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

                if (attacker.world instanceof WorldServer) {
                    ((WorldServer) attacker.world).playSound(null, attacker.posX, attacker.posY, attacker.posZ, ModSounds.LIFE_STEAL_ATTACK, SoundCategory.PLAYERS, 1.0F, (1.0F + (attacker.world.rand.nextFloat() - attacker.world.rand.nextFloat()) * 0.2F) * 0.7F);
                }
            });
        }
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public float getHeal(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().extra.heal, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.extra.globalPositiveEffect);
        return (float) (result * getEffectiveness());
    }

    public int getCooldown(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().cooldown, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalCooldown);
        return (int) (result * getEffectiveness());
    }

    public double getEffectiveness() {
        return Configuration.getSyncValues().effectiveness * CommonConfig.getSyncValues().skill.globalEffectiveness;
    }

    /*Advancement Section*/
    @Override
    @SideOnly(Side.CLIENT)
    public void addDescription(List<String> description) {
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(c -> {
            if (c.isOwned(this)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add("Hold SHIFT for stats.");
                } else {
                    c.getOwned(this).ifPresent(skillInfo -> {
                        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
                        description.clear();
                        description.add("Endurance Drain: " + ModAttributes.ENDURANCE.getEnduranceDrain(this));
                        description.add("");
                        if (abilityInfo.getLevel() >= getMaxLevel()) {
                            description.add("Max Level:");
                        } else {
                            description.add("Current Level:");
                        }
                        description.add("Cooldown: " + TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D) + "s");
                        description.add("Heal: " + TextHelper.format2FloatPoint(getHeal(abilityInfo) * 100) + "%");
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add("Next Level:");
                            description.add("Cooldown: " + TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D) + "s");
                            description.add("Heal: " + TextHelper.format2FloatPoint(getHeal(infoNew) * 100) + "%");
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getCostIncrement(EntityLivingBase entity, int total) {
        Optional<AdvancementCapability> optional = Capabilities.advancement(entity);
        if (optional.isPresent()) {
            AdvancementCapability advancement = optional.get();
            List<Skill> skillUnlockOrder = Arrays.asList(advancement.skillUnlockOrder);
            int index = skillUnlockOrder.indexOf(ModAbilities.BLEED);
            if (index == -1) {
                index = advancement.skillUnlockOrder.length;
            }
            return (int) (total * (1D + index * CommonConfig.getSyncValues().advancement.xp.costIncrement));
        }
        return total;
    }

    @Override
    public int getUpgradeCost(@Nullable AbilityInfo info) {
        int level = info != null ? getLevel(info) + 1 : 0;
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().advancement.upgrade, level, levelMax);
        return (int) (func * CommonConfig.getSyncValues().advancement.xp.globalCostMultiplier);
    }
    /*Advancement Section*/

    @Override
    public void initSyncConfig() {
        Configuration.getSyncValues().maxLevel = Configuration.getValues().maxLevel;
        Configuration.getSyncValues().cooldown = Configuration.getValues().cooldown;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        Configuration.getSyncValues().extra.heal = Configuration.getValues().extra.heal;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "extra.heal", Configuration.getValues().extra.heal);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().cooldown = NBTHelper.getArray(compound, "cooldown");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().extra.heal = NBTHelper.getArray(compound, "extra.heal");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.LIFE_STEAL)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.LIFE_STEAL)
        public static Values CONFIG = new Values();

        public static Values getValues() {
            return CONFIG;
        }

        @Config.Ignore
        protected static Values CONFIG_SYNC = new Values();

        public static Values getSyncValues() {
            return CONFIG_SYNC;
        }

        public static class Values {
            @Config.Comment("Skill specific extra Configuration")
            public final Extra extra = new Extra();
            @Config.Comment("Skill specific Advancement Configuration")
            public final Advancement advancement = new Advancement();

            @Config.Comment("Max level obtainable")
            @Config.RangeInt(min = 0)
            public int maxLevel = 4;

            @Config.Comment("Cooldown Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] cooldown = {
                    "(0+){7 * 20 + 3 * 20 * (1 - ((1 - (e^(-2.1 * (x/1)))) / (1 - e^(-2.1))))}",
                    "(2+){6 * 20 + 1 * 20 * (1- (((e^(0.1 * ((x-1) / (y-1))) - 1)/((e^0.1) - 1))))}",
                    "(4){(5 * 20)}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Extra {
                @Config.Comment("Heal Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
                public String[] heal = {
                        "(0+){0.01 + ((x / y) * (0.05 - 0.01))}"
                };
            }

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0){600}",
                        "(1+){((22070 / y) * (0 ^ (0 ^ x)))}",
                        "(50){((22070 / y) * (0 ^ (0 ^ x))) + ((22070 / y) * (0 ^ (0 ^ x))) * 0.1}"
                };
            }
        }
    }
}
