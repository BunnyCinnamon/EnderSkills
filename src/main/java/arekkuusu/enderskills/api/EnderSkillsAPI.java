package arekkuusu.enderskills.api;

import arekkuusu.enderskills.api.helper.ExpressionHelper;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import java.util.Map;
import java.util.function.Function;

public class EnderSkillsAPI {

    public static final Map<ResourceLocation, Object2ObjectMap<String, ExpressionHelper.FunctionInfo>> EXPRESSION_FUNCTION_CACHE = Maps.newHashMap();
    public static final Map<Tuple<ResourceLocation, String>, Int2DoubleArrayMap> EXPRESSION_CACHE = Maps.newHashMap();
    public static final Function<ResourceLocation, Object2ObjectArrayMap<String, ExpressionHelper.FunctionInfo>> EXPRESSION_FUNCTION_CACHE_SUPPLIER = (s) -> new Object2ObjectArrayMap<>();
    public static final Function<Tuple<ResourceLocation, String>, Int2DoubleArrayMap> EXPRESSION_CACHE_SUPPLIER = (s) -> new Int2DoubleArrayMap();
    public static boolean defaultHumanTeam = true;
}
