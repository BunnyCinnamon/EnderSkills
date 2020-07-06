package arekkuusu.enderskills.common.block;

import arekkuusu.enderskills.common.ES;
import arekkuusu.enderskills.common.block.tile.TileAltar;
import arekkuusu.enderskills.common.lib.LibGui;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockAltar extends BlockBase {

    public final boolean isUltimate;

    public BlockAltar(String name, boolean isUltimate) {
        super(name, isUltimate ? Material.ROCK : Material.WOOD);
        setHardness(isUltimate ? 25 : 1).setResistance(isUltimate ? 9999 : 0);
        setSoundType(isUltimate ? SoundType.STONE : SoundType.WOOD);
        if (isUltimate) {
            setHarvestLevel(Tool.PICK, ToolLevel.DIAMOND);
        }
        this.isUltimate = isUltimate;
        setTickRandomly(!isUltimate);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            playerIn.openGui(ES.getInstance(), LibGui.LEVEL_EDITING, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        for (int i = -5; i <= 5; ++i) {
            for (int j = -5; j <= 5; ++j) {
                if (rand.nextInt(4) == 0) {
                    for (int k = -5; k <= 5; ++k) {
                        BlockPos blockpos = pos.add(i, k, j);
                        IBlockState state = worldIn.getBlockState(blockpos);

                        if (state.getBlock() == ModBlocks.CRYSTAL) {
                            if (!worldIn.isAirBlock(pos.add(i / 2, 0, j / 2))) {
                                break;
                            }

                            worldIn.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, (double) pos.getX() + 0.5D, (double) pos.getY() + 2.0D, (double) pos.getZ() + 0.5D, (double) ((float) i + rand.nextFloat()) - 0.5D, (double) ((float) k - rand.nextFloat() - 1.0F), (double) ((float) j + rand.nextFloat()) - 0.5D);
                        }
                    }
                }
            }
        }
    }

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.1, 0, 0.1, 0.9, 1, 0.9);
    }

    @Override
    @Nonnull
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileAltar();
    }
}