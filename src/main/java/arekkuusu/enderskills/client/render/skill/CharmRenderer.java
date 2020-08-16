package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.entity.EntityThrowableDataRenderer;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.defense.light.Charm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CharmRenderer extends SkillRenderer<Charm> {

    private static final ResourceLocation SHADER = new ResourceLocation(LibMod.MOD_ID, "shaders/post/charm.json");

    public CharmRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
        EntityThrowableDataRenderer.add(ModAbilities.CHARM, ProjectileLight::new);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (entity.ticksExisted % 4 == 0 && entity.world.rand.nextDouble() < 0.2D && ClientProxy.canParticleSpawn()) {
            Vec3d vec = entity.getPositionEyes(1F);
            double posX = vec.x + entity.world.rand.nextDouble() - 0.5D;
            double posY = vec.y + entity.world.rand.nextDouble() - 0.5D;
            double posZ = vec.z + entity.world.rand.nextDouble() - 0.5D;
            EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0, 0), 1, 50, entity.world.rand.nextBoolean() ? 0xFFFFFF : 0x58DB11, ResourceLibrary.SPIRAL);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Events {
        public boolean wasActive = false;

        @SubscribeEvent
        @SuppressWarnings("ConstantConditions")
        public void playerTick(TickEvent.ClientTickEvent event) {
            if (event.type == TickEvent.Type.CLIENT) {
                EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
                if (SkillHelper.isActive(Minecraft.getMinecraft().player, ModAbilities.CHARM)) {
                    if (!wasActive) {
                        renderer.loadShader(SHADER);
                        wasActive = true;
                    }
                } else if (wasActive) {
                    if (renderer.getShaderGroup() != null && renderer.getShaderGroup().getShaderGroupName() != null && SHADER.toString().equals(renderer.getShaderGroup().getShaderGroupName())) {
                        renderer.stopUseShader();
                    }
                    wasActive = false;
                }
            }
        }
    }
}
