package arekkuusu.enderskills.common;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.EnduranceCapability;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.common.command.*;
import arekkuusu.enderskills.common.handler.GuiHandler;
import arekkuusu.enderskills.common.handler.WorldGenOre;
import arekkuusu.enderskills.common.item.ModItems;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHandler;
import arekkuusu.enderskills.common.potion.ModPotionTypes;
import arekkuusu.enderskills.common.proxy.IProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = LibMod.MOD_ID,
        name = LibMod.MOD_NAME,
        version = LibMod.MOD_VERSION,
        acceptedMinecraftVersions = "[1.12.2]"
)
public class ES {

    @SidedProxy(clientSide = LibMod.CLIENT_PROXY, serverSide = LibMod.SERVER_PROXY)
    private static IProxy proxy;
    private static final ES INSTANCE = new ES();
    public static final Logger LOG = LogManager.getLogger(LibMod.MOD_NAME);

    private ES() {
    }

    public static IProxy getProxy() {
        return proxy;
    }

    @Mod.InstanceFactory
    public static ES getInstance() {
        return INSTANCE;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        SkilledEntityCapability.init();
        EnduranceCapability.init();
        AdvancementCapability.init();
        PacketHandler.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        ModItems.init();
        ModPotionTypes.init();
        GameRegistry.registerWorldGenerator(new WorldGenOre(), 0);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        CommonConfig.initSyncConfig();
    }

    @EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandReload());
        event.registerServerCommand(new CommandSkill());
        event.registerServerCommand(new CommandWeight());
        event.registerServerCommand(new CommandCooldownReset());
        event.registerServerCommand(new CommandEnduranceReset());
        event.registerServerCommand(new CommandAdvancement());
    }
}
