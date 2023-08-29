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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

public class Dash extends BaseAbility {

    public Dash() {
        super(LibNames.DASH, new Properties() {
            @Override
            public boolean isKeyBound() {
                return false;
            }
        });
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void use(EntityLivingBase owner, SkillInfo skillInfo, Vec3d vector) {
        if (hasCooldown(skillInfo) || isClientWorld(owner)) return;
        if (isNotActionable(owner) || canNotActivate(owner)) return;

        InfoUpgradeable infoUpgradeable = (InfoUpgradeable) skillInfo;
        InfoCooldown infoCooldown = (InfoCooldown) skillInfo;
        int level = infoUpgradeable.getLevel();
        if (infoCooldown.canSetCooldown(owner)) {
            infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
        }

        //
        double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
        NBTTagCompound compound = new NBTTagCompound();
        NBTHelper.setVector(compound, "vector", vector);
        NBTHelper.setDouble(compound, "distance", distance);
        NBTHelper.setEntity(compound, owner, "owner");
        SkillData data = SkillData.of(this)
                .with(10)
                .put(compound)
                .overrides(SkillData.Overrides.SAME)
                .create();
        super.apply(owner, data);
        super.sync(owner, data);
        super.sync(owner);
    }

    @Override
    public void begin(EntityLivingBase owner, SkillData data) {
        SoundHelper.playSound(owner.world, owner.getPosition(), ModSounds.DASH);
        if (isClientWorld(owner) && !(owner instanceof EntityPlayer)) return;
        if (!owner.onGround) {
            Vec3d vector = NBTHelper.getVector(data.nbt, "vector");
            double distance = NBTHelper.getDouble(data.nbt, "distance");
            Vec3d from = owner.getPositionVector();
            Vec3d to = from.addVector(
                    vector.x * distance,
                    0,
                    vector.z * distance
            );
            moveEntity(to, from, owner);
        }
    }

    @Override
    public void update(EntityLivingBase owner, SkillData data, int tick) {
        if (isClientWorld(owner) && !(owner instanceof EntityPlayer)) return;
        if (tick < 10) {
            Vec3d vector = NBTHelper.getVector(data.nbt, "vector");
            double distance = NBTHelper.getDouble(data.nbt, "distance");
            Vec3d from = owner.getPositionVector();
            Vec3d to = from.addVector(
                    vector.x * distance,
                    0,
                    vector.z * distance
            );
            if (owner.onGround) {
                moveEntity(to, from, owner);
            }
            if (owner.collidedHorizontally) {
                owner.motionY = 0;
            }
        }
        if (owner.isSneaking()) {
            super.unapply(owner);
            super.async(owner);
        }
    }

    public void moveEntity(Vec3d pullerPos, Vec3d pushedPos, Entity pulled) {
        Vec3d distance = pullerPos.subtract(pushedPos);
        Vec3d motion = new Vec3d(distance.x / 10D, distance.y / 10D, distance.z / 10D).scale(-1);
        pulled.motionX += -motion.x;
        pulled.motionY += -motion.y;
        pulled.motionZ += -motion.z;
    }

    @SideOnly(Side.CLIENT)
    public static int ticksSinceLastTap;
    @SideOnly(Side.CLIENT)
    public static boolean keyWasPressed;
    @SideOnly(Side.CLIENT)
    public static boolean wasTapped;
    @SideOnly(Side.CLIENT)
    public static int ticksForNextTap;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (!KeyBounds.dash.isKeyDown()) return;
        Capabilities.get(player).flatMap(c -> c.getOwned(ModAbilities.DASH)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            if (abilityInfo.hasCooldown()) return;
            boolean tapped = KeyBounds.dash.isKeyDown();
            if (tapped && !wasTapped) {
                //Pressed same combination within 7 ticks
                if (ticksSinceLastTap <= 14 && !keyWasPressed) {
                    Capabilities.endurance(player).ifPresent(endurance -> {
                        int level = Capabilities.get(player).flatMap(a -> a.getOwned(ModAbilities.DASH)).map(a -> ((AbilityInfo) a).getLevel()).orElse(0);
                        int amount = ModAttributes.ENDURANCE.getEnduranceDrain(ModAbilities.DASH, level);
                        if (endurance.getEndurance() - amount >= 0) {
                            Vec3d lookVec = getVectorForRotation(player);
                            double x = lookVec.x;
                            double z = lookVec.z;
                            Vec3d moveVec = new Vec3d(0, 0, 0);
                            if (Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(x, 0, z));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(-z, 0, x));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(z, 0, -x));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(-x, 0, -z));
                            }
                            PacketHelper.sendDashUseRequestPacket(player, moveVec);
                        }
                    });
                    keyWasPressed = true;
                } else {
                    ticksSinceLastTap = 0;
                }
            }
            if (tapped && !wasTapped) wasTapped = true;
        });
    }

    @SideOnly(Side.CLIENT)
    protected final Vec3d getVectorForRotation(EntityLivingBase entity) {
        float f = MathHelper.cos(-entity.rotationYaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-entity.rotationYaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-0 * 0.017453292F);
        float f3 = MathHelper.sin(-0 * 0.017453292F);
        return new Vec3d(f1 * f2, f3, f * f2);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyTapUpdate(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()) keyWasPressed = false;
        if (ticksSinceLastTap < 17) ticksSinceLastTap++;
        boolean tapped = Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown();
        if (wasTapped && !tapped) wasTapped = false;
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.WIND_MOBILITY_CONFIG + LibNames.DASH;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
