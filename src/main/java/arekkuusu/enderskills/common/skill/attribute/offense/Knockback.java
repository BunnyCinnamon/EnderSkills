package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.*;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.configuration.parser.DSLParser;
import arekkuusu.enderskills.client.gui.data.SkillAdvancement;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class Knockback extends BaseAttribute {

    public Knockback() {
        super(LibNames.KNOCKBACK, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingKnockBackEvent event) {
        if (event.getOriginalAttacker() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) event.getOriginalAttacker();
            Capabilities.get(entity).ifPresent(capability -> {
                if (capability.isOwned(this)) {
                    capability.getOwned(this).ifPresent(skillInfo -> {
                        AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                        event.setStrength(event.getStrength() + DSLDefaults.getModifier(ModAttributes.KNOCKBACK, attributeInfo.getLevel()));
                    });
                }
            });
        }
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.KNOCKBACK;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
