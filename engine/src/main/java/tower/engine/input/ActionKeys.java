package tower.engine.input;

import com.badlogic.gdx.Gdx;

import static com.badlogic.gdx.Input.Keys.NUM_1;
import static com.badlogic.gdx.Input.Keys.NUM_2;
import static com.badlogic.gdx.Input.Keys.NUM_3;
import static com.badlogic.gdx.Input.Keys.NUM_4;
import static com.badlogic.gdx.Input.Keys.NUM_5;
import static com.badlogic.gdx.Input.Keys.NUM_6;
import static com.badlogic.gdx.Input.Keys.NUM_7;
import static com.badlogic.gdx.Input.Keys.NUM_8;
import static com.badlogic.gdx.Input.Keys.NUM_9;
import static com.badlogic.gdx.Input.Keys.NUMPAD_1;
import static com.badlogic.gdx.Input.Keys.NUMPAD_2;
import static com.badlogic.gdx.Input.Keys.NUMPAD_3;
import static com.badlogic.gdx.Input.Keys.NUMPAD_4;
import static com.badlogic.gdx.Input.Keys.NUMPAD_5;
import static com.badlogic.gdx.Input.Keys.NUMPAD_6;
import static com.badlogic.gdx.Input.Keys.NUMPAD_7;
import static com.badlogic.gdx.Input.Keys.NUMPAD_8;
import static com.badlogic.gdx.Input.Keys.NUMPAD_9;

public final class ActionKeys {

    public static final int NONE = -1;
    public static final int UNAVAILABLE = -2;

    private static final int[] ROW = {NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9};
    private static final int[] PAD = {
            NUMPAD_1, NUMPAD_2, NUMPAD_3, NUMPAD_4, NUMPAD_5, NUMPAD_6, NUMPAD_7, NUMPAD_8, NUMPAD_9
    };

    private ActionKeys() {
    }

    public static int selectedIndex(int availableCount) {
        for (int index = 0; index < ROW.length; index++) {
            if (Gdx.input.isKeyJustPressed(ROW[index]) || Gdx.input.isKeyJustPressed(PAD[index])) {
                return index < availableCount ? index : UNAVAILABLE;
            }
        }
        return NONE;
    }
}
