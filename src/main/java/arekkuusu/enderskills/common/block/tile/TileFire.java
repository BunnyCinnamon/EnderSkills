package arekkuusu.enderskills.common.block.tile;

import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.WorldHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

import static arekkuusu.enderskills.common.block.BlockBlackFireFlame.AGE;
import static net.minecraftforge.common.util.Constants.BlockFlags.DEFAULT_AND_RERENDER;

public class TileFire extends TileBase implements ITickable {

    public int tickCount;
    private int spread = 20 * 15;
    private UUID uuid;

    @Override
    public void update() {
        if (!world.isRemote && !isInvalid()) {
            if (tickCount >= Math.min(spread, 15 * 20)) {
                world.setBlockToAir(getPos());
                world.removeTileEntity(pos);
                markDirty();
                sync();
            } else burn();
        }
        ++this.tickCount;
    }
    
    void burn() {
        if (world.getGameRules().getBoolean("doFireTick") && world.rand.nextDouble() < 0.2D) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() != ModBlocks.BLACK_FIRE_FLAME) return;
            if (!world.isAreaLoaded(pos, 2)) return; // Forge: prevent loading unloaded chunks when spreading fire
            if (!this.canPlaceBlockAt(world, pos)) {
                world.setBlockToAir(pos);
                world.removeTileEntity(pos);
                markDirty();
                sync();
            }

            Block block = world.getBlockState(pos.down()).getBlock();
            boolean flag = block.isFireSource(world, pos.down(), EnumFacing.UP);

            int i = ((Integer) state.getValue(AGE)).intValue();

            if (!flag && world.isRaining() && this.canDie(world, pos) && world.rand.nextFloat() < 0.8F + (float) i * 0.03F) {
                world.setBlockToAir(pos);
                world.removeTileEntity(pos);
                markDirty();
                sync();
            } else {
                if (i < 15) {
                    state = state.withProperty(AGE, Integer.valueOf(i + world.rand.nextInt(3) / 2));
                    world.setBlockState(pos, state);
                    markDirty();
                    sync();
                }

                if (!flag) {
                    if (!this.canNeighborCatchFire(world, pos)) {
                        if (!world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP) || i > 3) {
                            world.setBlockToAir(pos);
                            world.removeTileEntity(pos);
                            markDirty();
                            sync();
                        }

                        return;
                    }

                    if (!this.canCatchFire(world, pos.down(), EnumFacing.UP) && i == 15 && world.rand.nextInt(4) == 0) {
                        world.setBlockToAir(pos);
                        world.removeTileEntity(pos);
                        markDirty();
                        sync();
                        return;
                    }
                }

                boolean flag1 = world.isBlockinHighHumidity(pos);
                int j = 0;

                if (flag1) {
                    j = -50;
                }

                this.tryCatchFire(world, pos.east(), 300 + j, world.rand, i, EnumFacing.WEST);
                this.tryCatchFire(world, pos.west(), 300 + j, world.rand, i, EnumFacing.EAST);
                this.tryCatchFire(world, pos.down(), 250 + j, world.rand, i, EnumFacing.UP);
                this.tryCatchFire(world, pos.up(), 250 + j, world.rand, i, EnumFacing.DOWN);
                this.tryCatchFire(world, pos.north(), 300 + j, world.rand, i, EnumFacing.SOUTH);
                this.tryCatchFire(world, pos.south(), 300 + j, world.rand, i, EnumFacing.NORTH);

