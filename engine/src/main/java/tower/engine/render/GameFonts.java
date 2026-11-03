package tower.engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS;
import static com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.Hinting.Full;

public final class GameFonts {

    private GameFonts() {
    }

    public static BitmapFont create() {
        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/PTSans-Regular.ttf"));
        var parameter = new FreeTypeFontParameter();
        parameter.size = 18;
        parameter.hinting = Full;
        parameter.minFilter = Linear;
        parameter.magFilter = Linear;
        parameter.characters = DEFAULT_CHARS + extraCharacters();
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        font.setUseIntegerPositions(true);
        return font;
    }

    private static String extraCharacters() {
        var extra = new StringBuilder("—–«»…");
        for (int code = 0x0400; code <= 0x04FF; code++) {
            extra.append((char) code);
        }
        return extra.toString();
    }
}
