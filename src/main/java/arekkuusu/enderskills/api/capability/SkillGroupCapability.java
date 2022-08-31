package arekkuusu.enderskills.api.capability;

import arekkuusu.enderskills.api.capability.data.SkillGroup;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

@SuppressWarnings("ConstantConditions")
public class SkillGroupCapability implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<SkillGroupCapability> {

    public static final Function<String, SkillGroup> GROUP_SUPPLIER = s -> new SkillGroup();
    public static final SkillGroup GROUP_EMPTY = new SkillGroup();
    public Map<String, SkillGroup> skillGroupMap = new LinkedHashMap<>();

    {
        //Defense-Light
        putWeight("Defense-Light", ModAbilities.CHARM, 0);
        putWeight("Defense-Light", ModAbilities.HEAL_AURA, 1);
        putWeight("Defense-Light", ModAbilities.POWER_BOOST, 2);
        putWeight("Defense-Light", ModAbilities.HEAL_OTHER, 3);
        putWeight("Defense-Light", ModAbilities.HEAL_SELF, 4);
        putWeight("Defense-Light", ModAbilities.NEARBY_INVINCIBILITY, 5);
        //Defense-Earth
        putWeight("Defense-Earth", ModAbilities.TAUNT, 0);
        putWeight("Defense-Earth", ModAbilities.WALL, 1);
        putWeight("Defense-Earth", ModAbilities.DOME, 2);
        putWeight("Defense-Earth", ModAbilities.THORNY, 3);
        putWeight("Defense-Earth", ModAbilities.SHOCKWAVE, 4);
        putWeight("Defense-Earth", ModAbilities.ANIMATED_STONE_GOLEM, 5);
        //Defense-Electric
        putWeight("Defense-Electric", ModAbilities.SHOCKING_AURA, 0);
        putWeight("Defense-Electric", ModAbilities.ELECTRIC_PULSE, 1);
        putWeight("Defense-Electric", ModAbilities.MAGNETIC_PULL, 2);
        putWeight("Defense-Electric", ModAbilities.POWER_DRAIN, 3);
        putWeight("Defense-Electric", ModAbilities.ENERGIZE, 4);
        putWeight("Defense-Electric", ModAbilities.VOLTAIC_SENTINEL, 5);
        //Defense-Fire
        putWeight("Defense-Fire", ModAbilities.FLARES, 0);
        putWeight("Defense-Fire", ModAbilities.BLAZING_AURA, 1);
        putWeight("Defense-Fire", ModAbilities.RING_OF_FIRE, 2);
        putWeight("Defense-Fire", ModAbilities.OVERHEAT, 3);
        putWeight("Defense-Fire", ModAbilities.WARM_HEART, 4);
        putWeight("Defense-Fire", ModAbilities.HOME_STAR, 5);
        //Mobility-Wind
        putWeight("Mobility-Wind", ModAbilities.DASH, 0);
        putWeight("Mobility-Wind", ModAbilities.EXTRA_JUMP, 1);
        putWeight("Mobility-Wind", ModAbilities.FOG, 2);
        putWeight("Mobility-Wind", ModAbilities.SMASH, 3);
        putWeight("Mobility-Wind", ModAbilities.HASTEN, 4);
        putWeight("Mobility-Wind", ModAbilities.SPEED_BOOST, 5);
        //Mobility-Void
        putWeight("Mobility-Void", ModAbilities.WARP, 0);
        putWeight("Mobility-Void", ModAbilities.INVISIBILITY, 1);
        putWeight("Mobility-Void", ModAbilities.HOVER, 2);
        putWeight("Mobility-Void", ModAbilities.UNSTABLE_PORTAL, 3);
        putWeight("Mobility-Void", ModAbilities.PORTAL, 4);
        putWeight("Mobility-Void", ModAbilities.TELEPORT, 5);
        //Offense-Void
        putWeight("Offense-Void", ModAbilities.SHADOW, 0);
        putWeight("Offense-Void", ModAbilities.GLOOM, 1);
        putWeight("Offense-Void", ModAbilities.SHADOW_JAB, 2);
        putWeight("Offense-Void", ModAbilities.GAS_CLOUD, 3);
        putWeight("Offense-Void", ModAbilities.GRASP, 4);
        putWeight("Offense-Void", ModAbilities.BLACK_HOLE, 5);
        //Offense-Blood
        putWeight("Offense-Blood", ModAbilities.BLEED, 0);
        putWeight("Offense-Blood", ModAbilities.BLOOD_POOL, 1);
        putWeight("Offense-Blood", ModAbilities.CONTAMINATE, 2);
        putWeight("Offense-Blood", ModAbilities.LIFE_STEAL, 3);
        putWeight("Offense-Blood", ModAbilities.SYPHON, 4);
        putWeight("Offense-Blood", ModAbilities.SACRIFICE, 5);
        //Offense-Wind
        putWeight("Offense-Wind", ModAbilities.SLASH, 0);
        putWeight("Offense-Wind", ModAbilities.PUSH, 1);
        putWeight("Offense-Wind", ModAbilities.PULL, 2);
        putWeight("Offense-Wind", ModAbilities.CRUSH, 3);
        putWeight("Offense-Wind", ModAbilities.UPDRAFT, 4);
        putWeight("Offense-Wind", ModAbilities.SUFFOCATE, 5);
        //Offense-Fire
        putWeight("Offense-Fire", ModAbilities.FIRE_SPIRIT, 0);
        putWeight("Offense-Fire", ModAbilities.FLAMING_BREATH, 1);
        putWeight("Offense-Fire", ModAbilities.FLAMING_RAIN, 2);
        putWeight("Offense-Fire", ModAbilities.FOCUS_FLAME, 3);
        putWeight("Offense-Fire", ModAbilities.FIREBALL, 4);
        putWeight("Offense-Fire", ModAbilities.EXPLODE, 5);
        //Offense-Light
        putWeight("Offense-Light", ModAbilities.RADIANT_RAY, 0);
        putWeight("Offense-Light", ModAbilities.LUMEN_WAVE, 1);
        putWeight("Offense-Light", ModAbilities.GLEAM_FLASH, 2);
        putWeight("Offense-Light", ModAbilities.SOLAR_LANCE, 3);
        putWeight("Offense-Light", ModAbilities.BARRAGE_WISP, 4);
        putWeight("Offense-Light", ModAbilities.FINAL_FLASH, 5);
    }

