package arekkuusu.enderskills.api.helper;

import arekkuusu.enderskills.api.registry.Skill;
import com.expression.parser.Parser;
import com.expression.parser.util.ParserResult;
import com.expression.parser.util.Point;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionHelper {

    public static Map<ResourceLocation, Object2ObjectMap<String, FunctionInfo>> FUNCTION_CACHE = Maps.newHashMap();
    public static Map<Tuple<ResourceLocation, String>, Int2DoubleArrayMap> EXPRESSION_CACHE = Maps.newHashMap();
    public static Function<ResourceLocation, Object2ObjectArrayMap<String, FunctionInfo>> MAP_CONDITION_SUPPLIER = (s) -> new Object2ObjectArrayMap<>();
    public static Function<Tuple<ResourceLocation, String>, Int2DoubleArrayMap> MAP_DOUBLE_SUPPLIER = (s) -> new Int2DoubleArrayMap();
    public static Function<String, FunctionInfo> CONDITION_SUPPLIER = ExpressionHelper::parse;
    public static String REGEX = "^\\((.+)\\)\\{(.+)\\}$";

    public static double getExpression(Skill skill, String function, int min, int max) {
        return getExpression(skill.getRegistryName(), function, min, max);
    }

    public static double getExpression(Skill skill, String[] functionArray, int min, int max) {
        return getExpression(skill.getRegistryName(), functionArray, min, max);
    }

    public static double getExpression(ResourceLocation location, String function, int min, int max) {
        Int2DoubleArrayMap map = EXPRESSION_CACHE.computeIfAbsent(new Tuple<>(location, function), MAP_DOUBLE_SUPPLIER);
        if (!map.containsKey(min)) {
            final Point x = new Point("x", String.valueOf(min));
            final Point y = new Point("y", String.valueOf(max));
            ParserResult result = Parser.eval(function, x, y);
            map.put(min, result.getValue().doubleValue());
        }
        return map.get(min);
    }

    public static double getExpression(ResourceLocation location, String[] functionArray, int min, int max) {
        Object2ObjectMap<String, FunctionInfo> map = FUNCTION_CACHE.computeIfAbsent(location, MAP_CONDITION_SUPPLIER);
        FunctionInfo match = null;
        for (String s : functionArray) {
            FunctionInfo info = map.computeIfAbsent(s, CONDITION_SUPPLIER);
            if (info.matches(min)) {
                match = info;
            }
        }
        return match != null ? ExpressionHelper.getExpression(location, match.function, min, max) : 0;
    }

    public static FunctionInfo parse(String string) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(string.trim());
        if (matcher.matches()) {
            String condition = matcher.group(1).trim().replace(" ", "");
            String function = matcher.group(2).trim();
            if (condition.endsWith("+")) {
                int min = Integer.parseInt(condition.substring(0, condition.length() - 1));
                return new FunctionInfo(FunctionInfo.Condition.PlusInfinite, function, min);
            } else if (condition.startsWith("-")) {
                int min = Integer.parseInt(condition.substring(1));
                return new FunctionInfo(FunctionInfo.Condition.MinusInfinite, function, min);
            } else {
                int index = condition.indexOf('-');
                int min = Integer.parseInt(condition.substring(0, index));
                int max = Integer.parseInt(condition.substring(index));
                return new FunctionInfo(FunctionInfo.Condition.Between, function, min, max);
            }
        } else {
            throw new IllegalStateException("[ExpressionHelper] - Expression " + string + " is not valid");
        }
    }

    public static class FunctionInfo {

        public Condition condition;
        public String function;
        public int min;
        public int max;

        public FunctionInfo(Condition condition, String function, int min, int max) {
            this.condition = condition;
            this.function = function;
            this.min = min;
            this.max = max;
        }

        public FunctionInfo(Condition condition, String function, int min) {
            this.condition = condition;
            this.function = function;
            this.min = min;
            this.max = -1;
        }

        public boolean matches(int level) {
            return condition.test(level, this.min, this.max);
        }

        public enum Condition {
            PlusInfinite {
                @Override
                boolean test(int level, int levelMin, int levelMax) {
                    return level >= levelMin;
                }
            },
            MinusInfinite {
                @Override
                boolean test(int level, int levelMin, int levelMax) {
                    return level <= levelMin;
                }
            },
            Between {
                @Override
                boolean test(int level, int levelMin, int levelMax) {
                    return level >= levelMin && level <= levelMax;
                }
            };

            abstract boolean test(int level, int levelMin, int levelMax);
        }
    }
}
