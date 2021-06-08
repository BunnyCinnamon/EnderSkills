package arekkuusu.enderskills.common.block;

import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.item.ModItems;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class BlockEnderOre extends BlockBase {

    public BlockEnderOre() {
        super(LibNames.ENDER_ORE, Material.ROCK);
        setHarvestLevel(Tool.PICK, ToolLevel.IRON);
        setResistance(5);
        setHardness(5F);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.ENDER_DUST;
    }

    @Override
    public int quantityDropped(Random random) {
        return CommonConfig.getSyncValues().worldGen.enderOreItemDropsMin +
                random.nextInt(CommonConfig.getSyncValues().worldGen.enderOreItemDropsMax - CommonConfig.getSyncValues().worldGen.enderOreItemDropsMin);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune > 0) {
            int i = random.nextInt(fortune + 2) - 1;
            if (i < 0) {
                i = 0;
            }
            return this.quantityDropped(random) * (i + 1);
        } else {
            return this.quantityDropped(random);
        }
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        return 3 + fortune;
    }
}
