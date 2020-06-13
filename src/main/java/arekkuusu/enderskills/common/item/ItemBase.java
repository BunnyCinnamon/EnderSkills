package arekkuusu.enderskills.common.item;

import arekkuusu.enderskills.client.util.helper.IModel;
import arekkuusu.enderskills.client.util.helper.ModelHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBase extends Item implements IModel {

	public ItemBase(String id) {
		super();
		ModItems.setRegistry(this, id);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		ModelHelper.registerModel(this, 0);
	}
}
