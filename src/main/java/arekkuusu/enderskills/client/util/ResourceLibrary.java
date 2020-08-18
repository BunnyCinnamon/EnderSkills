package arekkuusu.enderskills.client.util;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.Set;

public class ResourceLibrary {

    //* Atlas stitching textures *//
    public static final Set<ResourceLocation> ATLAS_SET = Sets.newHashSet();
    public static final ResourceLocation DARK_BACKGROUND = new ResourceLocation(LibMod.MOD_ID, "textures/entity/dark_background.png");
    public static final ResourceLocation PORTAL_BACKGROUND = new ResourceLocation("textures/entity/end_portal.png");
    //* Atlas textures *//
    public static final ResourceLocation PLUS = createAtlasTexture(LibMod.MOD_ID, "effect", "plus");
    public static final ResourceLocation MINUS = createAtlasTexture(LibMod.MOD_ID, "effect", "minus");
    public static final ResourceLocation MOTE = createAtlasTexture(LibMod.MOD_ID, "effect", "mote");
    public static final ResourceLocation SPIRAL = createAtlasTexture(LibMod.MOD_ID, "effect", "spiral");
    public static final ResourceLocation SPIT = createAtlasTexture(LibMod.MOD_ID, "effect", "spit");
    public static final ResourceLocation SKULL = createAtlasTexture(LibMod.MOD_ID, "effect", "skull");
    public static final ResourceLocation DROPLET = createAtlasTexture(LibMod.MOD_ID, "effect", "droplet");
    public static final ResourceLocation CROSS = createAtlasTexture(LibMod.MOD_ID, "effect", "cross");
    public static final ResourceLocation ANGRY = createAtlasTexture(LibMod.MOD_ID, "effect", "angry");
    public static final ResourceLocation GLOW = createAtlasTexture(LibMod.MOD_ID, "effect", "glow_particle");
    public static final ResourceLocation BLOOD = createAtlasTexture(LibMod.MOD_ID, "blocks", "blood");
    public static final ResourceLocation VOLT_PARTICLE = createAtlasTexture(LibMod.MOD_ID, "effect", "volt_particle");
    public static final ResourceLocation RAIN = createAtlasTexture(LibMod.MOD_ID, "effect", "rain");
    static {
        ATLAS_SET.add(PLUS);
        ATLAS_SET.add(MINUS);
        ATLAS_SET.add(MOTE);
        ATLAS_SET.add(SPIRAL);
        ATLAS_SET.add(SPIT);
        ATLAS_SET.add(SKULL);
        ATLAS_SET.add(DROPLET);
        ATLAS_SET.add(CROSS);
        ATLAS_SET.add(ANGRY);
        ATLAS_SET.add(GLOW);
        ATLAS_SET.add(BLOOD);
        ATLAS_SET.add(VOLT_PARTICLE);
        ATLAS_SET.add(RAIN);
    }
    //* GUI textures *//
    public static final ResourceLocation POTION_TEXTURES = new ResourceLocation(LibMod.MOD_ID, "textures/gui/potions.png");
    public static final ResourceLocation ENDURANCE_BACKGROUND = new ResourceLocation(LibMod.MOD_ID, "textures/gui/endurance_background.png");
    public static final ResourceLocation ENDURANCE_BACKGROUND_ = new ResourceLocation(LibMod.MOD_ID, "textures/gui/endurance_background_.png");
    public static final ResourceLocation SKILL_BACKGROUND = new ResourceLocation(LibMod.MOD_ID, "textures/gui/skill_background.png");

    public static ResourceLocation createAtlasTexture(String modId, String folder, String name) {
        return new ResourceLocation(modId, folder + "/" + name);
    }

    public static ResourceLocation getSkillTexture(Skill skill) {
        Objects.requireNonNull(skill.getRegistryName());
        return new ResourceLocation(skill.getRegistryName().getResourceDomain(), "skills" + "/" + skill.getRegistryName().getResourcePath());
    }
}
