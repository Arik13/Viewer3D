package viewer3D.Math;

import java.awt.Point;

/**
 * 
 * @author Arik Dicks
 */
public class Vector {
    private double[] components;
    private double zDistance;

    /**
     *
     * @param n
     */
    public Vector(int n) {
        components = new double[n];
    }

    /**
     *
     * @param components
     */
    public Vector(double[] components) {
        this.components = components;
    }

    /**
     *
     * @param n
     * @return
     */
    public double getComponent(int n) {
        return components[n];
    }

    /**
     *
     * @return
     */
    public Point getPoint() {
        return new Point((int)Math.round(components[0]), (int)Math.round(components[1]));
    }

    /**
     *
     * @return
     */
    public Vector getUnitVector() {
        
        Vector unitVector = new Vector(components.length);
        double length = getLength();
        for (int i = 0; i < unitVector.components.length; i++) {
            unitVector.components[i] = components[i]/length;
        }
        boolean isZero = true;
        for (int i = 0; i < components.length; i++) {
            if (components[i] != 0) {
                isZero = false;
            }
        }
        if (isZero) {
            return this;
        }
        return unitVector;
    }

    /**
     *
     * @param otherVector
     * @return
     */
    public double getAngle(Vector otherVector) {
        return Math.acos((this.dot(otherVector))/(this.getLength()*otherVector.getLength()));
    }

    /**
     *
     * @return
     */
    public double getLength() {
        double length = 0;
        for (int i = 0; i < components.length; i++) {
            length += Math.pow(components[i],2);
        }
        return Math.sqrt(length);
    }

    /**
     *
     * @return
     */
    public double getZDistance() {
        return zDistance;
    }

    /**
     *
     * @param value
     * @param n
     */
    public void setComponent(double value, int n) {
        components[n] = value;
    }

    /**
     *
     * @param givenComponents
     */
    public void setComponents(double[] givenComponents) {
        for (int i = 0; i < components.length; i++) {
            components[i] = givenComponents[i];
        }
    }

    /**
     *
     * @param zDistance
     */
    public void setZDistance(double zDistance) {
        this.zDistance = zDistance;
    }

    /**
     *
     * @param otherVector
     * @return
     */
    public Vector add(Vector otherVector) {
        Vector sumVector = new Vector(components.length);
        for (int i = 0; i < sumVector.components.length; i++) {
            sumVector.components[i] = this.components[i] + otherVector.components[i];
        }
        return sumVector;
    }

    /**
     *
     * @param otherVector
     * @return
     */
    public Vector subtract(Vector otherVector) {
        Vector sumVector = new Vector(components.length);
        for (int i = 0; i < sumVector.components.length; i++) {
            sumVector.components[i] = this.components[i] - otherVector.components[i];
        }
        return sumVector;
    }

    /**
     *
     * @param scalar
     * @return
     */
    public Vector multiply(double scalar) {
        Vector newVector = new Vector(components.length);
        for (int i = 0; i < components.length; i++) {
            newVector.setComponent(scalar*components[i], i);
        }
        return newVector;
    }

    /**
     *
     * @param matrix
     * @return
     */
    public Vector multiply(Matrix matrix) {
        Vector resultingVector = new Vector(components.length);
        for (int i = 0; i < matrix.getNumOfRows(); i++) {
            for (int j = 0; j < matrix.getNumOfColumns(); j++) {
                resultingVector.components[i] += matrix.getElement(i, j) * components[j];
            }
        }
        return resultingVector;
    }

    /**
     *
     * @param otherVector
     * @return
     */
    public double dot(Vector otherVector) {
        double product = 0;
        for (int i = 0; i < components.length; i++) {
            product += components[i]*otherVector.components[i];
        }
        return product;
    }

    /**
     *
     * @param otherVector
     * @return
     */
    public Vector cross(Vector otherVector) {
        double[] newComponents = new double[3];
        newComponents[0] = this.components[1]*otherVector.components[2] - this.components[2]*otherVector.components[1];
        newComponents[1] = this.components[2]*otherVector.components[0] - this.components[0]*otherVector.components[2];
        newComponents[2] = this.components[0]*otherVector.components[1] - this.components[1]*otherVector.components[0];
        return new Vector(newComponents);
    }
    /*
    Uses the Rodrigues' rotation formula
    */

    /**
     *
     * @param axisOfRotation
     * @param angle
     * @return
     */

    public Vector rotate(Vector axisOfRotation, double angle) {
        Vector term1 = this.multiply(Math.cos(angle));
        Vector term2 = ((axisOfRotation.cross(this)).multiply(Math.sin(angle)));
        Vector term3 = axisOfRotation.multiply(axisOfRotation.dot(this)*(1-Math.cos(angle)));
        return term1.add(term2).add(term3);
    }

    /**
     *
     * @return
     */
    public Vector copy() {
        Vector newVector = new Vector(components.length);
        return newVector.add(this);
    }
    @Override
    public String toString() {
        StringBuilder vectorString;
        if (components == null || components.length == 0)
            return null;
        //String vectorString = "{";
        vectorString = new StringBuilder("{");
        for (int i = 0; i < components.length; i++) {
            //vectorString += String.format("%.2f", components[i])  + ", ";
            vectorString.append(String.format("%.2f", components[i]));
            if (i+1 != components.length) {
                vectorString.append(", ");
            }
        }
        //return vectorString.substring(0, vectorString.length()-2)+"}";
        return vectorString.append("}").toString();
    }
}