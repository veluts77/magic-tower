package tower.engine.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.state.GameState;

import java.util.List;

import static com.badlogic.gdx.graphics.Color.WHITE;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled;
import static tower.domain.state.RoomState.LIT;
import static tower.engine.render.ActionPanel.HEIGHT;

public class RoomRenderer {

    private static final float DESIGN_WIDTH = 1280f;
    private static final float DESIGN_HEIGHT = 720f - HEIGHT;
    private static final float OBJECT_SIZE = 64f;
    private static final float MARKER_SIZE = 56f;

    private static final Color ROOM_COLOR = new Color(0.16f, 0.20f, 0.30f, 1f);
    private static final Color MARKER_COLOR = new Color(0.93f, 0.78f, 0.22f, 1f);
    private static final Color LIT_COLOR = new Color(0.98f, 0.62f, 0.12f, 1f);
    private static final Color[] OBJECT_COLORS = {
            new Color(0.75f, 0.42f, 0.22f, 1f),
            new Color(0.28f, 0.55f, 0.48f, 1f),
            new Color(0.48f, 0.36f, 0.66f, 1f)
    };

    private final GlyphLayout layout = new GlyphLayout();
    private final ObjectSprites sprites = new ObjectSprites();
    private final RoomBackgrounds backgrounds = new RoomBackgrounds();

    public void draw(RoomDefinition room, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font,
                     float screenWidth, float screenHeight, boolean numberObjects, GameState state) {
        float roomX = 0f;
        float roomY = HEIGHT;
        float roomWidth = screenWidth;
        float roomHeight = screenHeight - HEIGHT;
        float scale = Math.min(roomWidth / DESIGN_WIDTH, roomHeight / DESIGN_HEIGHT);
        float shapeSize = OBJECT_SIZE * scale;
        float markerSize = MARKER_SIZE * scale;

        Texture background = backgrounds.textureFor(room == null ? null : room.id());
        if (background != null) {
            batch.begin();
            batch.setColor(WHITE);
            drawBackground(batch, background, roomX, roomY, roomWidth, roomHeight);
            batch.end();
        }

        List<ObjectDefinition> objects = visibleObjects(room, state);
        int objectCount = objects.size();
        float slotWidth = objectCount == 0 ? 0f : roomWidth / objectCount;
        float shapeY = roomY + roomHeight * 0.45f;
        float markerX = roomX + 48f * scale;
        float markerY = roomY + 40f * scale;
        Texture player = sprites.textureFor("player");
        Texture spirit = summonedSpirit(state);

        shapes.begin(Filled);
        if (background == null) {
            shapes.setColor(ROOM_COLOR);
            shapes.rect(roomX, roomY, roomWidth, roomHeight);
        }

        for (int i = 0; i < objectCount; i++) {
            ObjectDefinition object = objects.get(i);
            if (sprites.textureFor(object, state) != null) {
                continue;
            }
            shapes.setColor(lit(object, state) ? LIT_COLOR : OBJECT_COLORS[i % OBJECT_COLORS.length]);
            shapes.rect(shapeX(roomX, slotWidth, i, shapeSize), shapeY, shapeSize, shapeSize);
        }

        if (player == null) {
            shapes.setColor(MARKER_COLOR);
            shapes.circle(markerX, markerY, 12f * scale);
        }
        shapes.end();

        font.setColor(WHITE);
        batch.begin();
        batch.setColor(WHITE);
        for (int i = 0; i < objectCount; i++) {
            Texture texture = sprites.textureFor(objects.get(i), state);
            if (texture == null) {
                continue;
            }
            drawSprite(batch, texture, shapeX(roomX, slotWidth, i, shapeSize), shapeY, shapeSize);
        }
        if (player != null) {
            drawSprite(batch, player, markerX - markerSize / 2f, markerY - 8f * scale, markerSize);
        }
        if (spirit != null) {
            drawSprite(batch, spirit, markerX + markerSize * 0.55f, markerY + 12f * scale, 40f * scale);
        }
        String title = room.displayName();
        layout.setText(font, title);
        font.draw(batch, layout, (screenWidth - layout.width) / 2f, screenHeight - 8f - font.getLineHeight());

        for (int i = 0; i < objectCount; i++) {
            ObjectDefinition object = objects.get(i);
            String label = object.displayName();
            if (lit(object, state)) {
                label = label + " (lit)";
            }
            if (numberObjects) {
                label = (i + 1) + ": " + label;
            }
            layout.setText(font, label);
            float labelX = roomX + slotWidth * i + (slotWidth - layout.width) / 2f;
            font.draw(batch, layout, labelX, shapeY + shapeSize + font.getLineHeight() + 6f);
        }
        batch.end();
    }

    public void dispose() {
        sprites.dispose();
        backgrounds.dispose();
    }

    private Texture summonedSpirit(GameState state) {
        if (state == null || !state.spiritSummoned("seeker")) {
            return null;
        }
        return sprites.textureFor("seeker");
    }

    private static float shapeX(float roomX, float slotWidth, int index, float shapeSize) {
        return roomX + slotWidth * index + (slotWidth - shapeSize) / 2f;
    }

    private static void drawBackground(
            SpriteBatch batch, Texture texture, float x, float y, float width, float height) {
        float srcW = texture.getWidth();
        float srcH = texture.getHeight();
        if (srcW <= 0f || srcH <= 0f || width <= 0f || height <= 0f) {
            return;
        }
        float scale = width / srcW;
        float visibleSrcH = Math.min(srcH, height / scale);
        int srcHeight = Math.max(1, Math.round(visibleSrcH));
        int srcY = Math.max(0, Math.round(srcH - srcHeight));
        batch.draw(texture, x, y, width, height, 0, srcY, Math.round(srcW), srcHeight, false, false);
    }

    private static void drawSprite(SpriteBatch batch, Texture texture, float boxX, float boxY, float boxSize) {
        float width = texture.getWidth();
        float height = texture.getHeight();
        if (width <= 0f || height <= 0f) {
            return;
        }
        float scale = Math.min(boxSize / width, boxSize / height);
        float drawWidth = width * scale;
        float drawHeight = height * scale;
        batch.draw(
                texture,
                boxX + (boxSize - drawWidth) / 2f,
                boxY + (boxSize - drawHeight) / 2f,
                drawWidth,
                drawHeight);
    }

    private static List<ObjectDefinition> visibleObjects(RoomDefinition room, GameState state) {
        if (room == null) {
            return List.of();
        }
        if (state == null) {
            return room.getObjects() == null ? List.of() : room.getObjects();
        }
        return state.visibleObjects(room);
    }

    private static boolean lit(ObjectDefinition object, GameState state) {
        return object != null && state != null && LIT.equals(state.objectCondition(object.id()));
    }
}
