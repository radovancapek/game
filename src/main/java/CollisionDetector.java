import engine.graph.Camera;
import engine.items.GameItem;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class CollisionDetector {
    private final Vector3f max;

    private final Vector3f min;

    private final Vector2f nearFar;

    private Vector3f dir;

    public CollisionDetector() {
        dir = new Vector3f();
        min = new Vector3f();
        max = new Vector3f();
        nearFar = new Vector2f();
    }

    public void selectGameItem(GameItem[] gameItems, Camera camera) {
        dir = camera.getViewMatrix().positiveZ(dir).negate();
        selectGameItem(gameItems, camera.getPosition(), dir);
    }

    protected boolean detectCollision(GameItem[] gameItems, Camera camera, boolean ammo) {
        Vector3f camPos = camera.getPosition();
        for(int i = 0; i < gameItems.length; i++) {
            min.set(gameItems[i].getPosition());
            max.set(gameItems[i].getPosition());
            min.add(-gameItems[i].getScale(), -gameItems[i].getScale(), -gameItems[i].getScale());
            max.add(gameItems[i].getScale(), gameItems[i].getScale(), gameItems[i].getScale());

            if((camPos.x > min.x && camPos.x < max.x) && (camPos.y > min.y - 1 && camPos.y < max.y + 1)
                    && (camPos.z > min.z && camPos.z < max.z)) {
                if(ammo) {
                    gameItems[i].getPosition().y = 50;
                }
                return true;
            }
        }
        return false;
    }

    protected void selectGameItem(GameItem[] gameItems, Vector3f center, Vector3f dir) {
        GameItem selectedGameItem = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (GameItem gameItem : gameItems) {
            gameItem.setSelected(false);
            min.set(gameItem.getPosition());
            max.set(gameItem.getPosition());
            min.add(-gameItem.getScale(), -gameItem.getScale(), -gameItem.getScale());
            max.add(gameItem.getScale(), gameItem.getScale(), gameItem.getScale());
            if (Intersectionf.intersectRayAab(center, dir, min, max, nearFar) && nearFar.x < closestDistance) {
                closestDistance = nearFar.x;
                selectedGameItem = gameItem;
            }
        }

        if (selectedGameItem != null) {
            selectedGameItem.setSelected(true);
        }
    }
}
