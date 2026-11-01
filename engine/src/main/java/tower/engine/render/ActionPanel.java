package tower.engine.render;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import tower.domain.action.GameAction;

import java.util.List;

import static com.badlogic.gdx.graphics.Color.WHITE;
import static com.badlogic.gdx.utils.Align.left;

public class ActionPanel {

    private static final float LEFT = 48f;
    private static final float BOTTOM = 56f;
    private static final float GAP = 8f;
    private static final float LINE = 20f;

    private final GlyphLayout layout = new GlyphLayout();

    public float reservedHeight(List<GameAction> actions, String message, BitmapFont font, float width) {
        float text = textHeight(actions, message, font, width);
        if (text == 0f) {
            return 0f;
        }
        return text + GAP;
    }

    public void draw(List<GameAction> actions, String message, SpriteBatch batch, BitmapFont font,
                     float width, float reservedHeight) {
        if (reservedHeight <= 0f) {
            return;
        }
        float top = BOTTOM + reservedHeight - GAP;
        font.setColor(WHITE);
        batch.begin();
        float baseline = top - font.getCapHeight();
        if (visible(message)) {
            layout.setText(font, message, WHITE, width, left, true);
            font.draw(batch, layout, LEFT, baseline);
            baseline -= layout.height + GAP;
        }
        int count = actions == null ? 0 : actions.size();
        for (int i = 0; i < count; i++) {
            font.draw(batch, (i + 1) + ": " + actions.get(i).label, LEFT, baseline);
            baseline -= LINE;
        }
        batch.end();
    }

    private float textHeight(List<GameAction> actions, String message, BitmapFont font, float width) {
        int count = actions == null ? 0 : actions.size();
        if (count == 0 && !visible(message)) {
            return 0f;
        }
        float height = font.getCapHeight() + 4f;
        if (visible(message)) {
            layout.setText(font, message, WHITE, width, left, true);
            height += layout.height + GAP;
        }
        height += count * LINE;
        return height;
    }

    private static boolean visible(String message) {
        return message != null && !message.isBlank();
    }
}
