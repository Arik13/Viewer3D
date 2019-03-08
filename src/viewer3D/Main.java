package viewer3D;

import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import viewer3D.GraphicsEngine.*;
import viewer3D.GUI.*;
import static viewer3D.GraphicsEngine.Direction.*;
import viewer3D.Math.Matrix;
import viewer3D.Math.Vector;

/**
 *
 * @author Arik Dicks
 */
public class Main {
    static BufferedImage image;
    /**
     * Creates a GUI containing the camera view, its control panel, and tables showing 
     * all polygon information, and updates the camera when the user presses keys
     * @param args Not used
     */
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        
        // Make World
        WorldSpace world = new TestWorldSpace();
        
        // Make Frame
        JFrame frame = new JFrame();
        //System.out.println(frame.getGraphicsConfiguration().getDevice().getAvailableAcceleratedMemory());
        
        
        // Get effective screen size
        Dimension screenSize = getScreenDimension(frame);

        // Set Camera View dimensions
        int width = 400;
        int height = width;
        
        // Make Camera
        Polygon[] polygons = world.getPolygons();
        Camera camera = new Camera(polygons, width, height, frame.getGraphicsConfiguration());
        Polygon[] screenSpacePolygons = camera.observe();
        image = camera.getBufferedImage();
        
        // Make Camera Control Panel
        int controlPanelHeight = 100;
        CameraControlPanel cameraControlPanel = new CameraControlPanel(camera);
        cameraControlPanel.setPreferredSize(new Dimension(width, controlPanelHeight));
        cameraControlPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK, 2, false), new EmptyBorder(10, 10, 10, 10)));
        
        // Make Camera View Component
        CameraViewComponent cameraViewComponent = new CameraViewComponent(camera, screenSpacePolygons, width, height);
        cameraViewComponent.updateData(camera.getData());
        cameraViewComponent.setPreferredSize(new Dimension(width, height));

        // Make Camera Panel
        JPanel cameraPanel = new JPanel();
        cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.Y_AXIS));
        cameraPanel.setPreferredSize(new Dimension(width, height + controlPanelHeight));
        cameraPanel.add(cameraViewComponent);
        cameraPanel.add(cameraControlPanel);

        // Make Polygon Control Panel
//        PolygonOutputPanel projectedPolygonOutputPanel = new PolygonOutputPanel("Screenspace Polygons");
//        projectedPolygonOutputPanel.setPolygons(screenSpacePolygons);
//        projectedPolygonOutputPanel.setPreferredSize(new Dimension((int)Math.round(screenSize.getWidth()-width-20), height + controlPanelHeight));
        
        // Make Master Panel
        JPanel masterPanel = new JPanel();
        masterPanel.add(cameraPanel);
        //masterPanel.add(projectedPolygonOutputPanel);
        
        frame.add(masterPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        //frame.setLocationRelativeTo(null);
        
        JComponent imageComponent = new JComponent() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                
                //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                //System.out.println(g2.drawImage(camera.getBufferedImage(), 0, 0, this));
                while(!g2.drawImage(image, 0, 0, this)){};
            }
//            @Override
//            public void update(Graphics g) {
//                paint(g);
//            }
//            @Override
//            public void paint(Graphics g) {
//                g.drawImage(camera.getBufferedImage(), 0, 0, null);
//            }
        };
        //imageComponent;
        imageComponent.setDoubleBuffered(true);
        JFrame imageFrame = new JFrame();
        
//        System.out.println(imageFrame.getBufferStrategy());
        imageFrame.getRootPane().setDoubleBuffered(true);
        imageFrame.add(imageComponent);
        imageFrame.setSize(width, height);
        imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        imageFrame.setVisible(true);
        imageFrame.setLocationRelativeTo(null);
        imageFrame.createBufferStrategy(2);
        
