/**
 * 07/12/2024
 * Represents a specific point in 2D space with double precision.
 * @author moormonkey
 */
public class Coordinate {
    public double x;
    public double y;

    /**
     * Creates a Coordinate with position (0, 0).
     */
    public Coordinate() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Creates a Coordinate with position (x, y).
     * @param x The x-value.
     * @param y The y-value.
     */
    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    
    /**
     * Converts from polar coordinates and returns a Cartesian Coordinate.
     * Note that different arguments can give equivalent results, such as (pi, 10) and (0, -10).
     * @param theta The rotation, where +x axis is zero, in radians.
     * @param dist The distance from the origin of this Coordinate.
     */
    public static Coordinate PolarCoordinate(double theta, double dist) {
        return new Coordinate(Math.cos(theta) * dist, Math.sin(theta) * dist);
    }

    /**
     * Creates a copy of the Coordinate and returns it.
     * @return The created copy.
     */
    public Coordinate copy() {
        return new Coordinate(x, y);
    }

    /**
     * Adds two Coordinate objects by adding the x and then the y.
     * @param other The other Coordinate to add to this one.
     * @return The Coordinate <code>(this.x + other.x, this.y + other.y)</code>.
     */
    public Coordinate add(Coordinate other) {
        return new Coordinate(this.x + other.x, this.y + other.y);
    }
    
    /**
     * Subtracts other from this, returning the result.
     * @param other The other Coordinate to subtract from this one.
     * @return The Coordinate <code>(this.x - other.x, this.y - other.y)</code>.
     */
    public Coordinate minus(Coordinate other) {
        return new Coordinate(this.x - other.x, this.y - other.y);
    }

    /**
     * Multiplies this * scalar, returning the result
     * @param scalar The scalar by which to multiply this Coordinate.
     * @return The Coordinate <code>(this.x * scalar, this.y * scalar)</code>.
     */
    public Coordinate times(double scalar) {
        return new Coordinate(x * scalar, y * scalar);
    }

    /**
     * @param other The other point in 2D space to compare to.
     * @return The Pythagorean distance between the two points (as in, a^2 + b^2 = c^2 -> this method returns c).
     */
    public double pythagoreanDistance(Coordinate other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    /**
     * @return The Pythagorean magnitude of this point (as in, a^2 + b^2 = c^2 -> this method returns c).
     */
    public double getMagnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * @return This point's angle from the +x axis.
     */
    public double getAngle() {
        return Math.atan2(y, x);
    }

    /**
     * @param origin The origin to use when calculating the angle.
     * @return This point's angle from the +x axis with the given origin.
     */
    public double getAngle(Coordinate origin) {
        return Math.atan2(y - origin.y, x - origin.x);
    }

    /**
     * @return This point as a String of the form (x, y).
     */
    @Override
    public String toString() {
        return x + ", " + y;
    }
}
