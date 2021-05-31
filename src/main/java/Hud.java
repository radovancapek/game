import engine.IHud;
import engine.Window;
import engine.graph.FontTexture;
import engine.graph.Material;
import engine.graph.Mesh;
import engine.graph.OBJLoader;
import engine.items.GameItem;
import engine.items.TextItem;
import org.joml.Vector4f;

import java.awt.*;

public class Hud implements IHud {
    private static final Font FONT = new Font("Arial", Font.PLAIN, 20);

    private static final String CHARSET = "ISO-8859-1";

    private final GameItem[] gameItems;

    private final TextItem statusTextItem;

    private final TextItem killsTextItem;

    private final TextItem ammoTextItem;
    
    private final TextItem gameOverTextItem;

    private final GameItem crossHairItem;

    private int kills;

    private int ammo;

    private boolean gameOver;

    public Hud(String statusText) throws Exception {
        gameOver = false;
        kills = 0;
        ammo = 10;
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusTextItem = new TextItem(statusText, fontTexture);
        this.statusTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.5f, 0.5f, 0.5f, 10f));

        this.killsTextItem = new TextItem("Kills: " + kills, fontTexture);
        this.killsTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.5f, 0.5f, 0.5f, 10f));

        this.ammoTextItem = new TextItem("Ammo: " + ammo, fontTexture);
        this.ammoTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.5f, 0.5f, 0.5f, 10f));

        this.gameOverTextItem = new TextItem("", fontTexture);
        this.gameOverTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(1, 0, 0, 10f));

        // CrossHair
        Mesh mesh = OBJLoader.loadMesh("/models/cross.obj");
        Material material = new Material();
        material.setAmbientColour(new Vector4f(1, 0, 0, 1));
        mesh.setMaterial(material);
        crossHairItem = new GameItem(mesh);
        crossHairItem.setScale(40.0f);

        gameItems = new GameItem[]{statusTextItem, crossHairItem, killsTextItem, ammoTextItem, gameOverTextItem};
    }

    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if(gameOver) this.gameOverTextItem.setText("GAME OVER - press ENTER to restart");
        else this.gameOverTextItem.setText("");
    }

    public void addKill() {
        kills++;
        this.killsTextItem.setText("Kills: " + kills);
    }

    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void addAmmo() {
        ammo+=10;
        this.ammoTextItem.setText("Ammo: " + ammo);
    }

    public void removeAmmo() {
        ammo--;
        this.ammoTextItem.setText("Ammo: " + ammo);
    }

    public int getAmmo() {
        return ammo;
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }

    public void updateSize(Window window) {
        if(!gameOver) {
            this.statusTextItem.setPosition(10f, window.getHeight() - 50f, 0);
            this.killsTextItem.setPosition(window.getWidth() - 100f, window.getHeight() - 50f, 0);
            this.ammoTextItem.setPosition(window.getWidth() - 200f, window.getHeight() - 50f, 0);
            this.crossHairItem.setPosition(window.getWidth() / 2.0f, window.getHeight() / 2.0f, 0);
            this.gameOverTextItem.setPosition(window.getWidth() / 2.0f, window.getHeight() / 2.0f, 0);
        } else {
            this.gameOverTextItem.setPosition(50f, window.getHeight() / 3.0f, 0);
        }
    }

    @Override
    public void cleanup() {
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }
}
