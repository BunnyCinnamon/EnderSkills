package arekkuusu.enderskills.client.keybind;

import arekkuusu.enderskills.common.lib.LibMod;
import com.google.common.collect.Lists;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class KeyBounds {

    public static List<KeyBinding> skillUseList = Lists.newLinkedList();
    public static KeyBinding skillGroupRotateRight;
    public static KeyBinding skillGroupRotateLeft;
    public static KeyBinding skillUse1;
    public static KeyBinding skillUse2;
    public static KeyBinding skillUse3;
    public static KeyBinding skillUse4;
    public static KeyBinding skillUse5;
    public static KeyBinding skillUse6;
    public static KeyBinding skillUse7;
    public static KeyBinding skillUse8;
    public static KeyBinding skillUse9;
    public static KeyBinding upgrade;
    public static KeyBinding hideOverlay;

    public static void init() {
        skillGroupRotateRight = create("rotate_right", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_D);
        skillGroupRotateLeft = create("rotate_left", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_A);
        skillUse1 = create("use_1", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_1);
        skillUse2 = create("use_2", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_2);
        skillUse3 = create("use_3", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_3);
        skillUse4 = create("use_4", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_4);
        skillUse5 = create("use_5", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_5);
        skillUse6 = create("use_6", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_6);
        skillUse7 = create("use_7", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_7);
        skillUse8 = create("use_8", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_8);
        skillUse9 = create("use_9", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_9);
        upgrade = create("assisted_upgrade", KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_LSHIFT);
        hideOverlay = create("hide_overlay", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_Y);
        skillUseList.add(skillUse1);
        skillUseList.add(skillUse2);
        skillUseList.add(skillUse3);
        skillUseList.add(skillUse4);
        skillUseList.add(skillUse5);
        skillUseList.add(skillUse6);
        skillUseList.add(skillUse7);
        skillUseList.add(skillUse8);
        skillUseList.add(skillUse9);
    }

    public static KeyBinding create(String description, KeyConflictContext context, KeyModifier modifier, int key) {
        KeyBinding binding = new KeyBinding("keybind." + LibMod.MOD_ID + ".skill_bound." + description, context, modifier, key, LibMod.MOD_ID);
        ClientRegistry.registerKeyBinding(binding);
        return binding;
    }
}
