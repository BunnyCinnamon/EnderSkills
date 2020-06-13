package arekkuusu.enderskills.common.handler;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;

import java.util.function.BooleanSupplier;

public class EnableTechRecipes implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext jsonContext, JsonObject jsonObject) {
        return () -> (OreDictionary.doesOreNameExist("dustGold")) == JsonUtils.getBoolean(jsonObject, "value");
    }
}