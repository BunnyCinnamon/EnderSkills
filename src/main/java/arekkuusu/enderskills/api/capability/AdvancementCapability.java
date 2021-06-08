package arekkuusu.enderskills.api.capability;

import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.XPHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

@SuppressWarnings("ConstantConditions")
public class AdvancementCapability implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<AdvancementCapability> {

    public Skill[] skillUnlockOrder = new Skill[0];
    public int experienceSpent;
    public int resetCount;
    /* Advancement specific */
    public double levelProgress;
    public int level;
    /* Advancement specific */
    /* Pins specific */
    public int tabPin = -1;
    public int tabPagePin = -1;
    /* Pins specific */
    /* Vanilla specific */
    public double experienceProgress;
    public int experienceLevel;
    /* Vanilla specific */

    public AdvancementCapability() {
        level = CommonConfig.getSyncValues().advancement.levels.defaultLevel;
    }

    public int getExperienceTotal(EntityLivingBase entity) {
        return XPHelper.getXPTotal(this.experienceLevel, this.experienceProgress)
                + (entity instanceof EntityPlayer ? XPHelper.getXPTotal((EntityPlayer) entity) : 0);
    }

    public void addExperienceToTotal(int amount) {
        int totalStored = XPHelper.getXPTotal(this.experienceLevel, this.experienceProgress);
        int totalAdded = totalStored + amount;
        this.experienceLevel = Math.max(XPHelper.getLevelFromXPValue(totalAdded), 0);
        this.experienceProgress = Math.max(XPHelper.getLevelProgressFromXPValue(totalAdded), 0D);
    }

    public void consumeExperienceFromTotal(EntityLivingBase entity, int amount) {
        int totalStored = XPHelper.getXPTotal(this.experienceLevel, this.experienceProgress);
        if (entity instanceof EntityPlayer) {
            if (totalStored < amount) {
                this.experienceProgress = 0;
                this.experienceLevel = 0;
                amount = amount - totalStored;
                XPHelper.takeXP((EntityPlayer) entity, amount);
            } else {
                takeXP(amount);
            }
            experienceSpent += amount;
        } else if (totalStored >= amount) {
            takeXP(amount);
            experienceSpent += amount;
        }
    }

    public void takeXP(int xp) {
        int total = XPHelper.getXPTotal(this.experienceLevel, this.experienceProgress);
        int taken = Math.min(xp, total);
        total -= taken;
        this.experienceLevel = Math.max(XPHelper.getLevelFromXPValue(total), 0);
        this.experienceProgress = Math.max(XPHelper.getLevelProgressFromXPValue(total), 0D);
    }

    public static void init() {
        CapabilityManager.INSTANCE.register(AdvancementCapability.class, new AdvancementCapability(), AdvancementCapability::new);
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.ADVANCEMENT ? Capabilities.ADVANCEMENT.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.ADVANCEMENT.getStorage().writeNBT(Capabilities.ADVANCEMENT, this, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.ADVANCEMENT.getStorage().readNBT(Capabilities.ADVANCEMENT, this, null, nbt);
    }

    //** NBT **//
    public static final String EXPERIENCE_NBT = "experience";
    public static final String EXPERIENCE_SPENT_NBT = "experience_spent";
    public static final String EXPERIENCE_PROGRESS_NBT = "experience_progress";
    public static final String LEVEL_NBT = "level";
    public static final String LEVEL_PROGRESS_NBT = "level_progress";
    public static final String RESET_COUNT_NBT = "reset_count";
    public static final String SKILL_UNLOCK_ORDER_NBT = "skill_unlock_order";
    public static final String TAB_PIN_BHT = "tabPin";
    public static final String TAB_PAGE_PIN_BHT = "tabPagePin";

    @Override
    @Nullable
    public NBTBase writeNBT(Capability<AdvancementCapability> capability, AdvancementCapability instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(EXPERIENCE_NBT, instance.experienceLevel);
        tag.setInteger(EXPERIENCE_SPENT_NBT, instance.experienceSpent);
        tag.setDouble(EXPERIENCE_PROGRESS_NBT, instance.experienceProgress);
        tag.setInteger(LEVEL_NBT, instance.level);
        tag.setDouble(LEVEL_PROGRESS_NBT, instance.levelProgress);
        tag.setInteger(RESET_COUNT_NBT, instance.resetCount);
        NBTTagList list = new NBTTagList();
        for (Skill skill : instance.skillUnlockOrder) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setRegistry(compound, "skill", skill);
            list.appendTag(compound);
        }
        tag.setTag(SKILL_UNLOCK_ORDER_NBT, list);
        tag.setInteger(TAB_PIN_BHT, instance.tabPin);
        tag.setInteger(TAB_PAGE_PIN_BHT, instance.tabPagePin);
        return tag;
    }

    @Override
    public void readNBT(Capability<AdvancementCapability> capability, AdvancementCapability instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        instance.experienceLevel = tag.getInteger(EXPERIENCE_NBT);
        instance.experienceSpent = tag.getInteger(EXPERIENCE_SPENT_NBT);
        instance.experienceProgress = tag.getDouble(EXPERIENCE_PROGRESS_NBT);
        instance.level = tag.getInteger(LEVEL_NBT);
        instance.levelProgress = tag.getDouble(LEVEL_PROGRESS_NBT);
        instance.resetCount = tag.getInteger(RESET_COUNT_NBT);
        NBTTagList list = tag.getTagList(SKILL_UNLOCK_ORDER_NBT, Constants.NBT.TAG_COMPOUND);
        instance.skillUnlockOrder = new Skill[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            instance.skillUnlockOrder[i] = NBTHelper.getRegistry(list.getCompoundTagAt(i), "skill", Skill.class);
        }
        if(tag.hasKey(TAB_PIN_BHT)) {
            instance.tabPin = tag.getInteger(TAB_PIN_BHT);
        }
        if(tag.hasKey(TAB_PAGE_PIN_BHT)) {
            instance.tabPagePin = tag.getInteger(TAB_PAGE_PIN_BHT);
        }
    }

    public static class Handler {
        private static final ResourceLocation KEY = new ResourceLocation(LibMod.MOD_ID, "XP_ADVANCEMENT");

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityLivingBase)
                event.addCapability(KEY, Capabilities.ADVANCEMENT.getDefaultInstance());
        }

        @SubscribeEvent
        public void clonePlayer(PlayerEvent.Clone event) {
            event.getEntityPlayer().getCapability(Capabilities.ADVANCEMENT, null)
                    .deserializeNBT(event.getOriginal().getCapability(Capabilities.ADVANCEMENT, null).serializeNBT());
        }
    }
}
