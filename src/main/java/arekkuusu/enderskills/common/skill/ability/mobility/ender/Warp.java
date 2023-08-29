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
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.helper.SoundHelper;
import arekkuusu.enderskills.client.keybind.KeyBounds;
import arekkuusu.enderskills.client.render.skill.WarpRenderer;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static arekkuusu.enderskills.common.skill.effect.BaseEffect.INSTANT;

public class Warp extends BaseAbility {

    public Warp() {
        super(LibNames.WARP, new Properties() {
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

        Vec3d eyesVector = owner.getPositionEyes(1F);
        Vec3d targetVector = eyesVector.add(vector);
        RayTraceResult traceBlocks = RayTraceHelper.rayTraceBlocks(owner.world, eyesVector, targetVector);
        if (traceBlocks != null) {
            targetVector = traceBlocks.hitVec;
            targetVector = new Vec3d(
                    MathHelper.floor(targetVector.x) + 0.5D,
                    MathHelper.floor(targetVector.y) + 0.1D,
                    MathHelper.floor(targetVector.z) + 0.5D
            );
        }
        double distance = DSLDefaults.triggerRange(owner, this, level).getAmount();
        if (owner.getDistance(targetVector.x, targetVector.y, targetVector.z) > distance + 1) return; //Cheater...

        BlockPos posFloor = new BlockPos(targetVector); //One Bwock fwom bwock tawgeted
        BlockPos posCeiling = posFloor.up(); //One spawce up fow youw wittle head uwu
        if (isSafePos(owner.world, posFloor) && isSafePos(owner.world, posCeiling)) {
            if (infoCooldown.canSetCooldown(owner)) {
                infoCooldown.setCooldown(DSLDefaults.getCooldown(this, level));
            }

            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setVector(compound, "origin", owner.getPositionVector());
            NBTHelper.setVector(compound, "target", targetVector);
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(INSTANT)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            super.apply(owner, data);
            super.sync(owner, data);
            super.sync(owner);
            owner.hurtResistantTime = 10; //Immune after skill use
        }
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        Vec3d vec = NBTHelper.getVector(data.nbt, "target");
        entity.setPositionAndUpdate(vec.x, vec.y, vec.z);
        if (entity.world instanceof WorldServer) {
            SoundHelper.playSound(entity.world, entity.getPosition(), ModSounds.WARP);
            SoundHelper.playSound(entity.world, new BlockPos(vec.x, vec.y, vec.z), ModSounds.WARP);
        }
    }

    @Override
    public void end(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) {
            spawnRift(entity, data);
        }
    }

    @SideOnly(Side.CLIENT)
    public void spawnRift(EntityLivingBase entity, SkillData data) {
        Vec3d offset = new Vec3d(0, entity.height / 2D, 0);
        WarpRenderer.WarpRift riftOrigin = new WarpRenderer.WarpRift(entity, NBTHelper.getVector(data.nbt, "origin"), false);
        WarpRenderer.WarpRift riftTarget = new WarpRenderer.WarpRift(entity, NBTHelper.getVector(data.nbt, "target").add(offset), true);
        WarpRenderer.WARP_RIFTS.add(riftOrigin);
        WarpRenderer.WARP_RIFTS.add(riftTarget);
    }

    public boolean isSafePos(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getCollisionBoundingBox(world, pos) == Block.NULL_AABB;
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
        if (!KeyBounds.warp.isKeyDown()) return;
        Capabilities.get(player).flatMap(c -> c.getOwned(ModAbilities.WARP)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            if (abilityInfo.hasCooldown()) return;
            boolean tapped = KeyBounds.warp.isKeyDown();
            if (tapped && !wasTapped) {
                //Pressed same combination within 7 ticks
                if (ticksSinceLastTap <= 14) {
                    Capabilities.endurance(player).ifPresent(endurance -> {
                        int level = Capabilities.get(player).flatMap(a -> a.getOwned(ModAbilities.WARP)).map(a -> ((AbilityInfo) a).getLevel()).orElse(0);
                        int amount = ModAttributes.ENDURANCE.getEnduranceDrain(ModAbilities.WARP, level);
                        if (endurance.getEndurance() - amount >= 0) {
                            double distance = DSLDefaults.triggerRange(player, ModAbilities.WARP, abilityInfo.getLevel()).getAmount();
                            Vec3d lookVec = player.getLookVec();
                            double x = lookVec.x * distance;
                            double y = lookVec.y * distance;
                            double z = lookVec.z * distance;
                            Vec3d moveVec = new Vec3d(0, 0, 0);
                            if (Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(x, y, z));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(-z, 0, x));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(z, 0, -x));
                            }
                            if (Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                                moveVec = moveVec.add(new Vec3d(-x, -y, -z));
                            }
                            PacketHelper.sendWarpUseRequestPacket(player, moveVec);
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
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyTapUpdate(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()) keyWasPressed = false;
        if (ticksSinceLastTap < 17) ticksSinceLastTap++;
        boolean tapped = Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown();
        if (wasTapped && !tapped) wasTapped = false;
    }

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_MOBILITY_CONFIG + LibNames.WARP;

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        public static DSL CONFIG = DSLFactory.create(CONFIG_FILE);
    }
    /*Config Section*/
}
