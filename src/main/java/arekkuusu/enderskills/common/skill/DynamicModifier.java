package arekkuusu.enderskills.common.skill;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.UUID;

public class DynamicModifier {

    public final IAttribute attributeTarget;
    public final String nameIn;
    public final UUID uuid;
    public final int op;

    public DynamicModifier(String uuid, String nameIn, IAttribute attributeTarget, int op) {
        this.attributeTarget = attributeTarget;
        this.uuid = UUID.fromString(uuid);
        this.nameIn = nameIn;
        this.op = op;
    }

    public void apply(EntityLivingBase entity, double amount) {
        IAttributeInstance attribute = entity.getEntityAttribute(attributeTarget);
        AttributeModifier attributeModifier = attribute.getModifier(uuid);
        if (attributeModifier == null || attributeModifier instanceof DynamicAttribute) {
            DynamicAttribute dynamicAttribute = (DynamicAttribute) attributeModifier;
            if (dynamicAttribute == null || dynamicAttribute.getAmount() != amount) {
                if (dynamicAttribute == null) dynamicAttribute = new DynamicAttribute(this);
                dynamicAttribute.setAmount(amount);
                attribute.removeModifier(dynamicAttribute);
                attribute.applyModifier(dynamicAttribute);
            }
        } else {
            attribute.removeModifier(attributeModifier);
        }
    }

    public boolean remove(EntityLivingBase entity) {
        IAttributeInstance attribute = entity.getEntityAttribute(attributeTarget);
        if (attribute != null) {
            AttributeModifier attributeModifier = attribute.getModifier(uuid);
            if (attributeModifier != null) {
                attribute.removeModifier(attributeModifier);
                return true;
            }
        }
        return false;
    }

    private static class DynamicAttribute extends AttributeModifier {

        public double amount;

        public DynamicAttribute(DynamicModifier dynamicModifier) {
            super(dynamicModifier.uuid, dynamicModifier.nameIn, 0, dynamicModifier.op);
            this.setSaved(false);
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
