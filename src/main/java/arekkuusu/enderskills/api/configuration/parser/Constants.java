package arekkuusu.enderskills.api.configuration.parser;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Constants {

    public static final String[] FAKE_SPACE = {"⠀", "└", "┌", "│", "├"};
    public static final String COMMENT = "#";
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

    public static final String VERSION = "v1.0";
    public static final String MIN_LEVEL = "min_level: ";
    public static final String MAX_LEVEL = "max_level: ";
    public static final String TOP_LEVEL = "top_level: ";
    public static final String PROPERTY_OPEN = "(";
    public static final String BLOCK_OPEN = "[";
    public static final String SHAPE = "shape: ";
    public static final String CURVE = "curve: ";
    public static final String RAMP_POSITIVE = "positive";
    public static final String RAMP_NEGATIVE = "negative";
    public static final String VALUE = "value: ";
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
}
