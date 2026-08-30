import java.util.ArrayList;
/**
 * 07/12/2024
 * Represents a softbody in the simulation.
 * @author moormonkey
 */
public class Softbody {
    /** Stores every active collision as between an edge and a set of points. */
    private static ArrayList<Collision> currentCollisions = new ArrayList<Collision>();

    /** Kinematic objects have idealShape defined in final world coordinates; that is, it tries to stay exactly where it started.
     *  Non-kinematic objects are able to move away from their original position, and their shape-match moves with them. */
    private boolean kinematic;

    /** The shape that this Softbody "wants", which is maintained by applying spring-like forces to the points of the Softbody. */
    private Coordinate[] idealShape;
    
    /** The distance that this Softbody "wants" to maintain between one vertex and the next, for shape-matching. */
    private double[] idealDist;

    /** The location of the center of mass of the Softbody. Used for shape-matching. */
    private Coordinate centerOfMass;

    /** The spring constant of the spring forces used for shape-matching. */
    private double shapeMatchK;

    /** The number of points this Softbody has. Must be at least 3. */
    private int pointCount;

    /** The list of all of the point-masses that make up this Softbody. */
    private JellyPointMass[] pointMasses;

    /** Stores if the total mass is zero. Should always be false, because I'm not using zero-mass points. */
    public boolean totalMassZero;

    /**
     * Creates a Softbody object with all points of mass 1 that is not kinematic.
     * @param idealShape The points defining the shape that this Softbody is "supposed to be". Shape-matching will force the Softbody into this shape.
     * @param origin The position of the Softbody, in world coordinates. Note that this acts as the center of rotation when creating the body, but is not used for simulation.
     * @param rot The rotation of the body, in radians.
     */
    public Softbody(Coordinate[] idealShape, Coordinate origin, double rot) {
        this(idealShape, 1.0, origin, rot, false);
    }

    /**
     * Creates a Softbody object that is not kinematic.
     * @param idealShape The points defining the shape that this Softbody is "supposed to be". Shape-matching will force the Softbody into this shape.
     * @param mass The mass of each point on the Softbody.
     * @param origin The position of the Softbody, in world coordinates. Note that this acts as the center of rotation when creating the body, but is not used for simulation.
     * @param rot The rotation of the body, in radians.
     */
    public Softbody(Coordinate[] idealShape, double mass, Coordinate origin, double rot) {
        this(idealShape, mass, origin, rot, false);
    }

    /**
     * Creates a Softbody object with all points of equal mass.
     * @param idealShape The points defining the shape that this Softbody is "supposed to be". Shape-matching will force the Softbody into this shape.
     * @param mass The mass of each point.
     * @param origin The position of the Softbody, in world coordinates. Note that this acts as the center of rotation when creating the body, but is not used for simulation.
     * @param rot The rotation of the body, in radians.
     * @param kinematic If the object is kinematic, or has an idealShape defined in world coordinates that does not move.
     */
    public Softbody(Coordinate[] idealShape, double mass, Coordinate origin, double rot, boolean kinematic) {
        this.idealShape = idealShape;
        this.kinematic = kinematic;
        pointCount = idealShape.length;
        pointMasses = new JellyPointMass[pointCount];
        idealDist = new double[pointCount];
        totalMassZero = mass == 0;
        for (int i = 0; i < pointCount; i++) {
            pointMasses[i] = new JellyPointMass(Coordinate.PolarCoordinate(idealShape[i].getAngle() + rot, idealShape[i].getMagnitude()).add(origin), mass, kinematic);
            idealDist[i] = idealShape[i].pythagoreanDistance(idealShape[(i + 1) % pointCount]);
        }
        centerOfMass = new Coordinate(0, 0);
        updateCOM();
        shapeMatchK = 2000;
        for (int i = 0; i < pointCount; i++) {
            if (kinematic) idealShape[i] = pointMasses[i].pos.copy();
            else idealShape[i] = pointMasses[i].pos.minus(centerOfMass);
        }
    }

