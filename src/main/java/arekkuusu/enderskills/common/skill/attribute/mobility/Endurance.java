package arekkuusu.enderskills.common.skill.attribute.mobility;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillActionableEvent;
import arekkuusu.enderskills.api.event.SkillActivateEvent;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Endurance extends BaseAttribute {

    //Vanilla Attribute
    public static final IAttribute MAX_ENDURANCE = new RangedAttribute(null, "enderskills.generic.maxEndurance", 0F, 0F, Float.MAX_VALUE).setDescription("Max Endurance").setShouldWatch(true);
    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier ENDURANCE_ATTRIBUTE = new DynamicModifier(
            "010bf31b-320d-4ef9-91ed-6f84adc38600",
            LibMod.MOD_ID + ":" + LibNames.ENDURANCE,
            Endurance.MAX_ENDURANCE,
            Constants.AttributeModifierOperation.ADD);
    //Vanilla Attribute Modifier for default Endurance attribute
    public static final DynamicModifier ENDURANCE_DEFAULT_ATTRIBUTE = new DynamicModifier(
            "010bf31b-320d-4ef9-91ed-6f84a3c38600",
            LibMod.MOD_ID + ":" + LibNames.ENDURANCE + "_default",
            Endurance.MAX_ENDURANCE,
            Constants.AttributeModifierOperation.ADD);

    public Endurance() {
        super(LibNames.ENDURANCE, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.ticksExisted % 20 != 0) return; //Slowdown cowboy! yee-haw!
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    ENDURANCE_ATTRIBUTE.apply(entity, DSLDefaults.getModifier(this, attributeInfo.getLevel()));
                });
            } else {
                if (ENDURANCE_ATTRIBUTE.remove(entity)) {
                    Capabilities.endurance(entity).ifPresent(enduranceCapability -> {
                        double amount = entity.getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue();
                        if (enduranceCapability.getEndurance() > amount) {
                            enduranceCapability.setEndurance(amount);
                            if (entity instanceof EntityPlayerMP) {
                                PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
                            }
                        }
                    });
                }
            }
        });
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase) {
            ((EntityLivingBase) event.getObject()).getAttributeMap().registerAttribute(MAX_ENDURANCE).setBaseValue(0F);
        }
    }

    @SubscribeEvent
    public void resetDefault(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityLivingBase) {
            ((EntityLivingBase) event.getEntity()).getEntityAttribute(MAX_ENDURANCE).setBaseValue(0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEnduranceTick(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntityLiving().getEntityWorld().isRemote) {
            Capabilities.endurance(event.getEntityLiving()).ifPresent(capability -> {
                double maxEndurance = event.getEntityLiving().getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue();
                if (capability.getEnduranceDelay() > 0) {
                    capability.setEnduranceDelay(capability.getEnduranceDelay() - 1);
                } else if (capability.getEndurance() < maxEndurance) {
                    double[] a = {CommonConfig.getSyncValues().skill.enduranceRegen};
                    Capabilities.get(event.getEntityLiving()).flatMap(aaa -> aaa.getOwned(this)).ifPresent(iii -> a[0] = DSLDefaults.getRegen(this, ((AttributeInfo) iii).getLevel()));
                    capability.setEnduranceDelay(a[0]); //Every half a second
                    capability.setEndurance(Math.min(capability.getEndurance() + (maxEndurance / (maxEndurance - capability.getEndurance())), maxEndurance));
                } else if (capability.getEndurance() > maxEndurance) {
                    double[] a = {CommonConfig.getSyncValues().skill.enduranceRegen};
                    Capabilities.get(event.getEntityLiving()).flatMap(aaa -> aaa.getOwned(this)).ifPresent(iii -> a[0] = DSLDefaults.getRegen(this, ((AttributeInfo) iii).getLevel()));
                    capability.setEnduranceDelay(a[0]); //Every half a second
                    capability.setEndurance(Math.max(capability.getEndurance() - (capability.getEndurance() / maxEndurance), 0D));
                }
                if (event.getEntityLiving() instanceof EntityPlayerMP) {
                    PacketHelper.sendEnduranceSync((EntityPlayerMP) event.getEntityLiving());
                }
            });
            ENDURANCE_DEFAULT_ATTRIBUTE.apply(event.getEntityLiving(), Configuration.LOCAL_VALUES.endurance);
        }
    }

    @SubscribeEvent
    public void onSkillShouldUse(SkillActionableEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.isCanceled()) return;
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.endurance(entity).ifPresent(capability -> {
            int level = Capabilities.get(entity).flatMap(a -> a.getOwned(this)).map(a -> a instanceof AbilityInfo ? ((AbilityInfo) a).getLevel() : a instanceof AttributeInfo ? ((AttributeInfo) a).getLevel() : 0).orElse(0);
            if (hasEnduranceDrain(event.getSkill(), level)) {
                int enduranceNeeded = getEnduranceDrain(event.getSkill(), level);
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
                    enduranceNeeded = 0;
                }
                if (capability.getEndurance() + capability.getAbsorption() < enduranceNeeded) {
                    event.setCanceled(true);
                }
            }
        });
    }

    @SubscribeEvent
    public void onSkillUse(SkillActivateEvent event) {
        if (isClientWorld(event.getEntityLiving()) || event.isCanceled()) return;
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.endurance(entity).ifPresent(capability -> {
            int level = Capabilities.get(entity).flatMap(a -> a.getOwned(this)).map(a -> a instanceof AbilityInfo ? ((AbilityInfo) a).getLevel() : a instanceof AttributeInfo ? ((AttributeInfo) a).getLevel() : 0).orElse(0);
            if (hasEnduranceDrain(event.getSkill(), level)) {
                int enduranceNeeded = getEnduranceDrain(event.getSkill(), level);
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
                    enduranceNeeded = 0;
                }
                if (capability.getEndurance() + capability.getAbsorption() < enduranceNeeded) {
                    event.setCanceled(true);
                    return;
                }
                double[] a = {CommonConfig.getSyncValues().skill.enduranceDelay};
                Capabilities.get(entity).flatMap(aaa -> aaa.getOwned(this)).ifPresent(iii -> a[0] = DSLDefaults.getDelay(this, ((AttributeInfo) iii).getLevel()));
                capability.drain(enduranceNeeded, a[0]);
                if (entity instanceof EntityPlayerMP) {
                    PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
                }
            }
        });
    }

    public boolean hasEnduranceDrain(Skill skill, int lvl) {
        return getEnduranceDrain(skill, lvl) != 0;
    }

    public int getEnduranceDrain(Skill skill, int lvl) {
        if (skill instanceof BaseAbility) {
            int endurance = DSLDefaults.getEndurance(skill, lvl);
            if (endurance == 0) {
                String skillRegistryName = Objects.requireNonNull(skill.getRegistryName()).toString();
                endurance = Configuration.LOCAL_VALUES.enduranceMap.getOrDefault(skillRegistryName, 0);
            }
            return endurance;
        }

        return 0;
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_MOBILITY_FOLDER + LibNames.ENDURANCE;

    @Config(modid = LibMod.MOD_ID, name = Endurance.CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static Configuration.Values LOCAL_VALUES = new Configuration.Values();
        public static Configuration.Values VALUES = new Configuration.Values();

        public static class Values {
            @Config.Comment("Default start endurance")
            public double endurance = 30D;

            @Config.Comment("Endurance drain by skill")
            public Map<String, Integer> enduranceMap = new HashMap<>(new ImmutableMap.Builder<String, Integer>()
                    //Defense-Light
                    .put(LibMod.MOD_ID + ":" + LibNames.CHARM, 6)
                    .put(LibMod.MOD_ID + ":" + LibNames.HEAL_AURA, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.POWER_BOOST, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.HEAL_OTHER, 12)
                    .put(LibMod.MOD_ID + ":" + LibNames.HEAL_SELF, 12)
                    .put(LibMod.MOD_ID + ":" + LibNames.NEARBY_INVINCIBILITY, 16)
                    //Defense-Earth
                    .put(LibMod.MOD_ID + ":" + LibNames.TAUNT, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.WALL, 9)
                    .put(LibMod.MOD_ID + ":" + LibNames.DOME, 9)
                    .put(LibMod.MOD_ID + ":" + LibNames.THORNY, 12)
                    .put(LibMod.MOD_ID + ":" + LibNames.SHOCKWAVE, 14)
                    .put(LibMod.MOD_ID + ":" + LibNames.ANIMATED_STONE_GOLEM, 16)
                    //Defense-Electric
                    .put(LibMod.MOD_ID + ":" + LibNames.SHOCKING_AURA, 1)
                    .put(LibMod.MOD_ID + ":" + LibNames.ELECTRIC_PULSE, 4)
                    .put(LibMod.MOD_ID + ":" + LibNames.MAGNETIC_PULL, 4)
                    .put(LibMod.MOD_ID + ":" + LibNames.POWER_DRAIN, 12)
                    .put(LibMod.MOD_ID + ":" + LibNames.ENERGIZE, 12)
                    .put(LibMod.MOD_ID + ":" + LibNames.VOLTAIC_SENTINEL, 16)
                    //Defense-Fire
                    .put(LibMod.MOD_ID + ":" + LibNames.FLARES, 1)
                    .put(LibMod.MOD_ID + ":" + LibNames.RING_OF_FIRE, 4)
                    .put(LibMod.MOD_ID + ":" + LibNames.BLAZING_AURA, 1)
                    .put(LibMod.MOD_ID + ":" + LibNames.OVERHEAT, 12)
                    .put(LibMod.MOD_ID + ":" + LibNames.WARM_HEART, 12)
                    .put(LibMod.MOD_ID + ":" + LibNames.HOME_STAR, 16)
                    //Mobility-Wind
                    .put(LibMod.MOD_ID + ":" + LibNames.DASH, 4)
                    .put(LibMod.MOD_ID + ":" + LibNames.EXTRA_JUMP, 2)
                    .put(LibMod.MOD_ID + ":" + LibNames.FOG, 16)
                    .put(LibMod.MOD_ID + ":" + LibNames.SMASH, 14)
                    .put(LibMod.MOD_ID + ":" + LibNames.HASTEN, 16)
                    .put(LibMod.MOD_ID + ":" + LibNames.SPEED_BOOST, 18)
                    //Mobility-Void
                    .put(LibMod.MOD_ID + ":" + LibNames.WARP, 6)
                    .put(LibMod.MOD_ID + ":" + LibNames.INVISIBILITY, 18)
                    .put(LibMod.MOD_ID + ":" + LibNames.HOVER, 2)
                    .put(LibMod.MOD_ID + ":" + LibNames.UNSTABLE_PORTAL, 14)
                    .put(LibMod.MOD_ID + ":" + LibNames.PORTAL, 16)
                    .put(LibMod.MOD_ID + ":" + LibNames.TELEPORT, 20)
                    //Offense-Void
                    .put(LibMod.MOD_ID + ":" + LibNames.SHADOW, 1)
                    .put(LibMod.MOD_ID + ":" + LibNames.GLOOM, 4)
                    .put(LibMod.MOD_ID + ":" + LibNames.SHADOW_JAB, 4)
                    .put(LibMod.MOD_ID + ":" + LibNames.GAS_CLOUD, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.GRASP, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.BLACK_HOLE, 18)
                    //Offense-Blood
                    .put(LibMod.MOD_ID + ":" + LibNames.BLEED, 1)
                    .put(LibMod.MOD_ID + ":" + LibNames.BLOOD_POOL, 6)
                    .put(LibMod.MOD_ID + ":" + LibNames.CONTAMINATE, 6)
                    .put(LibMod.MOD_ID + ":" + LibNames.LIFE_STEAL, 1)
                    .put(LibMod.MOD_ID + ":" + LibNames.SYPHON, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.SACRIFICE, 18)
                    //Offense-Wind
                    .put(LibMod.MOD_ID + ":" + LibNames.SLASH, 2)
                    .put(LibMod.MOD_ID + ":" + LibNames.PUSH, 4)
                    .put(LibMod.MOD_ID + ":" + LibNames.PULL, 4)
                    .put(LibMod.MOD_ID + ":" + LibNames.CRUSH, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.UPDRAFT, 6)
                    .put(LibMod.MOD_ID + ":" + LibNames.SUFFOCATE, 18)
                    //Offense-Fire
                    .put(LibMod.MOD_ID + ":" + LibNames.FIRE_SPIRIT, 1)
                    .put(LibMod.MOD_ID + ":" + LibNames.FLAMING_BREATH, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.FLAMING_RAIN, 10)
                    .put(LibMod.MOD_ID + ":" + LibNames.FOCUS_FLAME, 16)
                    .put(LibMod.MOD_ID + ":" + LibNames.FIREBALL, 16)
                    .put(LibMod.MOD_ID + ":" + LibNames.EXPLODE, 20)
                    //Offense-Light
                    .put(LibMod.MOD_ID + ":" + LibNames.RADIANT_RAY, 1)
                    .put(LibMod.MOD_ID + ":" + LibNames.LUMEN_WAVE, 8)
                    .put(LibMod.MOD_ID + ":" + LibNames.GLEAM_FLASH, 10)
                    .put(LibMod.MOD_ID + ":" + LibNames.SOLAR_LANCE, 16)
                    .put(LibMod.MOD_ID + ":" + LibNames.BARRAGE_WISPS, 16)
                    .put(LibMod.MOD_ID + ":" + LibNames.FINAL_FLASH, 20)
                    .build()
            );
        }

        public static DSL CONFIG = DSLFactory.create(Endurance.CONFIG_FILE);
    }
    /*Config Section*/
}
