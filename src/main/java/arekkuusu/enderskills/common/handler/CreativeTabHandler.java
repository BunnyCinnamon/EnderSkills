package arekkuusu.enderskills.common.handler;

import arekkuusu.enderskills.common.block.ModBlocks;
import arekkuusu.enderskills.common.item.ModItems;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public final class CreativeTabHandler {

    public static final CreativeTab MISC = new Bamboozled();

    private static abstract class CreativeTab extends CreativeTabs {

        NonNullList<ItemStack> list;

        CreativeTab(String name) {
            super(LibMod.MOD_ID + "." + name);
        }

        @Override
        @SideOnly(Side.CLIENT)
        @Nonnull
        public ItemStack getTabIconItem() {
            return getIconItemStack();
        }

        @SideOnly(Side.CLIENT)
        void addItem(Item item) {
            item.getSubItems(this, list);
        }

        @SideOnly(Side.CLIENT)
        void addBlock(Block block) {
            block.getSubBlocks(this, list);
        }
    }

    private static class Bamboozled extends CreativeTab {

        Bamboozled() {
            super("misc_tab");
            setBackgroundImageName("items.png");
        }

        @Override
        @Nonnull
        public ItemStack getIconItemStack() {
            return new ItemStack(ModBlocks.ALTAR_ULTIMATE);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllRelevantItems(@Nonnull NonNullList<ItemStack> list) {
            this.list = list;
            addBlock(ModBlocks.ACACIA_BREAK_ALTAR);
            addBlock(ModBlocks.BIRCH_BREAK_ALTAR);
            addBlock(ModBlocks.DARK_OAK_BREAK_ALTAR);
            addBlock(ModBlocks.OAK_BREAK_ALTAR);
            addBlock(ModBlocks.JUNGLE_BREAK_ALTAR);
            addBlock(ModBlocks.SPRUCE_BREAK_ALTAR);
            addBlock(ModBlocks.ALTAR_ULTIMATE);
            addBlock(ModBlocks.CRYSTAL);
            addBlock(ModBlocks.ENDER_ORE);
            addItem(ModItems.TOKEN);
            addItem(ModItems.CRYSTAL_MATRIX);
            addItem(ModItems.ENDER_DUST);
        }
    }
}
