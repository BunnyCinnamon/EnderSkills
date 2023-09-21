package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

public class MeleeDamage extends BaseAttribute {

    public MeleeDamage() {
        super(LibNames.DAMAGE, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving()) || SkillHelper.isSkillDamage(event.getSource())) return;
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || source instanceof SkillDamageSource || event.getAmount() <= 0)
            return;
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        Capabilities.get(attacker).ifPresent(capability -> {
            //Do Damage
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    if (Configuration.LOCAL_VALUES.applyAs == Configuration.Damage.MULTIPLICATION) {
                        event.setAmount(event.getAmount() + event.getAmount() * DSLDefaults.getModifier(ModAttributes.DAMAGE, attributeInfo.getLevel()));
                    } else {
                        event.setAmount(event.getAmount() + DSLDefaults.getModifier(ModAttributes.DAMAGE, attributeInfo.getLevel()));
                    }
                });
            }
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.DAMAGE;

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
