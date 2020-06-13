package arekkuusu.enderskills.common.entity;

import arekkuusu.enderskills.common.entity.placeable.*;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModEntities {

    private static int id;

    public static void register(IForgeRegistry<EntityEntry> registry) {
        register(registry, EntityThrowableData.class, "throwable_data", true);
        register(registry, EntityPlaceableData.class, "placeable_data", true);
        //Earth
        register(registry, EntityWall.class, LibNames.WALL, false);
        register(registry, EntityWallSegment.class, LibNames.WALL + "_segment", false);
        register(registry, EntityPlaceableShockwave.class, LibNames.SHOCKWAVE, false);
        register(registry, EntityStoneGolem.class, LibNames.ANIMATED_STONE_GOLEM, true);
        //Light
        //Blood
        register(registry, EntityPlaceableBloodPool.class, LibNames.BLOOD_POOL, false);
        //Void
        register(registry, EntityPortal.class, LibNames.PORTAL, false);
        register(registry, EntityBlackHole.class, LibNames.BLACK_HOLE, false);
        register(registry, EntityPlaceableGrasp.class, LibNames.GRASP, false);
        register(registry, EntityShadow.class, LibNames.SHADOW, false);
        //Wind
        register(registry, EntityPlaceableSmash.class, LibNames.SMASH, false);
        register(registry, EntityCrush.class, LibNames.CRUSH, false);
        register(registry, EntityPlaceableUpdraft.class, LibNames.UPDRAFT, true);
        //Fire
        register(registry, EntityPlaceableFlamingRain.class, LibNames.FLAMING_RAIN, false);
        register(registry, EntityPlaceableExplode.class, LibNames.EXPLODE, false);
        register(registry, EntityTokenOrb.class, LibNames.TOKEN, true);
    }

    private static void register(IForgeRegistry<EntityEntry> registry, Class<? extends Entity> clazz, String name, boolean update) {
        ResourceLocation resource = new ResourceLocation(LibMod.MOD_ID, name);
        registry.register(EntityEntryBuilder.create().entity(clazz).id(resource, ++id).name(name).tracker(64, 1, update).build());
    }
}
