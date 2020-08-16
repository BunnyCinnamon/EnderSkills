package arekkuusu.enderskills.api.helper;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EntitySelectors;

@SuppressWarnings({"Guava", "ConstantConditions", "unchecked"})
public class TeamHelper {

    public static final Function<Entity, Predicate<Entity>> SAME_TEAM = e -> e != null ? TeamHelper.getAllyTeamPredicate(e) : Predicates.alwaysFalse();
    public static final Function<Entity, Predicate<Entity>> NOT_SAME_TEAM = e -> e != null ? TeamHelper.getEnemyTeamPredicate(e) : Predicates.alwaysFalse();
    public static final Predicate<Entity> NOT_CREATIVE = entity -> !(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode || !((EntityPlayer) entity).capabilities.disableDamage;
    public static final Predicate<Entity> HUMAN_TEAM = entity -> !EnderSkillsAPI.defaultHumanTeam || !entity.isCreatureType(EnumCreatureType.MONSTER, false);
    public static final Predicate<Entity> NOT_HUMAN_TEAM = entity -> !EnderSkillsAPI.defaultHumanTeam || entity.isCreatureType(EnumCreatureType.MONSTER, false);
    public static final Function<Entity, Predicate<Entity>> SELECTOR_ALLY = (e) -> Predicates.or(Predicates.and(SAME_TEAM.apply(e), NOT_CREATIVE, HUMAN_TEAM), input -> input == e);
    public static final Function<Entity, Predicate<Entity>> SELECTOR_ENEMY = (e) -> Predicates.and(NOT_SAME_TEAM.apply(e), NOT_CREATIVE, NOT_HUMAN_TEAM, input -> input != e);

    public static <T extends Entity> Predicate<T> getAllyTeamPredicate(Entity owner) {
        Team ownerTeam = owner.getTeam();
        //Make sure the collision rule fails if not from same team!
        return ownerTeam == null ? Predicates.alwaysTrue() : Predicates.and(EntitySelectors.NOT_SPECTATING, target -> target.isOnSameTeam(owner));
    }

    public static <T extends Entity> Predicate<T> getEnemyTeamPredicate(Entity owner) {
        Team ownerTeam = owner.getTeam();
        //Make sure the collision rule fails if from same team!
        return ownerTeam == null ? Predicates.alwaysTrue() : Predicates.and(EntitySelectors.NOT_SPECTATING, target -> !target.isOnSameTeam(owner));
    }
}
