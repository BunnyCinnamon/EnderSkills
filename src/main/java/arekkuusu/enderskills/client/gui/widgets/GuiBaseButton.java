package arekkuusu.enderskills.client.gui.widgets;

import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;

public class GuiBaseButton extends GuiButton {

    public GuiBaseButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public GuiBaseButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.getRecord(ModSounds.PAGE_TURN, 2.5F, 0.25F));
    }
}
