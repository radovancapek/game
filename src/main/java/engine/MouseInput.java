package engine;

import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {
    private final Vector2d previousPos;

    private final Vector2d currentPos;

    private final Vector2f displVec;

    private boolean inWindow = false;

    private boolean leftButtonPressed = false;

    private boolean rightButtonPressed = false;

    private boolean leftButtonReleased = true;

    private boolean rightButtonReleased = true;

    public MouseInput() {
        previousPos = new Vector2d(-1, -1);
        currentPos = new Vector2d(0, 0);
        displVec = new Vector2f();
    }

    public void init(Window window) {
        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xpos, ypos) -> {
            currentPos.x = xpos;
            currentPos.y = ypos;
        });
        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> {
            inWindow = entered;
        });
        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            if(inWindow && button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS && !window.isCursorInGame()) {
                glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                window.setCursorInGame(true);
            } else if(window.isCursorInGame()) {
                leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
                rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
                leftButtonReleased = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE;
                rightButtonReleased = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_RELEASE;
            }

        });
    }

    public Vector2f getDisplVec() {
        return displVec;
    }

    public Vector2d getCurrentPos() {
        return currentPos;
    }

    public void input(Window window) {
        displVec.x = 0;
        displVec.y = 0;
        if (inWindow) {
            double deltax = currentPos.x - previousPos.x;
            double deltay = currentPos.y - previousPos.y;
            boolean rotateX = deltax != 0;
            boolean rotateY = deltay != 0;
            if (rotateX) {
                displVec.y = (float) deltax;
            }
            if (rotateY) {
                displVec.x = (float) deltay;
            }
        }
        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    public boolean isLeftButtonReleased() {
        return leftButtonReleased;
    }

    public boolean isRightButtonReleased() {
        return rightButtonReleased;
    }

    public boolean isInWindow() {
        return inWindow;
    }

    public void setInWindow(boolean inWindow) {
        this.inWindow = inWindow;
    }
}
