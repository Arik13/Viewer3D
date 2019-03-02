package viewer3D;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import viewer3D.GraphicsEngine.*;
import viewer3D.GUI.*;
import viewer3D.Math.Vector;
import viewer3D.Polyhedrons.*;

/**
 *
 * @author Arik Dicks
 */
public class Main {

    /**
     * Creates a GUI containing the camera view, its control panel, and tables showing 
     * all polygon information, and updates the camera when the user presses keys
     * @param args Not used
     */
    public static void main(String[] args) {
        Vector[][] points = new Vector[][] {
            {new Vector(new double[]{0, 0, 0}), new Vector(new double[]{1, 0, 0}), new Vector(new double[]{2, 0, 0}), new Vector(new double[]{3, 0, 0})}, 
            {new Vector(new double[]{0, 1, 0}), new Vector(new double[]{1, 1, 0}), new Vector(new double[]{2, 1, 0}), new Vector(new double[]{3, 1, 0})},
            {new Vector(new double[]{0, 2, 0}), new Vector(new double[]{1, 2, 0}), new Vector(new double[]{2, 2, 0}), new Vector(new double[]{3, 2, 0})},
            {new Vector(new double[]{0, 3, 0}), new Vector(new double[]{1, 3, 0}), new Vector(new double[]{2, 3, 0}), new Vector(new double[]{3, 3, 0})}
        };
        Vector[] vertices = new Vector[] {
            new Vector(new double[]{0.5, 0.5, 0}),
            new Vector(new double[]{0.5, 3, 0}),
            new Vector(new double[]{2, 1, 0})
        };
        VectorDrawer2D test = new VectorDrawer2D(points, vertices);
        
//        
//        System.setProperty("sun.java2d.opengl", "true");
//        
//        // Make World
//        WorldSpace world = new TestWorldSpace();
//        
//        // Make Frame
//        JFrame frame = new JFrame();
//        
//        // Get effective screen size
//        Dimension screenSize = getScreenDimension(frame);
//
//        // Make Camera
//        Polygon[] polygons = world.getPolygons();
//        Camera camera = new Camera(polygons);
//        Polygon[] projectedPolygons = camera.observe();
//        
//
//        // Set Camera View dimensions
//        int width = 601;
//        int height = width;
//        
//        // Make Camera Control Panel
//        int controlPanelHeight = 100;
//        CameraControlPanel cameraControlPanel = new CameraControlPanel(camera);
//        cameraControlPanel.setPreferredSize(new Dimension(width, controlPanelHeight));
//        cameraControlPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK, 2, false) ,new EmptyBorder(10, 10, 10, 10)));
//        
//        // Make Camera View Component
//        CameraViewComponent cameraViewComponent = new CameraViewComponent(projectedPolygons, width, height);
//        cameraViewComponent.updateData(camera.getData());
//        cameraViewComponent.setPreferredSize(new Dimension(width, height));
//
//        // Make Camera Panel
//        JPanel cameraPanel = new JPanel();
//        cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.Y_AXIS));
//        cameraPanel.setPreferredSize(new Dimension(width, height + controlPanelHeight));
//        cameraPanel.add(cameraViewComponent);
//        cameraPanel.add(cameraControlPanel);
//
//        PolygonOutputPanel projectedPolygonOutputPanel = new PolygonOutputPanel("Screenspace Polygons");
//        projectedPolygonOutputPanel.setPolygons(projectedPolygons);
//        projectedPolygonOutputPanel.setPreferredSize(new Dimension((int)Math.round(screenSize.getWidth()-width-20), height + controlPanelHeight));
//        
//        // Make Master Panel
//        JPanel masterPanel = new JPanel();
//        masterPanel.add(cameraPanel);
//        masterPanel.add(projectedPolygonOutputPanel);
//        
//        frame.add(masterPanel);
//        frame.pack();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setVisible(true);
//        
//        // Make keyboard listener
//        KeyListener kl = new KeyListener();
//        
//        // Time
//        int lastTenthOfASecond = 0;
//        double lastMilliSecond = 0;
//        double currentMilliSecond = System.currentTimeMillis();
//        double averageFrameRate = 0;
//        double currentFrameRate = 0;
//        long totalTime = 0;
//        long totalFrames = 0;
//        
//        // Redraw camera view upon keyboard input
//        while(true) {   
//            int currentTenthOfASecond = (int)((currentMilliSecond%1000)/100);
//            if (currentMilliSecond/100 > lastMilliSecond/100) {
//                boolean cameraMoved = false;
//                if (kl.isWPressed()) {
//                    camera.move(Direction.FORWARD); 
//                    cameraMoved = true;
//                }
//                if (kl.isAPressed()) {
//                    camera.move(Direction.LEFT); 
//                    cameraMoved = true;
//                }
//                if (kl.isSPressed()) {
//                    camera.move(Direction.BACKWARD); 
//                    cameraMoved = true;
//                }
//                if (kl.isDPressed()) {
//                    camera.move(Direction.RIGHT); 
//                    cameraMoved = true;
//                }
//                if (kl.isSpacePressed()) {
//                    camera.move(Direction.UP); 
//                    cameraMoved = true;
//                }
//                if (kl.isShiftPressed()) {
//                    camera.move(Direction.DOWN); 
//                    cameraMoved = true;
//                }
//                if (kl.isQPressed()) {
//                    camera.rotate(Direction.LEFT);
//                    cameraMoved = true;
//                }
//                if (kl.isEPressed()) {
//                    camera.rotate(Direction.RIGHT); 
//                    cameraMoved = true;
//                }
//                if (cameraControlPanel.getWasCameraChanged() || projectedPolygonOutputPanel.changedSelection()) {
//                    cameraMoved = true;
//                }
//                if (cameraMoved) {
//                    projectedPolygons = camera.observe();
//                    cameraViewComponent.drawPolygons(projectedPolygons);
//                    projectedPolygonOutputPanel.setPolygons(projectedPolygons);
//                    cameraControlPanel.update();   
//                    totalFrames++;
//                    totalTime += currentMilliSecond-lastMilliSecond + 1;
//                    averageFrameRate = (totalFrames)/(totalTime/1000.0); 
//                    currentFrameRate = (1000.0/(currentMilliSecond-lastMilliSecond));
//                }
//            }
//            if (currentTenthOfASecond > lastTenthOfASecond) {
//                
//                String[] cameraData = camera.getData();
//                String[] frameData = {
//                    "Average Framerate: " + String.format("%.2f", averageFrameRate), 
//                    "Current Framerate: " + String.format("%.2f",currentFrameRate)
//                };
//                
//                String[] data = new String[cameraData.length + frameData.length];
//                System.arraycopy(cameraData, 0, data, 0, cameraData.length);
//                System.arraycopy(frameData, 0, data, cameraData.length, frameData.length);
//                cameraViewComponent.updateData(data);
//            }   
//            lastTenthOfASecond = currentTenthOfASecond;
//            lastMilliSecond = currentMilliSecond;
//            currentMilliSecond = System.currentTimeMillis(); 
//        }
//    }
//    public static Dimension getScreenDimension(JFrame frame) {
//        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
//        int top = screenInsets.top;
//        int bottom = screenInsets.bottom;
//        int left = screenInsets.left;
//        int right = screenInsets.right;
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        screenSize.setSize(screenSize.getWidth()-left-right, screenSize.getHeight()-top-bottom);
//        return screenSize;
    }
}