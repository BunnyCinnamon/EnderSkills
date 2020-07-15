package arekkuusu.enderskills.client.util.resource;

import arekkuusu.enderskills.client.util.resource.shader.ShaderResource;
import arekkuusu.enderskills.common.EnderSkills;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class ShaderManager implements IResourceManagerReloadListener {

    private static final List<ShaderResource> SHADER_RESOURCES = Lists.newArrayList();
    private static final int VERTEX = ARBVertexShader.GL_VERTEX_SHADER_ARB;
    private static final int FRAGMENT = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
    public static final ShaderManager INSTANCE = new ShaderManager();
    private static boolean reloading;

    public static ShaderResource load(@Nullable String vsh, @Nullable String fsh) {
        ShaderResource resource = new ShaderResource(vsh, fsh);
        SHADER_RESOURCES.add(resource);
        return resource;
    }

    public static int create(IResourceManager manager, @Nullable ResourceLocation vsh, @Nullable ResourceLocation fsh) {
        int vshId = 0, fshId = 0, program = 0;
        if (vsh != null) {
            vshId = parse(manager, vsh, VERTEX);
        }
        if (fsh != null) {
            fshId = parse(manager, fsh, FRAGMENT);
        }
        if (vsh != null || fsh != null) {
            program = ARBShaderObjects.glCreateProgramObjectARB();
        }
        if (vsh != null) {
            OpenGlHelper.glAttachShader(program, vshId);
        }
        if (fsh != null) {
            OpenGlHelper.glAttachShader(program, fshId);
        }
        OpenGlHelper.glLinkProgram(program);
        if (OpenGlHelper.glGetShaderi(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            return 0;
        }
        if (OpenGlHelper.glGetShaderi(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            return 0;
        }
        return program;
    }

    private static int parse(IResourceManager manager, ResourceLocation location, int type) {
        int shader = OpenGlHelper.glCreateShader(type);
        try {
            InputStream inputStream = manager.getResource(location).getInputStream();
            String lines = IOUtils.readLines(inputStream, "UTF-8").stream()
                    .collect(Collectors.joining("\n"));
            ARBShaderObjects.glShaderSourceARB(shader, lines);
            OpenGlHelper.glCompileShader(shader);
            inputStream.close();
            if (OpenGlHelper.glGetShaderi(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
                String info = OpenGlHelper.glGetProgramInfoLog(shader, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB);
                throw new RuntimeException("Error creating shader: " + info);
            }
        } catch (IOException e) {
            EnderSkills.LOG.warn("[Shader Resource] Failed to load shader " + location.toString());
            OpenGlHelper.glDeleteShader(shader);
            e.printStackTrace();
        }
        return shader;
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        ProgressManager.ProgressBar bar = ProgressManager.push("Reloading Shader Manager", 0);
        reloading = true;
        SHADER_RESOURCES.forEach(shader -> shader.reload(manager));
        reloading = false;
        ProgressManager.pop(bar);
    }

    public static boolean isReloading() {
        return reloading;
    }
}
