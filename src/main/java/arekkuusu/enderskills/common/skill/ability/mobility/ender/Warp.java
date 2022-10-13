package arekkuusu.enderskills.common.skill.ability.mobility.ender;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.helper.RayTraceHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.ConfigDSL;
import arekkuusu.enderskills.client.gui.data.ISkillAdvancement;
import arekkuusu.enderskills.client.keybind.KeyBounds;
import arekkuusu.enderskills.client.render.skill.WarpRenderer;
import arekkuusu.enderskills.client.util.helper.TextHelper;
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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
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

import java.util.List;

public class Warp extends BaseAbility implements ISkillAdvancement {

    public Warp() {
        super(LibNames.WARP, new AbilityProperties() {
            @Override
            public boolean isKeyBound() {
                return false;
            }
        });
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void use(EntityLivingBase owner, SkillInfo skillInfo, Vec3d vector) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
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
        double distance = arekkuusu.enderskills.api.event.SkillRangeEvent.getRange(owner, this, getRange(abilityInfo));;
        if (owner.getDistance(targetVector.x, targetVector.y, targetVector.z) > distance + 1) return; //Cheater...

        BlockPos posFloor = new BlockPos(targetVector); //One Bwock fwom bwock tawgeted
        BlockPos posCeiling = posFloor.up(); //One spawce up fow youw wittle head uwu
        if (isSafePos(owner.world, posFloor) && isSafePos(owner.world, posCeiling) && isActionable(owner) && canActivate(owner)) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            NBTHelper.setVector(compound, "origin", owner.getPositionVector());
            NBTHelper.setVector(compound, "target", targetVector);
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            SkillData data = SkillData.of(this)
                    .by(owner)
                    .with(INSTANT)
                    .put(compound)
                    .overrides(SkillData.Overrides.EQUAL)
                    .create();
            apply(owner, data);
            sync(owner, data);
            sync(owner);
        }
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (isClientWorld(entity)) return;
        Vec3d vec = NBTHelper.getVector(data.nbt, "target");
        entity.setPositionAndUpdate(vec.x, vec.y, vec.z);
        if (entity.world instanceof WorldServer) {
            ((WorldServer) entity.world).playSound(null, entity.prevPosX, entity.prevPosY, entity.prevPosZ, ModSounds.WARP, SoundCategory.PLAYERS, 1.0F, (1.0F + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F) * 0.7F);
            ((WorldServer) entity.world).playSound(null, vec.x, vec.y, vec.z, ModSounds.WARP, SoundCategory.PLAYERS, 1.0F, (1.0F + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F) * 0.7F);
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
        Capabilities.get(player).flatMap(c -> c.getOwned(this)).ifPresent(skillInfo -> {
            AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
            if (abilityInfo.hasCooldown()) return;
            boolean tapped = KeyBounds.warp.isKeyDown();
            if (tapped && !wasTapped) {
                //Pressed same combination within 7 ticks
                if (ticksSinceLastTap <= 14) {
                    Capabilities.endurance(player).ifPresent(endurance -> {
                        int amount = ModAttributes.ENDURANCE.getEnduranceDrain(this);
                        if (endurance.getEndurance() - amount >= 0) {
                            double distance = arekkuusu.enderskills.api.event.SkillRangeEvent.getRange(player, this, getRange(abilityInfo));;
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

    public int getMaxLevel() {
        return this.config.max_level;
    }

    public double getRange(AbilityInfo info) {
        return this.config.get(this, "RANGE", info.getLevel());
    }

    public int getCooldown(AbilityInfo info) {
        return (int) this.config.get(this, "COOLDOWN", info.getLevel());
    }

    /*Advancement Section*/
    @Override
    @SideOnly(Side.CLIENT)
    public void addDescription(List<String> description) {
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(c -> {
            if (c.isOwned(this)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add(TextHelper.translate("desc.stats.shift"));
                } else {
                    c.getOwned(this).ifPresent(skillInfo -> {
                        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
                        description.clear();
                        description.add(TextHelper.translate("desc.stats.endurance", String.valueOf(ModAttributes.ENDURANCE.getEnduranceDrain(this))));
                        description.add("");
                        if (abilityInfo.getLevel() >= getMaxLevel()) {
                            description.add(TextHelper.translate("desc.stats.level_max", getMaxLevel()));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current", abilityInfo.getLevel(), abilityInfo.getLevel() + 1));
                        }
                        description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        if (abilityInfo.getLevel() < getMaxLevel()) {
                            if (!GuiScreen.isCtrlKeyDown()) {
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.ctrl"));
                            } else { //Copy info and set a higher level...
                                AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                                infoNew.setLevel(infoNew.getLevel() + 1);
                                description.add("");
                                description.add(TextHelper.translate("desc.stats.level_next", abilityInfo.getLevel(), infoNew.getLevel()));
                                description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                                description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Skill getParentSkill() {
        return ModAbilities.WARP;
    }

    @Override
    public double getExperience(int lvl) {
        return this.config.get(this, "XP", lvl);
    }
    /*Advancement Section*/

    /*Config Section*/
    public static final String CONFIG_FILE = LibNames.VOID_MOBILITY_CONFIG + LibNames.WARP;
    public ConfigDSL.Config config = new ConfigDSL.Config();

    @Override
    public void initSyncConfig() {
        Configuration.CONFIG_SYNC.dsl = Configuration.CONFIG.dsl;
        this.sigmaDic();
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        NBTHelper.setArray(compound, "config", Configuration.CONFIG.dsl);
    }

    @Override
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.CONFIG_SYNC.dsl = NBTHelper.getArray(compound, "config");
    }

    @Override
    public void sigmaDic() {
        this.config = ConfigDSL.parse(Configuration.CONFIG_SYNC.dsl);
    }

    @Config(modid = LibMod.MOD_ID, name = CONFIG_FILE)
    public static class Configuration {

        @Config.Ignore
        public static final Configuration.Values CONFIG_SYNC = new Configuration.Values();
        public static final Configuration.Values CONFIG = new Configuration.Values();

        public static class Values {

            public String[] dsl = {
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀",
                    "⠀min_level: 0",
                    "⠀max_level: 50",
                    "⠀",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀COOLDOWN (",
                    "⠀    curve: flat",
                    "⠀    start: 14s",
                    "⠀    end:   4s",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 8s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 6s",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀RANGE (",
                    "⠀    curve: flat",
                    "⠀    start: 1.75b",
                    "⠀    end:   3b",
                    "⠀",
                    "⠀    {0 to 25} [",
                    "⠀        curve: ramp -50% 50%",
                    "⠀        start: {start}",
                    "⠀        end: 2b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {25 to 49} [",
                    "⠀        curve: ramp 50% 50%",
                    "⠀        start: {0 to 25}",
                    "⠀        end: 2.5b",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: none",
                    "⠀        value: {end}",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
                    "⠀XP (",
                    "⠀    curve: flat",
                    "⠀    start: 170",
                    "⠀    end:   infinite",
                    "⠀",
                    "⠀    {0} [",
                    "⠀        curve: none",
                    "⠀        value: {start}",
                    "⠀    ]",
                    "⠀",
                    "⠀    {1 to 49} [",
                    "⠀        curve: multiply 4",
                    "⠀    ]",
                    "⠀",
                    "⠀    {50} [",
                    "⠀        curve: f(x, y) -> 4 * x + 4 * x * 0.1",
                    "⠀    ]",
                    "⠀)",
                    "⠀#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~",
            };
        }
    }
    /*Config Section*/
}
