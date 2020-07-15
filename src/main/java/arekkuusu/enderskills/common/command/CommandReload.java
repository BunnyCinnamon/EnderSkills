package arekkuusu.enderskills.common.command;

import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class CommandReload extends CommandBase {

    @Override
    public String getName() {
        return LibMod.MOD_ID + "_reload";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("es_reload");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /" + getName();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        { // Reload configs from disk
            if (!CommonConfig.getValues().syncValuesToClient) {
                message(sender, "not_allowed.es_reload");
                return;
            }
            Map<String, Multimap<Config.Type, ASMDataTable.ASMData>> asmData = ObfuscationReflectionHelper.getPrivateValue(ConfigManager.class, null, "asm_data");
            Map<String, Configuration> configs = ObfuscationReflectionHelper.getPrivateValue(ConfigManager.class, null, "CONFIGS");
            Method sync = ObfuscationReflectionHelper.findMethod(ConfigManager.class, "sync", Void.TYPE,
                    Configuration.class,
                    Class.class,
                    String.class,
                    String.class,
                    Boolean.TYPE,
                    Object.class
            );
            Multimap<Config.Type, ASMDataTable.ASMData> map = asmData.get(LibMod.MOD_ID);
            ClassLoader mcl = Loader.instance().getModClassLoader();
            File configDir = Loader.instance().getConfigDir();
            String modid = LibMod.MOD_ID;
            for (ASMDataTable.ASMData data : map.get(Config.Type.INSTANCE)) {
                try {
                    Class<?> cls = Class.forName(data.getClassName(), true, mcl);
                    String name = (String) data.getAnnotationInfo().get("name");
                    if (name == null)
                        name = modid;
                    String category = (String) data.getAnnotationInfo().get("category");
                    if (category == null)
                        category = "general";

                    File file = new File(configDir, name + ".cfg");
                    Configuration configuration = new Configuration(file);
                    configuration.load();
                    configs.put(file.getAbsolutePath(), configuration);
                    sync.invoke(null, configuration, cls, modid, category, true, null);
                    configuration.save();
                } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                    EnderSkills.LOG.fatal("[Config Reload] - Failed to reload Configuration from disk");
                    e.printStackTrace();
                }
            }
        }
        CommonConfig.initSyncConfig();
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) { //Sync EVERY player
            PacketHelper.sendConfigReload(player); //I hope you know what you're doing dude.... if this is what Y O U want then holy shit good luck brace for the lag spike and 9999999999 ping
            player.sendMessage(new TextComponentTranslation("command." + LibMod.MOD_ID + ".config.reload.success"));
        }
    }

    private void message(ICommandSender sender, String type, Object... args) {
        String key = "command." + LibMod.MOD_ID + "." + type;
        sender.sendMessage(new TextComponentTranslation(key, args));
    }
}
