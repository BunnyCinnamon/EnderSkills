package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillRangeEvent;
import arekkuusu.enderskills.api.helper.MathUtil;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.ModAttributes;
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

public class AbilityRange extends BaseAttribute {

    //Vanilla Attribute
    public static final IAttribute ABILITY_RANGE = new RangedAttribute(null, "enderskills.generic.abilityRage", 0F, 0F, Float.MAX_VALUE).setDescription("Ability Range").setShouldWatch(true);
    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier ABILITY_RANGE_ATTRIBUTE = new DynamicModifier(
            "9af579f8-0c4d-4e12-af15-f7bf25c2f428",
            LibMod.MOD_ID + ":" + LibNames.ABILITY_RANGE,
            AbilityRange.ABILITY_RANGE,
            Constants.AttributeModifierOperation.ADD);

    public AbilityRange() {
        super(LibNames.ABILITY_RANGE, new Properties());
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
                    ABILITY_RANGE_ATTRIBUTE.apply(entity, DSLDefaults.getModifier(ModAttributes.ABILITY_RANGE, attributeInfo.getLevel()));
                });
            } else {
                ABILITY_RANGE_ATTRIBUTE.remove(entity);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillRange(SkillRangeEvent event) {
        if (event.getEntityLiving() == null) return;
        if (event.getAmount() <= 0) return;
        EntityLivingBase entity = event.getEntityLiving();
        double amount = entity.getEntityAttribute(AbilityRange.ABILITY_RANGE).getAttributeValue();
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
            ((EntityLivingBase) event.getObject()).getAttributeMap().registerAttribute(ABILITY_RANGE).setBaseValue(0F);
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.ABILITY_RANGE;

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
