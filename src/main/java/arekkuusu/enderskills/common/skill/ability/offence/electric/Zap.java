package arekkuusu.enderskills.common.skill.ability.offence.electric;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.event.SkillDamageEvent;
import arekkuusu.enderskills.api.event.SkillDamageSource;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Vector;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

public class Zap extends Skill {

    public Zap() {
        super(new Properties());
        ModAbilities.setRegistry(this, LibNames.ZAP);
    }

    @Override
    public void begin(EntityLivingBase entity, SkillData data) {
        if (entity.world.isRemote) {
            EntityLivingBase owner = SkillHelper.getOwner(data);
            if (owner != null) {
                Vector posFrom = new Vector(owner.getPositionVector()).addVector(owner.world.rand.nextDouble() * 0.05D, owner.height / 2D + owner.world.rand.nextDouble() * 0.05D, owner.world.rand.nextDouble() * 0.05D);
                Vector posTo = new Vector(entity.getPositionVector()).addVector(entity.world.rand.nextDouble() * 0.05D, entity.height / 2D + entity.world.rand.nextDouble() * 0.05D, entity.world.rand.nextDouble() * 0.05D);
                EnderSkills.getProxy().spawnLightning(entity.world, posFrom, posTo, 4, 0.6F, 5, 0xF4F389, false);
            }
        } else {
            EnderSkills.getProxy().addToQueue(() -> {
                if (SkillHelper.isActive(entity, ModEffects.ELECTRIFIED)) {
                    int stun = NBTHelper.getInteger(data.nbt, "stun");
                    ModEffects.ELECTRIFIED.propagate(entity, data, stun);
                } else {
                    ModEffects.ELECTRIFIED.set(entity, data);
                }
            });
            if (entity.isWet()) {
                entity.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 4);
            }
            EntityLivingBase owner = SkillHelper.getOwner(data);
            double damage = data.nbt.getDouble("damage");
            SkillDamageSource source = new SkillDamageSource(BaseAbility.DAMAGE_HIT_TYPE, owner);
            source.setMagicDamage();
            SkillDamageEvent event = new SkillDamageEvent(owner, this, source, damage);
            MinecraftForge.EVENT_BUS.post(event);
            if(event.getAmount() > 0 && event.getAmount() < Double.MAX_VALUE) {
                entity.attackEntityFrom(event.getSource(), event.toFloat());
            }

            if (entity.world instanceof WorldServer) {
                ((WorldServer) entity.world).playSound(null, entity.posX, entity.posY, entity.posZ, ModSounds.ELECTRIC_HIT, SoundCategory.BLOCKS, 0.5F, (1.0F + (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }
}
