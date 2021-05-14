package arekkuusu.enderskills.common.handler;

import arekkuusu.enderskills.api.event.SkillActionableEvent;
import com.gamerforea.eventhelper.util.EventUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

public final class WorldGuardHelper {

    public final GameProfile fakePlayer = new GameProfile(UUID.fromString("d5de2607-0b2d-4fae-a615-9c4bcf39d6b6"), "[EnderSkills]");

    @SubscribeEvent
    public void canUseAbility(SkillActionableEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer
                && event.getEntityLiving().world instanceof WorldServer
                && isEventHelperLoaded()
                && EventUtils.cantAttack((EntityPlayer) event.getEntityLiving(), FakePlayerFactory.get((WorldServer) event.getEntityLiving().world, fakePlayer))) {
            event.setCanceled(true);
        }
    }

    public static boolean IS_LOADED;

    static {
        try {
            Class.forName("com.gamerforea.eventhelper.EventHelperMod");
            Class.forName("org.spongepowered.api.Server");
            IS_LOADED = Loader.isModLoaded("eventhelper");
        } catch (ClassNotFoundException ignored) {
            IS_LOADED = false;
        }
    }

    public static boolean isEventHelperLoaded() {
        //Is loaded!
        return IS_LOADED;
    }
}
