package arekkuusu.enderskills.common.block;

import arekkuusu.enderskills.client.gui.data.SkillAdvancementConditionAltar;
import arekkuusu.enderskills.common.block.tile.TileAltar;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

@SuppressWarnings("deprecation")
public class BlockCrystal extends BlockBase {

    public BlockCrystal() {
        super(LibNames.CRYSTAL, Material.GLASS);
        setHardness(0).setResistance(0);
        setSoundType(SoundType.GLASS);
        setLightLevel(0.5F);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (worldIn.isRemote) return;
        BlockPos distance = new BlockPos(5, 5, 5);
        Iterable<BlockPos> iterable = BlockPos.getAllInBox(pos.subtract(distance), pos.add(distance));
        for (BlockPos p : iterable) {
            getTile(TileAltar.class, worldIn, p).ifPresent(altar -> {
                if (altar.isUltimate()) return;
                double lastLevel = altar.lastLevel;
                double currentLevel = getLevel(altar);
                if (lastLevel < currentLevel) {
                    if (worldIn instanceof WorldServer) {
                        ((WorldServer) worldIn).playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1.0F, (1.0F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.2F) * 0.7F);
                    }
                    altar.lastLevel = currentLevel;
                    altar.lastLevelAnimationTimer = TileAltar.ANIMATION_TIME;
                    altar.sync();
                }
            });
        }
    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote) return;
        BlockPos distance = new BlockPos(5, 5, 5);
        Iterable<BlockPos> iterable = BlockPos.getAllInBox(pos.subtract(distance), pos.add(distance));
        for (BlockPos p : iterable) {
            getTile(TileAltar.class, worldIn, p).ifPresent(altar -> {
                if (altar.isUltimate()) return;
                double lastLevel = altar.lastLevel;
                double currentLevel = getLevel(altar);
                if (lastLevel > currentLevel) {
                    altar.lastLevel = currentLevel;
                    altar.sync();
                }
            });
        }
    }

    public double getLevel(TileAltar altar) {
        int count = altar.getCrystals();
        if (count >= 32)
            return SkillAdvancementConditionAltar.LEVEL_3;
        if (count >= 10)
            return SkillAdvancementConditionAltar.LEVEL_2;
        if (count >= 2)
            return SkillAdvancementConditionAltar.LEVEL_1;

        return SkillAdvancementConditionAltar.LEVEL_0;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}