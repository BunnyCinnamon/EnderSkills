package arekkuusu.enderskills.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;

public class GuiPauseAll extends GuiScreen {

    public static boolean paused = false;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        paused = false;
    }

    @Override
    public void initGui() {
        KeyBinding.updateKeyBindState();
        paused = true;
    }
}
