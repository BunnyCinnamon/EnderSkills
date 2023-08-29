package arekkuusu.enderskills.common.skill.ability.mobility.wind;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.event.SkillActivateEvent;
import arekkuusu.enderskills.api.helper.MathUtil;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.DynamicModifier;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Hasten extends BaseAbility {

    //Vanilla Attribute
    public static final IAttribute HASTEN = new RangedAttribute(null, "enderskills.generic.cooldownReduction", 0F, 0F, 1F).setDescription("Cooldown Reduction").setShouldWatch(true);
    //Vanilla Attribute Modifier for Endurance attribute
    public static final DynamicModifier HASTEN_ATTRIBUTE = new DynamicModifier(
            "010af31b-310d-4ef9-91ed-6f84adc38610",
            LibMod.MOD_ID + ":" + LibNames.HASTEN,
            Hasten.HASTEN,
            Constants.AttributeModifierOperation.ADD);

    public Hasten() {
        super(LibNames.HASTEN, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (hasCooldown(skillInfo) || isClientWorld(owner)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;

        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
        InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
        int level = infoUpgradeable.getLevel();
        if (infoCooldown.canSetCooldown(owner)) {
            infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
        }

        //
        int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        double cdr = DSLDefaults.getReduction(this, level);
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setDouble(compound, "cdr", cdr);
        NBTHelper.setEntity(compound, owner, "owner");
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(time)
                .put(compound)
                .overrides(SkillData.Overrides.SAME)
                .create();
        apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        SoundHelper.playSound(entity.world, entity.getPosition(), ModSounds.HASTEN);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityAttributeUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.ticksExisted % 20 != 0) return; //Slowdown cowboy! yee-haw!
        Capabilities.get(entity).ifPresent(capability -> {
            if (capability.isOwned(ModAbilities.HASTEN)) {
                SkillData data = SkillHelper.getActive(entity, ModAbilities.HASTEN, entity.getUniqueID().toString()).orElse(null);
                if (data != null) {
                    HASTEN_ATTRIBUTE.apply(entity, data.nbt.getDouble("cdr"));
                } else {
                    HASTEN_ATTRIBUTE.remove(entity);
                }
            } else {
                HASTEN_ATTRIBUTE.remove(entity);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityCDRUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        double crd = entity.getEntityAttribute(Hasten.HASTEN).getAttributeValue();
        if (MathUtil.fuzzyEqual(0, crd)) return;
        NBTTagList list = entity.getEntityData().getTagList(Hasten.HASTEN.getName(), Constants.NBT.TAG_COMPOUND);
        List<ResourceLocation> locations = new ArrayList<>();
        list.iterator().forEachRemaining(nbt -> {
            locations.add(NBTHelper.getResourceLocation((NBTTagCompound) nbt, "location"));
        });
        Capabilities.get(entity).map(c -> c.getAllOwned().entrySet()).ifPresent(entries -> {
            for (Map.Entry<Skill, SkillInfo> entry : entries) {
                SkillInfo info = entry.getValue();
                Skill skill = entry.getKey();
                ResourceLocation location = skill.getRegistryName();
                if (!locations.contains(location) && info instanceof InfoCooldown && info instanceof InfoUpgradeable && location != null) {
                    if (((InfoCooldown) info).hasCooldown()) {
                        ((InfoCooldown) info).setCooldown(
                                Math.max(0, ((InfoCooldown) info).getCooldown() - (int) (DSLDefaults.getCooldown(ModAbilities.HASTEN, ((InfoUpgradeable) info).getLevel()) * crd))
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
            entity.getEntityData().setTag(Hasten.HASTEN.getName(), list);
        });
    }

    @SubscribeEvent
    public void onSkillUse(SkillActivateEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        NBTTagList list = event.getEntityLiving().getEntityData().getTagList(Hasten.HASTEN.getName(), Constants.NBT.TAG_COMPOUND);
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
            event.getEntityLiving().getEntityData().setTag(Hasten.HASTEN.getName(), list);
        }
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase)
            ((EntityLivingBase) event.getObject()).getAttributeMap().registerAttribute(HASTEN).setBaseValue(0F);
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_MOBILITY_CONFIG + LibNames.HASTEN;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