                for (int k = -1; k <= 1; ++k) {
                    for (int l = -1; l <= 1; ++l) {
                        for (int i1 = -1; i1 <= 4; ++i1) {
                            if (k != 0 || i1 != 0 || l != 0) {
                                int j1 = 100;

                                if (i1 > 1) {
                                    j1 += (i1 - 1) * 100;
                                }

                                BlockPos blockpos = pos.add(k, i1, l);
                                int k1 = this.getNeighborEncouragement(world, blockpos);

                                if (k1 > 0) {
                                    int l1 = (k1 + 40 + world.getDifficulty().getDifficultyId() * 7) / (i + 30);

                                    if (flag1) {
                                        l1 /= 2;
                                    }

                                    if (l1 > 0 && world.rand.nextInt(j1) <= l1 && (!world.isRaining() || !this.canDie(world, blockpos))) {
                                        int i2 = i + world.rand.nextInt(5) / 4;

                                        if (i2 > 15) {
                                            i2 = 15;
                                        }

                                        world.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(i2)));
                                        TileFire tileEntity = (TileFire) world.getTileEntity(pos);
                                        EntityLivingBase entityLivingBase = getEntityLivingBase();
                                        UUID uuid = entityLivingBase == null ? null : entityLivingBase.getUniqueID();
                                        tileEntity.setOwnerTime(uuid, spread);
                                        tileEntity.markDirty();
                                        tileEntity.sync();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isTopSolid() || this.canNeighborCatchFire(worldIn, pos);
    }

    private boolean canNeighborCatchFire(World worldIn, BlockPos pos) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (this.canCatchFire(worldIn, pos.offset(enumfacing), enumfacing.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    public boolean canCatchFire(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return CommonConfig.getSyncValues().skill.destroyBlocks && getFlammability(world.getBlockState(pos).getBlock()) > 0;
    }

    private void tryCatchFire(World worldIn, BlockPos pos, int chance, Random random, int age, EnumFacing face) {
        IBlockState blockState = worldIn.getBlockState(pos);
        int i = getFlammability(blockState.getBlock());

        if (random.nextInt(chance) < i) {
            IBlockState iblockstate = worldIn.getBlockState(pos);

            if (random.nextInt(age + 30) < 25 && !worldIn.isRainingAt(pos)) {
                int j = age + random.nextInt(5) / 4;

                if (j > 15) {
                    j = 15;
                }

                world.setBlockState(pos, ModBlocks.BLACK_FIRE_FLAME.getDefaultState().withProperty(AGE, Integer.valueOf(j)));
                TileFire tileEntity = (TileFire) world.getTileEntity(pos);
                EntityLivingBase entityLivingBase = getEntityLivingBase();
                UUID uuid = entityLivingBase == null ? null : entityLivingBase.getUniqueID();
                tileEntity.setOwnerTime(uuid, spread);
                markDirty();
                sync();
            } else {
                world.setBlockToAir(pos);
                world.removeTileEntity(pos);
                markDirty();
                sync();
            }

            if (iblockstate.getBlock() == Blocks.TNT) {
                Blocks.TNT.onBlockDestroyedByPlayer(worldIn, pos, iblockstate.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
            }
        }
    }

    private int getNeighborEncouragement(World worldIn, BlockPos pos) {
        if (!worldIn.isAirBlock(pos)) {
            return 0;
        } else {
            int i = 0;

            for (EnumFacing enumfacing : EnumFacing.values()) {
                i = Math.max(worldIn.getBlockState(pos.offset(enumfacing)).getBlock().getFireSpreadSpeed(worldIn, pos.offset(enumfacing), enumfacing.getOpposite()), i);
            }

            return i;
        }
    }

    protected boolean canDie(World worldIn, BlockPos pos) {
        return worldIn.isRainingAt(pos) || worldIn.isRainingAt(pos.west()) || worldIn.isRainingAt(pos.east()) || worldIn.isRainingAt(pos.north()) || worldIn.isRainingAt(pos.south());
    }

    public int getFlammability(Block blockIn) {
        Integer integer = ModBlocks.BLACK_FIRE_FLAME.flammabilities.get(blockIn);
        return integer == null ? 0 : integer.intValue();
    }

    @Deprecated // Use Block.getFireSpreadSpeed
    public int getEncouragement(Block blockIn) {
        Integer integer = ModBlocks.BLACK_FIRE_FLAME.encouragements.get(blockIn);
        return integer == null ? 0 : integer.intValue();
    }

    public void setOwnerTime(UUID uuid, int time) {
        this.uuid = uuid;
        this.spread = time;
        markDirty();
        sync();
    }

    @Nullable
    public EntityLivingBase getEntityLivingBase() {
        return getEntityByUUID(uuid);
    }

    @Nullable
    public EntityLivingBase getEntityByUUID(UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid) && entity instanceof EntityLivingBase)
                return (EntityLivingBase) entity;
        }
        for (EntityLivingBase entity : world.playerEntities) {
            if (entity.getUniqueID().equals(uuid))
                return entity;
        }
        return null;
    }

    @Override
    void readNBT(NBTTagCompound compound) {
        this.uuid = NBTHelper.getUUID(compound, "uuid");
        this.spread = NBTHelper.getInteger(compound, "time");
    }

    @Override
    void writeNBT(NBTTagCompound compound) {
        if (this.uuid != null)
            NBTHelper.setUUID(compound, "uuid", this.uuid);
        NBTHelper.setInteger(compound, "time", this.spread);
    }
}
