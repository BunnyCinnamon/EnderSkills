package arekkuusu.enderskills.client.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelCrush extends ModelBase {

    private final ModelRenderer upperJaw;
    private final ModelRenderer lowerJaw;

    public ModelCrush() {
        textureHeight = 32;
        textureWidth = 32;
        this.upperJaw = new ModelRenderer(this, 0, 0);
        this.upperJaw.setRotationPoint(-0.5F, 22.0F, -8.0F);
        this.upperJaw.addBox(0.0F, 0.0F, 0.0F, 2, 16, 16);
        this.lowerJaw = new ModelRenderer(this, 0, 0);
        this.lowerJaw.setRotationPoint(0.5F, 22.0F, 8.0F);
        this.lowerJaw.addBox(0.0F, 0.0F, 0.0F, 2, 16, 16);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        float f = limbSwing * 2.0F;

        if (f > 1.0F) {
            f = 1.0F;
        }

        f = 1.0F - f * f * f;
        this.upperJaw.rotateAngleZ = (float) Math.PI - f * 0.35F * (float) Math.PI;
        this.lowerJaw.rotateAngleZ = (float) Math.PI + f * 0.35F * (float) Math.PI;
        this.lowerJaw.rotateAngleY = (float) Math.PI;
        float f1 = (limbSwing + MathHelper.sin(limbSwing * 2.7F)) * 0.6F * 12.0F;
        this.upperJaw.rotationPointY = 24.0F - f1;
        this.lowerJaw.rotationPointY = this.upperJaw.rotationPointY;
        this.upperJaw.render(scale);
        this.lowerJaw.render(scale);
    }
}
