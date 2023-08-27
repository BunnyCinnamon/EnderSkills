package arekkuusu.enderskills.api.configuration.parser;

import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.common.EnderSkills;
import com.google.common.collect.Queues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public final class DSLParser {

    public static final Logger LOGGER = LogManager.getLogger(DSLParser.class);

    @SuppressWarnings("UnnecessaryLabelOnContinueStatement")
    public static DSLConfig parse(String[] lines) {
        if(Arrays.stream(lines).anyMatch(s -> s.contains(Constants.FAKE_SPACE[0]))) {
            EnderSkills.LOG.warn("You are using an OLD config version, newest is `v1.0`");
        }

        DSLConfig config = new DSLConfig();
        Deque<Consumer<String>> filoContext = Queues.newArrayDeque();
        filoContext.add(findProperty(config, filoContext));

        loop:
        for (String s : lines) {
            String line = s.trim();
            for (Function<String, String> filter : Constants.FILTERS) {
                line = filter.apply(line);
                if (line.isEmpty())
                    continue loop;
            }

            if (line.startsWith(Constants.VERSION)) {
                continue loop;
            }
            if (line.startsWith(Constants.MIN_LEVEL)) {
                String string = line.substring(Constants.MIN_LEVEL.length());
                if (string.contains(Constants.INFINITE)) {
                    config.min_level = Integer.MAX_VALUE;
                } else {
                    config.min_level = Integer.parseInt(line.substring(Constants.MIN_LEVEL.length()));
                }
                continue loop;
            }
            if (line.startsWith(Constants.MAX_LEVEL)) {
                String string = line.substring(Constants.MAX_LEVEL.length());
                if (string.contains(Constants.INFINITE)) {
                    config.max_level = Integer.MAX_VALUE;
                } else {
                    config.max_level = Integer.parseInt(line.substring(Constants.MAX_LEVEL.length()));
                }
                continue loop;
            }
            if (line.startsWith(Constants.TOP_LEVEL)) {
                String string = line.substring(Constants.TOP_LEVEL.length());
                if (string.contains(Constants.INFINITE)) {
                    config.limit_level = Integer.MAX_VALUE;
                } else {
                    config.limit_level = Integer.parseInt(line.substring(Constants.TOP_LEVEL.length()));
                }
                continue loop;
            }

            if (!filoContext.isEmpty()) {
                filoContext.peekLast().accept(line);
            }
            if (line.startsWith(Constants.BLOCK_CLOSE)) {
                filoContext.pollLast();
            }
            if (line.startsWith(Constants.PROPERTY_CLOSE)) {
                filoContext.pollLast();
            }
        }

        return config;
    }

    public static Consumer<String> findProperty(DSLConfig config, Queue<Consumer<String>> contexts) {
        return line -> {
            if (line.endsWith(Constants.PROPERTY_OPEN)) {
                String propertyName = line.substring(0, line.indexOf(Constants.PROPERTY_OPEN)).trim();
                Property property = new Property();
                config.map.putIfAbsent(propertyName, property);

                contexts.add(findPropertyValues(config, property, contexts));
            }
        };
    }

    public static Consumer<String> findPropertyValues(DSLConfig config, Property property, Queue<Consumer<String>> contexts) {
        return line -> {
            if (line.startsWith(Constants.SHAPE)) {
                property.gradient = generateCurveFromString(property, line.substring(Constants.SHAPE.length()));
            }
            if (line.startsWith(Constants.START)) {
                property.start = parseDoubleFromString(property, line.substring(Constants.START.length()));
                property.end = property.start;
            }
            if (line.startsWith(Constants.MIN)) {
                property.start = parseDoubleFromString(property, line.substring(Constants.MIN.length()));
                property.end = property.start;
            }
            if (line.startsWith(Constants.END)) {
                property.end = parseDoubleFromString(property, line.substring(Constants.END.length()));
            }
            if (line.startsWith(Constants.MAX)) {
                property.end = parseDoubleFromString(property, line.substring(Constants.MAX.length()));
            }
            if (line.startsWith(Constants.VALUE)) {
                double value = parseDoubleFromString(property, line.substring(Constants.VALUE.length()));
                property.end = value;
                property.start = value;
            }
            if (line.endsWith(Constants.BLOCK_OPEN)) {
                String blockName = line.substring(0, line.indexOf(Constants.BLOCK_OPEN)).trim();
                String clampString = blockName.substring(1, blockName.length() - 1);
                String[] clampValueStrings = clampString.split("to");
                Section section = new Section();
                section.gradient = property.gradient;
                if (clampValueStrings[0].contains("%")) {
                    section.min = 0;
                } else {
                    section.min = Integer.parseInt(clampValueStrings[0].trim());
                }
                section.max = config.max_level;
                section.start = property.start;
                section.end = property.end;
                if (clampValueStrings[0].contains("%")) {
                    String percentage = clampValueStrings[0].trim();
                    double progress = Double.parseDouble(percentage.substring(0, percentage.indexOf(Constants.PERCENTAGE))) / 100D;
                    section.max = (int) Math.floor(progress * config.max_level);
                }
                if (clampValueStrings.length > 1) {
                    section.max = Integer.parseInt(clampValueStrings[1].trim());
                }
                section.clamp = (n) -> n >= section.min && n <= section.max;
                property.map.putIfAbsent(blockName, section);

                contexts.add(findBlockValues(config, property, section, contexts));
            }
        };
    }

    public static Consumer<String> findBlockValues(DSLConfig config, Property property, Section section, Queue<Consumer<String>> contexts) {
        return line -> {
            if (line.startsWith(Constants.SHAPE)) {
                section.gradient = generateCurveFromString(property, line.substring(Constants.SHAPE.length()));
            }
            if (line.startsWith(Constants.CURVE)) {
                section.gradient = generateCurveFromString(property, line.substring(Constants.CURVE.length()));
            }
            if (line.startsWith(Constants.START)) {
                section.start = parseDoubleFromString(property, line.substring(Constants.START.length()));
                section.end = section.start;
            }
            if (line.startsWith(Constants.END)) {
                section.end = parseDoubleFromString(property, line.substring(Constants.END.length()));
            }
            if (line.startsWith(Constants.RETURN)) {
                double value = parseDoubleFromString(property, line.substring(Constants.RETURN.length()));
                section.end = value;
                section.start = value;
            }
            if (line.startsWith(Constants.VALUE)) {
                double value = parseDoubleFromString(property, line.substring(Constants.VALUE.length()));
                section.end = value;
                section.start = value;
            }
        };
    }

    public static Gradient generateCurveFromString(Property property, String string) {
        String[] splits = string.split(" ");
        String[] values = Arrays.copyOfRange(splits, 1, splits.length);
        String type = splits[0];
        if (type.contains(Constants.REFERENCE)) {
            return property.gradient;
        }
        switch (type) {
            case "flat":
                return GradientsUtil.diagonalIncrement();
            case "ramp":
                return GradientsUtil.rampIncrement(property, values[0], values.length > 1 ? values[1] : "");
            case "curve":
                return GradientsUtil.curveIncrement(values);
            case "multiply":
                double amount = DSLParser.parseDoubleFromString(property, values[0]);
                return GradientsUtil.multiplesIncrement(amount);
            case "f(x,":
                String function = string.split("->")[0];
                return GradientsUtil.functionIncrement(function);
            case "solve":
                String impureFunction = string.split("for")[0];
                return GradientsUtil.functionIncrement(impureFunction);
            default:
                return GradientsUtil.clampToMinMax();
        }
    }

    public static double parseDoubleFromString(Property property, String string) {
        if (string.contains(Constants.PLACEHOLDER)) {
            return 0D;
        }
        if (string.contains(Constants.INFINITE)) {
            return Double.MAX_VALUE;
        }
        if (string.endsWith(Constants.PERCENTAGE)) {
            return Double.parseDouble(string.substring(0, string.indexOf(Constants.PERCENTAGE))) / 100D;
        }
        if (string.endsWith(Constants.SECONDS)) {
            return Double.parseDouble(string.substring(0, string.indexOf(Constants.SECONDS))) * 20D;
        }
        if (string.endsWith(Constants.BLOCKS)) {
            return Double.parseDouble(string.substring(0, string.indexOf(Constants.BLOCKS)));
        }
        if (string.endsWith(Constants.HEART)) {
            return Double.parseDouble(string.substring(0, string.indexOf(Constants.HEART))) * 2D;
        }
        if (string.endsWith(Constants.ENERGY)) {
            return Double.parseDouble(string.substring(0, string.indexOf(Constants.ENERGY)));
        }
        if (string.contains(Constants.REFERENCE)) {
            String trim = string.trim();
            String arg = trim.substring(1, trim.length() - 1);
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
