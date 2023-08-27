package arekkuusu.enderskills.common.network.command;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.EnduranceCapability;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.attribute.mobility.Endurance;
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

public class CommandEnduranceReset extends CommandBase {

    @Override
    public String getName() {
        return LibMod.MOD_ID + "_endurance";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("es_endurance");
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

        EnduranceCapability capability = Capabilities.endurance(entity).orElse(null);
        if (capability == null) {
            message(sender, "not_found.player");
            return;
        }

        switch (args[1]) {
            case "reset":
                capability.setEndurance(entity.getEntityAttribute(Endurance.MAX_ENDURANCE).getAttributeValue());
                message(sender, "endurance.reset");
                break;
            default:
                message(sender, "endurance.invalid");
                return;
        }
        if (entity instanceof EntityPlayerMP) {
            PacketHelper.sendEnduranceSync((EntityPlayerMP) entity);
        }
    }

    private void message(ICommandSender sender, String type, Object... args) {
        String key = "command." + LibMod.MOD_ID + "." + type;
        /*sender.sendMessage(new TextComponentTranslation(key, args));*/
        notifyCommandListener(sender, this, key, args);
    }
}
