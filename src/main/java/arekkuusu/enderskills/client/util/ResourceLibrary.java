package arekkuusu.enderskills.client.util;

import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

public class ResourceLibrary {
    //* Atlas stitching textures *//
    public static final Set<ResourceLocation> ATLAS_SET = Sets.newHashSet();
    public static final ResourceLocation DARK_BACKGROUND = new ResourceLocation(LibMod.MOD_ID, "textures/entity/dark_background.png");
    public static final ResourceLocation PORTAL_BACKGROUND = new ResourceLocation("textures/entity/end_portal.png");
    //* Atlas textures *//
    public static final ResourceLocation PLUS = createAtlasTexture("effect", "plus");
    public static final ResourceLocation MINUS = createAtlasTexture("effect", "minus");
    public static final ResourceLocation MOTE = createAtlasTexture("effect", "mote");
    public static final ResourceLocation SPIRAL = createAtlasTexture("effect", "spiral");
    public static final ResourceLocation SPIT = createAtlasTexture("effect", "spit");
    public static final ResourceLocation SKULL = createAtlasTexture("effect", "skull");
    public static final ResourceLocation DROPLET = createAtlasTexture("effect", "droplet");
    public static final ResourceLocation CROSS = createAtlasTexture("effect", "cross");
    public static final ResourceLocation ANGRY = createAtlasTexture("effect", "angry");
    public static final ResourceLocation GLOW_PARTICLE_EFFECT = createAtlasTexture("effect", "glow_particle");
    public static final ResourceLocation BLOOD = createAtlasTexture("blocks", "blood");
    public static final ResourceLocation VOLT_PARTICLE = createAtlasTexture("effect", "volt_particle");
    //* GUI textures *//
    public static final ResourceLocation POTION_TEXTURES = new ResourceLocation(LibMod.MOD_ID, "textures/gui/potions.png");
    public static final ResourceLocation ENDURANCE_BACKGROUND = new ResourceLocation(LibMod.MOD_ID, "textures/gui/endurance_background.png");
    public static final ResourceLocation ENDURANCE_BACKGROUND_ = new ResourceLocation(LibMod.MOD_ID, "textures/gui/endurance_background_.png");
    public static final ResourceLocation SKILL_BACKGROUND = new ResourceLocation(LibMod.MOD_ID, "textures/gui/skill_background.png");
    //Attributes
    public static final ResourceLocation ATTRIBUTE_0_0 = createAtlasTexture("gui", "attribute_0_0");
    public static final ResourceLocation ATTRIBUTE_0_1 = createAtlasTexture("gui", "attribute_0_1");
    public static final ResourceLocation ATTRIBUTE_1_0 = createAtlasTexture("gui", "attribute_1_0");
    public static final ResourceLocation ATTRIBUTE_1_1 = createAtlasTexture("gui", "attribute_1_1");
    public static final ResourceLocation ATTRIBUTE_2_0 = createAtlasTexture("gui", "attribute_2_0");
    public static final ResourceLocation ATTRIBUTE_2_1 = createAtlasTexture("gui", "attribute_2_1");
    //Light
    public static final ResourceLocation CHARM = createAtlasTexture("gui", LibNames.CHARM);
    public static final ResourceLocation HEAL_AURA = createAtlasTexture("gui", LibNames.HEAL_AURA);
    public static final ResourceLocation POWER_BOOST = createAtlasTexture("gui", LibNames.POWER_BOOST);
    public static final ResourceLocation HEAL_OTHER = createAtlasTexture("gui", LibNames.HEAL_OTHER);
    public static final ResourceLocation HEAL_SELF = createAtlasTexture("gui", LibNames.HEAL_SELF);
    public static final ResourceLocation NEARBY_INVINCIBILITY = createAtlasTexture("gui", LibNames.NEARBY_INVINCIBILITY);
    //Earth
    public static final ResourceLocation TAUNT = createAtlasTexture("gui", LibNames.TAUNT);
    public static final ResourceLocation WALL = createAtlasTexture("gui", LibNames.WALL);
    public static final ResourceLocation DOME = createAtlasTexture("gui", LibNames.DOME);
    public static final ResourceLocation THORNY = createAtlasTexture("gui", LibNames.THORNY);
    public static final ResourceLocation SHOCKWAVE = createAtlasTexture("gui", LibNames.SHOCKWAVE);
    public static final ResourceLocation ANIMATED_STONE_GOLEM = createAtlasTexture("gui", LibNames.ANIMATED_STONE_GOLEM);
    //Wind
    public static final ResourceLocation DASH = createAtlasTexture("gui", LibNames.DASH);
    public static final ResourceLocation EXTRA_JUMP = createAtlasTexture("gui", LibNames.EXTRA_JUMP);
    public static final ResourceLocation FOG = createAtlasTexture("gui", LibNames.FOG);
    public static final ResourceLocation SMASH = createAtlasTexture("gui", LibNames.SMASH);
    public static final ResourceLocation HASTEN = createAtlasTexture("gui", LibNames.HASTEN);
    public static final ResourceLocation SPEED_BOOST = createAtlasTexture("gui", LibNames.SPEED_BOOST);
    //Void
    public static final ResourceLocation WARP = createAtlasTexture("gui", LibNames.WARP);
    public static final ResourceLocation INVISIBILITY = createAtlasTexture("gui", LibNames.INVISIBILITY);
    public static final ResourceLocation HOVER = createAtlasTexture("gui", LibNames.HOVER);
    public static final ResourceLocation UNSTABLE_PORTAL = createAtlasTexture("gui", LibNames.UNSTABLE_PORTAL);
    public static final ResourceLocation PORTAL = createAtlasTexture("gui", LibNames.PORTAL);
    public static final ResourceLocation TELEPORT = createAtlasTexture("gui", LibNames.TELEPORT);
    //Void
    public static final ResourceLocation SHADOW = createAtlasTexture("gui", LibNames.SHADOW);
    public static final ResourceLocation GLOOM = createAtlasTexture("gui", LibNames.GLOOM);
    public static final ResourceLocation SHADOW_JAB = createAtlasTexture("gui", LibNames.SHADOW_JAB);
    public static final ResourceLocation GAS_CLOUD = createAtlasTexture("gui", LibNames.GAS_CLOUD);
    public static final ResourceLocation GRASP = createAtlasTexture("gui", LibNames.GRASP);
    public static final ResourceLocation BLACK_HOLE = createAtlasTexture("gui", LibNames.BLACK_HOLE);
    //Blood
    public static final ResourceLocation BLEED = createAtlasTexture("gui", LibNames.BLEED);
    public static final ResourceLocation BLOOD_POOL = createAtlasTexture("gui", LibNames.BLOOD_POOL);
    public static final ResourceLocation CONTAMINATE = createAtlasTexture("gui", LibNames.CONTAMINATE);
    public static final ResourceLocation LIFE_STEAL = createAtlasTexture("gui", LibNames.LIFE_STEAL);
    public static final ResourceLocation SYPHON = createAtlasTexture("gui", LibNames.SYPHON);
    public static final ResourceLocation SACRIFICE = createAtlasTexture("gui", LibNames.SACRIFICE);
    //Wind
    public static final ResourceLocation SLASH = createAtlasTexture("gui", LibNames.SLASH);
    public static final ResourceLocation PUSH = createAtlasTexture("gui", LibNames.PUSH);
    public static final ResourceLocation PULL = createAtlasTexture("gui", LibNames.PULL);
    public static final ResourceLocation CRUSH = createAtlasTexture("gui", LibNames.CRUSH);
    public static final ResourceLocation UPDRAFT = createAtlasTexture("gui", LibNames.UPDRAFT);
    public static final ResourceLocation SUFFOCATE = createAtlasTexture("gui", LibNames.SUFFOCATE);
    //Fire
    public static final ResourceLocation FIRE_SPIRIT = createAtlasTexture("gui", LibNames.FIRE_SPIRIT);
    public static final ResourceLocation FLAMING_BREATH = createAtlasTexture("gui", LibNames.FLAMING_BREATH);
    public static final ResourceLocation FLAMING_RAIN = createAtlasTexture("gui", LibNames.FLAMING_RAIN);
    public static final ResourceLocation FOCUS_FLAME = createAtlasTexture("gui", LibNames.FOCUS_FLAME);
    public static final ResourceLocation FIREBALL = createAtlasTexture("gui", LibNames.FIREBALL);
    public static final ResourceLocation EXPLODE = createAtlasTexture("gui", LibNames.EXPLODE);

    public static ResourceLocation createAtlasTexture(String folder, String name) {
        ResourceLocation location = new ResourceLocation(LibMod.MOD_ID, folder + "/" + name);
        ATLAS_SET.add(location);
        return location;
    }
}
