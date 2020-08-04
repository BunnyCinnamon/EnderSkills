package arekkuusu.enderskills.common.command;

import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.SkilledEntityCapability;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.network.PacketHelper;
import arekkuusu.enderskills.common.skill.BaseSkill;
import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class CommandSkill extends CommandBase {

    @Override
    public String getName() {
        return LibMod.MOD_ID + "_skill";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("es_skill");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /" + getName() + "[entity/player/@p] [modid:skillname] [set/add/sub/query/unlock/lock] [level]";
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
            String[] skills = GameRegistry.findRegistry(Skill.class).getKeys().stream()
                    .map(ResourceLocation::toString).toArray(String[]::new);
            return getListOfStringsMatchingLastWord(args, skills);
        } else if (args.length == 3) {
            return getListOfStringsMatchingLastWord(args, "set", "add", "sub", "query", "unlock", "lock");
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
                message(sender, "skill.invalid", args[1]);
                return;
            }
            //Lock/Unlock
            String action = args[2];
            switch (action) {
                case "unlock":
                case "lock":
                    if (action.equals("lock")) {
                        capability.removeOwned(skill);
                        message(sender, "skill.lock", args[1]);
                    } else {
                        capability.addOwned(skill);
                        message(sender, "skill.unlock", args[1]);
                    }
                    if (entity instanceof EntityPlayerMP) {
                        PacketHelper.sendSkillSync((EntityPlayerMP) entity, skill);
                    }
                    return;
            }
            //Level set/add/sub/get
            int levelToSet = args.length > 3 ? parseInt(args[3]) : 0; //We want to 'get'
            if (levelToSet < 0) {
                message(sender, "skill.invalid.level", levelToSet);
                return;
            }
            SkillInfo info = capability.getOwned(skill).orElse(null);
            if (info == null) {
                message(sender, "skill.invalid", args[1]);
                return;
            }
            if (!(skill.getProperties() instanceof BaseSkill.BaseProperties) || !(info instanceof SkillInfo.IInfoUpgradeable)) {
                message(sender, "skill.invalid", args[1]);
                return;
            }
            SkillInfo.IInfoUpgradeable skillLevel = (SkillInfo.IInfoUpgradeable) info;
            BaseSkill.BaseProperties properties = (BaseSkill.BaseProperties) skill.getProperties();
            switch (action) {
                case "set":
                    if (levelToSet > properties.getMaxLevel()) {
                        message(sender, "skill.invalid.level");
                        return;
                    }
                    skillLevel.setLevel(levelToSet);
                    break;
                case "add":
                    int sum = Math.min(skillLevel.getLevel() + levelToSet, Integer.MAX_VALUE);
                    if (sum > properties.getMaxLevel()) {
                        message(sender, "skill.invalid.level", levelToSet);
                        return;
                    }
                    skillLevel.setLevel(sum);
                    break;
                case "sub":
                    int sub = Math.max(skillLevel.getLevel() - levelToSet, 0);
                    if (sub > properties.getMaxLevel()) {
                        message(sender, "skill.invalid.level", levelToSet);
                        return;
                    }
                    skillLevel.setLevel(sub);
                    break;
                case "query":
                    message(sender, "skill.get.level", args[1], skillLevel.getLevel());
                    return;
                default:
            }
            message(sender, "skill.set.level", args[1], skillLevel.getLevel());
            if (entity instanceof EntityPlayerMP) {
                PacketHelper.sendSkillSync((EntityPlayerMP) entity, skill);
            }
        } catch (NumberFormatException ex) {
            message(sender, "invalid.number");
        }
    }

    private void message(ICommandSender sender, String type, Object... args) {
        String key = "command." + LibMod.MOD_ID + "." + type;
        notifyCommandListener(sender, this, key, args);
    }
}
