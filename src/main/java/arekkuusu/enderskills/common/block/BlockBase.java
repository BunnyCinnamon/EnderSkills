package arekkuusu.enderskills.common.block;

import arekkuusu.enderskills.client.util.helper.IModel;
import arekkuusu.enderskills.client.util.helper.ModelHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class BlockBase extends Block implements IModel {

    public BlockBase(String id, Material material) {
        super(material);
        this.setDefaultState(defaultState());
        ModBlocks.setRegistry(this, id);
    }

    public Block setHarvestLevel(Tool lvl, ToolLevel i) {
        setHarvestLevel(lvl.name, i.ordinal());
        return this;
    }

    public Block setSound(SoundType type) {
        return super.setSoundType(type);
    }

    public IBlockState defaultState() {
        return blockState.getBaseState();
    }

    @SuppressWarnings("unchecked")
    public <T extends TileEntity> Optional<T> getTile(Class<T> clazz, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return clazz.isInstance(tile) ? Optional.of((T) tile) : Optional.empty();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelHelper.registerModel(this, 0);
    }

    public enum Tool {
        PICK("pickaxe"),
        AXE("axe"),
        SHOVEL("shovel");

        final String name;

        Tool(String name) {
            this.name = name;
        }
    }

    public enum ToolLevel {
        WOOD_GOLD,
        STONE,
        IRON,
        DIAMOND
    }
}
