package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.network.PacketHelper;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityTokenOrb extends Entity {

    public EntityPlayer closestPlayer;
    public int delayBeforeCanPickup;
    public int orbHealth = 5;
    public int tokenValue;
    public int color, targetColor;
    public int age;

    public EntityTokenOrb(World worldIn, double x, double y, double z, int expValue) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
        this.setPosition(x, y, z);
        this.rotationYaw = (float) (Math.random() * 360.0D);
        this.motionX = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F;
        this.motionY = ((float) (Math.random() * 0.2D) * 2.0F);
        this.motionZ = ((float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
        this.tokenValue = expValue;
    }

    public EntityTokenOrb(World worldIn) {
        super(worldIn);
        this.setSize(0.25F, 0.25F);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.delayBeforeCanPickup > 0) {
            --this.delayBeforeCanPickup;
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (!this.hasNoGravity()) {
            this.motionY -= 0.029999999329447746D;
        }

        if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
            this.motionY = 0.20000000298023224D;
            this.motionX = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F;
            this.motionZ = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F;
            this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
        }

        this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);

        if (this.targetColor < this.color - 20 + this.getEntityId() % 100) {
            if (this.closestPlayer == null || this.closestPlayer.getDistanceSq(this) > 64.0D) {
                this.closestPlayer = this.world.getClosestPlayerToEntity(this, 8.0D);
            }

            this.targetColor = this.color;
        }

        if (this.closestPlayer != null && this.closestPlayer.isSpectator()) {
            this.closestPlayer = null;
        }

        if (this.closestPlayer != null) {
            double d1 = (this.closestPlayer.posX - this.posX) / 8.0D;
            double d2 = (this.closestPlayer.posY + this.closestPlayer.getEyeHeight() / 2.0D - this.posY) / 8.0D;
            double d3 = (this.closestPlayer.posZ - this.posZ) / 8.0D;
            double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
            double d5 = 1.0D - d4;

            if (d5 > 0.0D) {
                d5 = d5 * d5;
                this.motionX += d1 / d4 * d5 * 0.1D;
                this.motionY += d2 / d4 * d5 * 0.1D;
                this.motionZ += d3 / d4 * d5 * 0.1D;
            }
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        float f = 0.98F;

        if (this.onGround) {
            BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
            net.minecraft.block.state.IBlockState underState = this.world.getBlockState(underPos);
            f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;
        }

        this.motionX *= f;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= f;

        if (this.onGround) {
            this.motionY *= -0.8999999761581421D;
        }

        ++this.color;
        ++this.age;

        if (this.age >= 6000) {
            this.setDead();
        }
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) {
        if (!this.world.isRemote) {
            if (this.delayBeforeCanPickup == 0 && entityIn.xpCooldown == 0) {
                entityIn.xpCooldown = 2;
                entityIn.onItemPickup(this, 1);

                if (this.tokenValue > 0) {
                    Capabilities.advancement(entityIn).ifPresent(c -> {
                        for (int i = 0; i < this.tokenValue; i++) {
                            double exp = 1;
                            for (int j = 0; j < c.level; j++) {
                                exp = exp * 2D;
                            }
                            if(exp > CommonConfig.getSyncValues().advancement.levels.tokenCostThreshold) {
                                exp -= exp * CommonConfig.getSyncValues().advancement.levels.tokenDiminishableCost;
                            }
                            double xp = 2D / exp;
                            c.levelProgress += xp;
                            if (c.levelProgress >= 1D) {
                                c.levelProgress = c.levelProgress - 1D;
                                c.level++;
                            }
                        }
                        if (entityIn instanceof EntityPlayerMP) {
                            PacketHelper.sendAdvancementSync((EntityPlayerMP) entityIn);
                        }
                    });
                }

                this.setDead();
            }
        }
    }

    @Override
    public boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean handleWaterMovement() {
        return this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this);
    }

    @Override
    protected void dealFireDamage(int amount) {
        this.attackEntityFrom(DamageSource.IN_FIRE, (float) amount);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.world.isRemote || this.isDead) return false;
        if (!this.isEntityInvulnerable(source)) {
            this.markVelocityChanged();
            this.orbHealth = (int) ((float) this.orbHealth - amount);

            if (this.orbHealth <= 0) {
                this.setDead();
            }

        }
        return false;
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        float f = 0.5F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        int i = super.getBrightnessForRender();
        int j = i & 255;
        int k = i >> 16 & 255;
        j = j + (int) (f * 15.0F * 16.0F);

        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    @SideOnly(Side.CLIENT)
    public int getTextureByTokens() {
        if (this.tokenValue >= 2477) {
            return 10;
        } else if (this.tokenValue >= 1237) {
            return 9;
        } else if (this.tokenValue >= 617) {
            return 8;
        } else if (this.tokenValue >= 307) {
            return 7;
        } else if (this.tokenValue >= 149) {
            return 6;
        } else if (this.tokenValue >= 73) {
            return 5;
        } else if (this.tokenValue >= 37) {
            return 4;
        } else if (this.tokenValue >= 17) {
            return 3;
        } else if (this.tokenValue >= 7) {
            return 2;
        } else {
            return this.tokenValue >= 3 ? 1 : 0;
        }
    }

    public int getTokenValue() {
        return this.tokenValue;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setShort("Health", (short) this.orbHealth);
        compound.setShort("Age", (short) this.age);
        compound.setShort("Value", (short) this.tokenValue);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        this.orbHealth = compound.getShort("Health");
        this.age = compound.getShort("Age");
        this.tokenValue = compound.getShort("Value");
    }
}