package arekkuusu.enderskills.common.skill.ability.offence.blood;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.TeamHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
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
import net.minecraft.entity.player.EntityPlayerMP;
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

import java.util.List;

public class LifeSteal extends BaseAbility implements ISkillAdvancement {

    public LifeSteal() {
        super(LibNames.LIFE_STEAL, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setTopLevelGetter(this::getTopLevel);
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

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public int getTopLevel() {
        return this.config.top_level;
    }

    public float getHeal(AbilityInfo info) {
        return (float) this.config.get(this, "HEAL", info.getLevel(), CommonConfig.CONFIG_SYNC.skill.globalPositiveEffect);
    }

    public int getCooldown(AbilityInfo info) {
        return (int) this.config.get(this, "COOLDOWN", info.getLevel());
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
                        description.add(TextHelper.translate("desc.stats.endurance", String.valueOf(ModAttributes.ENDURANCE.getEnduranceDrain(this, abilityInfo.getLevel()))));
                        description.add("");
                        if (abilityInfo.getLevel() >= getMaxLevel()) {
                            description.add(TextHelper.translate("desc.stats.level_max", getMaxLevel()));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current", abilityInfo.getLevel(), abilityInfo.getLevel() + 1));
                        }
                        description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.heal", TextHelper.format2FloatPoint(getHeal(abilityInfo) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
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
                                description.add(TextHelper.translate("desc.stats.heal", TextHelper.format2FloatPoint(getHeal(infoNew) * 100), TextHelper.getTextComponent("desc.stats.suffix_percentage")));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.BLEED;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }

    @Override
    public int getEndurance(int lvl) {
        return (int) this.config.get(this, "ENDURANCE", lvl);
    }

    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.BLOOD_OFFENCE_CONFIG + LibNames.LIFE_STEAL;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
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

        public static class Values {

            public String[] dsl = {
                    "",
                    "┌ v1.0",
                    "│ ",
                    "├ min_level: 0",
                    "├ max_level: 50",
                    "└ ",
                    "",
                    "┌ COOLDOWN (",
                    "│     shape: none",
                    "│     min: 8s",
                    "└ )",
                    "",
                    "┌ HEAL (",
                    "│     shape: flat",
                    "│     min: 7.5%",
                    "│     max: 20%",
                    "│ ",
                    "│     {0 to 25} [",
                    "│         shape: ramp negative",
                    "│         start: {min}",
                    "│         end:   10%",
                    "│     ]",
                    "│ ",
                    "│     {25 to 49} [",
                    "│         shape: ramp positive",
                    "│         start: {0 to 25}",
                    "│         end:   15%",
                    "│     ]",
                    "│ ",
                    "│     {50} [",
                    "│         shape: none",
                    "│         return: {max}",
                    "│     ]",
                    "└ )",
                    "",
                    "┌ ENDURANCE (",
                    "│     shape: none",
                    "│     value: 1",
                    "└ )",
                    "",
                    "┌ XP (",
                    "│     shape: flat",
                    "│     min: 0",
                    "│     max: infinite",
                    "│ ",
                    "│     {0} [",
                    "│         shape: none",
                    "│         return: 600",
                    "│     ]",
                    "│ ",
                    "│     {1 to 50} [",
                    "│         shape: multiply 8",
                    "│     ]",
                    "└ )",
                    "",
            };
        }
    }
    /*Config Section*/
}
