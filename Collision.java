import java.util.ArrayList;
/**
 * 08/31/2024
 * Represents a Collision between any number of JellyPointMasses and a single edge.
 * @author moormonkey
 */
public class Collision {
    private JellyPointMass[] edge;
    private ArrayList<JellyPointMass> points;

    public Collision(JellyPointMass edge1, JellyPointMass edge2, JellyPointMass point) {
        edge = new JellyPointMass[] {edge1, edge2};
        points = new ArrayList<JellyPointMass>();
        points.add(point);
    }

    public void addPoint(JellyPointMass j) {
        points.add(j);
    }

    public void clearPoints() {
        points.clear();
    }

    public JellyPointMass[] getEdge() {
        return edge;
    }

    public void calculateForces(double deltaSeconds) {
        double thetaMotion = edge[0].pos.getAngle(edge[1].pos) + Math.PI / 2.0;
        Coordinate totalMomentum = edge[0].getMomentum().add(edge[1].getMomentum());
        double totalMass = edge[0].getMass() + edge[1].getMass();
        for (JellyPointMass pt : points) {
            totalMomentum = totalMomentum.add(pt.getMomentum());
            totalMass += pt.getMass();
        }
        Coordinate velSysPrime = totalMomentum.times(1 / totalMass);
        for (JellyPointMass pt : points) {
            pt.applyForceNormal(velSysPrime.minus(pt.getVel()).times(pt.getMass() / deltaSeconds), thetaMotion, false);
        }
        for (JellyPointMass pt : edge) {
            pt.applyForceNormal(velSysPrime.minus(pt.getVel()).times(pt.getMass() / deltaSeconds), thetaMotion, true);
        }

        
        // double mass = pointMasses[i].getMass();
        // Coordinate momentum = pointMasses[i].getVel().times(mass);
        // double edgeMass = other.pointMasses[pt1].getMass() + other.pointMasses[pt2].getMass();
        // Coordinate edgeMomentum = other.pointMasses[pt1].getVel().times(other.pointMasses[pt1].getMass()).add(other.pointMasses[pt2].getVel().times(other.pointMasses[pt2].getMass()));
        // Coordinate force = momentum.add(edgeMomentum).times(mass / (mass + edgeMass)).minus(momentum).times(1 / deltaSeconds);
        // force = Coordinate.PolarCoordinate(nearestEdge[1].getAngle(pointMasses[i].pos), Math.abs(force.getMagnitude()));
        // double existingForce = Math.cos(pointMasses[i].getForce().getAngle() - force.getAngle()) * pointMasses[i].getForce().getMagnitude();
        // force = force.minus(Coordinate.PolarCoordinate(force.getAngle(), existingForce));
        // pointMasses[i].applyForce(force);
        // other.pointMasses[pt1].applyForce(force.times(-.5));
        // other.pointMasses[pt2].applyForce(force.times(-.5));
    }
}