package arekkuusu.enderskills.client.util.resource.shader;

import arekkuusu.enderskills.client.util.resource.ShaderManager;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ShaderResource {

    private ResourceLocation vsh;
    private ResourceLocation fsh;
    private int program;

    public ShaderResource(@Nullable String vsh, @Nullable String fsh) {
        if (vsh != null)
            this.vsh = new ResourceLocation(LibMod.MOD_ID, "shaders/program/" + vsh + ".vsh");/*ResourceLibrary.getLocation(AssetLocation.SHADERS, ShaderLocation.PROGRAM, vsh, ".vsh");*/
        if (fsh != null)
            this.fsh = new ResourceLocation(LibMod.MOD_ID, "shaders/program/" + fsh + ".fsh");/*ResourceLibrary.getLocation(AssetLocation.SHADERS, ShaderLocation.PROGRAM, fsh, ".fsh");*/
        this.reload(Minecraft.getMinecraft().getResourceManager());
    }

    public boolean begin() {
        if (program != 0 && !ShaderManager.isReloading()) {
            OpenGlHelper.glUseProgram(program);
            return true;
        }
        return false;
    }

    public void set(String name, float val) {
        if (!ShaderManager.isReloading()) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(1);
            buffer.position(0);
            buffer.put(0, val);
            int in = OpenGlHelper.glGetUniformLocation(program, name);
            OpenGlHelper.glUniform1(in, buffer);
        }
    }

    public void set(String name, float val0, float val1) {
        if (!ShaderManager.isReloading()) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(2);
            buffer.position(0);
            buffer.put(0, val0);
            buffer.put(1, val1);
            int in = OpenGlHelper.glGetUniformLocation(program, name);
            OpenGlHelper.glUniform2(in, buffer);
        }
    }

    public void set(String name, float val0, float val1, float val2) {
        if (!ShaderManager.isReloading()) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
            buffer.position(0);
            buffer.put(0, val0);
            buffer.put(1, val1);
            buffer.put(2, val2);
            int in = OpenGlHelper.glGetUniformLocation(program, name);
            OpenGlHelper.glUniform3(in, buffer);
        }
    }

    public void set(String name, float val0, float val1, float val2, float val3) {
        if (!ShaderManager.isReloading()) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
            buffer.position(0);
            buffer.put(0, val0);
            buffer.put(1, val1);
            buffer.put(2, val2);
            buffer.put(3, val3);
            int in = OpenGlHelper.glGetUniformLocation(program, name);
            OpenGlHelper.glUniform4(in, buffer);
        }
    }

    public void set(String name, int val) {
        if (!ShaderManager.isReloading()) {
            IntBuffer buffer = BufferUtils.createIntBuffer(1);
            buffer.position(0);
            buffer.put(0, val);
            int in = OpenGlHelper.glGetUniformLocation(program, name);
            OpenGlHelper.glUniform1(in, buffer);
        }
    }

    public void end() {
        OpenGlHelper.glUseProgram(0);
    }

    public void reload(IResourceManager manager) {
        this.program = ShaderManager.create(manager, vsh, fsh);
    }
}
