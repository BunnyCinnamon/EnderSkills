package arekkuusu.enderskills.api.util;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.EnderSkills;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ConfigDSL {

    public static final Logger LOGGER = LogManager.getLogger(ConfigDSL.class);

    //Definitions
    public static final String[] FAKE_SPACE = {"⠀","└","┌","│","├"};
    public static final String COMMENT = "#";
    public static final String MIN_LEVEL = "min_level: ";
    public static final String MAX_LEVEL = "max_level: ";
    public static final String PROPERTY_OPEN = "(";
    public static final String BLOCK_OPEN = "[";
    public static final String SHAPE = "shape: ";
    @Deprecated public static final String CURVE = "curve: ";
    public static final String RAMP_POSITIVE = "ramp positive";
    public static final String RAMP_NEGATIVE = "ramp negative";
    @Deprecated public static final String VALUE = "value: ";
    public static final String RETURN = "return: ";
    public static final String START = "start: ";
    public static final String MIN = "min: ";
    public static final String END = "end: ";
    public static final String MAX = "max: ";
    public static final String BLOCK_CLOSE = "]";
    public static final String PROPERTY_CLOSE = ")";
    //Types
    public static final String PERCENTAGE = "%";
    public static final String SECONDS = "s";
    public static final String BLOCKS = "b";
    public static final String HEART = "h";
    public static final String ENERGY = "e";
    public static final String REFERENCE = "{";
    public static final String INFINITE = "infinite";
    public static final String PLACEHOLDER = "PLACEHOLDER";

    public static final List<Function<String, String>> FILTERS = Lists.newArrayList(
            (s) -> s.replace(FAKE_SPACE[0], ""),
            (s) -> s.replace(FAKE_SPACE[1], ""),
            (s) -> s.replace(FAKE_SPACE[2], ""),
            (s) -> s.replace(FAKE_SPACE[3], ""),
            (s) -> s.replace(FAKE_SPACE[4], ""),
            (s) -> s.trim(),
            (s) -> s.startsWith(COMMENT) ? "" : s,
            (s) -> Optional.of(s.indexOf(COMMENT)).filter(n -> n > 0).map(n -> s.substring(0, n)).orElse(s)
    );

    public static Consumer<String> findBlockValues(Config config, Property property, Block block, Queue<Consumer<String>> contexts) {
        return line -> {
            if (line.startsWith(SHAPE)) {
                block.curve = generateCurveFromString(property, line.substring(SHAPE.length()));
            }
            if (line.startsWith(CURVE)) {
                block.curve = generateCurveFromString(property, line.substring(CURVE.length()));
            }
            if (line.startsWith(START)) {
                block.start = parseDoubleFromString(property, line.substring(START.length()));
                block.end = block.start;
            }
            if (line.startsWith(END)) {
                block.end = parseDoubleFromString(property, line.substring(END.length()));
            }
            if (line.startsWith(RETURN)) {
                double value = parseDoubleFromString(property, line.substring(RETURN.length()));
                block.end = value;
                block.start = value;
            }
            if (line.startsWith(VALUE)) {
                double value = parseDoubleFromString(property, line.substring(VALUE.length()));
                block.end = value;
                block.start = value;
            }
        };
    }

    public static Consumer<String> findPropertyValues(Config config, Property property, Queue<Consumer<String>> contexts) {
        return line -> {
            if (line.startsWith(SHAPE)) {
                property.curve = generateCurveFromString(property, line.substring(SHAPE.length()));
            }
            if (line.startsWith(START)) {
                property.start = parseDoubleFromString(property, line.substring(START.length()));
                property.end = property.start;
            }
            if (line.startsWith(MIN)) {
                property.start = parseDoubleFromString(property, line.substring(MIN.length()));
                property.end = property.start;
            }
            if (line.startsWith(END)) {
                property.end = parseDoubleFromString(property, line.substring(END.length()));
            }
            if (line.startsWith(MAX)) {
                property.end = parseDoubleFromString(property, line.substring(MAX.length()));
            }
            if (line.startsWith(VALUE)) {
                double value = parseDoubleFromString(property, line.substring(VALUE.length()));
                property.end = value;
                property.start = value;
            }
            if (line.endsWith(BLOCK_OPEN)) {
                String blockName = line.substring(0, line.indexOf(BLOCK_OPEN)).trim();
                String clampString = blockName.substring(1, blockName.length() - 1);
                String[] clampValueStrings = clampString.split("to");
                Block block = new Block();
                block.curve = property.curve;
                block.min = Integer.parseInt(clampValueStrings[0].trim());
                block.max = config.max_level;
                block.start = property.start;
                block.end = property.end;
                if (clampValueStrings.length > 1) {
                    block.max = Integer.parseInt(clampValueStrings[1].trim());
                }
                block.clamp = (n) -> n >= block.min && n <= block.max;
                property.map.putIfAbsent(blockName, block);

                contexts.add(findBlockValues(config, property, block, contexts));
            }
        };
    }

    public static Consumer<String> findProperty(Config config, Queue<Consumer<String>> contexts) {
        return line -> {
            if (line.endsWith(PROPERTY_OPEN)) {
                String propertyName = line.substring(0, line.indexOf(PROPERTY_OPEN)).trim();
                Property property = new Property();
                config.map.putIfAbsent(propertyName, property);

                contexts.add(findPropertyValues(config, property, contexts));
            }
        };
    }

    @SuppressWarnings("UnnecessaryLabelOnContinueStatement") //????????????????????????????????????????????????????????
    public static Config parse(String[] lines) {
        if(Arrays.stream(lines).anyMatch(s -> s.contains(FAKE_SPACE[0]))) {
            EnderSkills.LOG.warn("You are using an OLD version config, newest is `v1.0` you are `v0.0`");
        }

        Config config = new Config();
        Deque<Consumer<String>> filoContext = Queues.newArrayDeque();
        filoContext.add(findProperty(config, filoContext));

        loop:
        for (String s : lines) {
            String line = s.trim();
            for (Function<String, String> filter : FILTERS) {
                line = filter.apply(line);
                if (line.isEmpty())
                    continue loop;
            }

            if (line.startsWith(MIN_LEVEL)) {
                String string = line.substring(MIN_LEVEL.length());
                if (string.contains(INFINITE)) {
                    config.min_level = Integer.MAX_VALUE;
                } else {
                    config.min_level = Integer.parseInt(line.substring(MIN_LEVEL.length()));
                }
                continue loop;
            }
            if (line.startsWith(MAX_LEVEL)) {
                String string = line.substring(MAX_LEVEL.length());
                if (string.contains(INFINITE)) {
                    config.max_level = Integer.MAX_VALUE;
                } else {
                    config.max_level = Integer.parseInt(line.substring(MAX_LEVEL.length()));
                }
                continue loop;
            }

            if (!filoContext.isEmpty()) {
                filoContext.peekLast().accept(line);
            }
            if (line.startsWith(BLOCK_CLOSE)) {
                filoContext.pollLast();
            }
            if (line.startsWith(PROPERTY_CLOSE)) {
                filoContext.pollLast();
            }
        }

        return config;
    }

    public static class Config {
        public int min_level, max_level;
        public Map<String, Property> map = Maps.newHashMap();

        public double get(Skill skill, String name, int lvl) {
            return get(skill, name, lvl, 1D);
        }

        public double get(Skill skill, String name, int lvl, double effective) {
            Int2DoubleArrayMap cache = EnderSkillsAPI.EXPRESSION_CACHE.asMap().computeIfAbsent(new Tuple<>(skill.getRegistryName(), name), ExpressionHelper.EXPRESSION_CACHE_SUPPLIER);
            if (cache.containsKey(lvl)) {
                return cache.get(lvl);
            }

            double result = 0D;
            try {
                Property property = map.get(name);
                Curve curve = property.curve;
                double start = property.start;
                double end = property.end;
                int min = min_level;
                int max = max_level;
                List<Block> collect = new ArrayList<>(property.map.values());
                for (int i = collect.size() - 1; i >= 0; i--) {
                    Block block = collect.get(i);
                    end = block.start; //First block in list defines max for fallback
                    if (block.clamp.apply(lvl)) {
                        curve = block.curve;
                        start = block.start;
                        end = block.end;
                        min = block.min;
                        max = block.max;
                        break;
                    }
                }
                double clampLeft = Math.min(start, end);
                double clampRight = Math.max(end, start);
                result = curve.get(min, max, start, end, lvl);
                result *= effective;
                result = MathHelper.clamp(result, clampLeft, clampRight);
            } catch (Exception e) {
                LOGGER.error("Malformed `{}` config for property: {} with level: {}", skill.getRegistryName(), name, lvl);
                LOGGER.error("It is possible it is missing the property with name {}", name);
                LOGGER.error("Please add this on the enderkills/.../{}.cfg file", skill.getRegistryName());
                e.printStackTrace();
                result = 0D;
            } finally {
                cache.put(lvl, result);
            }

            return result;
        }
    }

    public static class Property {
        public Curve curve;
        public double start;
        public double end;
        public Map<String, Block> map = Maps.newLinkedHashMap();
    }

    public static class Block {
        public Clamp clamp;
        public Curve curve;
        public int min;
        public int max;
        public double start;
        public double end;
    }

    public interface Clamp {
        boolean apply(int n);
    }

    public interface Curve {
        double get(int min, int max, double start, double end, int n);
    }

    public static final Curve CURVE_NONE = (min, max, start, end, n) -> {
        return n == max ? end : start;
    };
    public static final Curve CURVE_FLAT = (min, max, start, end, n) -> {
        double current = MathHelper.clamp(n, min, max);
        double difference = end - start;
        double progress = current / max;
        return start + difference * progress;
    };
    public static final BiFunction<Property, String[], Curve> CURVE_RAMP = (property, strings) -> {
        double strength = 0D;
        double position = 0.5D;
        switch (strings[0]) {
            case RAMP_POSITIVE:
                strength = 0.5D;
                break;
            case RAMP_NEGATIVE:
                strength = -0.5D;
                break;
            default:
                strength = parseDoubleFromString(property, strings[0]);
                position = parseDoubleFromString(property, strings[1]);
        }
        final double finalStrength = strength;
        final double finalPosition = position;
        return (min, max, start, end, n) -> {
            double current = MathHelper.clamp(n, min, max);
            double difference = end - start;
            double progress = (current - min) / (max - min);
            if (progress <= finalPosition) {
                double diff = progress / finalPosition;
                double mod = 1D + finalStrength * diff;
                progress *= mod;
            } else {
                double diff = 1D - (progress - finalPosition);
                double mod = 1D + finalStrength * diff;
                progress *= mod;
            }
            return start + difference * progress;
        };
    };
    public static final BiFunction<Property, String[], Curve> CURVE_MULTIPLY = (property, strings) -> {
        double number = parseDoubleFromString(property, strings[0]);
        return (min, max, start, end, n) -> {
            return n * number;
        };
    };
    public static final Function<String[], Curve> CURVE_FUNCTION = strings -> {
        String function = strings[0];
        return (min, max, start, end, n) -> {
            String pureFunction = function
                    .replace("{min}", "x")
                    .replace("{max}", "y")
                    .replace("{level}", "l");
            return ExpressionHelper.getExpression(new ResourceLocation("dsl"), pureFunction, min, max, n);
        };
    };

    public static Curve generateCurveFromString(Property property, String string) {
        String[] splits = string.split(" ");
        if (splits[0].contains(REFERENCE)) {
            return property.curve;
        }
        switch (splits[0]) {
            case "flat":
                return CURVE_FLAT;
            case "ramp":
                return CURVE_RAMP.apply(property, Arrays.copyOfRange(splits, 1, splits.length));
            case "multiply":
                return CURVE_MULTIPLY.apply(property, Arrays.copyOfRange(splits, 1, splits.length));
            case "f(x,":
                String[] function = string.split("->");
                return CURVE_FUNCTION.apply(Arrays.copyOfRange(function, 1, splits.length));
            case "solve":
                String[] impureFunction = string.split("for");
                return CURVE_FUNCTION.apply(Arrays.copyOfRange(impureFunction, 1, splits.length));
            default:
                return CURVE_NONE;
        }
    }

    public static double parseDoubleFromString(Property property, String string) {
        if (string.contains(PLACEHOLDER)) {
            return 0D;
        }
        if (string.contains(INFINITE)) {
            return Double.MAX_VALUE;
        }
        if (string.endsWith(PERCENTAGE)) {
            return Double.parseDouble(string.substring(0, string.indexOf(PERCENTAGE))) / 100D;
        }
        if (string.endsWith(SECONDS)) {
            return Double.parseDouble(string.substring(0, string.indexOf(SECONDS))) * 20D;
        }
        if (string.endsWith(BLOCKS)) {
            return Double.parseDouble(string.substring(0, string.indexOf(BLOCKS)));
        }
        if (string.endsWith(HEART)) {
            return Double.parseDouble(string.substring(0, string.indexOf(HEART))) * 2D;
        }
        if (string.endsWith(ENERGY)) {
            return Double.parseDouble(string.substring(0, string.indexOf(ENERGY)));
        }
        if (string.contains(REFERENCE)) {
            String arg = string.substring(1, string.length() - 1);
            switch (arg) {
                case "min":
                    return property.start;
                case "max":
                    return property.end;
                default:
                    return property.map.get(string).end;
            }
        }
        return Double.parseDouble(string);
    }
}
