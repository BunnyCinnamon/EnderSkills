package arekkuusu.enderskills.api.capability.data;

import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Map;

public class SkillGroup implements INBTSerializable<NBTTagCompound> {

    //** NBT **//
    public static final String WEIGHT_LIST_NBT = "weight_info";
    public static final String SKILL_NBT = "skill";
    public static final String SKILL_WEIGHT_NBT = "skill_weight";

    public Object2IntMap<Skill> map = new Object2IntArrayMap<>();

    public SkillGroup() {
    }

    public SkillGroup(NBTTagCompound nbt) {
        this.deserializeNBT(nbt);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList skillWeightList = new NBTTagList();
        //Write all Weights
        for (Map.Entry<Skill, Integer> set : map.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setResourceLocation(compound, SKILL_NBT, set.getKey().getRegistryName());
            compound.setInteger(SKILL_WEIGHT_NBT, set.getValue());
            skillWeightList.appendTag(compound);
        }
        nbt.setTag(WEIGHT_LIST_NBT, skillWeightList);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        map.clear();
        NBTTagList skillWeightList = nbt.getTagList(WEIGHT_LIST_NBT, Constants.NBT.TAG_COMPOUND);
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        //Read and add all Weights
        for (int i = 0; i < skillWeightList.tagCount(); i++) {
            NBTTagCompound compound = skillWeightList.getCompoundTagAt(i);
            ResourceLocation location = NBTHelper.getResourceLocation(compound, SKILL_NBT);
            Skill skill = registry.getValue(location);
            map.put(skill, compound.getInteger(SKILL_WEIGHT_NBT));
        }
    }
}
