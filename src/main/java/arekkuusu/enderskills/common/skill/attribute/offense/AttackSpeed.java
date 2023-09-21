package arekkuusu.enderskills.common.skill.attribute.offense;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.attribute.AttributeInfo;
import arekkuusu.enderskills.common.skill.attribute.BaseAttribute;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.UUID;

public class AttackSpeed extends BaseAttribute {

    public static final UUID ITEM_ATTACK_SPEED_MODIFIER;

    static {
        ITEM_ATTACK_SPEED_MODIFIER = ObfuscationReflectionHelper.getPrivateValue(
                Item.class, null,
                "ATTACK_SPEED_MODIFIER", "field_185050_h"
        );
    }

    public static final DynamicModifier SPEED_ATTRIBUTE = new DynamicModifier(
            "2b3b2ec9-00d5-43e7-86f4-bb51d6c5c1e7",
            LibMod.MOD_ID + ":" + LibNames.ATTACK_SPEED,
            SharedMonsterAttributes.ATTACK_SPEED,
            Constants.AttributeModifierOperation.ADD_MULTIPLE
    );

    public AttackSpeed() {
        super(LibNames.ATTACK_SPEED, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.ticksExisted % 40 != 0) return; //Slowdown cowboy! yee-haw!
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(this)) {
                capability.getOwned(this).ifPresent(skillInfo -> {
                    AttributeInfo attributeInfo = (AttributeInfo) skillInfo;
                    ItemStack heldMain = entity.getHeldItemMainhand();
                    ItemStack heldOff = entity.getHeldItemOffhand();
                    double heldMainSpeed = getItemStackSpeedModifier(entity, heldMain, EntityEquipmentSlot.MAINHAND);
                    double heldOffSpeed = getItemStackSpeedModifier(entity, heldOff, EntityEquipmentSlot.OFFHAND);
                    double bigChungus = Math.max(heldMainSpeed, heldOffSpeed);
                    float modifier = DSLDefaults.getModifier(ModAttributes.ATTACK_SPEED, attributeInfo.getLevel());
                    double chungness = getChungness(bigChungus);
                    SPEED_ATTRIBUTE.apply(entity, modifier * chungness);
                });
            } else {
                SPEED_ATTRIBUTE.remove(entity);
            }
        });
    }

    public double getChungness(double chungusThicc) {
        return MathHelper.clamp(chungusThicc / SharedMonsterAttributes.ATTACK_SPEED.getDefaultValue(), 0F, 1F);
    }

    public double getItemStackSpeedModifier(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot) {
        double amountAttribute = 0;
        Multimap<String, AttributeModifier> modifiers = stack.getItem().getAttributeModifiers(slot, stack);
        for (Map.Entry<String, AttributeModifier> entry : modifiers.entries()) {
            AttributeModifier attributemodifier = entry.getValue();
            if (attributemodifier.getID().equals(ITEM_ATTACK_SPEED_MODIFIER)) {
                amountAttribute = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                break;
            }
        }
        return amountAttribute;
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.ATTRIBUTE_OFFENCE_FOLDER + LibNames.ATTACK_SPEED;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
