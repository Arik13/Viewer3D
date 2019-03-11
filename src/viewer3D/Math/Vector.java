package viewer3D.Math;

import java.awt.Point;

/**
 * A class that describes an n-dimensional Vector, and the operations that can be 
 * conducted on a Vector.
 * @author Arik Dicks
 */
public class Vector {
    private double[] components;

    /**
     * Constructs a vector of n components.
     * @param n The number of components
     */
    public Vector(int n) {
        components = new double[n];
    }

    /**
     * Constructs a vector with components equivalent to the contents of the given double array
     * @param components The numbers specifying the components of the Vector
     */
    public Vector(double[] components) {
        this.components = components;
    }

    /**
     * Returns the n'th component
     * @param n The index of the desired component
     * @return The component at the given index
     */
    public double getComponent(int n) {
        return components[n];
    }

    /**
     * Returns a 2D point with the first two components of the Vector as the x and y
     * @return
     */
    public Point getPoint() {
        return new Point((int)Math.round(components[0]), (int)Math.round(components[1]));
    }

    /**
     * Returns a vector of equivalent direction to this vector with a magnitude of 1
     * @return a vector of equivalent direction to this vector with a magnitude of 1
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
     * Returns the angle between this Vector and the given Vector
     * @param otherVector Another vector
     * @return the angle between this Vector and the given Vector
     */
    public double getAngle(Vector otherVector) {
        return Math.acos((this.dot(otherVector))/(this.getLength()*otherVector.getLength()));
    }

    /**
     * Returns the length of this vector
     * @return the length of this vector
     */
    public double getLength() {
        double length = 0;
        for (int i = 0; i < components.length; i++) {
            length += Math.pow(components[i],2);
        }
        return Math.sqrt(length);
    }

    /**
     * Sets the value of the nth component to the given value
     * @param value The value of the nth component
     * @param n The index of the desired component
     */
    public void setComponent(double value, int n) {
        components[n] = value;
    }

    /**
     * Sets the components of this vector to the doubles in the given array
     * @param givenComponents Components of a vector
     */
    public void setComponents(double[] givenComponents) {
        System.arraycopy(givenComponents, 0, components, 0, components.length);
    }

    /**
     * Returns the Vector that is the sum of this Vector and the given Vector
     * @param otherVector Another vector
     * @return the Vector that is the sum of this Vector and the given Vector
     */
    public Vector add(Vector otherVector) {
        Vector sumVector = new Vector(components.length);
        for (int i = 0; i < sumVector.components.length; i++) {
            sumVector.components[i] = this.components[i] + otherVector.components[i];
        }
        return sumVector;
    }

    /**
     * Returns the Vector that is the difference of this Vector and the given Vector
     * @param otherVector Another vector
     * @return the Vector that is the difference of this Vector and the given Vector
     */
    public Vector subtract(Vector otherVector) {
        Vector sumVector = new Vector(components.length);
        for (int i = 0; i < sumVector.components.length; i++) {
            sumVector.components[i] = this.components[i] - otherVector.components[i];
        }
        return sumVector;
    }

    /**
     * Returns a vector that is the product of this vector and the given scalar
     * @param scalar A scalar
     * @return a vector that is the product of this vector and the given scalar
     */
    public Vector multiply(double scalar) {
        Vector newVector = new Vector(components.length);
        for (int i = 0; i < components.length; i++) {
            newVector.setComponent(scalar*components[i], i);
        }
        return newVector;
    }

    /**
     * Returns a vector that is the product of this vector and the given matrix
     * @param matrix A matrix
     * @return a vector that is the product of this vector and the given matrix
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
     * Returns the dot product of this vector and the given vector
     * @param otherVector Another vector
     * @return the dot product of this vector and the given vector
     */
    public double dot(Vector otherVector) {
        double product = 0;
        for (int i = 0; i < components.length; i++) {
            product += components[i]*otherVector.components[i];
        }
        return product;
    }

    /**
     * Returns the cross product of this vector and the given vector
     * @param otherVector Another vector
     * @return the cross product of this vector and the given vector
     */
    public Vector cross(Vector otherVector) {
        double[] newComponents = new double[3];
        newComponents[0] = this.components[1]*otherVector.components[2] - this.components[2]*otherVector.components[1];
        newComponents[1] = this.components[2]*otherVector.components[0] - this.components[0]*otherVector.components[2];
        newComponents[2] = this.components[0]*otherVector.components[1] - this.components[1]*otherVector.components[0];
        return new Vector(newComponents);
    }
    /**
     * Returns the vector that is this vector rotated by the given angle around the given axis
     * @param axisOfRotation A vector around which this vector will be rotated
     * @param angle An angle
     * @return the vector that is this vector rotated by the given angle around the given axis
     */
    // Uses the Rodrigues' rotation formula
    public Vector rotate(Vector axisOfRotation, double angle) {
        Vector term1 = this.multiply(Math.cos(angle));
        Vector term2 = ((axisOfRotation.cross(this)).multiply(Math.sin(angle)));
        Vector term3 = axisOfRotation.multiply(axisOfRotation.dot(this)*(1-Math.cos(angle)));
        return term1.add(term2).add(term3);
    }

    /**
     * Returns a copy of this vector
     * @return a copy of this vector
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
        vectorString = new StringBuilder("{");
        for (int i = 0; i < components.length; i++) {
            vectorString.append(String.format("%.2f", components[i]));
            if (i+1 != components.length) {
                vectorString.append(", ");
            }
        }
        return vectorString.append("}").toString();
    }
}