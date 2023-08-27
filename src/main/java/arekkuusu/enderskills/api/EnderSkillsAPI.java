package arekkuusu.enderskills.api;

import arekkuusu.enderskills.api.configuration.DSLConfig;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.ExpressionHelper.FunctionInfo;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.api.util.Pair;
import arekkuusu.enderskills.api.util.Triple;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class EnderSkillsAPI {

    public static final Cache<Pair<ResourceLocation, String>, FunctionInfo> EXPRESSION_FUNCTION_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    public static final Cache<Triple<ResourceLocation, String, Integer>, Double> EXPRESSION_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    public static final Map<ResourceLocation, DSLConfig> SKILL_DSL_CONFIG_MAP = Maps.newConcurrentMap();

    public static boolean defaultHumanTeam = true;
    public static boolean defaultAnimalTeam = false;
}
