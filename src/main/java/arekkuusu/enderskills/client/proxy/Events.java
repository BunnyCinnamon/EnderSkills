package arekkuusu.enderskills.client.proxy;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkillGroupCapability;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.SkillGroup;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.event.SkillUpgradeSyncEvent;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.gui.GuiScreenSkillAdvancements;
import arekkuusu.enderskills.client.keybind.KeyBounds;
import arekkuusu.enderskills.client.render.skill.SkillRendererDispatcher;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.attribute.mobility.Endurance;
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
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.*;

@EventBusSubscriber(modid = LibMod.MOD_ID, value = Side.CLIENT)
public class Events {

    public static boolean hideOverlay = false; //Hide all overlays
    public static int skillGroupPrev = 0; //Previous group selected
    public static int skillGroup = 0; //Current group selected
    public static String skillGroupName = ""; //Current group selected
    public static EnumFacing.AxisDirection rotate = EnumFacing.AxisDirection.POSITIVE;

    @SubscribeEvent
    public static void onSkillUpgradeSync(SkillUpgradeSyncEvent event) {
        if (GuiScreenSkillAdvancements.onSkillUpgradeRunnable != null) {
            GuiScreenSkillAdvancements.onSkillUpgradeRunnable.run();
            GuiScreenSkillAdvancements.onSkillUpgradeRunnable = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKeyPressHideOverlay(InputEvent.KeyInputEvent event) {
        if (KeyBounds.hideOverlay.isPressed()) {
            hideOverlay = !hideOverlay;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Capabilities.get(player).ifPresent(skills -> {
            Capabilities.weight(player).ifPresent(weight -> {
                if (KeyBounds.skillGroupRotateLeft.isPressed()) {
                    rotateSkillGroup(weight, true);
                    List<Tuple<Skill, SkillInfo>> display = getGroup(skills, weight);
                    if (!display.isEmpty()) {
                        player.sendStatusMessage(TextHelper.getTextComponent("rotate_skill_group", skillGroupName), true);
                    }
                    rotate = EnumFacing.AxisDirection.NEGATIVE;
                    return;
                }
                if (KeyBounds.skillGroupRotateRight.isPressed()) {
                    rotateSkillGroup(weight, false);
                    List<Tuple<Skill, SkillInfo>> display = getGroup(skills, weight);
                    if (!display.isEmpty()) {
                        player.sendStatusMessage(TextHelper.getTextComponent("rotate_skill_group", skillGroupName), true);
                    }
                    rotate = EnumFacing.AxisDirection.POSITIVE;
                    return;
                }
                List<KeyBinding> skillUseList = KeyBounds.skillUseList;
                for (int i = 0; i < skillUseList.size(); i++) {
                    KeyBinding binding = skillUseList.get(i);
                    if (binding.isPressed()) {
                        List<Tuple<Skill, SkillInfo>> display = getGroup(skills, weight);
                        if (i + 1 > display.size()) return;
                        Skill skill = display.get(i).getFirst();
                        if (skill.getProperties().isKeyBound()) {
                            PacketHelper.sendSkillUseRequestPacket(player, skill);
                        }
                        break;
                    }
                }
            });
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGroupRender(RenderGameOverlayEvent.Post event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Minecraft mc = Minecraft.getMinecraft();
        if (!ClientConfig.RENDER_CONFIG.skillGroup.renderOverlay
                || mc.world == null || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE || mc.isGamePaused()) {
            return;
        }
        Capabilities.get(player).ifPresent(skills -> {
            Capabilities.weight(player).ifPresent(weight -> {
                if (!ClientConfig.RENDER_CONFIG.skillGroup.renderUnowned && skills.getAllOwned().isEmpty()) {
                    return;
                }
                List<Tuple<Skill, SkillInfo>> display = getGroup(skills, weight);
                if (!ClientConfig.RENDER_CONFIG.skillGroup.renderUnowned && display.isEmpty()) {
                    if (!weight.skillGroupMap.isEmpty()) {
                        rotateSkillGroup(weight, rotate == EnumFacing.AxisDirection.NEGATIVE);
                        display = getGroup(skills, weight);
                        if (!display.isEmpty()) {
                            player.sendStatusMessage(TextHelper.getTextComponent("rotate_skill_group", skillGroupName), true);
                        }
                    }
                    return;
                }
                if (hideOverlay) return;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                boolean horizontal = ClientConfig.RENDER_CONFIG.skillGroup.orientation == ClientConfig.RenderValues.Orientation.HORIZONTAL;
                double scale = ClientConfig.RENDER_CONFIG.skillGroup.scale;
                double mSize = Math.pow(scale, -1D);
                int size = (int) (scale * 16);
                int posX = ClientConfig.RENDER_CONFIG.skillGroup.posX + 1;
                int posY = ClientConfig.RENDER_CONFIG.skillGroup.posY + 1;
                int i = ClientConfig.RENDER_CONFIG.skillGroup.inverse ? display.size() : 0;
                int step = ClientConfig.RENDER_CONFIG.skillGroup.step;
                int x = (int) (posX / scale);
                int y = (int) (posY / scale);
                //Skill group Background
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);
                mc.getTextureManager().bindTexture(ResourceLibrary.SKILL_BACKGROUND);
                if (horizontal) {
                    drawTexturedRectangle(x, y, 0, 32, 5, size * 2, 5, 32, 64);
                    drawTexturedRectangle(x + 5, y, 6, 32, (display.size() * step) - 3, size * 2, 20, 32, 64);
                    drawTexturedRectangle((display.size() * step) + x + 2, y, 27, 32, 5, size * 2, 5, 32, 64);
                } else {
                    drawTexturedRectangle(x, y, 0, 0, size * 2, 5, 32, 5, 64);
                    drawTexturedRectangle(x, y + 5, 0, 6, size * 2, (display.size() * step) - 3, 32, 20, 64);
                    drawTexturedRectangle(x, (display.size() * step) + y + 2, 0, 27, size * 2, 5, 32, 5, 64);
                }
                //Skill group title
                if (ClientConfig.RENDER_CONFIG.endurance.renderTitle) //Config toggle for skill_group.title
                { renderText(TextHelper.translate("skill_group.title"), x, y - 2, 0.5D, 0xFFFFFF); }
                GlStateManager.scale(mSize, mSize, mSize);
                GlStateManager.popMatrix();
                x = posX + 4;
                y = posY + 4;
                //Skills
                for (; ClientConfig.RENDER_CONFIG.skillGroup.inverse ? i > 0 : i < display.size(); i++) {
                    Tuple<Skill, SkillInfo> entry = display.get(i);
                    Skill skill = entry.getFirst();
                    SkillInfo info = entry.getSecond();
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(scale, scale, scale);
                    //Draw background
                    mc.getTextureManager().bindTexture(ResourceLibrary.SKILL_BACKGROUND);
                    drawTexturedRectangle(x, y, 48, 0, size, size, 16, 16, 64);
                    //Draw icon
                    mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    double remaining = getOwnerActiveRemainingTime(skill);
                    int cool = info instanceof SkillInfo.IInfoCooldown ? (int) Math.ceil(((SkillInfo.IInfoCooldown) info).getCooldown() / 20D) : 0;
                    boolean hasCool = cool > 0 || info == null;
                    float color = hasCool && remaining <= 0D ? 0.4F : 1F;
                    GlStateManager.color(color, color, color, 1F);
                    drawSprite(x, y, ResourceLibrary.getSkillTexture(skill), size);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
                    if (ClientConfig.RENDER_CONFIG.skillGroup.renderControls && skill.getProperties().isKeyBound() && i < KeyBounds.skillUseList.size()) {
                        KeyBinding binding = KeyBounds.skillUseList.get(i);
                        String control = binding.getKeyModifier().getLocalizedComboName(binding.getKeyCode());
                        renderText(control, x, y + 14, 0.3D, hasCool ? 0x8C605D : 8453920);
                    }
                    if (hasCool && info != null) {
                        if (ClientConfig.RENDER_CONFIG.skillGroup.renderDenominator)
                        { renderText(TextHelper.translate("cooldown.timer", cool), x + 1, y + 1, 0.5D, 0xFFFFFF); }
                        else
                        { renderText(TextHelper.translate("cooldown.timer.noDenominator", cool), x + 1, y + 1, 0.5D, 0xFFFFFF); }
                    }
                    GlStateManager.scale(mSize, mSize, mSize);
                    GlStateManager.popMatrix();
                    switch (ClientConfig.RENDER_CONFIG.skillGroup.orientation) {
                        case VERTICAL:
                            y += step;
                            break;
                        case HORIZONTAL:
                            x += step;
                            break;
                    }
                }
            });
        });
    }

    @Deprecated
    public static double getOwnerActiveRemainingTime(Skill skill) {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;
        return Capabilities.get(thePlayer)
                .flatMap(c -> c.getActives().stream().filter(a -> a.data.skill == skill
                        && NBTHelper.getEntity(EntityPlayer.class, a.data.nbt, "owner") == thePlayer).findFirst()
                ).map(a -> {
                    int maxTime = a.data.time;
                    if (maxTime == -1) return 1D;
                    int time = a.tick;
                    return 1D - ((double) time / (double) maxTime);
                }).orElse(-1D);
    }

    public static List<Tuple<Skill, SkillInfo>> getGroup(SkilledEntityCapability skills, SkillGroupCapability weight) {
        List<Tuple<Skill, SkillInfo>> display = new ArrayList<>(6);

        int index = 0;
        for (Map.Entry<String, SkillGroup> groupEntry : weight.getGroups().entrySet()) {
            if (index == skillGroup) {
                SkillGroup group = groupEntry.getValue();
                for (Map.Entry<Skill, Integer> integerEntry : group.map.entrySet()) {
                    Skill skill = integerEntry.getKey();
                    if (skill.getProperties().hasStatusIcon() && (ClientConfig.RENDER_CONFIG.skillGroup.renderUnowned || skills.isOwned(skill))) {
                        display.add(new Tuple<>(skill, skills.getOwned(skill).orElse(null)));
                    }
                }
                display.sort((a, b) -> {
                    int e1Weight = group.map.get(a.getFirst());
                    int e2Weight = group.map.get(b.getFirst());
                    return Integer.compare(e1Weight, e2Weight);
                });
                break;
            }
            index++;
        }

        return display;
    }

    public static void rotateSkillGroup(SkillGroupCapability weight, boolean inverse) {
        int groupSize = weight.skillGroupMap.size();
        skillGroupPrev = skillGroup;
        skillGroup += inverse ? -1 : 1;
        if (skillGroup < 0) skillGroup = groupSize;
        if (skillGroup > groupSize) skillGroup = 0;

        int index = 0;
        for (String name : weight.getGroups().keySet()) {
            if (index == skillGroup) {
                skillGroupName = name;
                break;
            }
            index++;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEnduranceRender(RenderGameOverlayEvent.Post event) {
        if (hideOverlay) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Minecraft mc = Minecraft.getMinecraft();
        if (!ClientConfig.RENDER_CONFIG.endurance.renderOverlay
                || mc.world == null || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE || mc.isGamePaused()) {
            return;
        }
        Capabilities.endurance(player).ifPresent(capability -> {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            double endurance = capability.getEndurance() + capability.getAbsorption();
            double enduranceMax = Math.max(player.getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue(), capability.getEndurance()) + capability.getAbsorption();
            boolean horizontal = ClientConfig.RENDER_CONFIG.endurance.orientation == ClientConfig.RenderValues.Orientation.HORIZONTAL;
            double scale = ClientConfig.RENDER_CONFIG.endurance.scale;
            double mSize = Math.pow(scale, -1D);
            int posX = ClientConfig.RENDER_CONFIG.endurance.posX + 1;
            int posY = ClientConfig.RENDER_CONFIG.endurance.posY + 1;
            int width = ClientConfig.RENDER_CONFIG.endurance.size;
            int fillWidth = (int) (((float) endurance / (float) enduranceMax) * (float) width + 1);
            int fill = (int) (((float) endurance / (float) enduranceMax) * (float) 182 + 1);
            int x = (int) (posX / scale);
            int y = (int) (posY / scale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            mc.getTextureManager().bindTexture(horizontal ? ResourceLibrary.ENDURANCE_BACKGROUND : ResourceLibrary.ENDURANCE_BACKGROUND_);
            GlStateManager.pushMatrix();
            //Endurance Background
            if (horizontal) {
                drawTexturedRectangle(x, y, 0, 245, width + 8, 11, 182 + 8, 11, 256);
            } else {
                drawTexturedRectangle(x, y, 245, 0, 11, width + 8, 11, 182 + 8, 256);
            }
            //Endurance Bar
            x += horizontal ? 4 : 3;
            y += horizontal ? 3 : 4;
            if (horizontal) {
                drawTexturedRectangle(x, y, 0, ClientConfig.RENDER_CONFIG.endurance.color.ordinal() * 5 * 2, width, 5, 182, 5, 256);
                if (fill > 0) {
                    drawTexturedRectangle(x, y, 0, ClientConfig.RENDER_CONFIG.endurance.color.ordinal() * 5 * 2 + 5, fillWidth, 5, fill, 5, 256);
                    drawTexturedRectangle(x, y, 0, 80 + (ClientConfig.RENDER_CONFIG.endurance.overlay.ordinal() - 1) * 5 * 2 + 5, fillWidth, 5, fill, 5, 256);
                }
            } else {
                drawTexturedRectangle(x, y, ClientConfig.RENDER_CONFIG.endurance.color.ordinal() * 5 * 2, 0, 5, width, 5, 182, 256);
                if (fill > 0) {
                    drawTexturedRectangle(x, y, ClientConfig.RENDER_CONFIG.endurance.color.ordinal() * 5 * 2 + 5, 0, 5, fillWidth, 5, fill, 256);
                    drawTexturedRectangle(x, y, 80 + (ClientConfig.RENDER_CONFIG.endurance.overlay.ordinal() - 1) * 5 * 2 + 5, 0, 5, fillWidth, 5, fill, 256);
                }
            }
            //Endurance title
            if (horizontal) {
                if (ClientConfig.RENDER_CONFIG.endurance.renderTitle) //config toggle for endurance.title
                { renderText(TextHelper.translate("endurance.title", (int) endurance, (int) enduranceMax), x - 4, y - 5, 0.5D, 0xFFFFFF); }
                String text = TextHelper.translate("endurance.amount", (int) endurance, (int) enduranceMax);
                renderText(text, x - mc.fontRenderer.getStringWidth(text) / 2 + width / 2, y, 0.8D, 0xFFFFFF);
            } else {
                if (ClientConfig.RENDER_CONFIG.endurance.renderTitle) //config toggle for endurance.title
                { renderText(TextHelper.translate("endurance.title", (int) endurance, (int) enduranceMax), x - 4, y - 5, 0.5D, 0xFFFFFF); }
                String text = TextHelper.translate("endurance.amount", (int) endurance, (int) enduranceMax);
                renderText(text, x, y - mc.fontRenderer.FONT_HEIGHT / 2 + width / 2, 0.8D, 0xFFFFFF);
            }
            GlStateManager.popMatrix();
            //Fix scale
            GlStateManager.scale(mSize, mSize, mSize);
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
            ClientProxy.LIGHTNING_MANAGER.update();
            ClientProxy.PARTICLE_RENDERER.update();
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

    public static final Queue<Runnable> QUEUE = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onNextTickExecute(TickEvent.ClientTickEvent event) {
        if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END) {
            Runnable runnable;
            while ((runnable = QUEUE.poll()) != null) {
                runnable.run();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityTickActive(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().getEntityWorld().isRemote) {
            EntityLivingBase entity = event.getEntityLiving();
            Capabilities.get(entity).ifPresent(skills -> {
                if(skills.getActives().isEmpty()) return;
                //Iterate Entity-level SkillHolders
                List<SkillHolder> iterated = Lists.newLinkedList(skills.getActives());
                for (SkillHolder holder : iterated) {
                    holder.tick(entity);
                    if (holder.isDead()) skills.getActives().remove(holder);
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityTickCooldown(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().getEntityWorld().isRemote) {
            EntityLivingBase entity = event.getEntityLiving();
            Capabilities.get(entity).ifPresent(skills -> {
                if(skills.getAllOwned().isEmpty()) return;
                //Iterate Cooldowns
                for (Map.Entry<Skill, SkillInfo> entry : skills.getAllOwned().entrySet()) {
                    SkillInfo skillInfo = entry.getValue();
                    if (skillInfo instanceof SkillInfo.IInfoCooldown && ((SkillInfo.IInfoCooldown) skillInfo).hasCooldown()) {
                        ((SkillInfo.IInfoCooldown) skillInfo).setCooldown(((SkillInfo.IInfoCooldown) skillInfo).getCooldown() - 1);
                    }
                }
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
        if (Minecraft.getMinecraft().getRenderManager().options == null)
            return; //WHY WHAT THE FUCK?????????????????????
        if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) return;
        if (Minecraft.getMinecraft().player.isElytraFlying()) return;
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(skills -> {
            for (SkillHolder skillHolder : skills.getActives()) {
                SkillRendererDispatcher.INSTANCE.getRenderer(skillHolder.data.skill).render(Minecraft.getMinecraft().player, 0, 0, 0, event.getPartialTicks(), skillHolder);
            }
        });
    }
}
