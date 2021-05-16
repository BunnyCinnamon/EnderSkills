package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.Hasten;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraftforge.common.util.Constants;

public class PotionCooldown extends PotionBase {
    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier HASTEN_ATTRIBUTE = new DynamicModifier(
            "010af31b-320d-4ef9-91ed-6f85adc38610",
            LibMod.MOD_ID + ":" + "potion_cooldown",
            Hasten.HASTEN,
            Constants.AttributeModifierOperation.ADD);

    protected PotionCooldown() {
        super(LibNames.COOLDOWN_EFFECT, 0xFFC300, 1);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap attributeMapIn, int amplifier) {
        HASTEN_ATTRIBUTE.apply(entity, 0.3D * (amplifier + 1));
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap attributeMapIn, int amplifier) {
        HASTEN_ATTRIBUTE.remove(entity);
    }
}
