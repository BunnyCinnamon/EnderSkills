package arekkuusu.enderskills.common.potion;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.event.SkillActivateEvent;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PotionCooldown extends PotionBase {

    protected PotionCooldown() {
        super(LibNames.COOLDOWN_EFFECT, 0xFFC300, 1);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onUpdate(EntityLivingBase entity, int amplifier) {
        if (entity.world.isRemote) return;
        NBTTagList list = entity.getEntityData().getTagList(getName(), Constants.NBT.TAG_COMPOUND);
        List<ResourceLocation> locations = new ArrayList<>();
        list.iterator().forEachRemaining(nbt -> {
            locations.add(NBTHelper.getResourceLocation((NBTTagCompound) nbt, "location"));
        });
        double crd = 0.3D * (amplifier + 1);
        Capabilities.get(entity).map(c -> c.getAllOwned().entrySet()).ifPresent(entries -> {
            for (Map.Entry<Skill, SkillInfo> entry : entries) {
                SkillInfo info = entry.getValue();
                Skill skill = entry.getKey();
                ResourceLocation location = skill.getRegistryName();
                if (skill.getProperties() instanceof BaseAbility.AbilityProperties && !locations.contains(location)
                        && info instanceof SkillInfo.IInfoCooldown && location != null) {
                    if (((SkillInfo.IInfoCooldown) info).hasCooldown()) {
                        ((SkillInfo.IInfoCooldown) info).setCooldown(
                                Math.max(0, ((SkillInfo.IInfoCooldown) info).getCooldown() - (int) (((BaseAbility.AbilityProperties) skill.getProperties()).getCooldown((AbilityInfo) info) * crd))
                        );
                        if (entity instanceof EntityPlayer) {
                            PacketHelper.sendSkillSync((EntityPlayerMP) entity, skill);
                        }
                        NBTTagCompound tag = new NBTTagCompound();
                        NBTHelper.setResourceLocation(tag, "location", location);
                        list.appendTag(tag);
                    }
                }
            }
        });
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap attributeMapIn, int amplifier) {
        if (!entity.getEntityData().hasKey(getName())) {
            entity.getEntityData().setTag(getName(), new NBTTagList());
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap attributeMapIn, int amplifier) {
    }

    @SubscribeEvent
    public void onSkillUse(SkillActivateEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        NBTTagList list = event.getEntityLiving().getEntityData().getTagList(getName(), Constants.NBT.TAG_COMPOUND);
        if (list.hasNoTags()) return; //No tags here to remove, go back!
        Skill skill = event.getSkill();
        ResourceLocation location = skill.getRegistryName();
        int index = -1;
        for (int i = 0; i < list.tagCount(); i++) {
            ResourceLocation savedLocation = NBTHelper.getResourceLocation(list.getCompoundTagAt(i), "location");
            if (savedLocation.equals(location)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            list.removeTag(index);
        }
    }
}
