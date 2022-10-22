package arekkuusu.enderskills.common.skill.ability.defense.fire;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.sounds.OverheatSound;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

public class Overheat extends BaseAbility implements ISkillAdvancement {

    public Overheat() {
        super(LibNames.OVERHEAT, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            int time = (int) arekkuusu.enderskills.api.event.SkillDurationEvent.getDuration(owner, this, getTime(abilityInfo));;
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(time)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            apply(owner, data);
            sync(owner, data);
            sync(owner);
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
        Minecraft.getMinecraft().getSoundHandler().playSound(new OverheatSound(entity));
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
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillDamage(SkillDamageEvent event) {
        if (event.getEntityLiving() == null) return;
        if (isClientWorld(event.getEntityLiving()) || event.getSkill() != ModEffects.BURNING) return;
        if (event.getAmount() <= 0) return;
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AbilityInfo attributeInfo = (AbilityInfo) skillInfo;
                    if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage.MULTIPLICATION) {
                        event.setAmount(event.getAmount() + event.getAmount() * getModifier(attributeInfo));
                    } else {
                        event.setAmount(event.getAmount() + getModifier(attributeInfo));
                    }
                });
            }
        });
    }

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public float getModifier(AbilityInfo info) {
        return (float) this.config.get(this, "DAMAGE", info.getLevel());
    }

    public int getCooldown(AbilityInfo info) {
        return (int) this.config.get(this, "COOLDOWN", info.getLevel());
    }

    public int getTime(AbilityInfo info) {
        return (int) this.config.get(this, "DURATION", info.getLevel());
    }

    /*Advancement Section*/
    @Override
    @SideOnly(Side.CLIENT)
    public void addDescription(List<String> description) {
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(c -> {
            if (c.isOwned(this)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add(TextHelper.translate("desc.stats.shift"));
                } else {
                    c.getOwned(this).ifPresent(skillInfo -> {
                        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
                        description.clear();
                        description.add(TextHelper.translate("desc.stats.endurance", String.valueOf(ModAttributes.ENDURANCE.getEnduranceDrain(this))));
                        description.add("");
                        if (abilityInfo.getLevel() >= getMaxLevel()) {
                            description.add(TextHelper.translate("desc.stats.level_max", getMaxLevel()));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current", abilityInfo.getLevel(), abilityInfo.getLevel() + 1));
                        }
                        description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage.MULTIPLICATION) {
                            description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifier(abilityInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                        } else {
                            description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifier(abilityInfo))));
                        }
                        if (abilityInfo.getLevel() < getMaxLevel()) {
                            if (!GuiScreen.isCtrlKeyDown()) {
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.ctrl"));
                            } else { //Copy info and set a higher level...
                                AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                                infoNew.setLevel(infoNew.getLevel() + 1);
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.level_next", abilityInfo.getLevel(), infoNew.getLevel()));
                                description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                if (Configuration.CONFIG_SYNC.applyAs == Configuration.Damage.MULTIPLICATION) {
                                    description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifier(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                                } else {
                                    description.add(TextHelper.translate("desc.stats.ap", TextHelper.format2FloatPoint(getModifier(infoNew))));
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.FLARES;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.FIRE_DEFENSE_CONFIG + LibNames.OVERHEAT;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
        NBTHelper.setEnum(compound, "applyAs", Configuration.CONFIG.applyAs);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
        Configuration.CONFIG_SYNC.applyAs = NBTHelper.getEnum(Configuration.Damage.class, compound, "applyAs");
    }

    @Override
    public void sigmaDic() {
        this.config = ConfigDSL.parse(Configuration.CONFIG_SYNC.dsl);
    }

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static final Configuration.Values CONFIG_SYNC = new Configuration.Values();
        public static final Configuration.Values CONFIG = new Configuration.Values();
        
        public enum Damage implements IStringSerializable {
            MULTIPLICATION,
            ADDITION;

            @Override
            public String getName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        public static class Values {
            
            public Damage applyAs = Damage.MULTIPLICATION;

            public String[] dsl = {
                    "",
                    "│ ",
                    "│ min_level: 0",
                    "│ max_level: 50",
                    "│ ",
                    "",
                    "┌ COOLDOWN (",
                    "│     shape: flat",
                    "│     start: 75s",
                    "│     end:   25s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {start}",
                    "│         end: 45s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end: 30s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         value: {end}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ DAMAGE (",
                    "│     shape: flat",
                    "│     start: 25%",
                    "│     end:   75%",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {start}",
                    "│         end: 30%",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end: 65%",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         value: {end}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ DURATION (",
                    "│     shape: flat",
                    "│     start: 6s",
                    "│     end:   24s",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {start}",
                    "│         end: 10s",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end: 12s",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         value: {end}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     start: 600",
                    "│     end:   infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         value: {start}",
                    "│     ]",
                    "│ ",
                    "│     {1 to 49} [",
                    "│         shape: multiply 4",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: f(x, y) -> 4 * x + 4 * x * 0.1",
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
