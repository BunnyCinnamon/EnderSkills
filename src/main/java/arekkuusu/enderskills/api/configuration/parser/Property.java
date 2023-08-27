package arekkuusu.enderskills.api.configuration.parser;

import com.google.common.collect.Maps;

import java.util.Map;

public class Property {
    public Gradient gradient;
    public double start;
    public double end;
    public Map<String, Section> map = Maps.newLinkedHashMap();
}
