package arekkuusu.enderskills.common.block;

import arekkuusu.enderskills.common.block.tile.TileAltar;
import arekkuusu.enderskills.common.handler.CreativeTabHandler;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder(LibMod.MOD_ID)
public class ModBlocks {

    private static final Block PLACE_HOLDER = new Block(Material.AIR);
    //--------------------------------Blocks--------------------------------//
    public static final Block ACACIA_BREAK_ALTAR = PLACE_HOLDER;
    public static final Block BIRCH_BREAK_ALTAR = PLACE_HOLDER;
    public static final Block DARK_OAK_BREAK_ALTAR = PLACE_HOLDER;
    public static final Block JUNGLE_BREAK_ALTAR = PLACE_HOLDER;
    public static final Block OAK_BREAK_ALTAR = PLACE_HOLDER;
    public static final Block SPRUCE_BREAK_ALTAR = PLACE_HOLDER;
    public static final Block ALTAR_ULTIMATE = PLACE_HOLDER;
    public static final Block CRYSTAL = PLACE_HOLDER;
    public static final Block ENDER_ORE = PLACE_HOLDER;
    public static final Block BLACK_FIRE_FLAME = PLACE_HOLDER;

    public static void register(IForgeRegistry<Block> registry) {
        registry.register(new BlockAltar(LibNames.ALTAR_ULTIMATE, true));
        registry.register(new BlockAltar(LibNames.ACACIA_BREAK_ALTAR, false));
        registry.register(new BlockAltar(LibNames.BIRCH_BREAK_ALTAR, false));
        registry.register(new BlockAltar(LibNames.DARK_OAK_BREAK_ALTAR, false));
        registry.register(new BlockAltar(LibNames.JUNGLE_BREAK_ALTAR, false));
        registry.register(new BlockAltar(LibNames.OAK_BREAK_ALTAR, false));
        registry.register(new BlockAltar(LibNames.SPRUCE_BREAK_ALTAR, false));
        registry.register(new BlockCrystal());
        registry.register(new BlockEnderOre());
        registry.register(new BlockBlackFireFlame(LibNames.BLACK_FIRE_FLAME));
        registerTiles();
    }

    private static void registerTiles() {
        registerTile(TileAltar.class, LibNames.ALTAR);
    }

    private static <T extends TileEntity> void registerTile(Class<T> tile, String name) {
        GameRegistry.registerTileEntity(tile, new ResourceLocation(LibMod.MOD_ID, name));
    }

    @SuppressWarnings({"UnusedReturnValue"}) //Shut up
    public static Block setRegistry(Block block, String id) {
        block.setUnlocalizedName(id);
        block.setRegistryName(LibMod.MOD_ID, id);
        if (!id.equals(LibNames.BLACK_FIRE_FLAME))
            block.setCreativeTab(CreativeTabHandler.MISC);
        return block;
    }
}
