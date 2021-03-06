package viewer3D.GraphicsEngine;

import viewer3D.Polyhedrons.Axes3D;
import viewer3D.Polyhedrons.Floor;
import viewer3D.Polyhedrons.Pyramid;
import viewer3D.Polyhedrons.*;

public class TestWorldSpace extends WorldSpace{
    public TestWorldSpace() {
        int xSquares = 40;
        int zSquares = 40;
        int xWidth = 30;
        int zWidth = 30;
        int startingX = 0-(xWidth*xSquares)/2;
        int startingZ = 0-(zWidth*zSquares)/2;
//        int xAxisLength = (xWidth*xSquares)/2 + 100;
//        int zAxisLength = (zWidth*zSquares)/2 + 100;
//        super.add(new Axes3D(xAxisLength, -1*xAxisLength, 500, -500, zAxisLength, -1*zAxisLength));
        super.add(new Floor(xSquares, zSquares, xWidth, zWidth, startingX, startingZ));

        super.add(new Pyramid(150, 1, 150, 20));
        super.add(new Pyramid(-150, 1, 150, 20));
        super.add(new Pyramid(150, 1, -150, 20));
        super.add(new Pyramid(-150, 1, -150, 20));
//        super.add(new BigAssPolygon());
    }
}
