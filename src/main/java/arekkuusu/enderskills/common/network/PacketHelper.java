package arekkuusu.enderskills.common.network;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.skill.IConfigSync;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Map;
import java.util.Objects;

public final class PacketHelper {

    public static void sendConfigReload(EntityPlayerMP player) {
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        for (Map.Entry<ResourceLocation, Skill> entry : registry.getEntries()) {
            if (entry.getValue() instanceof IConfigSync) {
                PacketHelper.sendConfigPacket(player, (Skill & IConfigSync) entry.getValue());
            }
        }
        PacketHelper.sendGlobalConfigPacket(player);
    }

    private static void sendGlobalConfigPacket(EntityPlayerMP player) {
        NBTTagCompound compound = new NBTTagCompound();
        CommonConfig.writeSyncConfig(compound);
        PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_GLOBAL_CONFIG, compound), player);
    }

    private static <T extends Skill & IConfigSync> void sendConfigPacket(EntityPlayerMP player, T skill) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setResourceLocation(compound, "location", Objects.requireNonNull(skill.getRegistryName()));
        skill.writeSyncConfig(compound);
        PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_SKILLS_CONFIG, compound), player);
    }

    public static void sendSkillsSync(EntityPlayerMP player) {
        Capabilities.get(player).ifPresent(s -> {
            PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_SKILLS, s.serializeNBT()), player);
        });
    }

    public static void sendSkillSync(EntityPlayerMP player, Skill skill) {
        Capabilities.get(player).flatMap(s -> s.getOwned(skill)).ifPresent(info -> {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setResourceLocation(compound, "location", Objects.requireNonNull(skill.getRegistryName()));
            NBTHelper.setNBT(compound, "info", info.serializeNBT());
            PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_SKILL, compound), player);
        });
    }

    public static void sendWeightSync(EntityPlayerMP player) {
        Capabilities.weight(player).ifPresent(s -> {
            PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_WEIGHT, s.serializeNBT()), player);
        });
    }

    public static void sendWeightSetPacket(EntityPlayerMP player, Skill skill, int weight) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setResourceLocation(compound, "location", Objects.requireNonNull(skill.getRegistryName()));
        NBTHelper.setInteger(compound, "weight", weight);
        PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_WEIGHT, compound), player);
    }

    public static void sendWeightRemovePacket(EntityPlayerMP player, Skill skill) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setResourceLocation(compound, "location", Objects.requireNonNull(skill.getRegistryName()));
        PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_WEIGHT, compound), player);
    }

    @SideOnly(Side.CLIENT)
    public static void sendSkillUpgradeRequestPacket(EntityPlayerSP entity, Skill skill) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setResourceLocation(compound, "location", Objects.requireNonNull(skill.getRegistryName()));
        NBTHelper.setEntity(compound, entity, "owner");
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.SKILL_UPGRADE_REQUEST, compound));
    }

    public static void sendSkillUpgradeSync(EntityPlayerMP player) {
        PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SKILL_UPGRADE_SYNC, new NBTTagCompound()), player);
    }

    @SideOnly(Side.CLIENT)
    public static void sendSkillUseRequestPacket(EntityPlayerSP entity, Skill skill) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setResourceLocation(compound, "location", Objects.requireNonNull(skill.getRegistryName()));
        NBTHelper.setEntity(compound, entity, "owner");
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.SKILL_USE_REQUEST, compound));
    }

    public static void sendSkillUseResponsePacket(EntityLivingBase entity, SkillData data) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setInteger(compound, "uuid", entity.getEntityId());
        NBTHelper.setWorld(compound, "world", entity.getEntityWorld());
        NBTHelper.setNBT(compound, "data", data.serializeNBT());
        PacketHandler.NETWORK.sendToAllAround(new ServerToClientPacket(PacketHandler.SKILL_USE_RESPONSE, compound), fromEntity(entity, 69)); // Nice
    }

    public static void sendSkillHolderUseResponsePacket(EntityLivingBase entity, SkillHolder holder) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setInteger(compound, "uuid", entity.getEntityId());
        NBTHelper.setWorld(compound, "world", entity.getEntityWorld());
        NBTHelper.setNBT(compound, "holder", holder.serializeNBT());
        PacketHandler.NETWORK.sendToAllAround(new ServerToClientPacket(PacketHandler.SKILL_HOLDER_USE_RESPONSE, compound), fromEntity(entity, 69)); // Nice
    }

    public static void sendSkillRemoveResponsePacket(EntityLivingBase entity, Skill skill) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setResourceLocation(compound, "location", Objects.requireNonNull(skill.getRegistryName()));
        NBTHelper.setInteger(compound, "uuid", entity.getEntityId());
        NBTHelper.setWorld(compound, "world", entity.getEntityWorld());
        PacketHandler.NETWORK.sendToAllAround(new ServerToClientPacket(PacketHandler.SKILL_REMOVE_RESPONSE, compound), fromEntity(entity, 69)); // Nice
    }

    public static void sendSkillDataRemoveResponsePacket(EntityLivingBase entity, SkillData data) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setResourceLocation(compound, "location", Objects.requireNonNull(data.skill.getRegistryName()));
        NBTHelper.setInteger(compound, "uuid", entity.getEntityId());
        NBTHelper.setWorld(compound, "world", entity.getEntityWorld());
        NBTHelper.setNBT(compound, "data", data.serializeNBT());
        PacketHandler.NETWORK.sendToAllAround(new ServerToClientPacket(PacketHandler.SKILL_DATA_REMOVE_RESPONSE, compound), fromEntity(entity, 69)); // Nice
    }

    //TODO: REMOVE TOO HARDCODED!!
    public static void sendParticle(World world, Vec3d pos, Vec3d speed, float scale, int age, int rgb, ResourceLocation location) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setWorld(compound, "world", world);
        NBTHelper.setVector(compound, "pos", pos);
        NBTHelper.setVector(compound, "speed", speed);
        NBTHelper.setFloat(compound, "scale", scale);
        NBTHelper.setInteger(compound, "age", age);
        NBTHelper.setInteger(compound, "rgb", rgb);
        NBTHelper.setResourceLocation(compound, "location", location);
        PacketHandler.NETWORK.sendToAllAround(new ServerToClientPacket(PacketHandler.PARTICLE, compound), fromWorldPos(world, new BlockPos(pos), 69));
    }

    public static void sendParticleLightning(World world, Vector from, Vector to, int generations, float offset, int age, int rgb, boolean branch) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setWorld(compound, "world", world);
        NBTHelper.setVector(compound, "from", from.toVec3d());
        NBTHelper.setVector(compound, "to", to.toVec3d());
        NBTHelper.setInteger(compound, "generations", generations);
        NBTHelper.setFloat(compound, "offset", offset);
        NBTHelper.setInteger(compound, "age", age);
        NBTHelper.setInteger(compound, "rgb", rgb);
        NBTHelper.setBoolean(compound, "branch", branch);
        PacketHandler.NETWORK.sendToAllAround(new ServerToClientPacket(PacketHandler.PARTICLE_LIGHTNING, compound), fromWorldPos(world, new BlockPos(from.toVec3d()), 69));
    }

    public static void sendEnduranceSync(EntityPlayerMP player) {
        Capabilities.endurance(player).ifPresent(capability -> {
            NBTTagCompound compound = capability.serializeNBT();
            PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_ENDURANCE, compound), player);
        });
    }

    public static void sendAdvancementSync(EntityPlayerMP player) {
        Capabilities.advancement(player).ifPresent(c -> {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("xp_advancement", c.serializeNBT());
            compound.setInteger("lvl", player.experienceLevel);
            compound.setFloat("lvl_progress", player.experience);
            compound.setFloat("lvl_total", player.experienceTotal);
            PacketHandler.NETWORK.sendTo(new ServerToClientPacket(PacketHandler.SYNC_ADVANCEMENT, compound), player);
        });
    }

    @SideOnly(Side.CLIENT)
    public static void sendPinRequestPacket(EntityPlayerSP entity, int tabPin, int tabPagePin) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, entity, "owner");
        compound.setInteger("tabPin", tabPin);
        compound.setInteger("tabPagePin", tabPagePin);
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.GUI_PIN, compound));
    }

    @SideOnly(Side.CLIENT)
    public static void sendDashUseRequestPacket(EntityPlayerSP entity, Vec3d vector) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, entity, "owner");
        NBTHelper.setVector(compound, "vector", vector);
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.USE_DASH_REQUEST, compound));
    }

    @SideOnly(Side.CLIENT)
    public static void sendBlindedUseRequestPacket(EntityPlayerSP entity, SkillData data) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, entity, "owner");
        NBTHelper.setNBT(compound, "data", data.serializeNBT());
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.USE_BLINDED_REQUEST, compound));
    }

    @SideOnly(Side.CLIENT)
    public static void sendWarpUseRequestPacket(EntityPlayerSP entity, Vec3d vector) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, entity, "owner");
        NBTHelper.setVector(compound, "vector", vector);
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.USE_WARP_REQUEST, compound));
    }

    @SideOnly(Side.CLIENT)
    public static void sendResetSkillsRequestPacket(EntityPlayerSP entity) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, entity, "owner");
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.RESET_SKILLS_REQUEST, compound));
    }

    @SideOnly(Side.CLIENT)
    public static void sendStoreXPRequestPacket(EntityPlayerSP entity) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, entity, "owner");
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.STORE_XP_REQUEST, compound));
    }

    @SideOnly(Side.CLIENT)
    public static void sendTakeXPRequestPacket(EntityPlayerSP entity) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, entity, "owner");
        PacketHandler.NETWORK.sendToServer(new ClientToServerPacket(PacketHandler.TAKE_XP_REQUEST, compound));
    }

    public static void sendBleedSoundEffectResponsePacket(EntityLivingBase entity) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, entity, "target");
        PacketHandler.NETWORK.sendToAllAround(new ServerToClientPacket(PacketHandler.BLEED_SOUND_EFFECT_PLAY, compound), fromEntity(entity, 69)); // Nice
    }
    //TODO: REMOVE TOO HARDCODED!!

    public static TargetPoint fromWorldPos(World world, BlockPos pos, int range) {
        return new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range);
    }

    public static TargetPoint fromTileEntity(TileEntity te, int range) {
        return new TargetPoint(te.getWorld().provider.getDimension(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), range);
    }

    public static TargetPoint fromEntity(Entity entity, int range) {
        return new TargetPoint(entity.world.provider.getDimension(), entity.posX, entity.posY, entity.posZ, range);
    }
}
