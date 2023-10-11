package arekkuusu.enderskills.api.configuration;

import arekkuusu.enderskills.api.configuration.parser.Property;
import com.google.common.collect.Maps;

import java.util.Map;

public class DSLConfig {
    public int min_level, max_level, limit_level;
    public Map<String, Property> map = Maps.newLinkedHashMap();
}
