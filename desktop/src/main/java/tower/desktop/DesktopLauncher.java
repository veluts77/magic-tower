package tower.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import tower.engine.TowerGame;

public class DesktopLauncher {

    private static final String ROOM_ARG = "--room=";

    public static void main(String[] args) {
        var config = new Lwjgl3ApplicationConfiguration();

        config.setTitle("Tower Game");
        config.setWindowedMode(1280, 720);
        config.setResizable(false);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new TowerGame(parseRoomId(args)), config);
    }

    private static String parseRoomId(String[] args) {
        if (args == null) {
            return null;
        }
        for (String arg : args) {
            if (arg != null && arg.startsWith(ROOM_ARG)) {
                String roomId = arg.substring(ROOM_ARG.length()).trim();
                return roomId.isEmpty() ? null : roomId;
            }
        }
        return null;
    }
}
