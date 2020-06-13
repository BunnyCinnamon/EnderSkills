package arekkuusu.enderskills.common.proxy;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import static net.minecraftforge.event.entity.player.PlayerEvent.Clone;

@EventBusSubscriber(modid = LibMod.MOD_ID)
public class Events {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PacketHelper.sendConfigReload((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            PacketHelper.sendSkillsSync((EntityPlayerMP) event.getEntity());
            PacketHelper.sendEnduranceSync((EntityPlayerMP) event.getEntity());
            PacketHelper.sendAdvancementSync((EntityPlayerMP) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            if (!event.isEndConquered()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();
        if (oldPlayer instanceof EntityPlayerMP && newPlayer instanceof EntityPlayerMP) {
            if (event.isWasDeath()) {
                Capabilities.get(oldPlayer).ifPresent(original -> {
                    Capabilities.get(newPlayer).ifPresent(replacement -> {
                        replacement.deserializeNBT(original.serializeNBT());
                    });
                });
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntityLiving().getEntityWorld().isRemote) {
            Capabilities.get(event.getEntityLiving()).ifPresent(skills -> {
                skills.tick(event.getEntityLiving());
            });
        }
    }
}