    /**
     * Calculates forces, velocities, and ultimately positions of every point on the Softbody.
     * @param others All Softbody objects in the current game, including this one (which is ignored in collision calculations, obviously).
     * @param deltaTime The change in time to use when calculating velocity and position from force and velocity, respectively, in NANOSECONDS.
     */
    public void calculateForces(ArrayList<Softbody> others) {
        // gravity and friction are handled in JellyPointMass
        // shape matching - actualPosition to idealShape
        if (kinematic) {
            for (int i = 0; i < pointCount; i++) {
                pointMasses[i].applyForce(idealShape[i].minus(pointMasses[i].pos).times(shapeMatchK));
            }
        } else {
            Coordinate rotationSum = new Coordinate(0, 0);
            for (int i = 0; i < pointCount; i++) {
                double angleDiff = pointMasses[i].pos.getAngle(centerOfMass) - idealShape[i].getAngle();
                rotationSum = rotationSum.add(new Coordinate(Math.cos(angleDiff), Math.sin(angleDiff)));
            }
            for (int i = 0; i < pointCount; i++) {
                Coordinate idealPoint = Coordinate.PolarCoordinate(rotationSum.getAngle() + idealShape[i].getAngle(), idealShape[i].getMagnitude()).add(centerOfMass);
                pointMasses[i].applyForce(idealPoint.minus(pointMasses[i].pos).times(shapeMatchK));
            }
        }
        // shape matching - edges as springs
        for (int i = 0; i < pointCount; i++) {
            Coordinate prev = i == 0 ? pointMasses[pointCount - 1].pos : pointMasses[i - 1].pos;
            Coordinate now = pointMasses[i].pos;
            Coordinate next = i == pointCount - 1 ? pointMasses[0].pos : pointMasses[i + 1].pos;
            pointMasses[i].applyForce(Coordinate.PolarCoordinate(prev.getAngle(now), -.5 * 1 * (idealDist[i == 0 ? pointCount - 1 : i - 1] - prev.pythagoreanDistance(now))));
            pointMasses[i].applyForce(Coordinate.PolarCoordinate(next.getAngle(now), -.5 * 1 * (idealDist[i] - next.pythagoreanDistance(now))));
        }
        // TODO: springs
        // TODO: collision system that robustly handles multiple points colliding with one edge
        for (Softbody other : others) {
            if (this == other) continue; // ignore itself for collisions
            // if we count an odd number of intersections, we are colliding with "other".
            for (int i = 0; i < pointCount; i++) {
                int count = 0;
                for (int otherEdge = 0; otherEdge < other.pointCount; otherEdge++) {
                    if (positiveXRaycast(pointMasses[i].pos, other.pointMasses[otherEdge].pos, other.pointMasses[(otherEdge + 1) % (other.pointCount)].pos)) {
                        count++;
                    }
                }
                if (count % 2 == 1) {
                    // collision!
                    Coordinate[] otherPos = new Coordinate[other.getNumPoints()];
                    for (int l = 0; l < other.getNumPoints(); l++) {
                        otherPos[l] = other.pointMasses[l].pos;
                    }
                    Coordinate[] nearestEdge = findNearestEdge(pointMasses[i].pos, otherPos);
                    JellyPointMass pt1 = other.pointMasses[(int)nearestEdge[0].x];
                    JellyPointMass pt2 = other.pointMasses[(int)nearestEdge[0].y];
                    boolean makeNewCollision = true;
                    for (Collision col : currentCollisions) {
                        if (col.getEdge()[0] == pt1 && col.getEdge()[1] == pt2 || col.getEdge()[1] == pt1 && col.getEdge()[0] == pt2) {
                            makeNewCollision = false;
                            col.addPoint(pointMasses[i]);
                            break;
                        }
                    }
                    if (makeNewCollision) {
                        currentCollisions.add(new Collision(pt1, pt2, pointMasses[i]));
                    }
                }
            }
        }
    }

    public static void calculateCollisions(double deltaTime) {
        double deltaSeconds = deltaTime / 1000000000.0;
        for (Collision col : currentCollisions) {
            col.calculateForces(deltaSeconds);
        }
        currentCollisions.clear();
    }

    public void updatePositions(long deltaTime) {
        double deltaSeconds = deltaTime / 1000000000.0;
        for (JellyPointMass point : pointMasses) {
            point.updatePosition(deltaSeconds);
        }
        updateCOM();
    }
    /**
     * Updates this Softbody's COM coordinate to actually be where it is supposed to be.
     * If the object has no mass, take the average position of all points instead.
     */
    public void updateCOM() {
        centerOfMass = new Coordinate();
        // all points have mass == 0, so just use the unweighted position average
        if (totalMassZero) { 
            for (JellyPointMass point : pointMasses) {
                centerOfMass = centerOfMass.add(point.pos.times(1 / pointCount));
            }
        } else { // some points have mass, so do the weighted average. Not affected by massless points.
            double totalMass = 0.0;
            for (JellyPointMass point : pointMasses) {
                centerOfMass = centerOfMass.add(point.pos.times(point.getMass()));
                totalMass += point.getMass();
            }
            centerOfMass = centerOfMass.times(1 / totalMass);
        }
    }

    /**
     * Checks if a ray, starting from the origin and extending infinitely in the +x direction, intersects with the line defined by two Coordinate points.
     * @param origin The point from which the infinite +x ray is drawn.
     * @param point1 One of two points that form the line that we are checking for intersections.
     * @param point2 One of two points that form the line that we are checking for intersections.
     * @return If an intersection was found.
     */
    public boolean positiveXRaycast(Coordinate origin, Coordinate point1, Coordinate point2) {
        if (point1.x < origin.x && point2.x < origin.x) return false; // both are behind xMin
        if (point1.y > origin.y && point2.y > origin.y || point1.y < origin.y && point2.y < origin.y) return false; // both are either above or below y
        if (point1.y - point2.y == 0) return false; // horizontal line
        double reciprocalSlope = (point1.x - point2.x) / (point1.y - point2.y); // dx / dy
        // calculate the x-value of the line at the given y-value, and return if it is greater than or equal to xMin
        return reciprocalSlope * (origin.y - point1.y) + point1.x >= origin.x;
    }

