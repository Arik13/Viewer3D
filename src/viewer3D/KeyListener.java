package viewer3D;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

/**
 *
 * @author Dicks
 */
public class KeyListener {
        private volatile boolean wPressed = false;
        private volatile boolean aPressed = false;
        private volatile boolean sPressed = false;
        private volatile boolean dPressed = false;
        private volatile boolean spacePressed = false;
        private volatile boolean shiftPressed = false;
        private volatile boolean qPressed = false;
        private volatile boolean ePressed = false;
        
    /**
     *
     */
    public KeyListener() {
            getKeyboardFocus();
        }

    /**
     *
     * @return
     */
    public boolean isWPressed() {
            synchronized (KeyListener.class) {
                return wPressed;
            }
        }

    /**
     *
     * @return
     */
    public boolean isAPressed() {
            synchronized (KeyListener.class) {
                return aPressed;
            }
        }

    /**
     *
     * @return
     */
    public boolean isSPressed() {
            synchronized (KeyListener.class) {
                return sPressed;
            }
        }

    /**
     *
     * @return
     */
    public boolean isDPressed() {
            synchronized (KeyListener.class) {
                return dPressed;
            }
        }

    /**
     *
     * @return
     */
    public boolean isSpacePressed() {
            synchronized (KeyListener.class) {
                return spacePressed;
            }
        }

    /**
     *
     * @return
     */
    public boolean isShiftPressed() {
            synchronized (KeyListener.class) {
                return shiftPressed;
            }
        }

    /**
     *
     * @return
     */
    public boolean isQPressed() {
            synchronized (KeyListener.class) {
                return qPressed;
            }
        }

    /**
     *
     * @return
     */
    public boolean isEPressed() {
            synchronized (KeyListener.class) {
                return ePressed;
            }
        }
        
    /**
     *
     */
    public void getKeyboardFocus() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                synchronized (KeyListener.class) {
                    switch (ke.getID()) {
                        case KeyEvent.KEY_PRESSED:
                            switch (ke.getKeyCode()) {
                                case KeyEvent.VK_W:
                                    wPressed = true;
                                    break;
                                case KeyEvent.VK_A:
                                    aPressed = true;
                                    break;
                                case KeyEvent.VK_S:
                                    sPressed = true;
                                    break;
                                case KeyEvent.VK_D:
                                    dPressed = true;
                                    break;
                                case KeyEvent.VK_SPACE:
                                    spacePressed = true;
                                    break;
                                case KeyEvent.VK_SHIFT:
                                    shiftPressed = true;
                                    break;
                                case KeyEvent.VK_Q:
                                    qPressed = true;
                                    break;
                                case KeyEvent.VK_E:
                                    ePressed = true;
                                    break;
                                default:
                                    break;
                            }
                                break;
                        case KeyEvent.KEY_RELEASED:
                            switch (ke.getKeyCode()) {
                                case KeyEvent.VK_W:
                                    wPressed = false;
                                    break;
                                case KeyEvent.VK_A:
                                    aPressed = false;
                                    break;
                                case KeyEvent.VK_S:
                                    sPressed = false;
                                    break;
                                case KeyEvent.VK_D:
                                    dPressed = false;
                                    break;
                                case KeyEvent.VK_SPACE:
                                    spacePressed = false;
                                    break;
                                case KeyEvent.VK_SHIFT:
                                    shiftPressed = false;
                                    break;
                                case KeyEvent.VK_Q:
                                    qPressed = false;
                                    break;
                                case KeyEvent.VK_E:
                                    ePressed = false;
                                    break;
                                default:
                                    break;
                        }
                            break;
                    }
                    return false;
                }
            }
        });
    }
}