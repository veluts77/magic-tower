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

public class RoomRenderer {

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

    public void draw(RoomDefinition room, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font,
                     float screenWidth, float screenHeight, float bottomInset, boolean numberObjects,
                     GameState state) {
        float roomX = 48f;
        float roomY = 56f + bottomInset;
        float roomWidth = screenWidth - 96f;
        float roomHeight = screenHeight - 128f - bottomInset;

        shapes.begin(Filled);
        shapes.setColor(ROOM_COLOR);
        shapes.rect(roomX, roomY, roomWidth, roomHeight);

        List<ObjectDefinition> objects = visibleObjects(room, state);
        int objectCount = objects.size();
        float shapeSize = 56f;
        float slotWidth = objectCount == 0 ? 0f : roomWidth / objectCount;
        float shapeY = roomY + roomHeight * 0.45f;
        for (int i = 0; i < objectCount; i++) {
            ObjectDefinition object = objects.get(i);
            if (sprites.textureFor(object.id()) != null) {
                continue;
            }
            shapes.setColor(lit(object, state) ? LIT_COLOR : OBJECT_COLORS[i % OBJECT_COLORS.length]);
            shapes.rect(shapeX(roomX, slotWidth, i, shapeSize), shapeY, shapeSize, shapeSize);
        }

        shapes.setColor(MARKER_COLOR);
        shapes.circle(roomX + 40f, roomY + 36f, 12f);
        shapes.end();

        font.setColor(WHITE);
        batch.begin();
        batch.setColor(WHITE);
        for (int i = 0; i < objectCount; i++) {
            Texture texture = sprites.textureFor(objects.get(i).id());
            if (texture == null) {
                continue;
            }
            drawSprite(batch, texture, shapeX(roomX, slotWidth, i, shapeSize), shapeY, shapeSize);
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

        font.draw(batch, "room: " + room.id(), 12f, 36f);
        font.draw(batch, "objects: " + objectCount, 12f, 16f);
        batch.end();
    }

    public void dispose() {
        sprites.dispose();
    }

    private static float shapeX(float roomX, float slotWidth, int index, float shapeSize) {
        return roomX + slotWidth * index + (slotWidth - shapeSize) / 2f;
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
