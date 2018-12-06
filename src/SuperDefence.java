import java.util.List;

public class SuperDefence {
    private static final float TARGET_LEFT_INNER_WALL_X = -8f;
    private final Ball ball;
    private final Player player;
    private final Game game;
    private Position playerWallStrikePoint;
    private double playerShootAngle;
    private static final Position TOP_TARGET_BAR_POSITION = new Position(MHN_AI.TARGET_LEFT_X, MHN_AI.TARGET_TOP_Y);
    private static final Position BOTTOM_TARGET_BAR_POSITION = new Position(MHN_AI.TARGET_LEFT_X, MHN_AI.TARGET_BOTTOM_Y);
    private boolean capable = false;
    private boolean capableTotal = true;
    private boolean isInside = false;

    public SuperDefence(Player player, Game game) {
        this.player = player;
        this.game = game;
        this.ball = game.getBall();
        System.out.println("///////////////////[START] SUPER DEFENCE FOR PLAYER--> " + player.getId());
        calculatePlayerWallStrikePoint();
        calculateThePlayerShootAngle();
        capable = isTheWayClean();
        System.out.println("///////////////////[END] SUPER DEFENCE FOR PLAYER--> " + player.getId());
    }

    public static SuperDefence findTheBestSuperDefence(List<SuperDefence> superDefences, Ball ball) {
        SuperDefence resultSuperDefence = null;
        SuperDefence tempSD;
        double min = Double.MAX_VALUE, tempD;

        for (int i = 0; i < superDefences.size(); i++) {
            tempSD = superDefences.get(i);
            tempD = MHN_AI.calculateDistanceBetweenTwoPoints(ball.getPosition(), tempSD.getPlayerWallStrikePoint());
            if (tempD < min) {
                min = tempD;
                resultSuperDefence = tempSD;
            }
        }
        return resultSuperDefence;
    }

    public boolean isThePlayerCapableOfSuperDefence() {
        return (capableTotal && capable);
    }

    public IndirectStrike getIndirectStrike() {
        return new IndirectStrike(MHN_AI.EMPTY_CODE, player, playerShootAngle);
    }