    /**
     * Finds the nearest edge, and returns two Coordinates:
     * <ul>
     * <li> Index 0: the indices of the edge, where x is the index of the first point and y is the index of the second point.
     * <li> Index 1: the nearest point on the edge to the given point
     * </ul>
     * @param point The single Coordinate representing the point from which the nearest edge is measured.
     * @param edges An array of Coordinates that represents all points making up the edges that we are checking from.
     * @return Two Coordinates, as described above.
     */
    public Coordinate[] findNearestEdge(Coordinate point, Coordinate[] edges) {
        Coordinate[] result = new Coordinate[2];
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < edges.length; i++) {
            Coordinate point1 = edges[i];
            Coordinate point2 = edges[(i+1) % (edges.length)];
            if (point1.y - point2.y == 0) {
                // horizontal line
                double distance = Math.abs(point1.y - point.y);
                if (distance < minDist) {
                    result[0] = new Coordinate(i, (i+1) % (edges.length));
                    result[1] = new Coordinate(point.x, point1.y);
                    minDist = distance;
                }
            } else if (point1.x - point2.x == 0) {
                // vertical line
                double distance = Math.abs(point1.x - point.x);
                if (distance < minDist) {
                    result[0] = new Coordinate(i, (i+1) % (edges.length));
                    result[1] = new Coordinate(point1.x, point.y);
                    minDist = distance;
                }
            } else {
                double slope = (point1.y - point2.y) / (point1.x - point2.x);
                double nearPointXValue = (slope * point1.x + point.x / slope + point.y - point1.y) / (slope + 1 / slope);
                double nearPointYValue = slope * (nearPointXValue - point1.x) + point1.y;
                Coordinate nearPoint = new Coordinate(nearPointXValue, nearPointYValue);
                double distance = point.pythagoreanDistance(nearPoint);
                if (distance < minDist) {
                    result[0] = new Coordinate(i, (i+1) % (edges.length));
                    result[1] = nearPoint;
                    minDist = distance;
                }
            }
        }
        return result;
    }

    /**
     * Updates the shape-match spring constant.
     * @param k The new k-value to use.
     */
    public void setShapeMatchK(double k) {
        this.shapeMatchK = k;
    }
    
    /**
     * This function is only used for drawing the Softbody onscreen.
     * @return The x-values of each point on the Softbody.
     */
    public double[] getPointsX() {
        double[] result = new double[pointCount];
        for (int i = 0; i < result.length; i++) {
            result[i] = pointMasses[i].pos.x;
        }
        return result;
    }
    
    /**
     * This function is only used for drawing the Softbody onscreen.
     * @return The y-values of each point on the Softbody.
     */
    public double[] getPointsY() {
        double[] result = new double[pointCount];
        for (int i = 0; i < result.length; i++) {
            result[i] = pointMasses[i].pos.y;
        }
        return result;
    }

    /**
     * @return The number of points on this Softbody.
     */
    public int getNumPoints() {
        return pointCount;
    }

    /**
     * @return The center of mass (COM) of this Softbody.
     */
    public Coordinate getCOM() {
        return centerOfMass;
    }

    /**
     * Used for rendering idealShape.
     * @return Two double[]: one for the x-coordinates of each point on the idealShape and one for the y-coordinates.
     */
    public double[][] getShapeMatchXY() {
        double[][] result = new double[2][pointCount];
        if (kinematic) {
            for (int i = 0; i < pointCount; i++) {
                result[0][i] = idealShape[i].x;
                result[1][i] = idealShape[i].y;
            }
        } else {
            Coordinate rotationSum = new Coordinate(0, 0);
            for (int i = 0; i < pointCount; i++) {
                double angleDiff = pointMasses[i].pos.getAngle(centerOfMass) - idealShape[i].getAngle();
                rotationSum = rotationSum.add(new Coordinate(Math.cos(angleDiff), Math.sin(angleDiff)));
            }
            for (int i = 0; i < pointCount; i++) {
                Coordinate idealPoint = new Coordinate(Math.cos(rotationSum.getAngle() + idealShape[i].getAngle()) * idealShape[i].getMagnitude(), Math.sin(rotationSum.getAngle() + idealShape[i].getAngle()) * idealShape[i].getMagnitude());
                idealPoint = idealPoint.add(centerOfMass);
                result[0][i] = idealPoint.x;
                result[1][i] = idealPoint.y;
            }
        }
        return result;
    }
}
