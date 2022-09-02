package arekkuusu.enderskills.client.render;

import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.render.entity.*;
import arekkuusu.enderskills.client.render.skill.*;
import arekkuusu.enderskills.client.render.skill.status.*;
import arekkuusu.enderskills.client.render.tile.TileAltarRenderer;
import arekkuusu.enderskills.common.block.tile.TileAltar;
import arekkuusu.enderskills.common.entity.*;
import arekkuusu.enderskills.common.entity.placeable.*;
import arekkuusu.enderskills.common.entity.throwable.EntityThrowableData;
import arekkuusu.enderskills.common.skill.ability.defense.earth.Shockwave;
import arekkuusu.enderskills.common.skill.ability.defense.earth.Taunt;
import arekkuusu.enderskills.common.skill.ability.defense.earth.Thorny;
import arekkuusu.enderskills.common.skill.ability.defense.electric.*;
import arekkuusu.enderskills.common.skill.ability.defense.fire.*;
import arekkuusu.enderskills.common.skill.ability.defense.light.*;
import arekkuusu.enderskills.common.skill.ability.mobility.ender.*;
import arekkuusu.enderskills.common.skill.ability.mobility.wind.*;
import arekkuusu.enderskills.common.skill.ability.offence.blood.*;
import arekkuusu.enderskills.common.skill.ability.offence.ender.*;
import arekkuusu.enderskills.common.skill.ability.offence.fire.*;
import arekkuusu.enderskills.common.skill.ability.offence.light.GleamFlash;
import arekkuusu.enderskills.common.skill.ability.offence.light.LumenWave;
import arekkuusu.enderskills.common.skill.ability.offence.light.RadiantRay;
import arekkuusu.enderskills.common.skill.ability.offence.wind.*;
import arekkuusu.enderskills.common.skill.effect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ModRenders {

    public static void preInit() {
        registerTESR(TileAltar.class, new TileAltarRenderer());
        registerEntity(EntityPlaceableData.class, EntityPlaceableDataRenderer::new);
        registerEntity(EntityThrowableData.class, EntityThrowableDataRenderer::new);
        registerEntity(EntityWall.class, EntityWallRender::new);
        registerEntity(EntityWallSegment.class, EntityWallSegmentRender::new);
        registerEntity(EntityPlaceableShockwave.class, ShockwaveRenderer.Placeable::new);
        registerEntity(EntityPlaceableBloodPool.class, BloodPoolRenderer.Placeable::new);
        registerEntity(EntityStoneGolem.class, StoneGolemRender::new);
        registerEntity(EntityShadow.class, EntityShadowRender::new);
        registerEntity(EntityPortal.class, EntityPortalRender::new);
        registerEntity(EntityBlackHole.class, BlackHoleRenderer.Placeable::new);
        registerEntity(EntityPlaceableGrasp.class, GraspRenderer.Placeable::new);
        registerEntity(EntityPlaceableFlamingRain.class, FlamingRainRenderer.Placeable::new);
        registerEntity(EntityPlaceableUpdraft.class, UpdraftRenderer.Placeable::new);
        registerEntity(EntityCrush.class, EntityCrushRenderer::new);
        registerEntity(EntityPlaceableExplode.class, ExplodeRenderer.Placeable::new);
        registerEntity(EntityTokenOrb.class, RenderTokenOrb::new);
        registerEntity(EntityVoltaicSentinel.class, VoltaicSentinelRender::new);
        registerEntity(EntityPlaceableRingOfFire.class, RingOfFireRenderer.Placeable::new);
        registerEntity(EntityPlaceableSlash.class, SlashRenderer.Placeable::new);
        registerEntity(EntityPlaceableLumenWave.class, LumenWaveRenderer.Placeable::new);
        registerEntity(EntityPlaceableGleamFlash.class, GleamFlashRenderer.Placeable::new);
        registerEntity(EntitySolarLance.class, SolarLanceRenderer.Placeable::new);
    }

    public static void init() {
        //Abilities
        registerSkill(Skill.class, new SkillRenderer<Skill>() {
        }); //Fallback
        registerSkill(Charm.class, new CharmRenderer());
        registerSkill(HealAura.class, new HealAuraRenderer());
        registerSkill(HealOther.class, new HealOtherRenderer());
        registerSkill(HealSelf.class, new HealSelfRenderer());
        registerSkill(PowerBoost.class, new PowerBoostRenderer());
        registerSkill(NearbyInvincibility.class, new NearbyInvincibilityRenderer());
        registerSkill(Taunt.class, new TauntRenderer());
        registerSkill(Thorny.class, new ThornyRenderer());
        registerSkill(Shockwave.class, new ShockwaveRenderer());
        registerSkill(Dash.class, new DashRenderer());
        registerSkill(ExtraJump.class, new ExtraJumpRenderer());
        registerSkill(Fog.class, new FogRenderer());
        registerSkill(Hasten.class, new HastenRenderer());
        registerSkill(Smash.class, new SmashRenderer());
        registerSkill(SpeedBoost.class, new SpeedBoostRenderer());
        registerSkill(Hover.class, new HoverRenderer());
        registerSkill(Invisibility.class, new InvisibilityRenderer());
        registerSkill(UnstablePortal.class, new UnstablePortalRenderer());
        registerSkill(Teleport.class, new TeleportRenderer());
        registerSkill(Warp.class, new WarpRenderer());
        registerSkill(Bleed.class, new BleedRenderer());
        registerSkill(BloodPool.class, new BloodPoolRenderer());
        registerSkill(Contaminate.class, new ContaminateRenderer());
        registerSkill(LifeSteal.class, new LifeStealRenderer());
        registerSkill(Sacrifice.class, new SacrificeRenderer());
        registerSkill(Syphon.class, new SyphonRenderer());
        registerSkill(GasCloud.class, new GasCloudRenderer());
        registerSkill(Gloom.class, new GloomRenderer());
        registerSkill(Grasp.class, new GraspRenderer());
        registerSkill(ShadowJab.class, new ShadowJabRenderer());
        registerSkill(BlackHole.class, new BlackHoleRenderer());
        registerSkill(FireSpirit.class, new FireSpiritRenderer());
        registerSkill(FlamingBreath.class, new FlamingBreathRenderer());
        registerSkill(FocusFlame.class, new FocusFlameRenderer());
        registerSkill(FlamingRain.class, new FlamingRainRenderer());
        registerSkill(Fireball.class, new FireballRenderer());
        registerSkill(Explode.class, new ExplodeRenderer());
        registerSkill(Updraft.class, new UpdraftRenderer());
        registerSkill(Pull.class, new PullRenderer());
        registerSkill(Push.class, new PushRenderer());
        registerSkill(Suffocate.class, new SuffocateRenderer());
        registerSkill(Slash.class, new SlashRenderer());
        registerSkill(Crush.class, new CrushRenderer());
        registerSkill(ShockingAura.class, new ShockingAuraRenderer());
        registerSkill(MagneticPull.class, new MagneticPullRenderer());
        registerSkill(PowerDrain.class, new PowerDrainRenderer());
        registerSkill(Energize.class, new EnergizeRenderer());
        registerSkill(ElectricPulse.class, new ElectricPulseRenderer());
        registerSkill(Flares.class, new FlaresRenderer());
        registerSkill(RingOfFire.class, new RingOfFireRenderer());
        registerSkill(BlazingAura.class, new BlazingAuraRenderer());
        registerSkill(Overheat.class, new OverheatRenderer());
        registerSkill(HomeStar.class, new HomeStarRenderer());
        registerSkill(RadiantRay.class, new RadiantRayRenderer());
        registerSkill(LumenWave.class, new LumenWaveRenderer());
        registerSkill(GleamFlash.class, new GleamFlashRenderer());
        //Effects
        registerSkill(Glowing.class, new GlowingRenderer());
        registerSkill(Bleeding.class, new BleedingRenderer());
        registerSkill(Blinded.class, new BlindedRenderer());
        registerSkill(Burning.class, new BurningRenderer());
        registerSkill(Invulnerable.class, new InvulnerableRenderer());
        registerSkill(Stunned.class, new StunnedRenderer());
        registerSkill(Overheal.class, new OverhealRenderer());
        registerSkill(Overcharge.class, new OverchargeRenderer());
        registerSkill(Electrified.class, new ElectrifiedRenderer());
        registerSkill(Pulsar.class, new PulsarRenderer());
    }

    public static void postInit() {
        EntityPlaceableDataRenderer.init(Minecraft.getMinecraft().getRenderManager());
        EntityThrowableDataRenderer.init(Minecraft.getMinecraft().getRenderManager());
    }

    public static <T extends Skill> void registerSkill(Class<T> skill, SkillRenderer<T> render) {
        SkillRendererDispatcher.INSTANCE.registerRenderer(skill, render);
    }

    public static <T extends TileEntity> void registerTESR(Class<T> tile, TileEntitySpecialRenderer<T> render) {
        ClientRegistry.bindTileEntitySpecialRenderer(tile, render);
    }

    public static <T extends Entity> void registerEntity(Class<T> entity, IRenderFactory<? super T> render) {
        RenderingRegistry.registerEntityRenderingHandler(entity, render);
    }
}
