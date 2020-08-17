package arekkuusu.enderskills.client.util;

import arekkuusu.enderskills.client.util.resource.ShaderManager;
import arekkuusu.enderskills.client.util.resource.shader.ShaderResource;

public class ShaderLibrary {

    public static final ShaderResource ALPHA = ShaderManager.load("alpha", "alpha");
    public static final ShaderResource GRAY_SCALE = ShaderManager.load("gray_scale", "gray_scale");
    public static final ShaderResource BLEED = ShaderManager.load("bleed", "bleed");
    public static final ShaderResource UNIVERSE = ShaderManager.load("universe", "universe");
    public static final ShaderResource UNIVERSE_DEFAULT = ShaderManager.load("universe_default", "universe_default");

    public static void init() {
    }
}
