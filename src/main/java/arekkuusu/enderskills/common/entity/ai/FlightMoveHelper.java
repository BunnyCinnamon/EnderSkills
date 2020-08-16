/*
 * Arekkuusu / Improbable plot machine. 2018
 *
 * This project is licensed under the MIT.
 * The source code is available on github:
 * https://github.com/ArekkuusuJerii/Improbable-plot-machine
 */
package arekkuusu.enderskills.common.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.util.math.MathHelper;

public class FlightMoveHelper extends EntityMoveHelper {

	public FlightMoveHelper(EntityLiving living) {
		super(living);
	}

	public void onUpdateMoveHelper() {
		if(action == Action.MOVE_TO && !entity.getNavigator().noPath()) {
			double d0 = posX - entity.posX;
			double d1 = posY - entity.posY;
			double d2 = posZ - entity.posZ;
			double speed = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

			float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
			this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f, 180F);
			this.entity.renderYawOffset = this.entity.rotationYaw;

			double d4 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
			float f2 = (float)(-(MathHelper.atan2(d1, d4) * (180D / Math.PI)));
			this.entity.rotationPitch = this.limitAngle(this.entity.rotationPitch, f2, 180F);

			this.entity.motionX += d0 / speed * 0.05D * this.speed;
			this.entity.motionY += d1 / speed * 0.05D * this.speed;
			this.entity.motionZ += d2 / speed * 0.05D * this.speed;
		} else {
			entity.setAIMoveSpeed(0F);
			entity.setMoveForward(0F);
			entity.setMoveVertical(0F);
			entity.motionX *= 0.75;
			entity.motionY *= 0.75;
			entity.motionZ *= 0.75;
		}
	}
}
