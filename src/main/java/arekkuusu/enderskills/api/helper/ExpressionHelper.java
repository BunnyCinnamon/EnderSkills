package arekkuusu.enderskills.api.helper;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.util.Pair;
import arekkuusu.enderskills.api.util.Triple;
import com.expression.parser.Parser;
import com.expression.parser.util.ParserResult;
import com.expression.parser.util.Point;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionHelper {

    public static final String EXPRESSION_REGEX = "^\\(([\\+\\-\\d]+)\\)\\{(.+)\\}$";

    public static double getExpression(ResourceLocation location, String function, int min, int max, int level) {
        Triple<ResourceLocation, String, Integer> key = new Triple<>(location, function, level);
        if (min > max) min = max;
        if (!EnderSkillsAPI.EXPRESSION_CACHE.asMap().containsKey(key)) {
            List<Point> points = new ArrayList<>();
            points.add(new Point("x", String.valueOf(min)));
            points.add(new Point("y", String.valueOf(max)));
            points.add(new Point("l", String.valueOf(level)));
            ParserResult result = Parser.eval(function, points.toArray(new Point[]{}));
            EnderSkillsAPI.EXPRESSION_CACHE.put(key, result.getValue());
        }
        return EnderSkillsAPI.EXPRESSION_CACHE.asMap().get(key);
    }

    public static double getExpression(ResourceLocation location, String function, int min, int max) {
        Triple<ResourceLocation, String, Integer> key = new Triple<>(location, function, min);
        if (min > max) min = max;
        if (!EnderSkillsAPI.EXPRESSION_CACHE.asMap().containsKey(key)) {
            Point x = new Point("x", String.valueOf(min));
            Point y = new Point("y", String.valueOf(max));
            ParserResult result = Parser.eval(function, x, y);
            EnderSkillsAPI.EXPRESSION_CACHE.put(key, result.getValue());
        }
        return EnderSkillsAPI.EXPRESSION_CACHE.asMap().get(key);
    }

    public static double getExpression(ResourceLocation location, String[] functionArray, int min, int max) {
        FunctionInfo match = null;
        FunctionInfo temp = null;
        if (min > max) min = max;
        for (String function : functionArray) {
            Pair<ResourceLocation, String> key = new Pair<>(location, function);
            FunctionInfo info = EnderSkillsAPI.EXPRESSION_FUNCTION_CACHE.asMap().computeIfAbsent(key, ignored -> parse(function));
            if (info.matches(min) && isNotOverride(temp, info, min)) {
                match = info;
            }
            temp = info;
        }
        return match != null ? ExpressionHelper.getExpression(location, match.function, min, max) : 0;
    }

    public static boolean isNotOverride(@Nullable FunctionInfo prev, FunctionInfo next, int min) {
        return prev == null || next.condition != FunctionInfo.Condition.MinusInfinite
                || (prev.condition == FunctionInfo.Condition.PlusInfinite && min > prev.min)
                || (prev.condition == FunctionInfo.Condition.Between && min > prev.max)
                || (prev.condition == FunctionInfo.Condition.MinusInfinite && min > prev.min);
    }

    public static FunctionInfo parse(String string) {
        Pattern pattern = Pattern.compile(ExpressionHelper.EXPRESSION_REGEX);
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
            } else if (condition.contains("-")) {
                int index = condition.indexOf('-');
                int min = Integer.parseInt(condition.substring(0, index));
                int max = Integer.parseInt(condition.substring(index + 1));
                return new FunctionInfo(FunctionInfo.Condition.Between, function, min, max);
            } else {
                int min = Integer.parseInt(condition);
                return new FunctionInfo(FunctionInfo.Condition.Equal, function, min);
            }
        } else {
            throw new IllegalStateException("[ExpressionHelper] - Expression " + string + " is not valid, might be missing a { or }");
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
            },
            Equal {
                @Override
                boolean test(int level, int levelMin, int levelMax) {
                    return level == levelMin;
                }
            };

            abstract boolean test(int level, int levelMin, int levelMax);
        }
    }
}
