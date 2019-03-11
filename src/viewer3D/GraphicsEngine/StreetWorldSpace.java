package viewer3D.GraphicsEngine;

import viewer3D.Polyhedrons.Cuboid;
import viewer3D.Polyhedrons.Floor;

public class StreetWorldSpace extends WorldSpace {
    public StreetWorldSpace() {
        int xSquares = 40;
        int zSquares = 40;
        int xWidth = 30;
        int zWidth = 30;
        int startingX = 0-(xWidth*xSquares)/2;
        int startingZ = 0-(zWidth*zSquares)/2;
        //super.add(new Floor(xSquares, zSquares, xWidth, zWidth, startingX, startingZ));
        super.add(new Cuboid(0, 0, 0, 100, 300, 100));
        super.add(new Cuboid(-200, 0, 0, 100, 300, 100));
        super.add(new Cuboid(0, 0, -200, 100, 300, 100));
        super.add(new Cuboid(-200, 0, -200, 100, 300, 100));
        super.add(new Cuboid(0, 0, 400, 100, 300, 100));
        super.add(new Cuboid(-200, 0, 400, 100, 300, 100));
        super.add(new Cuboid(0, 0, 200, 100, 300, 100));
        super.add(new Cuboid(-200, 0, 200, 100, 300, 100));
//        super.add(new Cuboid(-50, 0, 300, 100, 300, 100));
    }
}
