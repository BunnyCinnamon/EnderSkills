package arekkuusu.enderskills.api.helper;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;

@SuppressWarnings({"Guava", "ConstantConditions", "unchecked"})
public class TeamHelper {

    public static final Function<Entity, Predicate<Entity>> SAME_TEAM = owner -> owner != null ? TeamHelper.getAllyTeamPredicate(owner) : Predicates.alwaysFalse();
    public static final Function<Entity, Predicate<Entity>> NOT_SAME_TEAM = owner -> owner != null ? TeamHelper.getEnemyTeamPredicate(owner) : Predicates.alwaysFalse();
    public static final Function<Entity, Predicate<Entity>> ALLY = owner -> (entity -> owner instanceof EntityPlayer
            ? (entity instanceof EntityPlayer ? EnderSkillsAPI.defaultHumanTeam : (!entity.isCreatureType(EnumCreatureType.MONSTER, false) && EnderSkillsAPI.defaultAnimalTeam))
            : (owner.isCreatureType(EnumCreatureType.MONSTER, false) == entity.isCreatureType(EnumCreatureType.MONSTER, false))
    );
    public static final Function<Entity, Predicate<Entity>> ENEMY = owner -> (entity -> owner instanceof EntityPlayer
            ? (entity instanceof EntityPlayer ? !EnderSkillsAPI.defaultHumanTeam : (entity.isCreatureType(EnumCreatureType.MONSTER, false) || !EnderSkillsAPI.defaultAnimalTeam))
            : (owner.isCreatureType(EnumCreatureType.MONSTER, false) != entity.isCreatureType(EnumCreatureType.MONSTER, false))
    );

    public static final Predicate<Entity> NOT_CREATIVE = entity -> !(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode || !((EntityPlayer) entity).capabilities.disableDamage;

    public static final Function<Entity, Predicate<Entity>> SELECTOR_ALLY = (owner) -> Predicates.or(Predicates.and(SAME_TEAM.apply(owner), NOT_CREATIVE), input -> input == owner);
    public static final Function<Entity, Predicate<Entity>> SELECTOR_ENEMY = (owner) -> Predicates.and(NOT_SAME_TEAM.apply(owner), NOT_CREATIVE, input -> input != owner);

    public static <T extends Entity> Predicate<T> getAllyTeamPredicate(Entity owner) {
        return Predicates.and(EntitySelectors.NOT_SPECTATING, Predicates.or(target -> target.isOnSameTeam(owner), ALLY.apply(owner)));
    }

    public static <T extends Entity> Predicate<T> getEnemyTeamPredicate(Entity owner) {
        return Predicates.and(EntitySelectors.NOT_SPECTATING, Predicates.and(target -> !target.isOnSameTeam(owner), ENEMY.apply(owner)));
    }
}
