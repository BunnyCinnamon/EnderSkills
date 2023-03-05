package arekkuusu.enderskills.common.network;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.event.SkillUpgradeSyncEvent;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.XPHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.sounds.BleedSound;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.BaseSkill;
import arekkuusu.enderskills.common.skill.IConfigSync;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.Optional;

public final class PacketHandler {

    public static final List<IPacketHandler> HANDLERS = Lists.newArrayList();

    public static final IPacketHandler SYNC_GLOBAL_CONFIG = (((compound, context) -> {
        CommonConfig.readSyncConfig(compound);
    }));

    public static final IPacketHandler SYNC_SKILLS_CONFIG = (((compound, context) -> {
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        Skill skill = registry.getValue(NBTHelper.getResourceLocation(compound, "location"));
        assert skill != null;
        ((Skill & IConfigSync) skill).readSyncConfig(compound);
    }));

    public static final IPacketHandler SYNC_SKILLS = (((compound, context) -> {
        EntityPlayer player = EnderSkills.getProxy().getPlayer();
        Capabilities.get(player).ifPresent(s -> {
            s.deserializeNBT(compound);
        });
    }));

    public static final IPacketHandler SYNC_SKILLS_TRACKING = (((compound, context) -> {
        EntityLivingBase entity = NBTHelper.getEntity(EntityLivingBase.class, compound, "entity");
        Capabilities.get(entity).ifPresent(s -> {
            s.deserializeNBT(compound.getCompoundTag("skills"));
        });
    }));

    public static final IPacketHandler SYNC_SKILL = (((compound, context) -> {
        EntityPlayer player = EnderSkills.getProxy().getPlayer();
        Capabilities.get(player).ifPresent(skills -> {
            IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
            Skill skill = registry.getValue(NBTHelper.getResourceLocation(compound, "location"));
            assert skill != null;
            skills.getOwned(skill).ifPresent(info -> {
                info.deserializeNBT(NBTHelper.getNBTTag(compound, "info"));
            });
        });
    }));

    public static final IPacketHandler SYNC_WEIGHT = (((compound, context) -> {
        EntityPlayer player = EnderSkills.getProxy().getPlayer();
        Capabilities.weight(player).ifPresent(s -> {
            s.deserializeNBT(compound);
        });
    }));

    public static final IPacketHandler CHANGE_WEIGHT = (((compound, context) -> {
        EntityPlayer player = EnderSkills.getProxy().getPlayer();
        Capabilities.weight(player).ifPresent(capability -> {
            IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
            String name = NBTHelper.getString(compound, "name");
            Skill skill = registry.getValue(NBTHelper.getResourceLocation(compound, "location"));
            assert skill != null;
            if (NBTHelper.hasTag(compound, "weight")) {
                capability.putWeight(name, skill, compound.getInteger("weight"));
            } else {
                capability.removeWeight(name, skill);
            }
        });
    }));

