package mhn_ai;

import javafx.geometry.Pos;
import org.omg.PortableServer.POA;
import uiai.Ball;
import uiai.Game;
import uiai.Player;
import uiai.Position;

import java.util.ArrayList;
import java.util.List;

public class ParkTheBus {
    private static final float SCAN_STEP = MHN_AI.DIRECT_SHOOT_CHECK_STEP;
    private static final Position TARGET_TOP = new Position(MHN_AI.TARGET_LEFT_X, MHN_AI.TARGET_TOP_Y);
    private static final Position TARGET_BOTTOM = new Position(MHN_AI.TARGET_LEFT_X, MHN_AI.TARGET_BOTTOM_Y);
    private static final Position CENTER = new Position(-8, 0);
    private static final float SCAN_RADIUS_BEGIN = 1.75f;
    private static final float SCAN_RADIUS_END = 4f;
    private static final float SCAN_THRESHOLD_BEGIN = -7f;
    private static final float SCAN_THRESHOLD_END = -4f;
    private final Ball ball;
    private final Game game;
    private final Player player;
    private List<Position> destinations = new ArrayList<>();

    public ParkTheBus(Game game, Player player) {
        this.game = game;
        this.player = player;
        this.ball = game.getBall();
    }

    private void calculateTheHoles() {

    }

    private List<Position> findHoleAngles() {
        List<Position> result = new ArrayList<>();
        final double topAngle = MHN_AI.calculateTheAngleFromTo(CENTER, TARGET_TOP);
        final double bottomAngle = MHN_AI.calculateTheAngleFromTo(CENTER, TARGET_BOTTOM);
        int steps = (int) ((360 - bottomAngle + topAngle) / SCAN_STEP);
        Position holePoint;
        for (double angle = bottomAngle; steps > 0; angle += SCAN_STEP, steps--) {
            if (angle >= 360)
                angle = 0;
            holePoint = getTheFirstHoleForAngle(angle);
            if (holePoint != null)
                result.add(holePoint);
        }
        return result;
    }

    private Position getTheFirstHoleForAngle(double angle) {
        Position point;
        for (double xPos = SCAN_THRESHOLD_BEGIN + (SCAN_STEP / 2), yPos; xPos <= SCAN_THRESHOLD_END - (SCAN_STEP / 2); xPos += SCAN_STEP) {
//            point = new Position(xPos, xPos + SCAN_STEP)
            yPos = MHN_AI.calculateTheYOnX(CENTER, angle, xPos);
            point = new Position(xPos, yPos);
            if (isThePointEmpty(point))
                return point;
        }
        return null;
    }

    private boolean isThePointEmpty(Position point) {
        return false;
    }


    private class Hole {
        private final double angle;
        private final double distanceFromCenter;
        private Position emptyPoint;

        public Hole(double angle, double distanceFromCenter) {
            this.angle = angle;
            this.distanceFromCenter = distanceFromCenter;
        }

        public double getAngle() {
            return angle;
        }

        public double getDistanceFromCenter() {
            return distanceFromCenter;
        }

        public Position getEmptyPoint() {
            return emptyPoint;
        }

        public Hole setEmptyPoint(Position emptyPoint) {
            this.emptyPoint = emptyPoint;
            return this;
        }
    }
}
