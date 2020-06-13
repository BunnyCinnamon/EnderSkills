package arekkuusu.enderskills.client.util.helper;

import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SideOnly(Side.CLIENT)
public class TextHelper {

    public static final Pattern PATTERN = Pattern.compile("(.+) \\S+");

    public static String translate(String key, Object... args) {
        return new TextComponentTranslation("ui." + LibMod.MOD_ID + "." + key, args).getFormattedText();
    }

    public static String format2FloatPoint(double number) {
        String pattern = "####0.0#";
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ROOT);
        decimalFormat.applyPattern(pattern);
        String formatted = decimalFormat.format(number);
        return formatted.replaceAll("(\\.[0])$", "");
    }

    public static List<String> findOptimalLines(Minecraft mc, String p_192995_1_, int p_192995_2_) {
        if (p_192995_1_.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<String> list = mc.fontRenderer.listFormattedStringToWidth(p_192995_1_, p_192995_2_);

            if (list.size() < 2) {
                return list;
            } else {
                String s = list.get(0);
                String s1 = list.get(1);
                int i = mc.fontRenderer.getStringWidth(s + ' ' + s1.split(" ")[0]);

                if (i - p_192995_2_ <= 10) {
                    return mc.fontRenderer.listFormattedStringToWidth(p_192995_1_, i);
                } else {
                    Matcher matcher = PATTERN.matcher(s);

                    if (matcher.matches()) {
                        int j = mc.fontRenderer.getStringWidth(matcher.group(1));

                        if (p_192995_2_ - j <= 10) {
                            return mc.fontRenderer.listFormattedStringToWidth(p_192995_1_, j);
                        }
                    }

                    return list;
                }
            }
        }
    }
}