//        JFrame imageFrame = new JFrame();
//        imageFrame.setSize(width, height);
//        imageFrame.pack();
//        imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        imageFrame.setVisible(true);
        
        // Make keyboard listener
        KeyListener kl = new KeyListener();
        
        // Time
        int lastTenthOfASecond = 0;
        double lastMilliSecond = 0;
        double currentMilliSecond = System.currentTimeMillis();
        double averageFrameRate = 0;
        double currentFrameRate = 0;
        long totalTime = 0;
        long totalFrames = 0;
        
        // Redraw camera view upon keyboard input
        while(true) {   
            int currentTenthOfASecond = (int)((currentMilliSecond%1000)/100);
            if (currentMilliSecond/100 > lastMilliSecond/100) {
                boolean cameraMoved = false;
                if (kl.isWPressed()) {
                    camera.move(FORWARD); 
                    cameraMoved = true;
                }
                if (kl.isSPressed()) {
                    camera.move(BACKWARD); 
                    cameraMoved = true;
                }
                if (kl.isAPressed()) {
                    camera.move(LEFT); 
                    cameraMoved = true;
                }
                if (kl.isDPressed()) {
                    camera.move(RIGHT); 
                    cameraMoved = true;
                }
                if (kl.isSpacePressed()) {
                    camera.move(UP); 
                    cameraMoved = true;
                }
                if (kl.isShiftPressed()) {
                    camera.move(DOWN); 
                    cameraMoved = true;
                }
//                if (kl.isQPressed()) {
//                    camera.rotate(YAW_LEFT, 1);
//                    cameraMoved = true;
//                }
//                if (kl.isEPressed()) {
//                    camera.rotate(YAW_RIGHT, 1); 
//                    cameraMoved = true;
//                }
//                if (cameraControlPanel.getWasCameraChanged() || projectedPolygonOutputPanel.changedSelection() || cameraViewComponent.wasUpdated()) {
//                    cameraMoved = true;
//                }
                if (cameraControlPanel.getWasCameraChanged() || cameraViewComponent.wasUpdated()) {
                    cameraMoved = true;
                }
                if (cameraMoved) {
                    image = camera.getBufferedImage();
                    imageComponent.repaint();
                    screenSpacePolygons = camera.observe();
                    cameraViewComponent.drawPolygons(screenSpacePolygons);
                    //projectedPolygonOutputPanel.setPolygons(screenSpacePolygons);
                    cameraControlPanel.update();   
                    totalFrames++;
                    totalTime += currentMilliSecond-lastMilliSecond + 1;
                    averageFrameRate = (totalFrames)/(totalTime/1000.0); 
                    currentFrameRate = (1000.0/(currentMilliSecond-lastMilliSecond));
                    
//                    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
//                    GraphicsDevice[] devices = env.getScreenDevices();
//
//                    int sequence = 1;
//                    for (GraphicsDevice device : devices) {
//                        System.out.println("Screen Number [" + (sequence++) + "]");
//                        System.out.println("Width       : " + device.getDisplayMode().getWidth());
//                        System.out.println("Height      : " + device.getDisplayMode().getHeight());
//                        System.out.println("Refresh Rate: " + device.getDisplayMode().getRefreshRate());
//                        System.out.println("Bit Depth   : " + device.getDisplayMode().getBitDepth());
//                        System.out.println("");
//                    }
                }
            }
            if (currentTenthOfASecond > lastTenthOfASecond) {
                
                String[] cameraData = camera.getData();
                String[] frameData = {
                    "Average Framerate: " + String.format("%.2f", averageFrameRate), 
                    "Current Framerate: " + String.format("%.2f",currentFrameRate)
                };
                
                String[] data = new String[cameraData.length + frameData.length];
                System.arraycopy(cameraData, 0, data, 0, cameraData.length);
                System.arraycopy(frameData, 0, data, cameraData.length, frameData.length);
                cameraViewComponent.updateData(data);
            }   
            lastTenthOfASecond = currentTenthOfASecond;
            lastMilliSecond = currentMilliSecond;
            currentMilliSecond = System.currentTimeMillis(); 
        }
    }
    public static Dimension getScreenDimension(JFrame frame) {
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        int top = screenInsets.top;
        int bottom = screenInsets.bottom;
        int left = screenInsets.left;
        int right = screenInsets.right;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.setSize(screenSize.getWidth()-left-right, screenSize.getHeight()-top-bottom);
        return screenSize;
    }
}