package arekkuusu.enderskills.api.capability;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("ConstantConditions")
public class SkilledEntityCapability implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<SkilledEntityCapability> {

    private Map<Skill, SkillInfo> skillPlayerInfoMap = Maps.newHashMap();
    private List<SkillHolder> skillHolders = Lists.newLinkedList();

    /* Skill Info */
    public Map<Skill, SkillInfo> getAllOwned() {
        return skillPlayerInfoMap;
    }

    public Optional<SkillInfo> getOwned(Skill skill) {
        return Optional.ofNullable(skillPlayerInfoMap.get(skill));
    }

    public boolean isOwned(Skill skill) {
        return skillPlayerInfoMap.containsKey(skill);
    }

    public void addOwned(Skill skill) {
        if (!isOwned(skill)) {
            skillPlayerInfoMap.put(skill, skill.createInfo(new NBTTagCompound()));
        }
    }

    public void removeOwned(Skill skill) {
        skillPlayerInfoMap.remove(skill);
    }

    public void clearOwned() {
        skillPlayerInfoMap.clear();
    }
    /* Skill Info */

    /* Skill Holders */
    public List<SkillHolder> getActives() {
        return skillHolders;
    }

    public Optional<SkillHolder> getActive(Skill skill) {
        return skillHolders.stream().filter(h -> h.data.skill == skill).findFirst();
    }

    public boolean isActive(Skill skill) {
        return skillHolders.stream().anyMatch(h -> h.data.skill == skill);
    }

    public void activate(SkillHolder holder) {
        if (holder.data.overrides.length > 0) {
            for (SkillHolder skillHolder : skillHolders) {
                if (Arrays.stream(holder.data.overrides).anyMatch(s -> s == skillHolder.data.skill)) { //Remove Override!
                    skillHolder.setDead();
                }
            }
        }
        skillHolders.add(holder);
    }

    public void deactivate(Skill skill) {
        for (SkillHolder skillHolder : skillHolders) {
            if (skillHolder.data.skill == skill) {
                skillHolder.setDead();
            }
        }
    }

    public void deactivate(Skill skill, Function<SkillHolder, Boolean> function) {
        for (SkillHolder skillHolder : skillHolders) {
            if (skillHolder.data.skill == skill && function.apply(skillHolder)) {
                skillHolder.setDead();
            }
        }
    }

    public void clearActive() {
        skillHolders.forEach(SkillHolder::setDead);
    }
    /* Skill Holders */

    public static void init() {
        CapabilityManager.INSTANCE.register(SkilledEntityCapability.class, new SkilledEntityCapability(), SkilledEntityCapability::new);
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.SKILLED_ENTITY ? Capabilities.SKILLED_ENTITY.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.SKILLED_ENTITY.getStorage().writeNBT(Capabilities.SKILLED_ENTITY, this, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.SKILLED_ENTITY.getStorage().readNBT(Capabilities.SKILLED_ENTITY, this, null, nbt);
    }

    //** NBT **//
    public static final String SKILL_LIST_NBT = "skill_list";
    public static final String HOLDER_LIST_NBT = "holder_list";
    public static final String SKILL_NBT = "skill";
    public static final String SKILL_INFO_NBT = "skill_info";

    @Override
    @Nullable
    public NBTBase writeNBT(Capability<SkilledEntityCapability> capability, SkilledEntityCapability instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList attributeList = new NBTTagList();
        NBTTagList skillHolderList = new NBTTagList();
        //Write all Skills
        for (Entry<Skill, SkillInfo> set : instance.skillPlayerInfoMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setResourceLocation(compound, SKILL_NBT, set.getKey().getRegistryName());
            compound.setTag(SKILL_INFO_NBT, set.getValue().serializeNBT());
            attributeList.appendTag(compound);
        }
        //Write all Holders
        for (SkillHolder skillHolder : instance.skillHolders) {
            NBTTagCompound compound = skillHolder.serializeNBT();
            skillHolderList.appendTag(compound);
        }
        //Write tags
        tag.setTag(SKILL_LIST_NBT, attributeList);
        tag.setTag(HOLDER_LIST_NBT, skillHolderList);
        return tag;
    }

    @Override
    public void readNBT(Capability<SkilledEntityCapability> capability, SkilledEntityCapability instance, EnumFacing side, NBTBase nbt) {
        instance.skillPlayerInfoMap.clear();
        instance.skillHolders.clear();
        NBTTagCompound tag = (NBTTagCompound) nbt;
        NBTTagList attributeList = tag.getTagList(SKILL_LIST_NBT, Constants.NBT.TAG_COMPOUND);
        NBTTagList skillHolderList = tag.getTagList(HOLDER_LIST_NBT, Constants.NBT.TAG_COMPOUND);
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        //Read and add all Skills
        for (int i = 0; i < attributeList.tagCount(); i++) {
            NBTTagCompound compound = attributeList.getCompoundTagAt(i);
            ResourceLocation location = NBTHelper.getResourceLocation(compound, SKILL_NBT);
            Skill skill = registry.getValue(location);
            SkillInfo info = skill.createInfo(compound.getCompoundTag(SKILL_INFO_NBT));
            instance.skillPlayerInfoMap.put(skill, info);
        }
        //Read and add all Holders
        for (int i = 0; i < skillHolderList.tagCount(); i++) {
            NBTTagCompound compound = skillHolderList.getCompoundTagAt(i);
            instance.skillHolders.add(new SkillHolder(compound));
        }
    }

    public static class Handler {
        private static final ResourceLocation KEY = new ResourceLocation(LibMod.MOD_ID, "skilled_entity");

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityLivingBase)
                event.addCapability(KEY, Capabilities.SKILLED_ENTITY.getDefaultInstance());
        }

        @SubscribeEvent
        public void clonePlayer(PlayerEvent.Clone event) {
            event.getEntityPlayer().getCapability(Capabilities.SKILLED_ENTITY, null)
                    .deserializeNBT(event.getOriginal().getCapability(Capabilities.SKILLED_ENTITY, null).serializeNBT());
        }
    }
}
