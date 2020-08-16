package arekkuusu.enderskills.common.block.tile;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.gui.data.SkillAdvancementConditionAltar;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.block.ModBlocks;
import arekkuusu.enderskills.common.item.ModItems;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import java.util.List;

public class TileAltar extends TileBase implements ITickable {

    public static final int ANIMATION_TIME = 50;
    public static final int[] COLORS = new int[]{
            0x1E0034,
            0x260742,
            0x30104F,
            0x38185B,
            0x401E68,
            0x472476,
            0x4E2A84,
            0x5B3C8C,
            0x684C96,
            0x765D9F,
            0x836EA9,
    };
    public int tickCount;
    public float pageFlip;
    public float pageFlipPrev;
    public float flipT;
    public float flipA;
    public float bookSpread;
    public float bookSpreadPrev;
    public float bookRotation;
    public float bookRotationPrev;
    public float tRot;

    public int lastLevelAnimationTimer = 0;
    public int ultimateUpgradeAnimationTimer = -1;
    public double lastLevel = SkillAdvancementConditionAltar.LEVEL_0;

    @Override
    public void update() {
        if (!world.isRemote && tickCount % 10 == 0 && !isUltimate()) { //Check for Crystal Matrix every half a second
            List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(getPos().up()));
            if (!list.isEmpty()) {
                EntityItem entity = list.get(0);
                if (entity.getItem().getItem() == ModItems.CRYSTAL_MATRIX && getLevel() == 1F) {
                    this.ultimateUpgradeAnimationTimer = ANIMATION_TIME * 2;
                    this.sync();
                    if (world instanceof WorldServer) {
                        ((WorldServer) world).playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 1.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
                    }
                    entity.getItem().shrink(1);
                }
            }
            if (ultimateUpgradeAnimationTimer == 0) {
                this.makeUltimate();
            }
        }
        if (lastLevelAnimationTimer > 0) {
            if (world.isRemote) {
                double particlespeed = 4.5;
                for (int i = 0; i < 50; i++) {
                    Vec3d particlePos = new Vec3d(0, 0, 5);
                    particlePos = particlePos.rotateYaw(world.rand.nextFloat() * 180f);
                    particlePos = particlePos.rotatePitch(world.rand.nextFloat() * 360f);

                    Vec3d velocity = particlePos.normalize();
                    velocity = new Vec3d(
                            velocity.x * particlespeed,
                            velocity.y * particlespeed,
                            velocity.z * particlespeed
                    );
                    particlePos = particlePos.add(new Vec3d(getPos()));

                    world.spawnParticle(EnumParticleTypes.PORTAL, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
                }

                for (int i = 0; i < 3; i++) {
                    double posX = pos.getX() + 1 * world.rand.nextDouble();
                    double posY = pos.getY() + 0.1 * world.rand.nextDouble();
                    double posZ = pos.getZ() + 1 * world.rand.nextDouble();
                    EnderSkills.getProxy().spawnParticle(world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.1, 0), 4F, 50, COLORS[world.rand.nextInt(COLORS.length - 1)], ResourceLibrary.GLOW_PARTICLE_EFFECT);

                    posX = pos.getX() + 10D * (world.rand.nextDouble() - 0.5D);
                    posZ = pos.getZ() + 10D * (world.rand.nextDouble() - 0.5D);
                    EnderSkills.getProxy().spawnParticle(world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.05, 0), 1F, 25, COLORS[world.rand.nextInt(COLORS.length - 1)], ResourceLibrary.GLOW_PARTICLE_EFFECT);
                }
            }
            --lastLevelAnimationTimer;
        }
        if (ultimateUpgradeAnimationTimer > 0) {
            if (!world.isRemote) {
                if (world.rand.nextDouble() < 0.01) {
                    double posX = pos.getX() + 5D * (world.rand.nextDouble() - 0.5D);
                    double posY = pos.getY() + 5D + 5D * world.rand.nextDouble();
                    double posZ = pos.getZ() + 5D * (world.rand.nextDouble() - 0.5D);
                    world.addWeatherEffect(new EntityLightningBolt(world, posX, posY, posZ, true));
                }
            } else {
                Vector origin = new Vector(getPos()).addVector(0.5D, 0.5D, 0.5D);
                Vector from = Vector.fromSpherical(world.rand.nextFloat() * 360F, world.rand.nextFloat() * 180F - 90F).multiply(5D * world.rand.nextDouble()).add(origin);
                Vector to = Vector.fromSpherical(world.rand.nextFloat() * 360F, world.rand.nextFloat() * 180F - 90F).multiply(4D).add(origin);
                EnderSkills.getProxy().spawnLightning(world, from, to, 5, 0.75F, 5, 0x5194FF, true);
                world.playSound(null, from.x, from.y, from.z, ModSounds.SPARK, SoundCategory.BLOCKS, 5.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
            if (lastLevelAnimationTimer < ANIMATION_TIME) {
                lastLevelAnimationTimer += 2;
            }
            --ultimateUpgradeAnimationTimer;
        }

        this.bookSpreadPrev = this.bookSpread;
        this.bookRotationPrev = this.bookRotation;
        EntityPlayer entityplayer = this.world.getClosestPlayer((float) this.pos.getX() + 0.5F, (float) this.pos.getY() + 0.5F, (float) this.pos.getZ() + 0.5F, 3.0D, false);

        if (entityplayer != null) {
            double d0 = entityplayer.posX - (double) ((float) this.pos.getX() + 0.5F);
            double d1 = entityplayer.posZ - (double) ((float) this.pos.getZ() + 0.5F);
            this.tRot = (float) MathHelper.atan2(d1, d0);
            this.bookSpread += 0.1F;

            if (this.bookSpread < 0.5F || world.rand.nextInt(40) == 0) {
                float f1 = this.flipT;

                do {
                    this.flipT += (float) (world.rand.nextInt(4) - world.rand.nextInt(4));
                } while (f1 == this.flipT);
            }
        } else {
            this.tRot += 0.02F;
            this.bookSpread -= 0.1F;
        }

        while (this.bookRotation >= (float) Math.PI) {
            this.bookRotation -= ((float) Math.PI * 2F);
        }

        while (this.bookRotation < -(float) Math.PI) {
            this.bookRotation += ((float) Math.PI * 2F);
        }

        while (this.tRot >= (float) Math.PI) {
            this.tRot -= ((float) Math.PI * 2F);
        }

        while (this.tRot < -(float) Math.PI) {
            this.tRot += ((float) Math.PI * 2F);
        }

        float f2;

        f2 = this.tRot - this.bookRotation;
        while (f2 >= (float) Math.PI) {
            f2 -= ((float) Math.PI * 2F);
        }

        while (f2 < -(float) Math.PI) {
            f2 += ((float) Math.PI * 2F);
        }

        this.bookRotation += f2 * 0.4F;
        this.bookSpread = MathHelper.clamp(this.bookSpread, 0.0F, 1.0F);
        ++this.tickCount;
        this.pageFlipPrev = this.pageFlip;
        float f = (this.flipT - this.pageFlip) * 0.4F;
        f = MathHelper.clamp(f, -0.2F, 0.2F);
        this.flipA += (f - this.flipA) * 0.9F;
        this.pageFlip += this.flipA;
    }

    public void makeUltimate() {
        //Set Altar
        this.world.setBlockState(this.getPos(), ModBlocks.ALTAR_ULTIMATE.getDefaultState());
        getTile(TileAltar.class, world, getPos()).ifPresent(tile -> {
            tile.lastLevelAnimationTimer = ANIMATION_TIME;
            tile.sync();
        });
        //Remove Crystals
        BlockPos pos = new BlockPos(5, 5, 5);
        Iterable<BlockPos> iterable = BlockPos.getAllInBox(getPos().subtract(pos), getPos().add(pos));
        for (BlockPos p : iterable) {
            IBlockState state = this.world.getBlockState(p);
            if (state.getBlock() == ModBlocks.CRYSTAL) {
                world.setBlockToAir(p);
            }
        }
        //Explode
        if (world instanceof WorldServer) {
            ((WorldServer) world).playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
        }
        if (getObsidian() < 5) {
            this.world.playSound((EntityPlayer) null, getPos().getX() + 0.5D, getPos().getY() + 1.5D, getPos().getZ() + 0.5D, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 10.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
            this.world.createExplosion(null, getPos().getX() + 0.5D, getPos().getY() + 1.5D, getPos().getZ() + 0.5D, 8F, true);
        }
    }

    public int getObsidian() {
        BlockPos pos = new BlockPos(5, 5, 5);
        Iterable<BlockPos> iterable = BlockPos.getAllInBox(getPos().subtract(pos), getPos().add(pos));
        int count = 0;
        for (BlockPos p : iterable) {
            IBlockState state = this.world.getBlockState(p);
            if (state.getBlock() == Blocks.OBSIDIAN) {
                count++;
            }
        }
        return count;
    }

    public int getCrystals() {
        BlockPos pos = new BlockPos(5, 5, 5);
        Iterable<BlockPos> iterable = BlockPos.getAllInBox(getPos().subtract(pos), getPos().add(pos));
        int count = 0;
        for (BlockPos p : iterable) {
            IBlockState state = this.world.getBlockState(p);
            if (state.getBlock() == ModBlocks.CRYSTAL) {
                count++;
            }
        }
        return count;
    }

    public boolean isUltimate() {
        return getBlockType() == ModBlocks.ALTAR_ULTIMATE;
    }

    public double getLevel() {
        if (isUltimate()) return 1D;
        int count = getCrystals();
        if (count >= 32)
            return 1D;
        if (count > 10)
            return SkillAdvancementConditionAltar.LEVEL_2 + (SkillAdvancementConditionAltar.LEVEL_3 - SkillAdvancementConditionAltar.LEVEL_2) * (count / 32D);
        if (count > 2)
            return SkillAdvancementConditionAltar.LEVEL_1 + (SkillAdvancementConditionAltar.LEVEL_2 - SkillAdvancementConditionAltar.LEVEL_1) * (count / 10D);
        if (count > 0)
            return SkillAdvancementConditionAltar.LEVEL_0 + (SkillAdvancementConditionAltar.LEVEL_1 - SkillAdvancementConditionAltar.LEVEL_0) * (count / 2D);
        return SkillAdvancementConditionAltar.LEVEL_0;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return super.getRenderBoundingBox().grow(5);
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 || pass == 1;
    }

    @Override
    void readNBT(NBTTagCompound compound) {
        lastLevel = compound.getDouble("lastLevel");
        lastLevelAnimationTimer = compound.getInteger("lastLevelAnimationTimer");
        ultimateUpgradeAnimationTimer = compound.getInteger("ultimateUpgradeAnimationTimer");
    }

    @Override
    void writeNBT(NBTTagCompound compound) {
        compound.setDouble("lastLevel", lastLevel);
        compound.setInteger("lastLevelAnimationTimer", lastLevelAnimationTimer);
        compound.setInteger("ultimateUpgradeAnimationTimer", ultimateUpgradeAnimationTimer);
    }
}
