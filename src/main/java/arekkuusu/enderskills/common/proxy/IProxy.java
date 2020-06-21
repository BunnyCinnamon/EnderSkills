package arekkuusu.enderskills.common.proxy;

import arekkuusu.enderskills.api.util.Vector;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.UUID;

public interface IProxy {

    GameProfile fakePlayer = new GameProfile(UUID.fromString("d5de2607-0b2d-4fae-a615-9c4bcf39d6b6"), "[EnderSkills]");

    void preInit(FMLPreInitializationEvent event);

    void init(FMLInitializationEvent event);

    void postInit(FMLPostInitializationEvent event);

    EntityPlayer getPlayer();

    default void spawnParticle(World world, Vec3d pos, Vec3d speed, float scale, int age, int rgb, ResourceLocation location) {
        //NO-OP
    }

    default void spawnParticleLuminescence(World world, Vec3d pos, Vec3d speed, float scale, int age, ResourceLocation location) {
        //NO-OP
    }

    default void spawnLightning(Vector from, Vector to, int generations, float offset, int age, int rgb, boolean branch) {
        //NO-OP
    }

    default void playSound(World world, Vec3d vec, SoundEvent event, SoundCategory category, float volume) {
        //NO-OP
    }
}
