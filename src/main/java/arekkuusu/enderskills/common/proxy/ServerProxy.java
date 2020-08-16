package arekkuusu.enderskills.common.proxy;

import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
@EventBusSubscriber(modid = LibMod.MOD_ID, value = Side.SERVER)
public class ServerProxy implements IProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Override
    public EntityPlayer getPlayer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addToQueue(Runnable runnable) {
        Events.QUEUE.add(runnable);
    }

    @Override
    public void playSound(World world, Vec3d vec, SoundEvent event, SoundCategory category, float volume) {
        float pitch = (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F;
        ((WorldServer) world).getMinecraftServer().getPlayerList().sendToAllNearExcept(null, vec.x, vec.y, vec.z, volume > 1.0F ? (double) (16.0F * volume) : 16.0D, world.provider.getDimension(), new SPacketSoundEffect(event, category, vec.x, vec.y, vec.z, volume, pitch));
    }

    @Override
    public void spawnParticle(World world, Vec3d pos, Vec3d speed, float scale, int age, int rgb, ResourceLocation location) {
        PacketHelper.sendParticle(world, pos, speed, scale, age, rgb, location);
    }

    @Override
    public void spawnLightning(World world, Vector from, Vector to, int generations, float offset, int age, int rgb, boolean branch) {
        PacketHelper.sendParticleLightning(world, from, to, generations, offset, age, rgb, branch);
    }

    @SubscribeEvent
    public static void playerServerJoin(PlayerEvent.PlayerLoggedInEvent event) {

    }

    @SubscribeEvent
    public static void playerServerLeave(PlayerEvent.PlayerLoggedOutEvent event) {

    }

    @Mod.EventHandler
    public static void serverStop(FMLServerStoppedEvent event) {

    }
}
