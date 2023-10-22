package arekkuusu.enderskills.common.block;

import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.block.tile.TileFire;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

import static net.minecraftforge.common.util.Constants.BlockFlags.DEFAULT_AND_RERENDER;

public class BlockBlackFireFlame extends Block {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool UPPER = PropertyBool.create("up");
    public final Map<Block, Integer> encouragements = Maps.<Block, Integer>newIdentityHashMap();
    public final Map<Block, Integer> flammabilities = Maps.<Block, Integer>newIdentityHashMap();

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) && !canCatchFire(worldIn, pos.down(), EnumFacing.UP)) {
            return state.withProperty(NORTH, this.canCatchFire(worldIn, pos.north(), EnumFacing.SOUTH))
                    .withProperty(EAST, this.canCatchFire(worldIn, pos.east(), EnumFacing.WEST))
                    .withProperty(SOUTH, this.canCatchFire(worldIn, pos.south(), EnumFacing.NORTH))
                    .withProperty(WEST, this.canCatchFire(worldIn, pos.west(), EnumFacing.EAST))
                    .withProperty(UPPER, this.canCatchFire(worldIn, pos.up(), EnumFacing.DOWN));
        }
        return this.getDefaultState();
    }

    protected BlockBlackFireFlame(String id) {
        super(Material.FIRE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)).withProperty(UPPER, Boolean.valueOf(false)));
        this.init();
        ModBlocks.setRegistry(this, id);
    }

    public void init() {
        setFireInfo(Blocks.PLANKS, 5, 20);
        setFireInfo(Blocks.DOUBLE_WOODEN_SLAB, 5, 20);
        setFireInfo(Blocks.WOODEN_SLAB, 5, 20);
        setFireInfo(Blocks.OAK_FENCE_GATE, 5, 20);
        setFireInfo(Blocks.SPRUCE_FENCE_GATE, 5, 20);
        setFireInfo(Blocks.BIRCH_FENCE_GATE, 5, 20);
        setFireInfo(Blocks.JUNGLE_FENCE_GATE, 5, 20);
        setFireInfo(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
        setFireInfo(Blocks.ACACIA_FENCE_GATE, 5, 20);
        setFireInfo(Blocks.OAK_FENCE, 5, 20);
        setFireInfo(Blocks.SPRUCE_FENCE, 5, 20);
        setFireInfo(Blocks.BIRCH_FENCE, 5, 20);
        setFireInfo(Blocks.JUNGLE_FENCE, 5, 20);
        setFireInfo(Blocks.DARK_OAK_FENCE, 5, 20);
        setFireInfo(Blocks.ACACIA_FENCE, 5, 20);
        setFireInfo(Blocks.OAK_STAIRS, 5, 20);
        setFireInfo(Blocks.BIRCH_STAIRS, 5, 20);
        setFireInfo(Blocks.SPRUCE_STAIRS, 5, 20);
        setFireInfo(Blocks.JUNGLE_STAIRS, 5, 20);
        setFireInfo(Blocks.ACACIA_STAIRS, 5, 20);
        setFireInfo(Blocks.DARK_OAK_STAIRS, 5, 20);
        setFireInfo(Blocks.OBSIDIAN, 5, 5);
        setFireInfo(Blocks.STONE, 5, 5);
        setFireInfo(Blocks.GRASS, 5, 5);
        setFireInfo(Blocks.LOG, 5, 5);
        setFireInfo(Blocks.LOG2, 5, 5);
        setFireInfo(Blocks.LEAVES, 30, 60);
        setFireInfo(Blocks.LEAVES2, 30, 60);
        setFireInfo(Blocks.BOOKSHELF, 30, 20);
        setFireInfo(Blocks.TNT, 15, 100);
        setFireInfo(Blocks.TALLGRASS, 60, 100);
        setFireInfo(Blocks.DOUBLE_PLANT, 60, 100);
        setFireInfo(Blocks.YELLOW_FLOWER, 60, 100);
        setFireInfo(Blocks.RED_FLOWER, 60, 100);
        setFireInfo(Blocks.DEADBUSH, 60, 100);
        setFireInfo(Blocks.WOOL, 30, 60);
        setFireInfo(Blocks.VINE, 15, 100);
        setFireInfo(Blocks.COAL_BLOCK, 5, 5);
        setFireInfo(Blocks.HAY_BLOCK, 60, 20);
        setFireInfo(Blocks.CARPET, 60, 20);
    }

    public void setFireInfo(Block blockIn, int encouragement, int flammability) {
        if (blockIn == Blocks.AIR) throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
        this.encouragements.put(blockIn, Integer.valueOf(encouragement));
        this.flammabilities.put(blockIn, Integer.valueOf(flammability));
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileFire();
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public int quantityDropped(Random random) {
        return 0;
    }

    public boolean isCollidable() {
        return false;
    }

    @Deprecated // Use canCatchFire with face sensitive version below
    public boolean canCatchFire(IBlockAccess worldIn, BlockPos pos) {
        return canCatchFire(worldIn, pos, EnumFacing.UP);
    }

    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.TNT;
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(24) == 0) {
            worldIn.playSound((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
        }

        if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) && !canCatchFire(worldIn, pos.down(), EnumFacing.UP)) {
            if (canCatchFire(worldIn, pos.west(), EnumFacing.EAST)) {
                for (int j = 0; j < 2; ++j) {
                    double d3 = (double) pos.getX() + rand.nextDouble() * 0.10000000149011612D;
                    double d8 = (double) pos.getY() + rand.nextDouble();
                    double d13 = (double) pos.getZ() + rand.nextDouble();
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d3, d8, d13, 0.0D, 0.0D, 0.0D);
                }
            }

            if (canCatchFire(worldIn, pos.east(), EnumFacing.WEST)) {
                for (int k = 0; k < 2; ++k) {
                    double d4 = (double) (pos.getX() + 1) - rand.nextDouble() * 0.10000000149011612D;
                    double d9 = (double) pos.getY() + rand.nextDouble();
                    double d14 = (double) pos.getZ() + rand.nextDouble();
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d4, d9, d14, 0.0D, 0.0D, 0.0D);
                }
            }

            if (canCatchFire(worldIn, pos.north(), EnumFacing.SOUTH)) {
                for (int l = 0; l < 2; ++l) {
                    double d5 = (double) pos.getX() + rand.nextDouble();
                    double d10 = (double) pos.getY() + rand.nextDouble();
                    double d15 = (double) pos.getZ() + rand.nextDouble() * 0.10000000149011612D;
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d5, d10, d15, 0.0D, 0.0D, 0.0D);
                }
            }

            if (canCatchFire(worldIn, pos.south(), EnumFacing.NORTH)) {
                for (int i1 = 0; i1 < 2; ++i1) {
                    double d6 = (double) pos.getX() + rand.nextDouble();
                    double d11 = (double) pos.getY() + rand.nextDouble();
                    double d16 = (double) (pos.getZ() + 1) - rand.nextDouble() * 0.10000000149011612D;
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d6, d11, d16, 0.0D, 0.0D, 0.0D);
                }
            }

            if (canCatchFire(worldIn, pos.up(), EnumFacing.DOWN)) {
                for (int j1 = 0; j1 < 2; ++j1) {
                    double d7 = (double) pos.getX() + rand.nextDouble();
                    double d12 = (double) (pos.getY() + 1) - rand.nextDouble() * 0.10000000149011612D;
                    double d17 = (double) pos.getZ() + rand.nextDouble();
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d7, d12, d17, 0.0D, 0.0D, 0.0D);
                }
            }
        } else {
            for (int i = 0; i < 3; ++i) {
                double d0 = (double) pos.getX() + rand.nextDouble();
                double d1 = (double) pos.getY() + rand.nextDouble() * 0.5D + 0.5D;
                double d2 = (double) pos.getZ() + rand.nextDouble();
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public boolean canCatchFire(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return CommonConfig.getSyncValues().skill.destroyBlocks && getFlammability(world.getBlockState(pos).getBlock()) > 0;
    }

    public int getFlammability(Block blockIn) {
        Integer integer = flammabilities.get(blockIn);
        return integer == null ? 0 : integer.intValue();
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta));
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public int getMetaFromState(IBlockState state) {
        return ((Integer) state.getValue(AGE)).intValue();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{AGE, NORTH, EAST, SOUTH, WEST, UPPER});
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
