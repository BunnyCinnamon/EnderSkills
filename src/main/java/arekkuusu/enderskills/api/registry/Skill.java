package arekkuusu.enderskills.api.registry;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.common.network.PacketHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class Skill extends IForgeRegistryEntry.Impl<Skill> {

    private ResourceLocation texture;
    private String name;

    public void use(EntityLivingBase entity, SkillInfo skillInfo) {
        //For Rent
    }

    public void begin(EntityLivingBase entity, SkillData data) {
        update(entity, data, 0);
    }

    public void update(EntityLivingBase entity, SkillData data, int tick) {
        //For Rent
    }

    public void end(EntityLivingBase entity, SkillData data) {
        //For Rent
    }

    public boolean isClientWorld(EntityLivingBase entity) {
        return entity.getEntityWorld().isRemote;
    }

    public void apply(EntityLivingBase entity, SkillData data) {
        Capabilities.get(entity).ifPresent(skills -> skills.activate(new SkillHolder(data))); //Add to entity Server Side
    }

    public void unapply(EntityLivingBase entity) {
        Capabilities.get(entity).ifPresent(skills -> skills.deactivate(this)); //Remove from entity Server Side
    }

    public void sync(EntityLivingBase entity, SkillData data) {
        PacketHelper.sendSkillUseResponsePacket(entity, data); //Send to Client
    }

    public void async(EntityLivingBase entity) {
        PacketHelper.sendSkillRemoveResponsePacket(entity, this); //Send to Client
    }

    public void sync(EntityLivingBase entity) {
        PacketHelper.sendSkillSync((EntityPlayerMP) entity, this);
    }

    public int getMaxLevel() {
        return Integer.MAX_VALUE;
    }

    public void setUnlocalizedName(String name) {
        this.name = name;
    }

    public String getUnlocalizedName() {
        return "skill." + name;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public boolean hasStatusIcon() {
        return true;
    }

    public boolean isKeyBound() {
        return true;
    }

    @Deprecated
    public void initSyncConfig() {
        //For Rent
    }

    @Deprecated
    public void writeSyncConfig(NBTTagCompound compound) {
        //For Rent
    }

    @SideOnly(Side.CLIENT)
    @Deprecated
    public void readSyncConfig(NBTTagCompound compound) {
        //For Rent
    }

    @Nonnull
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new SkillInfo(compound);
    }
}