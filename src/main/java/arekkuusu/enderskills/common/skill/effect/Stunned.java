package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.event.SkillsActionableEvent;
import arekkuusu.enderskills.common.entity.AIOverride;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.SkillHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Stunned extends BaseEffect {

    public Stunned() {
        super(LibNames.STUNNED, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void update(EntityLivingBase target, SkillData data, int tick) {
        if (isClientWorld(target)) return;
        if (target instanceof EntityLiving) {
            ((EntityLiving) target).getNavigator().clearPath();
            ((EntityLiving) target).tasks.addTask(0, AIOverride.INSTANCE);
        }
    }

    @Override
    public void end(EntityLivingBase target, SkillData data) {
        if (isClientWorld(target)) return;
        if (target instanceof EntityLiving) {
            ((EntityLiving) target).tasks.removeTask(AIOverride.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onSkillShouldUse(SkillsActionableEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        if (SkillHelper.isActive(event.getEntityLiving(), this)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void mouseListener(MouseEvent event) {
        if (SkillHelper.isActive(Minecraft.getMinecraft().player, this)) {
            Minecraft.getMinecraft().mouseHelper.deltaX = Minecraft.getMinecraft().mouseHelper.deltaY = 0;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void inputListener(InputUpdateEvent event) {
        if (SkillHelper.isActive(event.getEntityLiving(), this)) {
            event.getMovementInput().forwardKeyDown = false;
            event.getMovementInput().rightKeyDown = false;
            event.getMovementInput().backKeyDown = false;
            event.getMovementInput().leftKeyDown = false;
            event.getMovementInput().sneak = false;
            event.getMovementInput().jump = false;
            event.getMovementInput().moveForward = 0;
            event.getMovementInput().moveStrafe = 0;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouseClick(InputEvent.MouseInputEvent event) {
        if (SkillHelper.isActive(Minecraft.getMinecraft().player, this) && Minecraft.getMinecraft().gameSettings.keyBindAttack.isPressed()) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
        }
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        this.set(entity, data, 10);
    }

    public void set(EntityLivingBase entity, SkillData data, int time) {
        SkillData status = SkillData.of(this)
                .by(data.id + ":" + data.skill.getRegistryName())
                .with(time)
                .overrides(SkillData.Overrides.EQUAL)
                .create();
        apply(entity, status);
        sync(entity, status);
    }
}
