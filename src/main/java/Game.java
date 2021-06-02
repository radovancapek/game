import engine.*;
import engine.graph.*;
import engine.graph.lights.DirectionalLight;
import engine.graph.lights.PointLight;
import engine.graph.lights.SpotLight;
import engine.graph.particles.FlowParticleEmitter;
import engine.graph.particles.Particle;
import engine.graph.weather.Fog;
import engine.items.GameItem;
import engine.items.SkyBox;
import engine.items.Terrain;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.3f;

    private static final int MONSTER_COUNT = 20;

    private static final int AMMO_BOX_COUNT = 20;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private Scene scene;

    private Hud hud;

    private float lightAngle;

    private float angleInc;

    private static final float CAMERA_POS_STEP = 0.05f;

    private Terrain terrain;

    private FlowParticleEmitter particleEmitter;

    private SpotLight[] spotLightList;

    private PointLight[] pointLightList;

    private CollisionDetector selectDetector;

    private GameItem[] gameItems;

    private List<GameItem> monsterItemsList;

    private GameItem[] monsterItems;

    private GameItem[] ammoBoxItems;

    private boolean gameOver = false;

    private int hp;

    private int kills;

    private boolean readyForShot = true;

    public Game() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        lightAngle = -90;
        angleInc = 0;
    }

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        scene = new Scene();

        selectDetector = new CollisionDetector();

        hp = 100;
        kills = 0;

        // Terrain
        float terrainScale = 40;
        int terrainSize = 1;
        float minY = -0.1f;
        float maxY = 0.1f;
        int textInc = 40;
        terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "textures/heightmap2.png", "textures/grass.png", textInc);
        scene.setGameItems(terrain.getGameItems());

        float reflectance = 1f;
        float skyBoxScale = 5.0f;

        int treeCount = 20 * 20;
        gameItems = new GameItem[treeCount];
        int index = 0;


        //monsters
        monsterItemsList = new ArrayList<>();
        monsterItems = new GameItem[MONSTER_COUNT];
        Mesh monsterMesh = OBJLoader.loadMesh("/models/2.obj");
        Material monsterMaterial = new Material(new Vector4f(1, 0, 0, 1.f), reflectance);
        monsterMesh.setMaterial(monsterMaterial);

        for (int i = 0; i < MONSTER_COUNT; i++) {
            GameItem monsterItem = new GameItem(monsterMesh);
            monsterItem.setScale(0.01f);
            int x = getRandomNumber(5, 20);
            int z = getRandomNumber(5, 20);
            if ((i % 2) == 0) {
                x *= -1;
                z *= -1;
            }
            float y = terrain.getHeight(new Vector3f(x, 0, z));
            monsterItem.setPosition(x, y, z);
            monsterItems[i] = monsterItem;
            monsterItemsList.add(monsterItem);
        }
        GameItem[] g = monsterItemsList.toArray(new GameItem[monsterItemsList.size()]);
        scene.setGameItems(monsterItems);

        // trees
        float blockScale = 0.3f;

        Mesh mesh = OBJLoader.loadMesh("/models/tree2.obj", treeCount);
        Texture texture = new Texture("textures/terrain.png");
        Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);

        for (int i = -20; i < 20; i += 2) {
            for (int j = -20; j < 20; j += 2) {
                GameItem gameItem = new GameItem(mesh);
                gameItem.setScale(blockScale);
                gameItem.setPosition(i, 1, j);
                float newY = terrain.getHeight(gameItem.getPosition());
                gameItem.setPosition(i, newY, j);
                gameItems[index] = gameItem;
                index++;
            }
        }
        scene.setGameItems(gameItems);

        // Setup Lights
        setupLights();

        // ammo boxes
        ammoBoxItems = new GameItem[AMMO_BOX_COUNT];
        float ammoBlockScale = 0.1f;

        Mesh ammoMesh = OBJLoader.loadMesh("/models/cube.obj", AMMO_BOX_COUNT);
        Texture ammoTexture = new Texture("textures/ammo-box.png");
        Material ammoMaterial = new Material(ammoTexture, reflectance);
        ammoMesh.setMaterial(ammoMaterial);
        SceneLight sceneLight = scene.getSceneLight();
        spotLightList = new SpotLight[AMMO_BOX_COUNT];
        pointLightList = new PointLight[AMMO_BOX_COUNT];
        for (int i = 0; i < AMMO_BOX_COUNT; i++) {
            GameItem ammoItem = new GameItem(ammoMesh);
            ammoItem.setScale(ammoBlockScale);
            int x = getRandomNumber(5, 20);
            int z = getRandomNumber(5, 20);
            if ((i % 2) == 0) {
                x *= -1;
                z *= -1;
            }
            float y = terrain.getHeight(new Vector3f(x, 0, z));
            ammoItem.setPosition(x, y, z);
            ammoBoxItems[i] = ammoItem;


            // Spot Light
            Vector3f lightPosition = new Vector3f(x, y + 0.2f, z);
            float lightIntensity = 1.0f;
            PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
            PointLight pointLight = new PointLight(new Vector3f(1, 0, 0), lightPosition, lightIntensity);
            pointLight.setColor(new Vector3f(1, 0, 0));
            att = new PointLight.Attenuation(0.0f, 0.0f, 1f);
            pointLight.setAttenuation(att);
            Vector3f coneDir = new Vector3f(0, 1, 0);
            float cutoff = (float) Math.cos(12.5f);
            SpotLight spotLight = new SpotLight(pointLight, coneDir, cutoff);
            spotLightList[i] = spotLight;
            pointLightList[i] = pointLight;

        }
        sceneLight.setSpotLightList(spotLightList);
        scene.setGameItems(ammoBoxItems);

        int maxParticles = 200;
        Vector3f particleSpeed = new Vector3f(1, 0, 0);
        particleSpeed.mul(2.5f);
        long ttl = 4000;
        long creationPeriodMillis = 300;
        float scale = 0.01f;

        Mesh partMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture partTexture = new Texture("textures/fire-texture.png");
        Material partMaterial = new Material(partTexture, reflectance);
        partMesh.setMaterial(partMaterial);
        Particle particle = new Particle(partMesh, particleSpeed, ttl);
        particle.setPosition(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        particle.setScale(scale);
        particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
        particleEmitter.setActive(true);
        this.scene.setParticleEmitters(new FlowParticleEmitter[]{particleEmitter});


        // Setup Fog
        scene.setFog(new Fog(true, new Vector3f(0.5f, 0.5f, 0.5f), 0.5f));

        // Setup  SkyBox
        SkyBox skyBox = new SkyBox("/models/skybox.obj", "textures/skybox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // Create HUD
        hud = new Hud("HP: " + hp);
        hud.setAmmo(10);
        hud.setKills(0);
        camera.getPosition().x = 0.5f;
        camera.getPosition().y = 0;
        camera.getPosition().z = 0;

    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        SpotLight spotLight = spotLightList[0];
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_ENTER)) {
            if(gameOver) {
                gameOver = false;
                for (int i = 0; i < MONSTER_COUNT; i++) {
                    int x = getRandomNumber(5, 20);
                    int z = getRandomNumber(5, 20);
                    if ((i % 2) == 0) {
                        x *= -1;
                        z *= -1;
                    }
                    monsterItems[i].getPosition().x = x;
                    monsterItems[i].getPosition().z = z;
                }
                for (int i = 0; i < AMMO_BOX_COUNT; i++) {
                    int x = getRandomNumber(5, 20);
                    int z = getRandomNumber(5, 20);
                    if ((i % 2) == 0) {
                        x *= -1;
                        z *= -1;
                    }
                    float y = terrain.getHeight(new Vector3f(x, 0, z));
                    ammoBoxItems[i].setPosition(x, y, z);
                }
                hud.setAmmo(10);
                hud.setKills(0);
                hp = 100;
                hud.setStatusText("HP: " + hp);
                hud.setGameOver(false);
                camera.getPosition().x = 0.5f;
                camera.getPosition().y = 0;
                camera.getPosition().z = 0;
            }
        }
    }

    private void shoot() {
        if (hud.getAmmo() > 0) {
            Vector3f shotPos = new Vector3f(camera.getPosition().x, camera.getPosition().y - 0.05f, camera.getPosition().z);
            particleEmitter.createParticleAtPos(shotPos, camera.getRotation());
            hud.removeAmmo();
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput, Window window) {
        // Update camera position
        if(!gameOver) {
            Vector3f prevPos = new Vector3f(camera.getPosition());
            camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

            // Check if there has been a collision. If true, set the y position to
            // the maximum height
            Vector3f newPos = new Vector3f(camera.getPosition());
            float height = terrain != null ? terrain.getHeight(camera.getPosition()) : -Float.MAX_VALUE;
            if (camera.getPosition().y - 0.05 <= height) {
                camera.setPosition(newPos.x, prevPos.y + 0.05f, newPos.z);
            } else if (camera.getPosition().y + 0.05 >= height) {
                camera.setPosition(newPos.x, height + 0.5f, newPos.z);
            }

            if (this.selectDetector.detectCollision(gameItems, camera, false)) {
                camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
            }

            if (this.selectDetector.detectCollision(ammoBoxItems, camera, true)) {
                hud.addAmmo();
            }


            for (int i = 0; i < MONSTER_COUNT; i++) {
                Vector3f monsterPos = monsterItems[i].getPosition();

                float xDist = prevPos.x - monsterPos.x;
                float yDist = prevPos.y - monsterPos.y;
                float zDist = prevPos.z - monsterPos.z;
                float hyp = (float) Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                monsterPos.x += xDist / hyp * 0.01;
                monsterPos.y += yDist / hyp * 0.01;
                monsterPos.z += zDist / hyp * 0.01;
                monsterPos.y = terrain.getHeight(monsterPos);

                if (hyp < 0.6) {
                    monsterPos.x += 20;
                    hp -= 10;
                    hud.setStatusText("HP: " + hp);
                    if(hp <= 0) {
                        gameOver = true;
                        hud.setGameOver(true);
                    }
                }
                float monsterScale = 0.5f;
                for (GameItem p : particleEmitter.getParticles()) {
                    Vector3f shotPos = p.getPosition();
                    if ((Math.abs(monsterPos.x - shotPos.x) < monsterScale) && (Math.abs(monsterPos.y - shotPos.y) < monsterScale) && (Math.abs(monsterPos.z - shotPos.z) < monsterScale)) {
                        monsterPos.x += 20;
                        p.getPosition().y = 50;
                        hud.addKill();
                    }
                }

            }

            particleEmitter.update((long) (interval * 1000));

            // Update camera based on mouse
            if (window.isCursorInGame()) {
                Vector2f rotVec = mouseInput.getDisplVec();
                camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            }
            if (mouseInput.isLeftButtonPressed() && readyForShot) {
                shoot();
                readyForShot = false;
            }
            if (mouseInput.isLeftButtonReleased()) {
                readyForShot = true;
            }

            SceneLight sceneLight = scene.getSceneLight();
            SpotLight spotLight = sceneLight.getSpotLightList()[0];

            spotLight.getPointLight().setPosition(new Vector3f(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z));

            // Update directional light direction, intensity and colour
            DirectionalLight directionalLight = sceneLight.getDirectionalLight();
            lightAngle += 1.1f;
            if (lightAngle > 90) {
                directionalLight.setIntensity(0);
                if (lightAngle >= 360) {
                    lightAngle = -90;
                }
            } else if (lightAngle <= -80 || lightAngle >= 80) {
                float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
                directionalLight.setIntensity(factor);
                directionalLight.getColor().y = Math.max(factor, 0.9f);
                directionalLight.getColor().z = Math.max(factor, 0.5f);
            } else {
                directionalLight.setIntensity(1);
                directionalLight.getColor().x = 1;
                directionalLight.getColor().y = 1;
                directionalLight.getColor().z = 1;
            }
            double angRad = Math.toRadians(lightAngle);
            directionalLight.getDirection().x = (float) Math.sin(angRad);
            directionalLight.getDirection().y = (float) Math.cos(angRad);

            // Update view matrix
            camera.updateViewMatrix();
        } else {

        }
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));


        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        sceneLight.setDirectionalLight(directionalLight);
    }

    @Override
    public void render(Window window) {
        if (hud != null) {
            hud.updateSize(window);
        }
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        scene.cleanup();
        if (hud != null) {
            System.out.println("clean");
            hud.cleanup();
        }
    }
}
