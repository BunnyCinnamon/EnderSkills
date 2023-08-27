package arekkuusu.enderskills.api.configuration;

import arekkuusu.enderskills.api.EnderSkillsAPI;
import arekkuusu.enderskills.api.configuration.parser.Gradient;
import arekkuusu.enderskills.api.configuration.parser.Property;
import arekkuusu.enderskills.api.configuration.parser.Section;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Triple;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public final class DSLEvaluator {

    public static final Logger LOGGER = LogManager.getLogger(DSLEvaluator.class);

    public static int evaluateMinLevel(Skill skill) {
        DSLConfig config = EnderSkillsAPI.SKILL_DSL_CONFIG_MAP.get(skill.getRegistryName());

        return config.min_level;
    }

    public static int evaluateMaxLevel(Skill skill) {
        DSLConfig config = EnderSkillsAPI.SKILL_DSL_CONFIG_MAP.get(skill.getRegistryName());

        return config.max_level;
    }

    public static int evaluateLimitLevel(Skill skill) {
        DSLConfig config = EnderSkillsAPI.SKILL_DSL_CONFIG_MAP.get(skill.getRegistryName());

        return config.limit_level;
    }

    public static int evaluateInt(Skill skill, String name, int lvl, double effective) {
        return ((int) evaluateDouble(skill, name, lvl, effective));
    }

    public static float evaluateFloat(Skill skill, String name, int lvl, double effective) {
        return ((float) evaluateDouble(skill, name, lvl, effective));
    }

    public static double evaluateDouble(Skill skill, String name, int lvl, double effective) {
        DSLConfig config = EnderSkillsAPI.SKILL_DSL_CONFIG_MAP.get(skill.getRegistryName());
        Triple<ResourceLocation, String, Integer> key = new Triple<>(skill.getRegistryName(), name, lvl);
        if (EnderSkillsAPI.EXPRESSION_CACHE.asMap().containsKey(key)) {
            return EnderSkillsAPI.EXPRESSION_CACHE.asMap().get(key);
        }

        double result = 0D;
        try {
            Property property = config.map.get(name);
            Gradient gradient = property.gradient;
            double start = property.start;
            double end = property.end;
            int min = config.min_level;
            int max = config.max_level;
            List<Section> collect = new ArrayList<>(property.map.values());
            for (int i = collect.size() - 1; i >= 0; i--) {
                Section section = collect.get(i);
                end = section.start; //First block in list defines max for fallback
                if (section.clamp.apply(lvl)) {
                    gradient = section.gradient;
                    start = section.start;
                    end = section.end;
                    min = section.min;
                    max = section.max;
                    break;
                }
            }
            double clampLeft = Math.min(start, end);
            double clampRight = Math.max(end, start);
            result = gradient.get(min, max, start, end, lvl);
            result = MathHelper.clamp(result, clampLeft, clampRight);
            result *= effective;
        } catch (Exception e) {
            DSLEvaluator.LOGGER.error("Malformed `{}` config for property: {} with level: {}", skill.getRegistryName(), name, lvl);
            DSLEvaluator.LOGGER.error("It is possible it is missing the property with name {}", name);
            DSLEvaluator.LOGGER.error("Please add this on the enderkills/.../{}.cfg file", skill.getRegistryName());
            e.printStackTrace();
            result = 0D;
        } finally {
            EnderSkillsAPI.EXPRESSION_CACHE.put(key, result);
        }

        return result;
    }
}
