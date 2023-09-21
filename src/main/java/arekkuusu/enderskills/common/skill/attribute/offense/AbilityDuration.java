package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDurationEvent;
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

public class AbilityDuration extends BaseAttribute {

    //Vanilla Attribute
    public static final IAttribute ABILITY_DURATION = new RangedAttribute(null, "enderskills.generic.abilityDuration", 0F, 0F, Float.MAX_VALUE).setDescription("Ability Duration").setShouldWatch(true);
    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier ABILITY_DURATION_ATTRIBUTE = new DynamicModifier(
            "df7d610b-8be5-40ae-8f91-1eaa83a7fb82",
            LibMod.MOD_ID + ":" + LibNames.ABILITY_DURATION,
            AbilityDuration.ABILITY_DURATION,
            Constants.AttributeModifierOperation.ADD);

    public AbilityDuration() {
        super(LibNames.ABILITY_DURATION, new Properties());
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
                    ABILITY_DURATION_ATTRIBUTE.apply(entity, DSLDefaults.getModifier(ModAttributes.ABILITY_DURATION, attributeInfo.getLevel()));
                });
            } else {
                ABILITY_DURATION_ATTRIBUTE.remove(entity);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSkillRange(SkillDurationEvent event) {
        if (event.getEntityLiving() == null) return;
        if (event.getAmount() <= 0) return;
        EntityLivingBase entity = event.getEntityLiving();
        double amount = entity.getEntityAttribute(AbilityDuration.ABILITY_DURATION).getAttributeValue();
        if (!MathUtil.fuzzyEqual(0, amount)) {
            if (Configuration.LOCAL_VALUES.applyAs == Configuration.Damage.MULTIPLICATION) {
                event.setAmount((int) (event.getAmount() + event.getAmount() * amount));
            } else {
                event.setAmount((int) (event.getAmount() + amount));
            }
        }
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase)
            ((EntityLivingBase) event.getObject()).getAttributeMap().registerAttribute(ABILITY_DURATION).setBaseValue(0F);
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.ABILITY_DURATION;

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

            public Damage applyAs = Damage.MULTIPLICATION;
        }
    }
    /*Config Section*/
}
