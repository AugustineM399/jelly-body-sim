/**
 * 08/31/2024
 * Represents a single point on a Softbody object.
 * @author moormonkey
 */
public class JellyPointMass {
    public Coordinate pos;
    private double mass;
    private Coordinate vel;
    private Coordinate grav;
    private Coordinate force;
    private boolean kinematic;

    public JellyPointMass(Coordinate pos, double mass, boolean kinematic) {
        this.pos = pos;
        this.mass = mass;
        this.kinematic = kinematic;
        vel = new Coordinate();
        grav = new Coordinate(0, -9.81);
        force = new Coordinate();
    }

    public void updatePosition(double deltaSeconds) {
        if (mass != 0) {
            if (!kinematic) force = force.add(grav.times(mass));
            vel = vel.add(force.times(deltaSeconds / mass));
        }
        pos = pos.add(vel.times(deltaSeconds));
        force = new Coordinate();
    }

    public double getMass() {
        return mass;
    }

    public void applyForce(Coordinate otherForce) {
        force = force.add(otherForce);
    }

    public void applyForceNormal(Coordinate otherForce, double thetaMotion, boolean edge) {
        double existingForce = Math.cos(force.add(grav.times(mass)).getAngle() - thetaMotion) * force.add(grav.times(mass)).getMagnitude();
        double magnitude = otherForce.getMagnitude() * Math.cos(otherForce.getAngle() - thetaMotion) - Math.abs(existingForce);
        if (edge && magnitude > 0) {
            force = force.add(Coordinate.PolarCoordinate(thetaMotion, magnitude));
        } else if (!edge && magnitude < 0) {
            force = force.add(Coordinate.PolarCoordinate(thetaMotion, magnitude));
        }
    }

    public Coordinate getVel() {
        return vel;
    }

    public Coordinate getForce() {
        return force;
    }

    public Coordinate getMomentum() {
        return vel.times(mass);
    }
}
