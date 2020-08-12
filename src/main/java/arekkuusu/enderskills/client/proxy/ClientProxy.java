package arekkuusu.enderskills.client.proxy;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.keybind.KeyBounds;
import arekkuusu.enderskills.client.render.ModRenders;
import arekkuusu.enderskills.client.render.effect.*;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.ModelHelper;
import arekkuusu.enderskills.client.util.helper.RenderMisc;
import arekkuusu.enderskills.client.util.resource.ShaderManager;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.proxy.IProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = LibMod.MOD_ID, value = Side.CLIENT)
public class ClientProxy implements IProxy {

    public static final ParticleManager PARTICLE_RENDERER = new ParticleManager();
    public static final LightningManager LIGHTNING_MANAGER = new LightningManager();

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelHelper.registerModels();
    }

    @SubscribeEvent
    public static void stitchTextures(TextureStitchEvent event) {
        TextureMap map = event.getMap();
        ResourceLibrary.ATLAS_SET.forEach(map::registerSprite);
        for (Map.Entry<ResourceLocation, Skill> entry : GameRegistry.findRegistry(Skill.class).getEntries()) {
            if (entry.getValue().getProperties().hasTexture()) {
                map.registerSprite(ResourceLibrary.createAtlasTexture(entry.getKey().getResourceDomain(), "skills", entry.getKey().getResourcePath()));
            }
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(ShaderManager.INSTANCE);
        ModRenders.preInit();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        KeyBounds.init();
        ShaderLibrary.init();
        ModRenders.init();
        RenderMisc.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        ModRenders.postInit();
    }

    @Override
    public EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void addToQueue(Runnable runnable) {
        Events.QUEUE.add(runnable);
    }

    @Override
    public void spawnParticle(World world, Vec3d pos, Vec3d speed, float scale, int age, int rgb, ResourceLocation location) {
        if (canParticleSpawn()) {
            PARTICLE_RENDERER.add(new ParticleBase(world, pos, speed, scale, age, rgb, location));
        }
    }

    @Override
    public void spawnParticleLuminescence(World world, Vec3d pos, Vec3d speed, float scale, int age, ResourceLocation location) {
        if (canParticleSpawn()) {
            PARTICLE_RENDERER.add(new ParticleLuminescence(world, pos, speed, scale, age, location));
        }
    }

    @Override
    public void spawnLightning(Vector from, Vector to, int generations, float offset, int age, int rgb, boolean branch) {
        LIGHTNING_MANAGER.add(new Lightning(from, to, generations, offset, age, rgb, branch));
    }

    @Override
    public void playSound(World world, Vec3d vec, SoundEvent event, SoundCategory category, float volume) {
        float pitch = (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F;
        world.playSound(vec.x, vec.y, vec.z, event, category, volume, pitch, false);
    }

    public static boolean canParticleSpawn() {
        int setting = Minecraft.getMinecraft().gameSettings.particleSetting;
        float chance;
        switch (setting) {
            case 1:
                chance = 0.6F * (float) ClientConfig.RENDER_CONFIG.rendering.particleSpawning;
                break;
            case 2:
                chance = 0.2F * (float) ClientConfig.RENDER_CONFIG.rendering.particleSpawning;
                break;
            default:
                chance = (float) ClientConfig.RENDER_CONFIG.rendering.particleSpawning;
                break;
        }
        return Math.random() < chance;
    }

    public static boolean isOptifineInstalled() {
        return FMLClientHandler.instance().hasOptifine();
    }
}
