package arekkuusu.enderskills.common.item;

import arekkuusu.enderskills.common.block.ModBlocks;
import arekkuusu.enderskills.common.handler.CreativeTabHandler;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder(LibMod.MOD_ID)
public final class ModItems {

    private static final Item PLACE_HOLDER = new Item();
    //--------------------------------Items--------------------------------//
    public static final Item ACACIA_BREAK_ALTAR = PLACE_HOLDER;
    public static final Item BIRCH_BREAK_ALTAR = PLACE_HOLDER;
    public static final Item DARK_OAK_BREAK_ALTAR = PLACE_HOLDER;
    public static final Item JUNGLE_BREAK_ALTAR = PLACE_HOLDER;
    public static final Item OAK_BREAK_ALTAR = PLACE_HOLDER;
    public static final Item SPRUCE_BREAK_ALTAR = PLACE_HOLDER;
    public static final Item CRYSTAL = PLACE_HOLDER;
    public static final Item TOKEN = PLACE_HOLDER;
    public static final Item CRYSTAL_MATRIX = PLACE_HOLDER;
    public static final Item ENDER_DUST = PLACE_HOLDER;

    public static void register(IForgeRegistry<Item> registry) {
        registry.register(itemBlock(ModBlocks.ACACIA_BREAK_ALTAR));
        registry.register(itemBlock(ModBlocks.BIRCH_BREAK_ALTAR));
        registry.register(itemBlock(ModBlocks.DARK_OAK_BREAK_ALTAR));
        registry.register(itemBlock(ModBlocks.JUNGLE_BREAK_ALTAR));
        registry.register(itemBlock(ModBlocks.OAK_BREAK_ALTAR));
        registry.register(itemBlock(ModBlocks.SPRUCE_BREAK_ALTAR));
        registry.register(itemBlock(ModBlocks.ALTAR_ULTIMATE));
        registry.register(itemBlock(ModBlocks.CRYSTAL));
        registry.register(new ItemToken());
        registry.register(new ItemBase(LibNames.CRYSTAL_MATRIX));
        registry.register(new ItemBase(LibNames.ENDER_DUST));
        registry.register(itemBlock(ModBlocks.ENDER_ORE));
    }

    public static void init() {
        OreDictionary.registerOre("dustEnder", ENDER_DUST);
    }

    @SuppressWarnings("ConstantConditions")
    private static Item itemBlock(Block block) {
        return new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"}) //Shut up
    public static Item setRegistry(Item item, String id) {
        item.setUnlocalizedName(id);
        item.setRegistryName(LibMod.MOD_ID, id);
        item.setCreativeTab(CreativeTabHandler.MISC);
        return item;
    }
}
