package arekkuusu.enderskills.common.command;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class CommandWeight extends CommandBase {

    @Override
    public String getName() {
        return LibMod.MOD_ID + "_weight";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("es_weight");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /" + getName() + "[entity/player/@p] [modid:skillname] [set/add/sub] [weight]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
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
            String[] skills = GameRegistry.findRegistry(Skill.class).getKeys().stream()
                    .map(ResourceLocation::toString).toArray(String[]::new);
            return getListOfStringsMatchingLastWord(args, skills);
        } else if (args.length == 3) {
            return getListOfStringsMatchingLastWord(args, "set", "add", "sub", "get", "before", "after");
        } else if (args.length == 4 && args[2].matches("before|after")) {
            String[] skills = GameRegistry.findRegistry(Skill.class).getKeys().stream()
                    .map(ResourceLocation::toString).toArray(String[]::new);
            return getListOfStringsMatchingLastWord(args, skills);
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityLivingBase entity = getEntity(server, sender, args[0], EntityLivingBase.class);
        if(sender.getCommandSenderEntity() != entity && !sender.canUseCommand(2, this.getName())) {
            message(sender, "not_found.player");
        }
        if (entity == null) {
            message(sender, "not_found.player");
            return;
        }
        if (args.length < 2) {
            message(sender, "not_found.arguments");
            return;
        }

        try {
            SkilledEntityCapability capability = Capabilities.get(entity).orElse(null);
            if (capability == null) {
                message(sender, "not_found.player");
                return;
            }
            Skill skill = GameRegistry.findRegistry(Skill.class).getValue(new ResourceLocation(args[1]));
            if (skill == null) {
                message(sender, "skill.invalid.skill", args[1]);
                return;
            }
            int weight = capability.getWeight(skill);
            int weightToSet = 0;

            String action = args[2];
            switch (action) {
                case "set":
                    weightToSet = args.length > 3 ? parseInt(args[3]) : 0; //We want to 'get'
                    weightToSet = MathHelper.clamp(weightToSet, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    capability.putWeight(skill, weightToSet);
                    message(sender, "weight.set.value", args[1], weight);
                    break;
                case "add":
                    weightToSet = args.length > 3 ? parseInt(args[3]) : 0; //We want to 'get'
                    int sum = MathHelper.clamp(weight + weightToSet, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    capability.putWeight(skill, sum);
                    message(sender, "weight.set.value", args[1], weight);
                    break;
                case "sub":
                    weightToSet = args.length > 3 ? parseInt(args[3]) : 0; //We want to 'get'
                    int sub = MathHelper.clamp(weight - weightToSet, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    capability.putWeight(skill, sub);
                    message(sender, "weight.set.value", args[1], weight);
                    break;
                case "get":
                    message(sender, "weight.get.value", args[1], weight);
                    return;
                case "before":
                case "after":
                    Skill skillOther = GameRegistry.findRegistry(Skill.class).getValue(new ResourceLocation(args[3]));
                    if (skillOther == null) {
                        message(sender, "skill.invalid.skill", args[3]);
                        return;
                    }
                    int weightOther = capability.getWeight(skillOther);
                    int weightNew = 0;
                    int weightNewOther = 0;

                    boolean isBefore = action.equals("before");
                    boolean wasBefore = weightOther > weight;
                    if (isBefore) {
                        if (wasBefore) {
                            weightNew = weightOther - 1;
                            weightNewOther = weightOther;
                        } else {
                            weightNew = weightOther - 1;
                            weightNewOther = weightOther;
                        }
                    } else {
                        if (wasBefore) {
                            weightNew = weightOther;
                            weightNewOther = weightOther - 1;
                        } else {
                            weightNew = weightOther + 1;
                            weightNewOther = weightOther;
                        }
                    }

                    if (weightNew == weight) {
                        return;
                    }
                    capability.putWeight(skill, weightNew);
                    capability.putWeight(skillOther, weightNewOther);
                    for (Skill s : GameRegistry.findRegistry(Skill.class).getValuesCollection()) {
                        if (capability.hasWeight(s)) {
                            int w = capability.getWeight(s);
                            if ((wasBefore ? (w < weightNew && w > weight) : (w > weightNewOther && w < weight)) && s != skill && s != skillOther) {
                                capability.putWeight(s, wasBefore ? w - 1 : w + 1);
                                PacketHelper.sendWeightSync((EntityPlayerMP) entity, s, capability.getWeight(s));
                            }
                        }
                    }
                    PacketHelper.sendWeightSync((EntityPlayerMP) entity, skillOther, capability.getWeight(skillOther));
                    break;
                default:
            }
            if (entity instanceof EntityPlayerMP) {
                PacketHelper.sendWeightSync((EntityPlayerMP) entity, skill, capability.getWeight(skill));
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