    private boolean isTheWayClean() {
        Player checkingPlayer;
//        if (!MHN_AI.isTheWayClean(player.getPosition(), playerWallStrikePoint, ball.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER)
//                && ball.getPosition().getY() <= MHN_AI.TARGET_TOP_Y && ball.getPosition().getY() >= MHN_AI.TARGET_BOTTOM_Y)
        if (!MHN_AI.isTheWayClean(player.getPosition(), playerWallStrikePoint, ball.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER))
            return false;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            if (player != game.getMyTeam().getPlayer(i)) {
                checkingPlayer = game.getMyTeam().getPlayer(i);
                if (!MHN_AI.isTheWayClean(player.getPosition(), playerWallStrikePoint, checkingPlayer.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
                if (!MHN_AI.isTheWayClean(playerWallStrikePoint, ball.getPosition(), checkingPlayer.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
            if (player != game.getOppTeam().getPlayer(i)) {
                checkingPlayer = game.getOppTeam().getPlayer(i);
                if (!MHN_AI.isTheWayClean(player.getPosition(), playerWallStrikePoint, checkingPlayer.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
                if (!MHN_AI.isTheWayClean(playerWallStrikePoint, ball.getPosition(), checkingPlayer.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
        }
        return true;
    }

    private void calculatePlayerWallStrikePoint() {
        double distanceA = Math.abs(MHN_AI.FIELD_MIN_X - player.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
        double distanceB = Math.abs(MHN_AI.FIELD_MIN_X - ball.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
        double xWallStrikePoint = MHN_AI.FIELD_MIN_X + (MHN_AI.PLAYER_DIAMETER / 2);
        double yWallStrikePoint = ((player.getPosition().getY() * distanceB) + (ball.getPosition().getY() * distanceA)) / (distanceA + distanceB);
//        if (player.getPosition().getY() < ball.getPosition().getY()) yWallStrikePoint *= -1;
        final Position resultPosition = new Position(xWallStrikePoint, yWallStrikePoint);
        double playerAngleTemp = MHN_AI.calculateTheAngleFromTo(player.getPosition(), resultPosition);
        double yOnTargetLine = MHN_AI.calculateTheYOnX(player.getPosition(), playerAngleTemp, MHN_AI.TARGET_LEFT_X);
        System.out.println("CALCULATED PLAYER ANGLE TEMP:   " + playerAngleTemp);
        System.out.println("CALCULATED Y ON X:              " + yOnTargetLine);
        System.out.println("CALCULATED WALL STRIKE POS:     " + resultPosition.toString());
        if (distanceA < 0
                || distanceB < 0
                || !(
                (yOnTargetLine + (MHN_AI.PLAYER_DIAMETER / 2)) > TOP_TARGET_BAR_POSITION.getY()
                        || (yOnTargetLine - (MHN_AI.PLAYER_DIAMETER / 2)) < BOTTOM_TARGET_BAR_POSITION.getY())
                ) {
            //HERE IS THE CONDITION FOR STRIKING INSIDE OF THE TARGET...
            System.out.println("HITTING INSIDE FOR PLAYER" + player.getId());
            distanceA = Math.abs(TARGET_LEFT_INNER_WALL_X - player.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
            distanceB = Math.abs(TARGET_LEFT_INNER_WALL_X - ball.getPosition().getX()) - (MHN_AI.PLAYER_DIAMETER / 2);
            xWallStrikePoint = TARGET_LEFT_INNER_WALL_X + (MHN_AI.PLAYER_DIAMETER / 2);
            yWallStrikePoint = ((player.getPosition().getY() * distanceB) + (ball.getPosition().getY() * distanceA)) / (distanceA + distanceB);
//            if (player.getPosition().getY() < ball.getPosition().getY()) yWallStrikePoint *= -1;
            playerWallStrikePoint = new Position(xWallStrikePoint, yWallStrikePoint);
            playerAngleTemp = MHN_AI.calculateTheAngleFromTo(player.getPosition(), playerWallStrikePoint);
            System.out.println("CALCULATED PLAYER ANGLE TEMP:   " + playerAngleTemp);
            System.out.println("CALCULATED WALL STRIKE POS:     " + playerWallStrikePoint);
            if (playerAngleTemp > 180)
                playerAngleTemp = 270 - playerAngleTemp;
            else
                playerAngleTemp = 180 - playerAngleTemp;
            yOnTargetLine = MHN_AI.calculateTheYOnX(playerWallStrikePoint, playerAngleTemp, MHN_AI.TARGET_LEFT_X);
            System.out.println("CALCULATED Y ON X:              " + yOnTargetLine);
            if (((yOnTargetLine + (MHN_AI.PLAYER_DIAMETER / 2)) >= TOP_TARGET_BAR_POSITION.getY()) || ((yOnTargetLine - (MHN_AI.PLAYER_DIAMETER / 2)) <= BOTTOM_TARGET_BAR_POSITION.getY()))
                capableTotal = false;
            return;
        }
        playerWallStrikePoint = resultPosition;
//        if (yWallStrikePoint + (MHN_AI.PLAYER_DIAMETER / 2) >= TOP_TARGET_BAR_POSITION.getY() || yWallStrikePoint - (MHN_AI.PLAYER_DIAMETER / 2) <= BOTTOM_TARGET_BAR_POSITION.getY()) {
//            playerWallStrikePoint = new Position(xWallStrikePoint, yWallStrikePoint);
//            return;
//        } [ORIGINAL]
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

    public static Position getTopTargetBarPosition() {
        return TOP_TARGET_BAR_POSITION;
    }

    public static Position getBottomTargetBarPosition() {
        return BOTTOM_TARGET_BAR_POSITION;
    }

    public boolean isInside() {
        return isInside;
    }
}
