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

    private final Properties properties;

    public Skill(Properties properties) {
        this.properties = properties;
    }

    public void use(EntityLivingBase entity, SkillInfo skillInfo) {
        //For Rent
    }

    public void begin(EntityLivingBase entity, SkillData data) {
        //For Rent
    }

    public void update(EntityLivingBase entity, SkillData data, int tick) {
        //For Rent
    }

    public void end(EntityLivingBase entity, SkillData data) {
        //For Rent
    }

    public void apply(EntityLivingBase entity, SkillData data) {
        Capabilities.get(entity).ifPresent(skills -> skills.activate(new SkillHolder(data))); //Add to entity Server Side
    }

    public void unapply(EntityLivingBase entity) {
        Capabilities.get(entity).ifPresent(skills -> skills.deactivate(this)); //Remove all from entity Server Side
    }

    public void unapply(EntityLivingBase entity, SkillData data) {
        Capabilities.get(entity).ifPresent(skills -> skills.deactivate(this, data)); //Remove from entity Server Side
    }

    public void sync(EntityLivingBase entity, SkillData data) {
        PacketHelper.sendSkillUseResponsePacket(entity, data); //Add to entity Client Side
    }

    public void async(EntityLivingBase entity) {
        PacketHelper.sendSkillRemoveResponsePacket(entity, this); //Remove from all entity Client Side
    }

    public void async(EntityLivingBase entity, SkillData data) {
        PacketHelper.sendSkillDataRemoveResponsePacket(entity, data); //Remove from entity Client Side
    }

    public void sync(EntityLivingBase entity) {
        PacketHelper.sendSkillSync((EntityPlayerMP) entity, this);
    }

    public Properties getProperties() {
        return properties;
    }

    @Nonnull
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new SkillInfo(compound);
    }

    public static class Properties {

        boolean isKeyBound;
        boolean hasStatusIcon;
        boolean hasTexture;

        public boolean isKeyBound() {
            return isKeyBound;
        }

        public Properties setKeyBound() {
            isKeyBound = true;
            return this;
        }

        public boolean hasStatusIcon() {
            return hasStatusIcon;
        }

        public Properties setHasStatusIcon() {
            this.hasStatusIcon = true;
            return this;
        }

        public boolean hasTexture() {
            return hasTexture;
        }

        public Properties setHasTexture() {
            this.hasTexture = true;
            return this;
        }
    }
}