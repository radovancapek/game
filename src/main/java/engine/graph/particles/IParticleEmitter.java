package engine.graph.particles;

import engine.items.GameItem;

import java.util.List;

public interface IParticleEmitter {
    void cleanup();

    Particle getBaseParticle();

    List<GameItem> getParticles();
}
