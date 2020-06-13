package arekkuusu.enderskills.api.helper;

import arekkuusu.enderskills.api.registry.Skill;
import com.expression.parser.Parser;
import com.expression.parser.util.ParserResult;
import com.expression.parser.util.Point;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import net.minecraft.util.Tuple;

import java.util.Map;
import java.util.function.Function;

public class ExpressionHelper {

    public static Map<Tuple<Skill, String>, Int2DoubleArrayMap> EXPRESSION_CACHE = Maps.newHashMap();
    public static Function<Tuple<Skill, String>, Int2DoubleArrayMap> MAP_SUPPLIER = (s) -> new Int2DoubleArrayMap();

    public static double getExpression(Skill skill, String function, int min, int max) {
        Int2DoubleArrayMap map = EXPRESSION_CACHE.computeIfAbsent(new Tuple<>(skill, function), MAP_SUPPLIER);
        if (!map.containsKey(min)) {
            final Point x = new Point("x", String.valueOf(min));
            final Point y = new Point("y", String.valueOf(max));
            ParserResult result = Parser.eval(function, x, y);
            map.put(min, result.getValue().doubleValue());
        }
        return map.get(min);
    }
}
