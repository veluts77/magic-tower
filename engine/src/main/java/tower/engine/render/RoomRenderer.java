package tower.engine.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;

public class RoomRenderer {

    private static final Color ROOM_COLOR = new Color(0.16f, 0.20f, 0.30f, 1f);
    private static final Color MARKER_COLOR = new Color(0.93f, 0.78f, 0.22f, 1f);
    private static final Color[] OBJECT_COLORS = {
            new Color(0.75f, 0.42f, 0.22f, 1f),
            new Color(0.28f, 0.55f, 0.48f, 1f),
            new Color(0.48f, 0.36f, 0.66f, 1f)
    };

    private final GlyphLayout layout = new GlyphLayout();

    public void draw(RoomDefinition room, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font,
                     float screenWidth, float screenHeight) {
        float roomX = 48f;
        float roomY = 56f;
        float roomWidth = screenWidth - 96f;
        float roomHeight = screenHeight - 128f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(ROOM_COLOR);
        shapes.rect(roomX, roomY, roomWidth, roomHeight);

        int objectCount = room.objects == null ? 0 : room.objects.size();
        float shapeSize = 56f;
        float slotWidth = objectCount == 0 ? 0f : roomWidth / objectCount;
        float shapeY = roomY + roomHeight * 0.45f;
        for (int i = 0; i < objectCount; i++) {
            float shapeX = roomX + slotWidth * i + (slotWidth - shapeSize) / 2f;
            shapes.setColor(OBJECT_COLORS[i % OBJECT_COLORS.length]);
            shapes.rect(shapeX, shapeY, shapeSize, shapeSize);
        }

        shapes.setColor(MARKER_COLOR);
        shapes.circle(roomX + 40f, roomY + 36f, 12f);
        shapes.end();

        font.setColor(Color.WHITE);
        batch.begin();
        String title = room.displayName == null ? "" : room.displayName;
        layout.setText(font, title);
        font.draw(batch, layout, (screenWidth - layout.width) / 2f, screenHeight - 28f);

        for (int i = 0; i < objectCount; i++) {
            ObjectDefinition object = room.objects.get(i);
            String label = object.displayName == null ? "" : object.displayName;
            layout.setText(font, label);
            float labelX = roomX + slotWidth * i + (slotWidth - layout.width) / 2f;
            font.draw(batch, layout, labelX, shapeY + shapeSize + 22f);
        }

        font.draw(batch, "room: " + room.id, 12f, 36f);
        font.draw(batch, "objects: " + objectCount, 12f, 16f);
        batch.end();
    }
}
