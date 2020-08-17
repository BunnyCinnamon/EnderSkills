package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.MathUtil;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.attribute.mobility.Endurance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Overcharge extends BaseEffect {

    public Overcharge() {
        super(LibNames.OVERCHARGE, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (tick % 20 != 0) return;
        double charge = NBTHelper.getDouble(data.nbt, "over_charge") - 1D;
        if (charge > 0) {
            NBTHelper.setDouble(data.nbt, "over_charge", charge);
        } else if (!isClientWorld(entity)) {
            unapply(entity, data);
            async(entity, data);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        Capabilities.endurance(entity).ifPresent(capability -> {
            double amount = getOvercharge(entity);
            if (!MathUtil.fuzzyEqual(capability.getAbsorption(), amount)) {
                capability.setAbsorption(amount);
            }
        });
    }

    public double getOvercharge(EntityLivingBase entity) {
        return Capabilities.get(entity).map(c -> c.getActives().stream().filter(h -> h.data.skill == this).mapToDouble(h -> NBTHelper.getDouble(h.data.nbt, "over_charge")).sum()).orElse(0D);
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
        this.set(entity, 0);
    }

    public void set(EntityLivingBase entity, double amount) {
        Capabilities.endurance(entity).ifPresent(capability -> {
            double maxEndurance = entity.getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue();
            double endurance = capability.getEndurance();
            double remainingCharge = MathHelper.clamp((amount + endurance) - maxEndurance, 0, (maxEndurance * 1.5));
            capability.setEndurance(Math.min(endurance + amount, maxEndurance));
            if (remainingCharge > 0) {
                NBTTagCompound compound = new NBTTagCompound();
                NBTHelper.setDouble(compound, "over_charge", remainingCharge);
                SkillData data = SkillData.of(this)
                        .with(INDEFINITE)
                        .put(compound)
                        .create();
                apply(entity, data);
                sync(entity, data);
            }
        });
    }
}
