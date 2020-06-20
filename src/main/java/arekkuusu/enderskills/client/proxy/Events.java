package arekkuusu.enderskills.client.proxy;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.keybind.SkillKeyBounds;
import arekkuusu.enderskills.client.render.skill.SkillRendererDispatcher;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LibMod.MOD_ID, value = Side.CLIENT)
public class Events {

    public static int skillGroupPrev = 0; //Previous group selected
    public static int skillGroup = 0; //Current group selected
    public static EnumFacing.AxisDirection rotate = EnumFacing.AxisDirection.POSITIVE;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Capabilities.get(player).ifPresent(capability -> {
            if (SkillKeyBounds.skillGroupRotateLeft.isPressed()) {
                rotateSkillGroup(capability, true);
                player.sendStatusMessage(TextHelper.getTextComponent("rotate_skill_group", skillGroup), true);
                rotate = EnumFacing.AxisDirection.NEGATIVE;
                return;
            }
            if (SkillKeyBounds.skillGroupRotateRight.isPressed()) {
                rotateSkillGroup(capability, false);
                player.sendStatusMessage(TextHelper.getTextComponent("rotate_skill_group", skillGroup), true);
                rotate = EnumFacing.AxisDirection.POSITIVE;
                return;
            }
            List<KeyBinding> skillUseList = SkillKeyBounds.skillUseList;
            for (int i = 0; i < skillUseList.size(); i++) {
                KeyBinding binding = skillUseList.get(i);
                if (binding.isPressed()) {
                    List<Map.Entry<Skill, SkillInfo>> display = getGroup(capability);
                    if (i + 1 > display.size()) return;
                    Skill skill = display.get(i).getKey();
                    if (skill.isKeyBound()) {
                        PacketHelper.sendSkillUseRequestPacket(player, skill);
                    }
                    break;
                }
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGroupRender(RenderGameOverlayEvent.Post event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Minecraft mc = Minecraft.getMinecraft();
        if (!CommonConfig.RENDER_CONFIG.skillGroup.renderOverlay
                || mc.world == null || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE || mc.isGamePaused()) {
            return;
        }
        Capabilities.get(player).ifPresent(capability -> {
            if (!CommonConfig.RENDER_CONFIG.skillGroup.renderUnowned && capability.getAll().isEmpty()) {
                return;
            }
            List<Map.Entry<Skill, SkillInfo>> display = getGroup(capability);
            if (!CommonConfig.RENDER_CONFIG.skillGroup.renderUnowned && display.isEmpty()) {
                rotateSkillGroup(capability, rotate == EnumFacing.AxisDirection.NEGATIVE);
                return;
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean horizontal = CommonConfig.RENDER_CONFIG.skillGroup.orientation == CommonConfig.RenderValues.Orientation.HORIZONTAL;
            double scale = CommonConfig.RENDER_CONFIG.skillGroup.scale;
            double mSize = Math.pow(scale, -1D);
            int size = (int) (scale * 16);
            int posX = CommonConfig.RENDER_CONFIG.skillGroup.posX + 1;
            int posY = CommonConfig.RENDER_CONFIG.skillGroup.posY + 1;
            int i = CommonConfig.RENDER_CONFIG.skillGroup.inverse ? display.size() : 0;
            int step = CommonConfig.RENDER_CONFIG.skillGroup.step;
            int x = (int) ((horizontal ? -posY : posX) / scale);
            int y = (int) ((horizontal ? -posX : posY) / scale);
            //Skill group Background
            GlStateManager.pushMatrix();
            /*if (horizontal) { //TODO: HORIZONTAL
                GlStateManager.translate(posX - 1 + size / 2F, posY - 3 - size - size / 2F, 0);
                GlStateManager.rotate(270, 0, 0, 1);
            }*/
            GlStateManager.scale(scale, scale, scale);
            mc.getTextureManager().bindTexture(ResourceLibrary.SKILL_BACKGROUND);
            drawTexturedRectangle(x, y, 0, 0, size * 2, 5, 32, 5, 32);
            drawTexturedRectangle(x, y + 5, 0, 6, size * 2, (display.size() * step) - 3, 32, 20, 32);
            drawTexturedRectangle(x, (display.size() * step) + y + 2, 0, 27, size * 2, 5, 32, 5, 32);
            //Skill group title
            renderText(TextHelper.translate("skill_group.title"), x, y - 2, 0.5D, 0xFFFFFF);
            GlStateManager.scale(mSize, mSize, mSize);
            /*if (horizontal) {
                GlStateManager.translate(0, 0, 0);
            }*/
            GlStateManager.popMatrix();
            x = posX + 4 + (horizontal ? (int) ((float) step / size) : 0);
            y = posY + 4 + (horizontal ? 0 : (int) ((float) step / size));
            //Skills
            for (; CommonConfig.RENDER_CONFIG.skillGroup.inverse ? i > 0 : i < display.size(); i++) {
                Map.Entry<Skill, SkillInfo> entry = display.get(i);
                Skill skill = entry.getKey();
                SkillInfo info = entry.getValue();
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                int cool = info instanceof IInfoCooldown ? ((IInfoCooldown) info).getCooldown() / 20 : 0;
                boolean hasCool = cool > 0 || info == null;
                float color = hasCool ? 0.4F : 1F;
                GlStateManager.color(color, color, color, 1F);
                drawSprite(x, y, skill.getTexture(), size);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                double remaining = getOwnerActiveRemainingTime(skill);
                if (remaining != -1) { //Draw white overlay
                    GlStateManager.disableTexture2D();
                    double progress = 1D - remaining;
                    int height = (int) (progress * size);
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    bufferbuilder.pos(x, y + size, 0D).color(1F, 1F, 1F, 0.5F).endVertex();
                    bufferbuilder.pos(x + size, y + size, 0D).color(1F, 1F, 1F, 0.5F).endVertex();
                    bufferbuilder.pos(x + size, y + height, 0D).color(1F, 1F, 1F, 0.5F).endVertex();
                    bufferbuilder.pos(x, y + height, 0D).color(1F, 1F, 1F, 0.5F).endVertex();
                    tessellator.draw();
                    GlStateManager.enableTexture2D();
                }
                if (CommonConfig.RENDER_CONFIG.skillGroup.renderControls && skill.isKeyBound()) {
                    KeyBinding binding = SkillKeyBounds.skillUseList.get(i);
                    String control = binding.getKeyModifier().getLocalizedComboName(binding.getKeyCode());
                    renderText(control, x, y + 14, 0.3D, hasCool ? 0x8C605D : 8453920);
                }
                if (hasCool && info != null) {
                    renderText(TextHelper.translate("cooldown.timer", cool), x + 1, y + 1, 0.5D, 0xFFFFFF);
                }
                GlStateManager.scale(mSize, mSize, mSize);
                GlStateManager.popMatrix();
                switch (CommonConfig.RENDER_CONFIG.skillGroup.orientation) {
                    case VERTICAL:
                        y += step;
                        break;
                    case HORIZONTAL:
                        x += step;
                        break;
                }
            }
        });
    }

    public static double getOwnerActiveRemainingTime(Skill skill) {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;
        return Capabilities.get(thePlayer)
                .flatMap(c -> c.getActives().stream().filter(a -> a.data.skill == skill
                        && NBTHelper.getEntity(EntityPlayer.class, a.data.nbt, "user") == thePlayer).findFirst()
                ).map(a -> {
                    int maxTime = a.data.time;
                    if (maxTime == -1) return 1D;
                    int time = a.tick;
                    return 1D - ((double) time / (double) maxTime);
                }).orElse(-1D);
    }

    public static List<Map.Entry<Skill, SkillInfo>> getGroup(SkilledEntityCapability capability) {
        List<Map.Entry<Skill, SkillInfo>> display = Lists.newArrayList();
        List<Map.Entry<Skill, SkillInfo>> list = getList(capability);
        int groupSize = CommonConfig.RENDER_CONFIG.skillGroup.overlayIcons;
        for (int i = skillGroup * groupSize, j = 0; i < list.size() && j < groupSize; i++, j++) {
            Map.Entry<Skill, SkillInfo> entry = list.get(i);
            if (CommonConfig.RENDER_CONFIG.skillGroup.renderUnowned || entry.getValue() != null) {
                display.add(entry);
            }
        }
        return display;
    }

    public static List<Map.Entry<Skill, SkillInfo>> getList(SkilledEntityCapability capability) {
        List<Map.Entry<Skill, SkillInfo>> list;
        if (!CommonConfig.RENDER_CONFIG.skillGroup.weightUnowned) {
            list = capability.getAll().entrySet().stream()
                    .filter(e -> e.getKey().hasStatusIcon())
                    .sorted((e1, e2) -> {
                        int e1Weight = capability.getWeight(e1.getKey());
                        int e2Weight = capability.getWeight(e2.getKey());
                        return Integer.compare(e1Weight, e2Weight);
                    }).collect(Collectors.toList());
        } else {
            List<Skill> temp = GameRegistry.findRegistry(Skill.class).getValuesCollection().stream()
                    .filter(Skill::hasStatusIcon)
                    .sorted((e1, e2) -> {
                        int e1Weight = capability.getWeight(e1);
                        int e2Weight = capability.getWeight(e2);
                        return Integer.compare(e1Weight, e2Weight);
                    }).collect(Collectors.toList());
            list = Lists.newArrayList();
            for (Skill skill : temp) {
                list.add(new AbstractMap.SimpleEntry<>(skill, capability.get(skill).orElse(null)));
            }
        }
        return list;
    }

    public static void rotateSkillGroup(SkilledEntityCapability capability, boolean inverse) {
        List<Map.Entry<Skill, SkillInfo>> display = getList(capability);
        int groupSize = display.size() / CommonConfig.RENDER_CONFIG.skillGroup.overlayIcons;
        skillGroupPrev = skillGroup;
        skillGroup += inverse ? -1 : 1;
        if (skillGroup < 0) skillGroup = groupSize;
        if (skillGroup > groupSize) skillGroup = 0;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEnduranceRender(RenderGameOverlayEvent.Post event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Minecraft mc = Minecraft.getMinecraft();
        if (!CommonConfig.RENDER_CONFIG.endurance.renderOverlay
                || mc.world == null || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE || mc.isGamePaused()) {
            return;
        }
        Capabilities.endurance(player).ifPresent(capability -> {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int endurance = capability.getEndurance();
            int enduranceMax = capability.getEnduranceMax();
            boolean vertical = CommonConfig.RENDER_CONFIG.endurance.orientation == CommonConfig.RenderValues.Orientation.VERTICAL;
            double scale = CommonConfig.RENDER_CONFIG.endurance.scale;
            double mSize = Math.pow(scale, -1D);
            int posX = CommonConfig.RENDER_CONFIG.endurance.posX + 1;
            int posY = CommonConfig.RENDER_CONFIG.endurance.posY + 1;
            int width = CommonConfig.RENDER_CONFIG.endurance.size;
            int fillWidth = (int) (((float) endurance / (float) enduranceMax) * (float) width + 1);
            int fill = (int) (((float) endurance / (float) enduranceMax) * (float) 182 + 1);
            int x = (int) ((vertical ? -posY : posX) / scale);
            int y = (int) ((vertical ? -posX : posY) / scale);
            GlStateManager.pushMatrix();
            /*if (vertical) { //TODO: VERTICAL
                GlStateManager.translate(posX, posY, 0);
                GlStateManager.rotate(90, 0, 0, 1);
            }*/
            GlStateManager.scale(scale, scale, scale);
            mc.getTextureManager().bindTexture(ResourceLibrary.ENDURANCE_BACKGROUND);
            GlStateManager.pushMatrix();
            //Endurance Background
            drawTexturedRectangle(x, y, 0, 245, width + 8, 11, 182 + 8, 11, 256);
            //Endurance Bar
            x += 4;
            y += 3;
            drawTexturedRectangle(x, y, 0, CommonConfig.RENDER_CONFIG.endurance.color.ordinal() * 5 * 2, width, 5, 182, 5, 256);
            if (fill > 0) {
                drawTexturedRectangle(x, y, 0, CommonConfig.RENDER_CONFIG.endurance.color.ordinal() * 5 * 2 + 5, fillWidth, 5, fill, 5, 256);
                drawTexturedRectangle(x, y, 0, 80 + (CommonConfig.RENDER_CONFIG.endurance.overlay.ordinal() - 1) * 5 * 2 + 5, fillWidth, 5, fill, 5, 256);
            }
            //Endurance title
            renderText(TextHelper.translate("endurance.title", endurance), x - 4, y - 5, 0.5D, 0xFFFFFF);
            GlStateManager.popMatrix();
            //Fix scale
            GlStateManager.scale(mSize, mSize, mSize);
            /*if (vertical) {
                GlStateManager.translate(-posX, -posY, 0);
            }*/
            GlStateManager.popMatrix();
        });
    }

    public static void renderText(String text, int xCoord, int yCoord, double textScale, int color) {
        FontRenderer fontRender = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.scale(textScale, textScale, textScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        fontRender.drawStringWithShadow(text, xCoord / (float) textScale, yCoord / (float) textScale, color);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        double mSize = Math.pow(textScale, -1D);
        GlStateManager.scale(mSize, mSize, mSize);
        GlStateManager.popMatrix();
    }

    public static void drawSprite(int xCoord, int yCoord, ResourceLocation texture, int size) {
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xCoord, yCoord + size, 0D).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + size, yCoord + size, 0D).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + size, yCoord, 0D).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        bufferbuilder.pos(xCoord, yCoord, 0D).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        tessellator.draw();
    }

    public static void drawTexturedRectangle(int x, int y, int textureX, int textureY, int width, int height, int widthU, int heightV, int textureSize) {
        float texturePixel = 1F / textureSize;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0D).tex((float) (textureX) * texturePixel, (float) (textureY + heightV) * texturePixel).endVertex();
        bufferbuilder.pos(x + width, y + height, 0D).tex((float) (textureX + widthU) * texturePixel, (float) (textureY + heightV) * texturePixel).endVertex();
        bufferbuilder.pos(x + width, y, 0D).tex((float) (textureX + widthU) * texturePixel, (float) (textureY) * texturePixel).endVertex();
        bufferbuilder.pos(x, y, 0D).tex((float) (textureX) * texturePixel, (float) (textureY) * texturePixel).endVertex();
        tessellator.draw();
    }

    //----------------Particle Renderer Start----------------//
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ClientProxy.PARTICLE_RENDERER.update();
            ClientProxy.LIGHTNING_MANAGER.update();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderAfterWorld(RenderWorldLastEvent event) {
        GlStateManager.pushMatrix();
        ClientProxy.LIGHTNING_MANAGER.renderAll(event.getPartialTicks());
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        ClientProxy.PARTICLE_RENDERER.renderAll(event.getPartialTicks());
        GlStateManager.popMatrix();
    }
    //----------------Particle Renderer End----------------//

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().getEntityWorld().isRemote) {
            EntityLivingBase entity = event.getEntityLiving();
            Capabilities.get(entity).ifPresent(skills -> {
                skills.tick(entity);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingPostRender(RenderLivingEvent.Post<EntityLivingBase> event) {
        Capabilities.get(event.getEntity()).ifPresent(skills -> {
            if (event.getEntity() == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
                return;
            for (SkillHolder skillHolder : skills.getActives()) {
                SkillRendererDispatcher.INSTANCE.getRenderer(skillHolder.data.skill).render(event.getEntity(), event.getX(), event.getY(), event.getZ(), event.getPartialRenderTick(), skillHolder);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerScreenRender(RenderWorldLastEvent event) {
        if(Minecraft.getMinecraft().getRenderManager().options == null) return; //WHY WHAT THE FUCK?????????????????????
        if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) return;
        if (Minecraft.getMinecraft().player.isElytraFlying()) return;
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(skills -> {
            for (SkillHolder skillHolder : skills.getActives()) {
                SkillRendererDispatcher.INSTANCE.getRenderer(skillHolder.data.skill).render(Minecraft.getMinecraft().player, 0, 0, 0, event.getPartialTicks(), skillHolder);
            }
        });
    }
}
