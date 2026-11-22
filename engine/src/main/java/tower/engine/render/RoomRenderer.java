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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.badlogic.gdx.graphics.Color.WHITE;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled;
import static tower.domain.state.RoomState.LIT;
import static tower.engine.render.ActionPanel.HEIGHT;

public class RoomRenderer {

    private static final float DESIGN_WIDTH = 1280f;
    private static final float DESIGN_HEIGHT = 720f - HEIGHT;
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
                     float screenWidth, float screenHeight, GameState state) {
        float roomX = 0f;
        float roomY = HEIGHT;
        float roomWidth = screenWidth;
        float roomHeight = screenHeight - HEIGHT;
        float scale = Math.min(roomWidth / DESIGN_WIDTH, roomHeight / DESIGN_HEIGHT);
        float markerSize = MARKER_SIZE * scale;
        float markerX = roomX + 48f * scale;
        float markerY = roomY + 40f * scale;

        Texture background = backgrounds.textureFor(room == null ? null : room.id());
        if (background != null) {
            batch.begin();
            batch.setColor(WHITE);
            drawBackground(batch, background, roomX, roomY, roomWidth, roomHeight);
            batch.end();
        }

        List<ObjectDefinition> objects = sortedVisible(room, state);
        Texture player = sprites.textureFor("player");
        Texture spirit = summonedSpirit(state);

        shapes.begin(Filled);
        if (background == null) {
            shapes.setColor(ROOM_COLOR);
            shapes.rect(roomX, roomY, roomWidth, roomHeight);
        }
        for (int i = 0; i < objects.size(); i++) {
            ObjectDefinition object = objects.get(i);
            if (sprites.textureFor(object, state) != null) {
                continue;
            }
            float[] box = pixelBox(object, roomX, roomY, roomWidth, roomHeight);
            shapes.setColor(lit(object, state) ? LIT_COLOR : OBJECT_COLORS[i % OBJECT_COLORS.length]);
            shapes.rect(box[0], box[1], box[2], box[3]);
        }
        if (player == null) {
            shapes.setColor(MARKER_COLOR);
            shapes.circle(markerX, markerY, 12f * scale);
        }
        shapes.end();

        font.setColor(WHITE);
        batch.begin();
        batch.setColor(WHITE);
        for (ObjectDefinition object : objects) {
            Texture texture = sprites.textureFor(object, state);
            if (texture == null) {
                continue;
            }
            float[] box = pixelBox(object, roomX, roomY, roomWidth, roomHeight);
            drawSprite(batch, texture, box[0], box[1], box[2], box[3]);
        }
        if (player != null) {
            drawSprite(batch, player, markerX - markerSize / 2f, markerY - 8f * scale, markerSize, markerSize);
        }
        if (spirit != null) {
            float spiritSize = 40f * scale;
            drawSprite(batch, spirit, markerX + markerSize * 0.55f, markerY + 12f * scale, spiritSize, spiritSize);
        }
        String title = room.displayName();
        layout.setText(font, title);
        font.draw(batch, layout, (screenWidth - layout.width) / 2f, screenHeight - 8f - font.getLineHeight());
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

    private static List<ObjectDefinition> sortedVisible(RoomDefinition room, GameState state) {
        List<ObjectDefinition> objects = visibleObjects(room, state);
        var sorted = new ArrayList<>(objects);
        sorted.sort(Comparator.comparingInt(ObjectDefinition::z));
        return sorted;
    }

    private static float[] pixelBox(
            ObjectDefinition object, float roomX, float roomY, float roomWidth, float roomHeight) {
        float width = object.width() * roomWidth;
        float height = object.height() * roomHeight;
        float centerX = roomX + object.posX() * roomWidth;
        float centerY = roomY + roomHeight - object.posY() * roomHeight;
        return new float[] {centerX - width / 2f, centerY - height / 2f, width, height};
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

    private static void drawSprite(
            SpriteBatch batch, Texture texture, float boxX, float boxY, float boxWidth, float boxHeight) {
        float width = texture.getWidth();
        float height = texture.getHeight();
        if (width <= 0f || height <= 0f || boxWidth <= 0f || boxHeight <= 0f) {
            return;
        }
        float scale = Math.min(boxWidth / width, boxHeight / height);
        float drawWidth = width * scale;
        float drawHeight = height * scale;
        batch.draw(
                texture,
                boxX + (boxWidth - drawWidth) / 2f,
                boxY + (boxHeight - drawHeight) / 2f,
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
