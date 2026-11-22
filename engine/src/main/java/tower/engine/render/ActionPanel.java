package tower.engine.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;

import static com.badlogic.gdx.graphics.Color.WHITE;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;
import static com.badlogic.gdx.utils.Align.left;

public class ActionPanel {

    public static final float HEIGHT = 120f;

    private static final float LEFT = 12f;
    private static final float PADDING = 8f;
    private static final float GAP = 4f;
    private static final Color PANEL_COLOR = new Color(0.10f, 0.10f, 0.12f, 1f);
    private static final Color BORDER_COLOR = new Color(0.45f, 0.45f, 0.50f, 1f);

    private final GlyphLayout layout = new GlyphLayout();

    public void drawChrome(ShapeRenderer shapes, float screenWidth) {
        shapes.begin(Filled);
        shapes.setColor(PANEL_COLOR);
        shapes.rect(0f, 0f, screenWidth, HEIGHT);
        shapes.end();

        shapes.begin(Line);
        shapes.setColor(BORDER_COLOR);
        shapes.rect(0.5f, 0.5f, screenWidth - 1f, HEIGHT - 1f);
        shapes.end();
    }

    public void draw(
            String status,
            String heading,
            List<String> lines,
            String message,
            SpriteBatch batch,
            BitmapFont font,
            float screenWidth) {
        float width = Math.max(1f, screenWidth - LEFT * 2f);
        float line = lineHeight(font);
        float baseline = HEIGHT - PADDING - font.getCapHeight();
        float minBaseline = PADDING;

        font.setColor(WHITE);
        batch.begin();
        if (visible(status) && baseline >= minBaseline) {
            font.draw(batch, status, LEFT, baseline);
            baseline -= line;
        }
        if (visible(heading) && baseline >= minBaseline) {
            font.draw(batch, heading, LEFT, baseline);
            baseline -= line;
        }
        if (visible(message) && baseline >= minBaseline) {
            float maxHeight = baseline - minBaseline;
            layout.setText(font, message, WHITE, width, left, true);
            if (layout.height <= maxHeight) {
                font.draw(batch, layout, LEFT, baseline);
                baseline -= layout.height + GAP;
            }
        }
        int count = lines == null ? 0 : lines.size();
        for (int i = 0; i < count; i++) {
            if (baseline < minBaseline) {
                break;
            }
            font.draw(batch, (i + 1) + ": " + lines.get(i), LEFT, baseline);
            baseline -= line;
        }
        batch.end();
    }

    private static float lineHeight(BitmapFont font) {
        return Math.max(font.getLineHeight(), font.getCapHeight() + 4f);
    }

    private static boolean visible(String message) {
        return message != null && !message.isBlank();
    }
}
