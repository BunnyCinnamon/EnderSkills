package arekkuusu.enderskills.common.proxy;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.event.SkillActionableEvent;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import com.google.common.collect.Lists;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

import static net.minecraftforge.event.entity.player.PlayerEvent.Clone;

@EventBusSubscriber(modid = LibMod.MOD_ID)
public class Events {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PacketHelper.sendConfigReload((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            PacketHelper.sendSkillsSync((EntityPlayerMP) event.getEntity());
            PacketHelper.sendEnduranceSync((EntityPlayerMP) event.getEntity());
            PacketHelper.sendAdvancementSync((EntityPlayerMP) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerTrackEntity(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        if(event.getTarget() instanceof EntityLivingBase) {
            PacketHelper.sendSkillsSyncTracking((EntityPlayerMP) event.getEntity(), (EntityLivingBase) event.getTarget());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            if (!event.isEndConquered()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Capabilities.get(event.getEntity()).ifPresent(c -> {
            c.clearActive();
            c.getActives().forEach(h -> h.tick((EntityLivingBase) event.getEntity()));
            c.skillHolders.clear();
        });
    }

    @SubscribeEvent
    public static void onEntityDimensionChange(EntityTravelToDimensionEvent event) {
        Capabilities.get(event.getEntity()).ifPresent(c -> {
            c.clearActive();
            c.getActives().forEach(h -> h.tick((EntityLivingBase) event.getEntity()));
            c.skillHolders.clear();
        });
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Capabilities.get(event.player).ifPresent(c -> {
            c.clearActive();
            c.getActives().forEach(h -> h.tick(event.player));
            c.skillHolders.clear();
        });
    }

    @SubscribeEvent
    public void onPlayerClone(Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();
        if (oldPlayer instanceof EntityPlayerMP && newPlayer instanceof EntityPlayerMP) {
            if (event.isWasDeath()) {
                Capabilities.get(oldPlayer).ifPresent(original -> {
                    Capabilities.get(newPlayer).ifPresent(replacement -> {
                        replacement.deserializeNBT(original.serializeNBT());
                    });
                });
                Capabilities.endurance(oldPlayer).ifPresent(original -> {
                    Capabilities.endurance(newPlayer).ifPresent(replacement -> {
                        replacement.deserializeNBT(original.serializeNBT());
                    });
                });
                Capabilities.advancement(oldPlayer).ifPresent(original -> {
                    Capabilities.advancement(newPlayer).ifPresent(replacement -> {
                        replacement.deserializeNBT(original.serializeNBT());
                    });
                });
                Capabilities.weight(oldPlayer).ifPresent(original -> {
                    Capabilities.weight(newPlayer).ifPresent(replacement -> {
                        replacement.deserializeNBT(original.serializeNBT());
                    });
                });
                Capabilities.powerBoost(oldPlayer).ifPresent(original -> {
                    Capabilities.powerBoost(newPlayer).ifPresent(replacement -> {
                        replacement.deserializeNBT(original.serializeNBT());
                    });
                });
            } else {
                Capabilities.get(oldPlayer).ifPresent(c -> {
                    c.clearActive();
                    c.getActives().forEach(h -> h.tick(oldPlayer));
                    c.skillHolders.clear();
                });
            }
        }
    }

    public static final Queue<Runnable> QUEUE = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onNextTickExecute(TickEvent.ServerTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.START) {
            Runnable runnable;
            while ((runnable = QUEUE.poll()) != null) {
                try {
                    runnable.run();
                } catch (ConcurrentModificationException e) {
                    //fuck you
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityTickActive(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntityLiving().getEntityWorld().isRemote) {
            EntityLivingBase entity = event.getEntityLiving();
            if(Math.floor(entity.getHealth()) < 0F) {
                entity.setDead();
            }
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
        if (!event.getEntityLiving().getEntityWorld().isRemote) {
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
    public static void onKnockbackTick(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntityLiving().getEntityWorld().isRemote) {
            EntityLivingBase entity = event.getEntityLiving();
            Capabilities.knockback(entity).ifPresent(c -> {
                if(c.lastKnockback < 20) {
                    c.lastKnockback++;
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSkillActionable(SkillActionableEvent event) {
        if(event.getEntityLiving() instanceof EntityPlayer && ((EntityPlayer) event.getEntityLiving()).isSpectator()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!event.getEntityLiving().getEntityWorld().isRemote) {
            Capabilities.knockback(event.getEntityLiving()).ifPresent(c -> {
                DamageSource source = event.getEntityLiving().getLastDamageSource();
                boolean isDamageAnDoTAbility = source != null
                        && CommonConfig.getSyncValues().skill.preventAbilityDoTKnockback
                        && source.damageType.equals(BaseAbility.DAMAGE_DOT_TYPE);
                if (isDamageAnDoTAbility) {
                    event.setCanceled(true);
                } else {
                    c.lastKnockback = 0;
                }
            });
        }
    }
}
