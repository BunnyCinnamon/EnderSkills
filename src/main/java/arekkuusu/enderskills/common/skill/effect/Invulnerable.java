package arekkuusu.enderskills.common.skill.effect;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Invulnerable extends BaseEffect {

    public Invulnerable() {
        super(LibNames.INVULNERABLE, new Properties());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void update(EntityLivingBase entity, SkillData data, int tick) {
        if (isClientWorld(entity)) return;
        EntityLivingBase owner = SkillHelper.getOwner(data);
        if (owner == null || !isWithinEffectiveDistance(entity, owner)) {
            unapply(entity, data);
            async(entity, data);
        } else {
            if (entity.isBurning()) {
                entity.extinguish();
            }
            if (!entity.getActivePotionEffects().isEmpty()) {
                entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
            }
        }
    }

    public boolean isWithinEffectiveDistance(EntityLivingBase entity, EntityLivingBase owner) {
        SkillData data = SkillHelper.getActive(owner, ModAbilities.HOME_STAR, owner.getUniqueID().toString()).orElse(null);
        if(data == null) return false;
        double time = NBTHelper.getInteger(data.nbt, "time");
        double tick = NBTHelper.getInteger(data.nbt, "tick");
        double progress = MathHelper.clamp((double) tick / Math.min(time, EntityPlaceableData.MIN_TIME), 0D, 1D);
        double distance = NBTHelper.getDouble(data.nbt, "range") * progress;
        return distance >= owner.getDistance(entity);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingHurtEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (SkillHelper.isActive(entity, this)) {
            event.setAmount(0);
        }
    }

    @Override
    public void set(EntityLivingBase entity, SkillData data) {
            SkillData status = SkillData.of(ModEffects.INVULNERABLE)
                    .by(data.id)
                    .with(5)
                    .overrides(SkillData.Overrides.SAME)
                    .put(data.nbt.copy(), data.watcher.copy())
                    .create();
            apply(entity, status);
            sync(entity, status);
    }
}
