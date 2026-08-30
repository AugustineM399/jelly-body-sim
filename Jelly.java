import java.util.ArrayList;
/**
 * 07/12/2024
 * A simulation of softbody physics in Java.
 * @author moormonkey
 */
public class Jelly {
    private static GraphicsFrame graphics;
    private static ArrayList<Softbody> softbodies = new ArrayList<Softbody>();
    private static double timescale = 1.0;
    private static double actualFPS = 0.0;
    private static double timeElapsed = 0.0;
    public static void main(String[] args) {
        final int FPS = 120;
        final long REFRESH_NANOSECONDS = 1000000000 / FPS;
        long lastFrame = System.nanoTime();
        long lastSecond = System.nanoTime();
        int frames = 0;
        boolean running = true;
        long deltaTime = 0L;
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-25,10), new Coordinate(-10,10), new Coordinate(0, 10), new Coordinate(10,10), new Coordinate(25,10), new Coordinate(25,0), new Coordinate(25,-10), new Coordinate(10,-10), new Coordinate(0,-10), new Coordinate(-10,-10), new Coordinate(-25,-10), new Coordinate(-25,0)}, 20, new Coordinate(0, 100), 0));
        softbodies.get(0).setShapeMatchK(10000);
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(-15, 60), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(0, 60.5), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(15, 61), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(-15, 160), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(0, 160.5), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(15, 161), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(-15, 260), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(0, 260.5), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-5,5), new Coordinate(0,5), new Coordinate(5,5), new Coordinate(5, 0), new Coordinate(5, -5), new Coordinate(0,-5), new Coordinate(-5,-5), new Coordinate(-5,0)}, 6, new Coordinate(15, 261), 0));
        softbodies.add(new Softbody(new Coordinate[] {new Coordinate(-500,600), new Coordinate(-250,0), new Coordinate(250,0), new Coordinate(500,600), new Coordinate(1000, -100), new Coordinate(-1000, -100)}, 10, new Coordinate(0, 0), 0, true));
        graphics = new GraphicsFrame(softbodies);
        // game loop - 120 Hz re-render and update game running
        while (running) {
            do deltaTime = System.nanoTime() - lastFrame;
            while (timescale == 0.0 || deltaTime < REFRESH_NANOSECONDS / timescale);  // force cap at 120 fps
            lastFrame = System.nanoTime();
            frames++;
            if (System.nanoTime() - lastSecond >= 1000000000) {
                actualFPS = frames;
                frames = 0;
                lastSecond = System.nanoTime();
            }
            deltaTime = REFRESH_NANOSECONDS; // if actual FPS is lower, game will lag to maintain 120 fps
            timeElapsed += deltaTime / 1000000000.0;
            final int PHYS_FRAMES_PER_DRAW = 30;
            deltaTime /= PHYS_FRAMES_PER_DRAW;
            for (int i = 0; i < PHYS_FRAMES_PER_DRAW; i++) {
                for (Softbody body : softbodies) {
                    if (body.totalMassZero) continue;
                    body.calculateForces(softbodies);
                }
                Softbody.calculateCollisions(deltaTime);
                for (Softbody body : softbodies) {
                    if (body.totalMassZero) continue;
                    body.updatePositions(deltaTime);
                }
                graphics.repaint();
            }
        }
    }

    public static void timescale(double scale) {
        if (timescale != 0) timescale *= scale;
        else timescale = 1.0;
    }

    public static double[] stats() {
        return new double[] {timescale, actualFPS, timeElapsed};
    }
}