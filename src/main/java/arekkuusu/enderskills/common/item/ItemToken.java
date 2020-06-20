package arekkuusu.enderskills.common.item;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.common.entity.EntityTokenOrb;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemToken extends ItemBase {

    public ItemToken() {
        super(LibNames.TOKEN);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (!worldIn.isRemote) {
            Capabilities.advancement(playerIn).ifPresent(c -> {
                ItemStack stack = playerIn.getHeldItem(handIn);
                if (playerIn.isSneaking() && stack.getCount() > 1) {
                    int orbCount = 1 + worldIn.rand.nextInt(stack.getCount());
                    int xpAmount = stack.getCount();
                    int xpAmountPerOrb = xpAmount / orbCount;
                    for (int i = 0; i < orbCount; i++) {
                        EntityTokenOrb orb = new EntityTokenOrb(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, xpAmountPerOrb);
                        worldIn.spawnEntity(orb);
                    }
                    stack.setCount(0);
                } else {
                    EntityTokenOrb orb = new EntityTokenOrb(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, 1);
                    worldIn.spawnEntity(orb);
                    stack.shrink(1);
                }
                worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.PLAYERS, 1.0F, (1.0F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.2F) * 0.7F);
            });
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
