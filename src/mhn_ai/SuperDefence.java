package mhn_ai;

import uiai.Ball;
import uiai.Game;
import uiai.Player;
import uiai.Position;

public class SuperDefence {
    private final Ball ball;
    private final Player player;
    private final Game game;
    private Position playerWallStrikePoint;
    private double playerShootAngle;
    private static final Position topTargetPosition = new Position(MHN_AI.TARGET_LEFT_X, MHN_AI.TARGET_TOP_Y);
    private static final Position bottomTargetPosition = new Position(MHN_AI.TARGET_LEFT_X, MHN_AI.TARGET_BOTTOM_Y);

    public SuperDefence(Ball ball, Player player, Game game) {
        this.ball = ball;
        this.player = player;
        this.game = game;
    }

    public boolean canPlayerDoSuperDefence() {
        return false;
    }


    private boolean isTheWayClean() {
        Player checkingPlayer;
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
        final double distanceA = Math.abs(MHN_AI.FIELD_MIN_X - player.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
        final double distanceB = Math.abs(MHN_AI.FIELD_MIN_X - ball.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
        final double xWallStrikePoint = MHN_AI.FIELD_MIN_X + (MHN_AI.PLAYER_DIAMETER / 2);
        final double yWallStrikePoint = ((player.getPosition().getY() * distanceB) + (ball.getPosition().getY() * distanceA)) / (distanceA + distanceB);
        if (yWallStrikePoint >= topTargetPosition.getY() || yWallStrikePoint <= bottomTargetPosition.getY()) {
            playerWallStrikePoint = new Position(xWallStrikePoint, yWallStrikePoint);
            return;
        }
        //TODO==> WRITE ANOTHER CONDITION! (URGENT)
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
}
