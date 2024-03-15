package arekkuusu.enderskills.common.skill.ability.mobility.wind;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.configuration.*;
import arekkuusu.enderskills.api.configuration.parser.DSLParser;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INSTANT;

public class ExtraJump extends BaseAbility {

    public ExtraJump() {
        super(LibNames.EXTRA_JUMP, new Properties() {
            @Override
            public boolean isKeyBound() {
                return false;
            }
        });
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (isClientWorld(owner)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;
        SkillData data = SkillData.of(this)
                .with(INSTANT)
                .overrides(SkillData.Overrides.SAME)
                .create();
        super.apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        if (isClientWorld(owner)) { //YEEHAWW
            if (owner instanceof EntityPlayerSP && owner == Minecraft.getMinecraft().player) {
                ((EntityPlayerSP) owner).jump();
            }
        } else {
            if (owner instanceof EntityLiving) {
                ((EntityLiving) owner).getJumpHelper().setJumping();
            } else {
                owner.motionY += 0.3F;
            }
        }
        if (owner.motionY > 0 || owner instanceof EntityLiving) {
            owner.fallDistance = 0;
        }

        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.JUMP);
    }

    @SideOnly(Side.CLIENT)
    public static int jumps;
    @SideOnly(Side.CLIENT)
    public static boolean wasTapped;
    @SideOnly(Side.CLIENT)
    public static int ticksForNextTap;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player.capabilities.isCreativeMode) return;
        Capabilities.get(player).flatMap(c -> c.getOwned(ModAbilities.EXTRA_JUMP)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            if (abilityInfo.hasCooldown() || DSLDefaults.triggerRange(player, ModAbilities.EXTRA_JUMP, abilityInfo.getLevel()).getAmount() <= jumps) return;
            boolean tapped = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
            if (tapped && !wasTapped && ticksForNextTap == 0 && !player.onGround) {
                Capabilities.endurance(player).ifPresent(endurance -> {
                    int level = Capabilities.get(player).flatMap(a -> a.getOwned(ModAbilities.EXTRA_JUMP)).map(a -> ((AbilityInfo) a).getLevel()).orElse(0);
                    int amount = ModAttributes.ENDURANCE.getEnduranceDrain(ModAbilities.EXTRA_JUMP, level);
                    if (endurance.getEndurance() - amount >= 0) {
                        jumps++;
                        ticksForNextTap = 5;
                        PacketHelper.sendSkillUseRequestPacket(player, ModAbilities.EXTRA_JUMP);
                    }
                });
            }
            if (tapped && !wasTapped) wasTapped = true;
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyTapUpdate(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.onGround && jumps > 0) jumps = 0;
        boolean tapped = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
        if (wasTapped && !tapped) wasTapped = false;
        if (ticksForNextTap > 0) ticksForNextTap--;
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_MOBILITY_CONFIG + LibNames.EXTRA_JUMP;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }

    public static int getJumps(int level) {
        return DSLEvaluator.evaluateInt(ModAbilities.EXTRA_JUMP, "JUMPS", level, 1D);
    }
    /*Config Section*/
}
