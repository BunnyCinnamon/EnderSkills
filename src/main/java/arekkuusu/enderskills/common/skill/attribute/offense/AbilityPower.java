package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.helper.MathUtil;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

public class AbilityPower extends BaseAttribute {

    //Vanilla Attribute
    public static final IAttribute ABILITY_POWER = new RangedAttribute(null, "enderskills.generic.abilityPower", 0F, 0F, Float.MAX_VALUE).setDescription("Ability Power").setShouldWatch(true);
    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier ABILITY_POWER_ATTRIBUTE = new DynamicModifier(
            "010bf31b-320d-4ef9-91ed-6f84adc38600",
            LibMod.MOD_ID + ":" + LibNames.ABILITY_POWER,
            AbilityPower.ABILITY_POWER,
            Constants.AttributeModifierOperation.ADD);

    public AbilityPower() {
        super(LibNames.ABILITY_POWER, new Properties());
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
                    ABILITY_POWER_ATTRIBUTE.apply(entity, DSLDefaults.getModifier(ModAttributes.ABILITY_POWER, attributeInfo.getLevel()));
                });
            } else {
                ABILITY_POWER_ATTRIBUTE.remove(entity);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillDamage(SkillDamageEvent event) {
        if (event.getEntityLiving() == null) return;
        if (isClientWorld(event.getEntityLiving()) || !SkillHelper.isSkillDamage(event.getSource())) return;
        if (event.getAmount() <= 0) return;
        EntityLivingBase entity = event.getEntityLiving();
        double amount = entity.getEntityAttribute(AbilityPower.ABILITY_POWER).getAttributeValue();
        if (!MathUtil.fuzzyEqual(0, amount)) {
            if (Configuration.LOCAL_VALUES.applyAs == Configuration.Damage.MULTIPLICATION) {
                event.setAmount(event.getAmount() + event.getAmount() * amount);
            } else {
                event.setAmount(event.getAmount() + amount);
            }
        }
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase)
            ((EntityLivingBase) event.getObject()).getAttributeMap().registerAttribute(ABILITY_POWER).setBaseValue(0F);
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.ABILITY_POWER;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);

        @Config.Ignore
        public static Configuration.Values LOCAL_VALUES = new Configuration.Values();
        public static Configuration.Values VALUES = new Configuration.Values();

        public enum Damage implements IStringSerializable {
            MULTIPLICATION,
            ADDITION;

            @Override
            public String getName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        public static class Values {

            public Configuration.Damage applyAs = Configuration.Damage.MULTIPLICATION;
        }
    }
    /*Config Section*/
}
