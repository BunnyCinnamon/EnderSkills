package arekkuusu.enderskills.common.network.command;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.InfoCooldown;
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

import javax.annotation.Nullable;
import java.util.List;

public class CommandBookExperienceReset extends CommandBase {

    @Override
    public String getName() {
        return LibMod.MOD_ID + "_book_experience";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("es_book_experience");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /" + getName() + "[entity/player/@p] reset";
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
            return getListOfStringsMatchingLastWord(args, "reset");
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityLivingBase entity = getEntity(server, sender, args[0], EntityLivingBase.class);
        if (entity == null) {
            message(sender, "not_found.player");
            return;
        }

        AdvancementCapability capability = Capabilities.advancement(entity).orElse(null);
        if (capability == null) {
            message(sender, "not_found.player");
            return;
        }

        switch (args[1]) {
            case "reset":
                capability.experienceLevel = 0;
                capability.experienceProgress = 0;
                capability.experienceSpent = 0;
                message(sender, "book.reset");
                break;
            default:
                message(sender, "book.invalid");
                return;
        }
        if (entity instanceof EntityPlayerMP) {
            PacketHelper.sendAdvancementSync((EntityPlayerMP) entity);
        }
    }

    private void message(ICommandSender sender, String type, Object... args) {
        String key = "command." + LibMod.MOD_ID + "." + type;
        /*sender.sendMessage(new TextComponentTranslation(key, args));*/
        notifyCommandListener(sender, this, key, args);
    }
}
