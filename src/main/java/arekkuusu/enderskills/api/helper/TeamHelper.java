package arekkuusu.enderskills.api.helper;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EntitySelectors;

@SuppressWarnings({"Guava", "ConstantConditions", "unchecked"})
public class TeamHelper {

    public static final Function<Entity, Predicate<Entity>> SAME_TEAM = e -> e != null ? TeamHelper.getTeamCollisionPredicate(e) : Predicates.alwaysFalse();
    public static final Function<Entity, Predicate<Entity>> NOT_SAME_TEAM = e -> e != null ? EntitySelectors.getTeamCollisionPredicate(e) : Predicates.alwaysFalse();
    public static final Predicate<Entity> NOT_CREATIVE = entity -> !(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode;
    public static final Predicate<Entity> HUMAN_TEAM = entity -> !EnderSkillsAPI.defaultHumanTeam || !entity.isCreatureType(EnumCreatureType.MONSTER, false);
    public static final Predicate<Entity> NOT_HUMAN_TEAM = entity -> EnderSkillsAPI.defaultHumanTeam &&  entity.isCreatureType(EnumCreatureType.MONSTER, false);
    public static final Function<Entity, Predicate<Entity>> SELECTOR_ALLY = (e) -> Predicates.or(Predicates.and(SAME_TEAM.apply(e), NOT_CREATIVE, HUMAN_TEAM), input -> input == e);
    public static final Function<Entity, Predicate<Entity>> SELECTOR_ENEMY = (e) -> Predicates.and(NOT_SAME_TEAM.apply(e), NOT_CREATIVE, NOT_HUMAN_TEAM, input -> input != e);

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Predicate<T> getTeamCollisionPredicate(final Entity entityIn) {
        final Team team = entityIn.getTeam();
        final Team.CollisionRule team$collisionrule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
        Predicate<?> ret = team$collisionrule == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> {
            if (!entity.canBePushed()) {
                return false;
            } else if (!entityIn.world.isRemote || entity instanceof EntityPlayer && ((EntityPlayer) entity).isUser()) {
                Team team1 = entity.getTeam();
                Team.CollisionRule team$collisionrule1 = team1 == null ? Team.CollisionRule.ALWAYS : team1.getCollisionRule();

                if (team$collisionrule1 == Team.CollisionRule.NEVER) {
                    return false;
                } else {
                    boolean flag = team != null && team.isSameTeam(team1);

                    if ((team$collisionrule == Team.CollisionRule.HIDE_FOR_OTHER_TEAMS || team$collisionrule1 == Team.CollisionRule.HIDE_FOR_OTHER_TEAMS) && !flag) {
                        return false;
                    } else {
                        return team$collisionrule != Team.CollisionRule.HIDE_FOR_OWN_TEAM && team$collisionrule1 != Team.CollisionRule.HIDE_FOR_OWN_TEAM || flag;
                    }
                }
            } else {
                return false;
            }
        });
        return (Predicate<T>) ret;
    }
}
