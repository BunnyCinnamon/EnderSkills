package arekkuusu.enderskills.common.skill.ability.mobility.wind;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.configuration.parser.DSLParser;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.keybind.KeyBounds;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableSmash;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INDEFINITE;

public class Smash extends BaseAbility implements IScanEntities, IExpand, IFindEntity {

    public Smash() {
        super(LibNames.SMASH, new Properties() {
            @Override
            public boolean isKeyBound() {
                return false;
            }
        });
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (hasCooldown(skillInfo) || isClientWorld(owner)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;
        if ((owner instanceof EntityPlayer && ((EntityPlayer) owner).capabilities.isCreativeMode)) return;

        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
        InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
        int level = infoUpgradeable.getLevel();

        if (!owner.onGround) {
            if (infoCooldown.canSetCooldown(owner)) {
                infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
            }

            //
            double range = DSLDefaults.triggerRange(owner, this, level).getAmount();
            int time = DSLDefaults.triggerDuration(owner, this, level).getAmount();
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setDouble(compound, "range", range);
            NBTHelper.setInteger(compound, "time", time);
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(INDEFINITE)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            super.apply(owner, data);
            super.sync(owner, data);
            super.sync(owner);
        }
    }

    //* Entity *//
    @Override
    public AxisAlignedBB expand(Entity source, AxisAlignedBB bb, float amount) {
        return bb.grow(amount, 0, amount);
    }

    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        DamageSource damageSource = new DamageSource("smash");
        target.attackEntityFrom(damageSource, skillData.nbt.getFloat("damage"));
        ModEffects.STUNNED.set(target, skillData, skillData.nbt.getInteger("time"));
    }
    //* Entity *//

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        SoundHelper.playSound(entity.world, entity.getPosition(), ModSounds.SMASH_START);
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (!isClientWorld(owner) && owner.onGround) {
            super.unapply(owner, data);
            super.async(owner, data);
        }
        if (owner.motionY < 0D) {
            owner.motionY *= 1.05D;
        }
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        SoundHelper.playSound(entity.world, entity.getPosition(), ModSounds.SMASH_HIT);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFallDamage(LivingFallEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase owner = event.getEntityLiving();
        SkillHelper.getActiveFrom(owner, ModAbilities.SMASH).ifPresent(data -> {
            double range = NBTHelper.getDouble(data.nbt, "range");
            float damage = MathHelper.ceil(event.getDistance() - 3F);
            SkillData copy = data.copy();
            NBTHelper.setFloat(copy.nbt, "damage", (damage + (damage * -((float) owner.motionY))));
            EntityPlaceableSmash spawn = new EntityPlaceableSmash(owner.world, owner, copy, EntityPlaceableData.MIN_TIME);
            spawn.setPosition(owner.posX, owner.posY, owner.posZ);
            spawn.setRadius(range);
            owner.world.spawnEntity(spawn);
            super.unapply(owner, data);
            super.async(owner, data);
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Capabilities.get(player).flatMap(c -> c.getOwned(ModAbilities.SMASH)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            if (abilityInfo.hasCooldown()) return;
            if (KeyBounds.smash.isKeyDown() && !player.onGround) {
                Capabilities.endurance(player).ifPresent(endurance -> {
                    int level = Capabilities.get(player).flatMap(a -> a.getOwned(ModAbilities.SMASH)).map(a -> ((AbilityInfo) a).getLevel()).orElse(0);
                    int amount = ModAttributes.ENDURANCE.getEnduranceDrain(ModAbilities.SMASH, level);
                    if (endurance.getEndurance() - amount >= 0) {
                        PacketHelper.sendSkillUseRequestPacket(player, ModAbilities.SMASH);
                    }
                });
            }
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_MOBILITY_CONFIG + LibNames.SMASH;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
