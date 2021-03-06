package engine.graph.particles;

import engine.graph.Mesh;
import engine.graph.Texture;
import engine.items.GameItem;
import org.joml.Vector3f;

public class Particle extends GameItem {
    private long updateTextureMillis;

    private long currentAnimTimeMillis;

    private Vector3f speed;

    /**
     * Time to live for particle in milliseconds.
     */
    private long ttl;

    private int animFrames;

    public Particle(Mesh mesh, Vector3f speed, long ttl) {
        super(mesh);
        this.speed = new Vector3f(speed);
        this.ttl = ttl;
    }

    public Particle(Particle baseParticle) {
        super(baseParticle.getMesh());
        Vector3f aux = baseParticle.getPosition();
        setPosition(aux.x, aux.y, aux.z);
        aux = baseParticle.getRotation();
        setRotation(new Vector3f(aux.x, aux.y, aux.z));
        setScale(baseParticle.getScale());
        this.speed = new Vector3f(baseParticle.speed);
        this.ttl = baseParticle.geTtl();
    }

    public Vector3f getSpeed() {
        return speed;
    }

    public void setSpeed(Vector3f speed) {
        this.speed = speed;
    }

    public long geTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /**
     * Updates the Particle's TTL
     * @param elapsedTime Elapsed Time in milliseconds
     * @return The Particle's TTL
     */
    public long updateTtl(long elapsedTime) {
        this.ttl -= elapsedTime;
        return this.ttl;
    }
}
