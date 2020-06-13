package arekkuusu.enderskills.common.command;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CommandAdvancement extends CommandBase {

    @Override
    public String getName() {
        return LibMod.MOD_ID + "_advancement";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("es_advancement");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /" + getName() + "[entity/player/@p] [retries/level] [set/add/sub/get] [value]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "retries", "level");
        } else if (args.length == 3) {
            return getListOfStringsMatchingLastWord(args, "set", "add", "sub", "get");
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityLivingBase entity = getEntity(server, sender, args[0], EntityLivingBase.class);
        //String playerName = args.length > 0 && !args[0].equals("@p") ? args[0] : sender.getName();
       // EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
        if (entity == null) {
            message(sender, "not_found.player");
            return;
        }

        try {
            AdvancementCapability capability = Capabilities.advancement(entity).orElse(null);
            if (capability == null) {
                message(sender, "not_found.player");
                return;
            }

            switch (args[1]) {
                case "retries":
                    int valueToSet = args.length > 3 ? parseInt(args[3]) : 0; //We want to 'get'
                    if (valueToSet < 0) {
                        message(sender, "advancement.invalid.retries", valueToSet);
                        return;
                    }
                    switch (args[2]) {
                        case "set":
                            if (valueToSet > CommonConfig.getValues().advancement.maxRetries) {
                                message(sender, "advancement.invalid.retries");
                                return;
                            }
                            capability.resetCount = valueToSet;
                            break;
                        case "add":
                            int sum = Math.min(capability.resetCount + valueToSet, Integer.MAX_VALUE);
                            if (sum > CommonConfig.getValues().advancement.maxRetries) {
                                message(sender, "advancement.invalid.retries", valueToSet);
                                return;
                            }
                            capability.resetCount = sum;
                            break;
                        case "sub":
                            int sub = Math.max(capability.resetCount - valueToSet, 0);
                            if (sub > CommonConfig.getValues().advancement.maxRetries) {
                                message(sender, "advancement.invalid.retries", valueToSet);
                                return;
                            }
                            capability.resetCount = sub;
                            break;
                        case "get":
                            message(sender, "advancement.get.retries", capability.resetCount);
                            return;
                        default:
                    }
                    message(sender, "advancement.set.retries", capability.resetCount);
                    break;
                case "level":
                    int levelToSet = args.length > 3 ? parseInt(args[3]) : 0; //We want to 'get'
                    if (levelToSet < 0) {
                        message(sender, "advancement.invalid.level", levelToSet);
                        return;
                    }
                    switch (args[2]) {
                        case "set":
                            if (levelToSet > Integer.MAX_VALUE) {
                                message(sender, "advancement.invalid.level");
                                return;
                            }
                            capability.level = levelToSet;
                            capability.levelProgress = 0;
                            break;
                        case "add":
                            int sum = Math.min(capability.level + levelToSet, Integer.MAX_VALUE);
                            if (sum > Integer.MAX_VALUE) {
                                message(sender, "advancement.invalid.level", levelToSet);
                                return;
                            }
                            capability.level = sum;
                            break;
                        case "sub":
                            int sub = Math.max(capability.level - levelToSet, 0);
                            if (sub > Integer.MAX_VALUE) {
                                message(sender, "advancement.invalid.level", levelToSet);
                                return;
                            }
                            capability.level = sub;
                            break;
                        case "get":
                            message(sender, "advancement.get.level", capability.level);
                            return;
                        default:
                    }
                    message(sender, "advancement.set.level", capability.level);
                    break;
                default:
                    message(sender, "advancement.invalid");
                    return;
            }
            if(entity instanceof EntityPlayerMP) {
                PacketHelper.sendAdvancementSync((EntityPlayerMP) entity);
            }
        } catch (NumberFormatException ex) {
            message(sender, "invalid.number");
        }
    }

    private void message(ICommandSender sender, String type, Object... args) {
        String key = "command." + LibMod.MOD_ID + "." + type;
        /*sender.sendMessage(new TextComponentTranslation(key, args));*/
        notifyCommandListener(sender, this, key, args);
    }
}