    public static final IPacketHandler SKILL_USE_REQUEST = (((compound, context) -> {
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        Skill skill = registry.getValue(NBTHelper.getResourceLocation(compound, "location"));
        assert skill != null;
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Capabilities.get(e).flatMap(skills -> skills.getOwned(skill)).ifPresent(info -> skill.use(e, info));
        });
    }));

    public static final IPacketHandler SKILL_USE_RESPONSE = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getWorld(compound, "world").getEntityByID(compound.getInteger("uuid")))
                .flatMap(Capabilities::get).ifPresent(capability -> {
            Optional.of(NBTHelper.getNBTTag(compound, "data")).ifPresent(nbt -> {
                capability.activate(new SkillHolder(new SkillData(nbt)));
            });
        });
    }));

    public static final IPacketHandler SKILL_HOLDER_USE_RESPONSE = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getWorld(compound, "world").getEntityByID(compound.getInteger("uuid")))
                .flatMap(Capabilities::get).ifPresent(capability -> {
            Optional.of(NBTHelper.getNBTTag(compound, "holder")).ifPresent(nbt -> {
                capability.activate(new SkillHolder(nbt));
            });
        });
    }));

    public static final IPacketHandler SKILL_REMOVE_RESPONSE = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getWorld(compound, "world").getEntityByID(compound.getInteger("uuid")))
                .flatMap(Capabilities::get).ifPresent(capability -> {
            IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
            Skill skill = registry.getValue(NBTHelper.getResourceLocation(compound, "location"));
            assert skill != null;
            capability.deactivate(skill);
        });
    }));

    public static final IPacketHandler SKILL_DATA_REMOVE_RESPONSE = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getWorld(compound, "world").getEntityByID(compound.getInteger("uuid")))
                .flatMap(Capabilities::get).ifPresent(capability -> {
            IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
            Skill skill = registry.getValue(NBTHelper.getResourceLocation(compound, "location"));
            assert skill != null;
            SkillData data = new SkillData(NBTHelper.getNBTTag(compound, "data"));
            capability.deactivate(skill, data);
        });
    }));

    public static final IPacketHandler SKILL_UPGRADE_REQUEST = (((compound, context) -> {
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        Skill skill = registry.getValue(NBTHelper.getResourceLocation(compound, "location"));
        assert skill != null;
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Capabilities.get(e).ifPresent(c -> {
                if (c.isOwned(skill)) {
                    c.getOwned(skill).ifPresent(info -> {
                        if (skill.getProperties() instanceof BaseSkill.BaseProperties && info instanceof IInfoUpgradeable) {
                            int lvl = ((IInfoUpgradeable) info).getLevel() + 1;
                            if (lvl <= ((BaseSkill.BaseProperties) skill.getProperties()).getMaxLevel()) {
                                if (skill instanceof ISkillAdvancement) {
                                    ISkillAdvancement advancement = (ISkillAdvancement) skill;
                                    if (advancement.canUpgrade(e)) {
                                        advancement.onUpgrade(e);
                                        ((IInfoUpgradeable) info).setLevel(lvl);
                                        PacketHelper.sendSkillsSync((EntityPlayerMP) e);
                                        PacketHelper.sendAdvancementSync((EntityPlayerMP) e);
                                        PacketHelper.sendSkillUpgradeSync((EntityPlayerMP) e);
                                    }
                                } else {
                                    ((IInfoUpgradeable) info).setLevel(lvl);
                                    PacketHelper.sendSkillSync((EntityPlayerMP) e, skill);
                                    PacketHelper.sendSkillUpgradeSync((EntityPlayerMP) e);
                                }
                            }
                        }
                    });
                } else {
                    if (skill instanceof ISkillAdvancement) {
                        ISkillAdvancement advancement = (ISkillAdvancement) skill;
                        if (advancement.canUpgrade(e)) {
                            advancement.onUpgrade(e);
                            c.addOwned(skill);
                            PacketHelper.sendSkillsSync((EntityPlayerMP) e);
                            PacketHelper.sendAdvancementSync((EntityPlayerMP) e);
                            PacketHelper.sendSkillUpgradeSync((EntityPlayerMP) e);
                        }
                    } else {
                        c.addOwned(skill);
                        PacketHelper.sendSkillsSync((EntityPlayerMP) e);
                        PacketHelper.sendSkillUpgradeSync((EntityPlayerMP) e);
                    }
                }
            });
        });
    }));

    public static final IPacketHandler SKILL_UPGRADE_SYNC = (((compound, context) -> {
        MinecraftForge.EVENT_BUS.post(new SkillUpgradeSyncEvent());
    }));

    //TODO: REMOVE TOO HARDCODED!!
    public static final IPacketHandler PARTICLE = (((compound, context) -> {
        World world = NBTHelper.getWorld(compound, "world");
        Vec3d pos = NBTHelper.getVector(compound, "pos");
        Vec3d speed = NBTHelper.getVector(compound, "speed");
        float scale = NBTHelper.getFloat(compound, "scale");
        int age = NBTHelper.getInteger(compound, "age");
        int rgb = NBTHelper.getInteger(compound, "rgb");
        ResourceLocation location = NBTHelper.getResourceLocation(compound, "location");
        EnderSkills.getProxy().spawnParticle(world, pos, speed, scale, age, rgb, location);
    }));

    public static final IPacketHandler PARTICLE_LIGHTNING = (((compound, context) -> {
        World world = NBTHelper.getWorld(compound, "world");
        Vec3d from = NBTHelper.getVector(compound, "from");
        Vec3d to = NBTHelper.getVector(compound, "to");
        int generations = NBTHelper.getInteger(compound, "generations");
        float offset = NBTHelper.getFloat(compound, "offset");
        int age = NBTHelper.getInteger(compound, "age");
        int rgb = NBTHelper.getInteger(compound, "rgb");
        boolean branch = NBTHelper.getBoolean(compound, "branch");
        EnderSkills.getProxy().spawnLightning(world, new Vector(from), new Vector(to), generations, offset, age, rgb, branch);
    }));

    public static final IPacketHandler SYNC_ENDURANCE = (((compound, context) -> {
        EntityPlayer player = EnderSkills.getProxy().getPlayer();
        Capabilities.endurance(player).ifPresent(capability -> {
            capability.deserializeNBT(compound);
        });
    }));

    public static final IPacketHandler SYNC_ADVANCEMENT = (((compound, context) -> {
        EntityPlayer player = EnderSkills.getProxy().getPlayer();
        Capabilities.advancement(player).ifPresent(c -> {
            player.experienceLevel = compound.getInteger("lvl");
            player.experience = compound.getInteger("lvl_progress");
            player.experienceTotal = compound.getInteger("lvl_total");
            c.deserializeNBT(compound.getCompoundTag("xp_advancement"));
        });
    }));

    public static final IPacketHandler GUI_PIN = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Capabilities.advancement(e).ifPresent(c -> {
                c.tabPin = compound.getInteger("tabPin");
                c.tabPagePin = compound.getInteger("tabPagePin");
                if (e instanceof EntityPlayerMP) {
                    PacketHelper.sendAdvancementSync((EntityPlayerMP) e);
                }
            });
        });
    }));

    public static final IPacketHandler USE_DASH_REQUEST = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Capabilities.get(e).flatMap(skills -> skills.getOwned(ModAbilities.DASH)).ifPresent(info -> ModAbilities.DASH.use(e, info, NBTHelper.getVector(compound, "vector")));
        });
    }));

    public static final IPacketHandler USE_BLINDED_REQUEST = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Optional.of(NBTHelper.getNBTTag(compound, "data")).ifPresent(nbt -> {
                SkillData data = new SkillData(nbt);
                ModEffects.BLINDED.set(e, data);
            });
        });
    }));

    public static final IPacketHandler USE_WARP_REQUEST = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Capabilities.get(e).flatMap(skills -> skills.getOwned(ModAbilities.WARP)).ifPresent(info -> ModAbilities.WARP.use(e, info, NBTHelper.getVector(compound, "vector")));
        });
    }));

    public static final IPacketHandler RESET_SKILLS_REQUEST = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Capabilities.get(e).ifPresent(SkilledEntityCapability::clearOwned);
            Capabilities.advancement(e).ifPresent(c -> {
                c.skillUnlockOrder = new Skill[0];
                c.resetCount++;
                c.addExperienceToTotal((int) (c.experienceSpent * CommonConfig.getSyncValues().advancement.xp.retryXPReturn));
                c.experienceSpent = 0;
            });
            if (e instanceof EntityPlayer) {
                PacketHelper.sendAdvancementSync((EntityPlayerMP) e);
                PacketHelper.sendSkillsSync((EntityPlayerMP) e);
            }
        });
    }));

    public static final IPacketHandler STORE_XP_REQUEST = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Capabilities.advancement(e).ifPresent(c -> {
                if (e instanceof EntityPlayer) {
                    int xp = (int) (XPHelper.getXPTotal((EntityPlayer) e) * CommonConfig.getSyncValues().advancement.xp.xpStoreTariff);
                    c.addExperienceToTotal(xp);
                    ((EntityPlayer) e).experienceTotal = 0;
                    ((EntityPlayer) e).experienceLevel = 0;
                    ((EntityPlayer) e).experience = 0;
                    PacketHelper.sendAdvancementSync((EntityPlayerMP) e);
                }
            });
        });
    }));

    public static final IPacketHandler TAKE_XP_REQUEST = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "owner")).ifPresent(e -> {
            Capabilities.advancement(e).ifPresent(c -> {
                if (e instanceof EntityPlayer) {
                    int xpStored = (int) (XPHelper.getXPTotal(c.experienceLevel, c.experienceProgress) * CommonConfig.getSyncValues().advancement.xp.xpTakeTariff);
                    int xpTotal = xpStored + XPHelper.getXPTotal((EntityPlayer) e);
                    ((EntityPlayer) e).experienceTotal = xpTotal;
                    ((EntityPlayer) e).experienceLevel = XPHelper.getLevelFromXPValue(xpTotal);
                    ((EntityPlayer) e).experience = XPHelper.getLevelProgressFromXPValue(xpTotal);
                    c.experienceLevel = 0;
                    c.experienceProgress = 0;
                    PacketHelper.sendAdvancementSync((EntityPlayerMP) e);
                }
            });
        });
    }));

    public static final IPacketHandler BLEED_SOUND_EFFECT_PLAY = (((compound, context) -> {
        Optional.ofNullable(NBTHelper.getEntity(EntityLivingBase.class, compound, "target")).ifPresent(e -> {
            PacketHandler.bleedSoundEffectPlay(e);
        });
    }));

    @SideOnly(Side.CLIENT)
    public static void bleedSoundEffectPlay(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new BleedSound(entity));
    }
    //TODO: REMOVE TOO HARDCODED!!

    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(LibMod.MOD_ID);
    private static int id = 0;

    public static void init() {
        register(ServerToClientPacket.Handler.class, ServerToClientPacket.class, Side.CLIENT);
        register(ClientToServerPacket.Handler.class, ClientToServerPacket.class, Side.SERVER);
        HANDLERS.add(SYNC_GLOBAL_CONFIG);
        HANDLERS.add(SYNC_SKILLS_CONFIG);
        HANDLERS.add(SYNC_SKILLS);
        HANDLERS.add(SYNC_SKILLS_TRACKING);
        HANDLERS.add(SYNC_SKILL);
        HANDLERS.add(SYNC_WEIGHT);
        HANDLERS.add(CHANGE_WEIGHT);
        HANDLERS.add(SKILL_USE_REQUEST);
        HANDLERS.add(SKILL_HOLDER_USE_RESPONSE);
        HANDLERS.add(SKILL_USE_RESPONSE);
        HANDLERS.add(SKILL_REMOVE_RESPONSE);
        HANDLERS.add(SKILL_DATA_REMOVE_RESPONSE);
        HANDLERS.add(SKILL_UPGRADE_REQUEST);
        HANDLERS.add(SKILL_UPGRADE_SYNC);
        //TODO: REMOVE TOO HARDCODED!!
        HANDLERS.add(PARTICLE);
        HANDLERS.add(PARTICLE_LIGHTNING);
        HANDLERS.add(SYNC_ENDURANCE);
        HANDLERS.add(SYNC_ADVANCEMENT);
        HANDLERS.add(GUI_PIN);
        HANDLERS.add(RESET_SKILLS_REQUEST);
        HANDLERS.add(USE_DASH_REQUEST);
        HANDLERS.add(USE_BLINDED_REQUEST);
        HANDLERS.add(USE_WARP_REQUEST);
        HANDLERS.add(STORE_XP_REQUEST);
        HANDLERS.add(TAKE_XP_REQUEST);
        HANDLERS.add(BLEED_SOUND_EFFECT_PLAY);
        //TODO: REMOVE TOO HARDCODED!!
    }

    private static <H extends IMessageHandler<M, IMessage>, M extends IMessage> void register(Class<H> handler, Class<M> message, Side side) {
        NETWORK.registerMessage(handler, message, id++, side);
    }
}
