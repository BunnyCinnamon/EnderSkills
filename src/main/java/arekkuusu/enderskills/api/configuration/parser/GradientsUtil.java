package arekkuusu.enderskills.api.configuration.parser;

import arekkuusu.enderskills.api.helper.ExpressionHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class GradientsUtil {

    public static Gradient clampToMinMax() {
        return (min, max, start, end, n) -> n == max ? end : start;
    }

    public static Gradient diagonalIncrement() {
        return (min, max, start, end, n) -> {
            double current = MathHelper.clamp(n, min, max);
            double difference = end - start;
            double progress = current / max;
            return start + difference * progress;
        };
    }

    public static Gradient rampIncrement(Property property, String left, String right) {
        double strength = 0D;
        double position = 0.5D;
        switch (left) {
            case Constants.RAMP_POSITIVE:
                strength = 0.5D;
                break;
            case Constants.RAMP_NEGATIVE:
                strength = -0.5D;
                break;
            default:
                strength = DSLParser.parseDoubleFromString(property, left);
                position = DSLParser.parseDoubleFromString(property, right);
        }
        final double finalStrength = strength;
        final double finalPosition = position;

        return (min, max, start, end, n) -> {
            double current = MathHelper.clamp(n, min, max);
            double difference = end - start;
            double progress = (current - min) / (max - min);
            if (progress <= finalPosition) {
                double diff = progress / finalPosition;
                double mod = 1D + finalStrength * diff;
                progress *= mod;
            } else {
                double diff = 1D - (progress - finalPosition);
                double mod = 1D + finalStrength * diff;
                progress *= mod;
            }
            return start + difference * progress;
        };
    }

    public static Gradient curveIncrement(String[] strings) {
        double modifier = Double.parseDouble(strings[1]);
        boolean inverse = true;
        switch (strings[0]) {
            case Constants.RAMP_POSITIVE:
                inverse = false;
                break;
            case Constants.RAMP_NEGATIVE:
                break;
        }
        final double finalModifier = modifier;
        final boolean finalInverse = inverse;

        return (min, max, start, end, n) -> {
            double current = MathHelper.clamp(n, min, max);
            double difference = end - start;
            double progress = (current - min) / (max - min);
            double curvature = ((Math.exp(finalModifier * progress) - 1D) / (Math.exp(modifier) - 1D));
            return start + ((finalInverse ? (1 - curvature) : (curvature)) * difference);
        };
    }

    public static Gradient multiplesIncrement(double amount) {
        return (min, max, start, end, n) -> n * amount;
    }

    public static Gradient functionIncrement(String function) {
        return (min, max, start, end, n) -> {
            String pureFunction = function
                    .replaceAll("\\{min\\}", "x")
                    .replaceAll("\\{max\\}", "y")
                    .replaceAll("\\{level\\}", "l")
                    .replaceAll("([\\d]+)%", "($1/100)");
            return ExpressionHelper.getExpression(new ResourceLocation("dsl"), pureFunction, min, max, n);
        };
    }
}
