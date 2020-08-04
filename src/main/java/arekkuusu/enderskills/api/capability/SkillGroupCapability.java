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
        int i = -1;
        putWeight("Defense-Light", ModAbilities.CHARM, i + 1);
        putWeight("Defense-Light", ModAbilities.HEAL_AURA, i + 2);
        putWeight("Defense-Light", ModAbilities.POWER_BOOST, i + 3);
        putWeight("Defense-Light", ModAbilities.HEAL_OTHER, i + 4);
        putWeight("Defense-Light", ModAbilities.HEAL_SELF, i + 5);
        putWeight("Defense-Light", ModAbilities.NEARBY_INVINCIBILITY, i + 6);
        //Defense-Earth
        i = -1;
        putWeight("Defense-Earth", ModAbilities.TAUNT, i + 1);
        putWeight("Defense-Earth", ModAbilities.WALL, i + 2);
        putWeight("Defense-Earth", ModAbilities.DOME, i + 3);
        putWeight("Defense-Earth", ModAbilities.THORNY, i + 4);
        putWeight("Defense-Earth", ModAbilities.SHOCKWAVE, i + 5);
        putWeight("Defense-Earth", ModAbilities.ANIMATED_STONE_GOLEM, i + 6);
        //Mobility-Wind
        i = -1;
        putWeight("Mobility-Wind", ModAbilities.DASH, i + 1);
        putWeight("Mobility-Wind", ModAbilities.EXTRA_JUMP, i + 2);
        putWeight("Mobility-Wind", ModAbilities.FOG, i + 3);
        putWeight("Mobility-Wind", ModAbilities.SMASH, i + 4);
        putWeight("Mobility-Wind", ModAbilities.HASTEN, i + 5);
        putWeight("Mobility-Wind", ModAbilities.SPEED_BOOST, i + 6);
        //Mobility-Void
        i = -1;
        putWeight("Mobility-Void", ModAbilities.WARP, i + 1);
        putWeight("Mobility-Void", ModAbilities.INVISIBILITY, i + 2);
        putWeight("Mobility-Void", ModAbilities.HOVER, i + 3);
        putWeight("Mobility-Void", ModAbilities.UNSTABLE_PORTAL, i + 4);
        putWeight("Mobility-Void", ModAbilities.PORTAL, i + 5);
        putWeight("Mobility-Void", ModAbilities.TELEPORT, i + 6);
        //Offense-Void
        i = -1;
        putWeight("Offense-Void", ModAbilities.SHADOW, i + 1);
        putWeight("Offense-Void", ModAbilities.GLOOM, i + 2);
        putWeight("Offense-Void", ModAbilities.SHADOW_JAB, i + 3);
        putWeight("Offense-Void", ModAbilities.GAS_CLOUD, i + 4);
        putWeight("Offense-Void", ModAbilities.GRASP, i + 5);
        putWeight("Offense-Void", ModAbilities.BLACK_HOLE, i + 6);
        //Offense-Blood
        i = -1;
        putWeight("Offense-Blood", ModAbilities.BLEED, i + 1);
        putWeight("Offense-Blood", ModAbilities.BLOOD_POOL, i + 2);
        putWeight("Offense-Blood", ModAbilities.CONTAMINATE, i + 3);
        putWeight("Offense-Blood", ModAbilities.LIFE_STEAL, i + 4);
        putWeight("Offense-Blood", ModAbilities.SYPHON, i + 5);
        putWeight("Offense-Blood", ModAbilities.SACRIFICE, i + 6);
        //Offense-Wind
        i = -1;
        putWeight("Offense-Wind", ModAbilities.SLASH, i + 1);
        putWeight("Offense-Wind", ModAbilities.PUSH, i + 2);
        putWeight("Offense-Wind", ModAbilities.PULL, i + 3);
        putWeight("Offense-Wind", ModAbilities.CRUSH, i + 4);
        putWeight("Offense-Wind", ModAbilities.UPDRAFT, i + 5);
        putWeight("Offense-Wind", ModAbilities.SUFFOCATE, i + 6);
        //Offense-Fire
        i = -1;
        putWeight("Offense-Fire", ModAbilities.FIRE_SPIRIT, i + 1);
        putWeight("Offense-Fire", ModAbilities.FLAMING_BREATH, i + 2);
        putWeight("Offense-Fire", ModAbilities.FLAMING_RAIN, i + 3);
        putWeight("Offense-Fire", ModAbilities.FOCUS_FLAME, i + 4);
        putWeight("Offense-Fire", ModAbilities.FIREBALL, i + 5);
        putWeight("Offense-Fire", ModAbilities.EXPLODE, i + 6);
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
