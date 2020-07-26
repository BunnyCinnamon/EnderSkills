package arekkuusu.enderskills.common.item;

import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibGui;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemBook extends ItemBase {

    public ItemBook() {
        super(LibNames.BOOK);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            playerIn.openGui(EnderSkills.getInstance(), LibGui.LEVEL_EDITING, worldIn, 0, 0, 0);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