    /* Skill Weights */
    public boolean hasWeight(String group, Skill skill) {
        return skillGroupMap.containsKey(group) && skillGroupMap.get(group).map.containsKey(skill);
    }

    public void putWeight(String group, Skill skill, int weight) {
        SkillGroup skillGroup = skillGroupMap.computeIfAbsent(group, GROUP_SUPPLIER);
        skillGroup.map.put(skill, weight);
    }

    public void removeWeight(String group, Skill skill) {
        if (skillGroupMap.containsKey(group)) {
            skillGroupMap.get(group).map.remove(skill);
            if(skillGroupMap.get(group).map.isEmpty()) {
                skillGroupMap.remove(group);
            }
        }
    }

    public int getWeight(String group, Skill skill) {
        SkillGroup skillGroup = skillGroupMap.getOrDefault(group, GROUP_EMPTY);
        return skillGroup.map.getOrDefault(skill, Integer.MAX_VALUE);
    }

    public SkillGroup getGroup(String group) {
        return skillGroupMap.get(group);
    }

    public Map<String, SkillGroup> getGroups() {
        return skillGroupMap;
    }

    public void clearWeight() {
        skillGroupMap.clear();
    }
    /* Skill Weights */

    public static void init() {
        CapabilityManager.INSTANCE.register(SkillGroupCapability.class, new SkillGroupCapability(), SkillGroupCapability::new);
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.WEIGHT ? Capabilities.WEIGHT.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.WEIGHT.getStorage().writeNBT(Capabilities.WEIGHT, this, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.WEIGHT.getStorage().readNBT(Capabilities.WEIGHT, this, null, nbt);
    }

    //** NBT **//
    public static final String GROUP_LIST_NBT = "group_list";
    public static final String GROUP_NAME_NBT = "group_name";
    public static final String GROUP_DATA_NBT = "group_data";

    @Override
    @Nullable
    public NBTBase writeNBT(Capability<SkillGroupCapability> capability, SkillGroupCapability instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList skillWeightList = new NBTTagList();
        //Write all Weights
        for (Entry<String, SkillGroup> set : instance.skillGroupMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setString(compound, GROUP_NAME_NBT, set.getKey());
            NBTHelper.setNBT(compound, GROUP_DATA_NBT, set.getValue().serializeNBT());
            skillWeightList.appendTag(compound);
        }
        //Write tags
        tag.setTag(GROUP_LIST_NBT, skillWeightList);
        return tag;
    }

    @Override
    public void readNBT(Capability<SkillGroupCapability> capability, SkillGroupCapability instance, EnumFacing side, NBTBase nbt) {
        instance.skillGroupMap.clear();
        NBTTagCompound tag = (NBTTagCompound) nbt;
        NBTTagList skillWeightList = tag.getTagList(GROUP_LIST_NBT, Constants.NBT.TAG_COMPOUND);
        //Read and add all Weights
        for (int i = 0; i < skillWeightList.tagCount(); i++) {
            NBTTagCompound compound = skillWeightList.getCompoundTagAt(i);
            String name = NBTHelper.getString(compound, GROUP_NAME_NBT);
            SkillGroup group = new SkillGroup(NBTHelper.getNBTTag(compound, GROUP_DATA_NBT));
            instance.skillGroupMap.put(name, group);
        }
    }

    public static class Handler {
        private static final ResourceLocation KEY = new ResourceLocation(LibMod.MOD_ID, "skill_group");

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityLivingBase)
                event.addCapability(KEY, Capabilities.WEIGHT.getDefaultInstance());
        }

        @SubscribeEvent
        public void clonePlayer(PlayerEvent.Clone event) {
            event.getEntityPlayer().getCapability(Capabilities.WEIGHT, null)
                    .deserializeNBT(event.getOriginal().getCapability(Capabilities.WEIGHT, null).serializeNBT());
        }
    }
}
