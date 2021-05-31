import engine.GameEngine;
import engine.IGameLogic;
import engine.Window;

public class Main {

    public static void main(String[] args) {
        try {
            System.setProperty("java.awt.headless", "true");
            boolean vSync = true;
            IGameLogic gameLogic = new Game();
            Window.WindowOptions opts = new Window.WindowOptions();
            opts.cullFace = false;
            opts.showFps = true;
            opts.compatibleProfile = true;
            opts.antialiasing = true;
            GameEngine gameEng = new GameEngine("GAME", 600, 480, vSync, opts, gameLogic);
            gameEng.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
