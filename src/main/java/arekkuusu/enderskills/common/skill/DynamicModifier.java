package arekkuusu.enderskills.common.skill;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.UUID;

public class DynamicModifier {

    public final IAttribute attributeTarget;
    private final String nameIn;
    private final UUID uuid;

    public DynamicModifier(String uuid, String nameIn, IAttribute attributeTarget) {
        this.attributeTarget = attributeTarget;
        this.uuid = UUID.fromString(uuid);
        this.nameIn = nameIn;
    }

    public void apply(EntityLivingBase entity, double amount) {
        IAttributeInstance attribute = entity.getEntityAttribute(attributeTarget);
        DynamicAttribute dynamicAttribute = (DynamicAttribute) attribute.getModifier(uuid);
        if (dynamicAttribute == null || dynamicAttribute.getAmount() != amount) {
            if (dynamicAttribute == null) dynamicAttribute = new DynamicAttribute(this);
            dynamicAttribute.setAmount(amount);
            attribute.removeModifier(dynamicAttribute);
            attribute.applyModifier(dynamicAttribute);
        }
    }

    public void remove(EntityLivingBase entity) {
        IAttributeInstance attribute = entity.getEntityAttribute(attributeTarget);
        if(attribute != null) {
            DynamicAttribute dynamicAttribute = (DynamicAttribute) attribute.getModifier(uuid);
            if (dynamicAttribute != null) {
                attribute.removeModifier(dynamicAttribute);
            }
        }
    }

    private static class DynamicAttribute extends AttributeModifier {

        public double amount;

        public DynamicAttribute(DynamicModifier dynamicModifier) {
            super(dynamicModifier.uuid, dynamicModifier.nameIn, 0, 0);
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        @Override
        public boolean isSaved() {
            return false;
        }
    }
}
