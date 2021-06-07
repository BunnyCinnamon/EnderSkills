package arekkuusu.enderskills.api;

import arekkuusu.enderskills.api.helper.ExpressionHelper;
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

    public static final Cache<ResourceLocation, Object2ObjectMap<String, ExpressionHelper.FunctionInfo>> EXPRESSION_FUNCTION_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    public static final Cache<Tuple<ResourceLocation, String>, Int2DoubleArrayMap> EXPRESSION_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    public static boolean defaultHumanTeam = true;
    public static boolean defaultAnimalTeam = false;
}
