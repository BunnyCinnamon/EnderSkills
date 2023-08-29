package arekkuusu.enderskills.common.skill.ability.mobility.ender;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
import arekkuusu.enderskills.api.capability.data.InfoUpgradeable;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.DSL;
import arekkuusu.enderskills.api.configuration.DSLDefaults;
import arekkuusu.enderskills.api.configuration.DSLFactory;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.client.sounds.HoverSound;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Hover extends BaseAbility {

    public Hover() {
        super(LibNames.HOVER, new Properties() {
            @Override
            public boolean isKeyBound() {
                return false;
            }
        });
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (hasCooldown(skillInfo)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;

        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
        int level = infoUpgradeable.getLevel();

        int maxHover = DSLDefaults.triggerDuration(owner, this, level).getAmount();
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setEntity(compound, owner, "owner");
        SkillData data = SkillData.of(this)
                .by(owner)
                .with(maxHover)
                .put(compound)
                .overrides(SkillData.Overrides.SAME)
                .create();
        super.apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        if (isClientWorld(owner)) {
            makeSound(owner);
        }
    }

    @SideOnly(Side.CLIENT)
    public void makeSound(EntityLivingBase entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new HoverSound(entity));
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        owner.fallDistance = 0;
        if (isClientWorld(owner) && !(owner instanceof EntityPlayer)) return;
        if (owner.motionY < 0) owner.motionY *= 0.4D;
    }

    @SideOnly(Side.CLIENT)
    public static int ticksSinceLastTap;
    @SideOnly(Side.CLIENT)
    public static int hoverTime;
    @SideOnly(Side.CLIENT)
    public static boolean hovering;
    @SideOnly(Side.CLIENT)
    public static boolean canHover;

    static { //... really?
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            canHover = true;
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player.capabilities.isCreativeMode) return;
        Capabilities.get(player).flatMap(c -> c.getOwned(ModAbilities.HOVER)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            int maxHover = DSLDefaults.triggerDuration(player, ModAbilities.HOVER, abilityInfo.getLevel()).getAmount();
            if (maxHover <= 0) return;

            boolean pressed = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
            boolean tapped = Minecraft.getMinecraft().gameSettings.keyBindJump.isPressed();
            if (tapped && !player.onGround) {
                if (ticksSinceLastTap <= 7 && canHover) {
                    hovering = true;
                    canHover = false;
                } else {
                    ticksSinceLastTap = 0;
                }
            }
            if (!pressed && hovering) {
                PacketHelper.sendSkillRemoveResponsePacket(player, ModAbilities.HOVER);
                hovering = false;
                hoverTime = 0;
            }
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyTapUpdate(TickEvent.ClientTickEvent event) {
        if (ticksSinceLastTap < 10) ticksSinceLastTap++;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null && player.onGround && !canHover) canHover = true;
        if (player == null || !hovering) return;
        if (player.onGround) {
            PacketHelper.sendSkillRemoveResponsePacket(player, this);
            hovering = false;
            hoverTime = 0;
            return;
        }
        Capabilities.get(player).flatMap(c -> c.getOwned(this)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            int maxHover = DSLDefaults.triggerDuration(player, this, abilityInfo.getLevel()).getAmount();
            if (maxHover <= 0) return;
            if (hoverTime > maxHover) {
                PacketHelper.sendSkillRemoveResponsePacket(player, this);
                hovering = false;
                hoverTime = 0;
            } else {
                Capabilities.endurance(player).ifPresent(endurance -> {
                    int level = Capabilities.get(player).flatMap(a -> a.getOwned(this)).map(a -> ((AbilityInfo) a).getLevel()).orElse(0);
                    int amount = ModAttributes.ENDURANCE.getEnduranceDrain(this, level);
                    if (endurance.getEndurance() - amount >= 0) {
                        if (hovering) {
                            if (hoverTime++ == 0) {
                                PacketHelper.sendSkillUseRequestPacket(player, this);
                            }
                        }
                    } else {
                        PacketHelper.sendSkillRemoveResponsePacket(player, this);
                        hovering = false;
                        hoverTime = 0;
                    }
                });
            }
        });
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_MOBILITY_CONFIG + LibNames.HOVER;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
