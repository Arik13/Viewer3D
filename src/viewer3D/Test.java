package viewer3D;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Line2D;
import javax.swing.JFrame;
import viewer3D.Math.Vector;

public class Test {
    Vector[][] points;
    Vector[] vertices;
    public Test() {
        points = new Vector[][] {
            {new Vector(new double[]{0, 0, 0}), new Vector(new double[]{1, 0, 0}), new Vector(new double[]{2, 0, 0}), new Vector(new double[]{3, 0, 0})}, 
            {new Vector(new double[]{0, 1, 0}), new Vector(new double[]{1, 1, 0}), new Vector(new double[]{2, 1, 0}), new Vector(new double[]{3, 1, 0})},
            {new Vector(new double[]{0, 2, 0}), new Vector(new double[]{1, 2, 0}), new Vector(new double[]{2, 2, 0}), new Vector(new double[]{3, 2, 0})},
            {new Vector(new double[]{0, 3, 0}), new Vector(new double[]{1, 3, 0}), new Vector(new double[]{2, 3, 0}), new Vector(new double[]{3, 3, 0})}
        };
        vertices = new Vector[] {
            new Vector(new double[]{0.5, 0.5, 0}),
            new Vector(new double[]{0.5, 2, 0}),
            new Vector(new double[]{2, 1, 0})
        };
        recursiveThing();
        
//        JFrame frame = new JFrame();
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        int width = 300;
//        int height = width;
//        frame.setSize(new Dimension(width, height));
        //Line2D.Double line1 = new Line2D.Double(new Point(width, height));
    }
    public void recursiveThing() {
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[0].length; j++) {
                System.out.println("" + points[i][j] + " : " + isInTriangle(points[i][j]));
            }
        }
    }
    public boolean isInTriangle(Vector point) {
        // AB x AP, BC x BP, CA x CP
        Vector vector1 = vertices[0].subtract(vertices[1]).cross(vertices[0].subtract(point));
        Vector vector2 = vertices[1].subtract(vertices[2]).cross(vertices[1].subtract(point));
        Vector vector3 = vertices[2].subtract(vertices[0]).cross(vertices[2].subtract(point));
        System.out.println("\t" + vector1);
        System.out.println("\t" + vector2);
        System.out.println("\t" + vector3);
        if (vector1.getComponent(2) > 0 && vector2.getComponent(2) > 0 && vector3.getComponent(2) > 0) {
            return true;
        }
        return false;
    }
}