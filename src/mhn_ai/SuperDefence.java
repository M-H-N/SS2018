package mhn_ai;

import uiai.Ball;
import uiai.Game;
import uiai.Player;
import uiai.Position;

public class SuperDefence {
    private static final float TARGET_LEFT_INNER_WALL_X = -8f;
    private final Ball ball;
    private final Player player;
    private final Game game;
    private Position playerWallStrikePoint;
    private double playerShootAngle;
    private static final Position topTargetPosition = new Position(MHN_AI.TARGET_LEFT_X, MHN_AI.TARGET_TOP_Y);
    private static final Position bottomTargetPosition = new Position(MHN_AI.TARGET_LEFT_X, MHN_AI.TARGET_BOTTOM_Y);
    private boolean capable = false;

    public SuperDefence(Player player, Game game) {
        this.ball = game.getBall();
        this.player = player;
        this.game = game;
        calculatePlayerWallStrikePoint();
        calculateThePlayerShootAngle();
        capable = isTheWayClean();
    }

    public boolean isThePlayerCapableOfSuperDefence() {
        return capable;
    }

    public IndirectStrike getIndirectStrike() {
        return new IndirectStrike(MHN_AI.EMPTY_CODE, player, playerShootAngle);
    }

    private boolean isTheWayClean() {
        Player checkingPlayer;
        if (!MHN_AI.isTheWayClean(player.getPosition(), playerWallStrikePoint, ball.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER))
            return false;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            if (i != player.getId()) {
                checkingPlayer = game.getMyTeam().getPlayer(i);
                if (!MHN_AI.isTheWayClean(player.getPosition(), playerWallStrikePoint, checkingPlayer.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
                if (!MHN_AI.isTheWayClean(playerWallStrikePoint, ball.getPosition(), checkingPlayer.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
            checkingPlayer = game.getOppTeam().getPlayer(i);
            if (!MHN_AI.isTheWayClean(player.getPosition(), playerWallStrikePoint, checkingPlayer.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                return false;
            if (!MHN_AI.isTheWayClean(playerWallStrikePoint, ball.getPosition(), checkingPlayer.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                return false;
        }
        return true;
    }

    private void calculatePlayerWallStrikePoint() {
        double distanceA = Math.abs(MHN_AI.FIELD_MIN_X - player.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
        double distanceB = Math.abs(MHN_AI.FIELD_MIN_X - ball.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
        double xWallStrikePoint = MHN_AI.FIELD_MIN_X + (MHN_AI.PLAYER_DIAMETER / 2);
        double yWallStrikePoint = ((player.getPosition().getY() * distanceB) + (ball.getPosition().getY() * distanceA)) / (distanceA + distanceB);
        if (yWallStrikePoint >= topTargetPosition.getY() || yWallStrikePoint <= bottomTargetPosition.getY()) {
            playerWallStrikePoint = new Position(xWallStrikePoint, yWallStrikePoint);
            return;
        }
        //TODO==> WRITE ANOTHER CONDITION! (URGENT)
        distanceA = Math.abs(TARGET_LEFT_INNER_WALL_X - player.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
        distanceB = Math.abs(TARGET_LEFT_INNER_WALL_X - ball.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
        xWallStrikePoint = TARGET_LEFT_INNER_WALL_X + (MHN_AI.PLAYER_DIAMETER / 2);
        yWallStrikePoint = ((player.getPosition().getY() * distanceB) + (ball.getPosition().getY() * distanceA)) / (distanceA + distanceB);
        playerWallStrikePoint = new Position(xWallStrikePoint, yWallStrikePoint);
    }

    private void calculateThePlayerShootAngle() {
        if (playerWallStrikePoint == null)
            calculatePlayerWallStrikePoint();
        playerShootAngle = MHN_AI.calculateTheAngleFromTo(player.getPosition(), playerWallStrikePoint);
    }


    public Ball getBall() {
        return ball;
    }

    public Player getPlayer() {
        return player;
    }

    public static float getTargetLeftInnerWallX() {
        return TARGET_LEFT_INNER_WALL_X;
    }

    public Game getGame() {
        return game;
    }

    public Position getPlayerWallStrikePoint() {
        return playerWallStrikePoint;
    }

    public double getPlayerShootAngle() {
        return playerShootAngle;
    }

    public static Position getTopTargetPosition() {
        return topTargetPosition;
    }

    public static Position getBottomTargetPosition() {
        return bottomTargetPosition;
    }

    public boolean isCapable() {
        return capable;
    }
}
