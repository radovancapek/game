package engine.graph.particles;

import engine.items.GameItem;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlowParticleEmitter implements IParticleEmitter {

    private boolean active;

    private final List<GameItem> particles;

    private final Particle baseParticle;

    private long lastCreationTime;

    public FlowParticleEmitter(Particle baseParticle, int maxParticles, long creationPeriodMillis) {
        particles = new ArrayList<>();
        this.baseParticle = baseParticle;
        this.active = false;
        this.lastCreationTime = 0;
    }

    @Override
    public Particle getBaseParticle() {
        return baseParticle;
    }

    @Override
    public List<GameItem> getParticles() {
        return particles;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void update(long elapsedTime) {
        long now = System.currentTimeMillis();
        if (lastCreationTime == 0) {
            lastCreationTime = now;
        }
        Iterator<? extends GameItem> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = (Particle) it.next();
            if (particle.updateTtl(elapsedTime) < 0) {
                it.remove();
            } else {
                updatePosition(particle, elapsedTime);
            }
        }
    }

    public void createParticleAtPos(Vector3f position, Vector3f rotation) {
        Particle particle = new Particle(this.getBaseParticle());
        particle.setPosition(position.x, position.y, position.z);
        particle.setRotation(rotation);
        particles.add(particle);
    }

    /**
     * Updates a particle position
     *
     * @param particle    The particle to update
     * @param elapsedTime Elapsed time in milliseconds
     */
    public void updatePosition(Particle particle, long elapsedTime) {
        Vector3f position = particle.getPosition();
        Vector3f rotation = particle.getRotation();
        float speed = 0.2f;
        position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * -1 * speed;
        position.z += (float) Math.cos(Math.toRadians(rotation.y)) * -1 * speed;
        position.y += (float) Math.tan(Math.toRadians(rotation.x)) * -1 * speed;
    }

    @Override
    public void cleanup() {
        for (GameItem particle : getParticles()) {
            particle.cleanup();
        }
    }
}
