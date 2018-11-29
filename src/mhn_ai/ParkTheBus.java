package mhn_ai;

import uiai.*;

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
    private final Game game;
    private List<Position> destinations = new ArrayList<>();
    private List<Defence> defences = new ArrayList<>();


    public ParkTheBus(Game game) {
        this.game = game;
        List<Player> forwardPlayers = getForwardPlayers(game.getMyTeam());
        if (forwardPlayers.size() == 0)
            return;
        findFirstHoles();
        calculateDefences(forwardPlayers);
    }


    private void calculateDefences(List<Player> players) {
        for (int i = 0; i < destinations.size(); i++) {
            for (int j = 0; j < players.size(); j++) {
                if (isTheWayOfPlayerClean(players.get(j), destinations.get(i)))
                    defences.add(new Defence(destinations.get(i), players.get(j)));
            }
        }
    }

    private boolean isTheWayOfPlayerClean(Player player, Position destinationPoint) {
        if (!MHN_AI.isTheWayClean(player.getPosition(), destinationPoint, game.getBall().getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER))
            return false;
        Position checkingPosition;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            if (player != game.getMyTeam().getPlayer(i)) {
                checkingPosition = game.getMyTeam().getPlayer(i).getPosition();
                if (!MHN_AI.isTheWayClean(player.getPosition(), destinationPoint, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
            if (player != game.getOppTeam().getPlayer(i)) {
                checkingPosition = game.getOppTeam().getPlayer(i).getPosition();
                if (!MHN_AI.isTheWayClean(player.getPosition(), destinationPoint, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
        }
        return true;
    }

    private List<Player> getForwardPlayers(Team team) {
        List<Player> resultPlayers = new ArrayList<>();
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            if (isForwardPlayer(team.getPlayer(i)))
                resultPlayers.add(team.getPlayer(i));
        }
        return resultPlayers;
    }

    private boolean isForwardPlayer(Player player) {
        return player.getPosition().getX() > SCAN_THRESHOLD_END;
    }


    private void findFirstHoles() {
        final double topAngle = MHN_AI.calculateTheAngleFromTo(CENTER, TARGET_TOP);
        final double bottomAngle = MHN_AI.calculateTheAngleFromTo(CENTER, TARGET_BOTTOM);
        int steps = (int) ((360 - bottomAngle + topAngle) / SCAN_STEP);
        Position holePoint;
        for (double angle = bottomAngle; steps > 0; angle += SCAN_STEP, steps--) {
            if (angle >= 360)
                angle = 0;
            holePoint = getTheFirstHoleForAngle(angle);
            if (holePoint != null)
                destinations.add(holePoint);
        }
    }

    private Position getTheFirstHoleForAngle(double angle) {
        Position point;
        for (double xPos = SCAN_THRESHOLD_BEGIN + (SCAN_STEP / 2), yPos; xPos <= SCAN_THRESHOLD_END - (SCAN_STEP / 2); xPos += SCAN_STEP) {
//            point = new Position(xPos, xPos + SCAN_STEP)
            yPos = MHN_AI.calculateTheYOnX(CENTER, angle, xPos);
            point = new Position(xPos, yPos); //would be much better if the 'Position' class has setters...
            if (isThePointEmpty(point))
                return point;
        }
        return null;
    }

    private boolean isThePointEmpty(Position point) {
        Position checkingPoint;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            checkingPoint = game.getMyTeam().getPlayer(i).getFirstPosition(); //Checking My Players!
            if (MHN_AI.calculateDistanceBetweenTwoPoints(point, checkingPoint) <= MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS)
                return false;
            checkingPoint = game.getOppTeam().getPlayer(i).getFirstPosition(); //Checking The Players of enemy!
            if (MHN_AI.calculateDistanceBetweenTwoPoints(point, checkingPoint) <= MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS)
                return false;
        }
        return true;
    }

    public List<Defence> getDefences() {
        return defences;
    }

    private class Defence {
        private final Position destinationPoint;
        private final Player player;
        private double playerShootAngle;
        private int playerShootPower;

        public Defence(Position destinationPoint, Player player) {
            this.destinationPoint = destinationPoint;
            this.player = player;
            calculatePlayerShootAngle();
            calculatePlayerShootPower();
        }

        private void calculatePlayerShootPower() {
            final double distance = MHN_AI.calculateDistanceBetweenTwoPoints(player.getPosition(), destinationPoint);
            playerShootPower = MHN_AI.getPowerByDistance(distance);
        }

        private void calculatePlayerShootAngle() {
            playerShootAngle = MHN_AI.calculateTheAngleFromTo(player.getPosition(), destinationPoint);
        }

        public Position getDestinationPoint() {
            return destinationPoint;
        }

        public Player getPlayer() {
            return player;
        }

        public double getPlayerShootAngle() {
            return playerShootAngle;
        }

        public Defence setPlayerShootAngle(double playerShootAngle) {
            this.playerShootAngle = playerShootAngle;
            return this;
        }

        public int getPlayerShootPower() {
            return playerShootPower;
        }

        public Defence setPlayerShootPower(int playerShootPower) {
            this.playerShootPower = playerShootPower;
            return this;
        }
    }
}
