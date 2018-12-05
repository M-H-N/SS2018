

public class IndirectStrike {
    private Ball ball;
    private final double ballAngle;
    private Player player;
    private Position strikePlayerPosition;
    private Position wallStrikePoint;
    private double playerShootAngle = MHN_AI.FAILED_CODE;

    public IndirectStrike(Ball ball, double ballAngle, Player player) {
        this.ball = ball;
        this.ballAngle = ballAngle;
        this.player = player;
    }

    public IndirectStrike(double ballAngle, Player player, double playerShootAngle) {
        this.ballAngle = ballAngle;
        this.player = player;
        this.playerShootAngle = playerShootAngle;
    }

    public void calculatePlayerShootAngle() {
//        System.out.println("CALCULATING THE PLAYER(" + player.getId() + ") INDIRECT STRIKE ANGLE FOR BALL-ANGLE(" + ballAngle + ")");
        calculateStrikePlayerPosition();
        calculateWallStrikePoint();

//        System.out.println(">>>>>>>>>>>>>>>>>>>THE PLAYER POSITION:                 " + player.getPosition());
//        System.out.println(">>>>>>>>>>>>>>>>>>>THE BALL POSITION:                   " + ball.getPosition());
//        System.out.println(">>>>>>>>>>>>>>>>>>>THE BALL SHOOT ANGLE:                " + ballAngle);
//        System.out.println(">>>>>>>>>>>>>>>>>>>THE EXPECTED WALL STRIKE POSITION:   " + wallStrikePoint.toString());
//        System.out.println(">>>>>>>>>>>>>>>>>>>THE EXPECTED PLAYER STRIKE POSITION: " + strikePlayerPosition.toString());

        final double M = MHN_AI.calculateTheMBetween2Points(player.getPosition(), wallStrikePoint);
        double result = Math.abs(Math.toDegrees(Math.atan(M)));
//        System.out.println("THE PURE ABSOLUTE ANGLE IS: " + result);
        if (wallStrikePoint.getX() > player.getPosition().getX()) {
            if (wallStrikePoint.getY() < player.getPosition().getY())
                result = 360 - result;
        } else {
            if (wallStrikePoint.getY() < player.getPosition().getY())
                result += 180;
            else
                result = 180 - result;
        }
        playerShootAngle = result;
//        System.out.println("THE PLAYER INDIRECT SHOOT ANGLE IS:     " + playerShootAngle);
    }

    protected boolean canThePlayerStrikeIndirectly() {
        if ((ballAngle > 270 || ballAngle < 90) && player.getPosition().getX() > ball.getPosition().getX())
            return false;
        if (ballAngle > 90 && ballAngle < 270 && player.getPosition().getX() < ball.getPosition().getX())
            return false;
        if (MHN_AI.FIELD_MAX_Y - ball.getPosition().getY() < MHN_AI.PLAYER_DIAMETER)
            return false;
        if (ball.getPosition().getY() - MHN_AI.FIELD_MIN_Y < MHN_AI.PLAYER_DIAMETER)
            return false;
        if (wallStrikePoint != null) {
            if ((ball.getPosition().getY() >= (MHN_AI.FIELD_MAX_Y - (MHN_AI.PLAYER_DIAMETER / 2))) || (ball.getPosition().getY() < (MHN_AI.FIELD_MIN_Y + (MHN_AI.PLAYER_DIAMETER / 2))))
                return false;
        }
        return true;
    }

    private void calculateStrikePlayerPosition() {
//        System.out.println("CALCULATING THE STRIKE PLAYER POINT");
        double xPos = ball.getPosition().getX() - (Math.cos(Math.toRadians(ballAngle)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        double yPos = ball.getPosition().getY() - (Math.sin(Math.toRadians(ballAngle)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        strikePlayerPosition = new Position(xPos, yPos);
//        System.out.println("THE CALCULATED STRIKE PLAYER POSITION IS:   " + strikePlayerPosition.toString());
    }

    public boolean isThePlayerWayToTheBallClean(Game game) {
        if (playerShootAngle == MHN_AI.FAILED_CODE || wallStrikePoint == null)
            calculatePlayerShootAngle();
        return (isThePlayerWayToTheWallClean(game) && isThePlayerWayFromWallToTheBallIsClean(game) && thresholdFromWallToTheBall());
    }


    private boolean thresholdFromWallToTheBall() {
        final double angleFromWallToTheBall = MHN_AI.calculateTheAngleFromTo(wallStrikePoint, strikePlayerPosition);
        return (Math.abs(ballAngle - angleFromWallToTheBall) < MHN_AI.DIRECT_SHOOT_THRESHOLD_ANGLE);
    }

    //    private void calculateWallStrikePoint() {
//        final double valC = Math.abs(strikePlayerPosition.getX() - player.getPosition().getX());
//        final double valA = valC / (1 + (valC / player.getPosition().getY()));
//        final double valB = valC - valA;
//        if (ballAngle > 0 && ballAngle < 180)
//            wallStrikePoint = new Position(player.getPosition().getX() + valB, MHN_AI.PLAYER_DIAMETER / 2);
//        else
//            wallStrikePoint = new Position(player.getPosition().getX() + valB, MHN_AI.FIELD_MAX_Y - (MHN_AI.PLAYER_DIAMETER / 2));
//        System.out.println("THE CALCULATE WALL STRIKE POINT IS:     " + wallStrikePoint.toString());
//    }
//    private void calculateWallStrikePoint() {
//        final double valC = strikePlayerPosition.getX() - player.getPosition().getX();
//        final double valB = valC / (1 + (strikePlayerPosition.getY() / player.getPosition().getY()));
////        final double valA = valC - valB;
//        if (ballAngle > 0 && ballAngle < 180)
//            wallStrikePoint = new Position(player.getPosition().getX() + valB, MHN_AI.FIELD_MIN_Y - (MHN_AI.PLAYER_DIAMETER / 2));
//        else
//            wallStrikePoint = new Position(player.getPosition().getX() + valB, MHN_AI.FIELD_MAX_Y - (MHN_AI.PLAYER_DIAMETER / 2));
//        System.out.println("THE CALCULATE WALL STRIKE POINT IS:     " + wallStrikePoint.toString());
//    }
    private void calculateWallStrikePoint() {
        double distanceA, distanceB;
        double yWallStrikePos;
        if (ballAngle < 180) { //THRESHOLD IS BOTTOM (MIRROR IS DOWN)
            distanceA = Math.abs(Math.abs(MHN_AI.FIELD_MIN_Y - player.getPosition().getY()) - (MHN_AI.PLAYER_DIAMETER / 2)); //FOR PLAYER POSITION
            distanceB = Math.abs(Math.abs(MHN_AI.FIELD_MIN_Y - strikePlayerPosition.getY()) - (MHN_AI.PLAYER_DIAMETER / 2)); //FOR EXPECTED STRIKING PLAYER POSITION
//            distanceA = Math.abs(MHN_AI.FIELD_MIN_Y - player.getPosition().getY()); //FOR PLAYER POSITION
//            distanceB = Math.abs(MHN_AI.FIELD_MIN_X - strikePlayerPosition.getY()); //FOR EXPECTED STRIKING PLAYER POSITION
            yWallStrikePos = MHN_AI.FIELD_MIN_Y + (MHN_AI.PLAYER_DIAMETER / 2);
        } else { //THRESHOLD IS TOP (MIRROR IS UP)
            distanceA = Math.abs(Math.abs(MHN_AI.FIELD_MAX_Y - player.getPosition().getY()) - (MHN_AI.PLAYER_DIAMETER / 2)); //FOR PLAYER POSITION
            distanceB = Math.abs(Math.abs(MHN_AI.FIELD_MAX_Y - strikePlayerPosition.getY() - (MHN_AI.PLAYER_DIAMETER / 2))); //FOR EXPECTED STRIKING PLAYER POSITION
            yWallStrikePos = MHN_AI.FIELD_MAX_Y - (MHN_AI.PLAYER_DIAMETER / 2);
        }
        double xWallStrikePos = (player.getPosition().getX() * distanceB + strikePlayerPosition.getX() * distanceA) / (distanceA + distanceB);
        wallStrikePoint = new Position(xWallStrikePos, yWallStrikePos);
//        System.out.println("        D1:     " + distanceA);
//        System.out.println("        D2:     " + distanceB);
//        System.out.println("        YWall:  " + yWallStrikePos);
//        System.out.println("        XWall:  " + xWallStrikePos);
//        System.out.println("THE CALCULATE WALL STRIKE POINT IS:     " + wallStrikePoint.toString());
    }

    private boolean isThePlayerWayToTheWallClean(Game game) {
//        System.out.println("CHECKING--> IS THE WAY TO THE WALL CLEAN FOR PLAYER(" + player.getId() + ")");
        Position checkingPosition;
        if (!MHN_AI.isTheWayClean(player.getPosition(), wallStrikePoint, ball.getPosition(), MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER))
            return false;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            checkingPosition = game.getMyTeam().getPlayer(i).getPosition();
            if (game.getMyTeam().getPlayer(i) == player)
                continue;
            if (checkingPosition.getX() == player.getPosition().getX() && checkingPosition.getY() == player.getPosition().getY())
                continue;
            if (!MHN_AI.isTheWayClean(player.getPosition(), wallStrikePoint, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                return false;
        }
//        System.out.println("THE PLAYER(" + player.getId() + ") DOESN'T COLLIDE WITH OUR PLAYERS");
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            checkingPosition = game.getOppTeam().getPlayer(i).getPosition();
            if (game.getOppTeam().getPlayer(i) == player)
                continue;
            if (checkingPosition.getX() == player.getPosition().getX() && checkingPosition.getY() == player.getPosition().getY())
                continue;
            if (!MHN_AI.isTheWayClean(player.getPosition(), wallStrikePoint, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                return false;
        }
//        System.out.println("THE PLAYER(" + player.getId() + ") DOESN'T COLLIDE WITH ENEMY PLAYERS");
//        System.out.println("THE WAY OF PLAYER(" + player.getId() + ") TO THE WALL IS TOTALLY CLEAN FOR ANGLE(" + playerShootAngle + ")!");
        return true;
    }

    private boolean isThePlayerWayFromWallToTheBallIsClean(Game game) {
//        System.out.println("CHECKING--> IS THE WAY FROM THE WALL TO THE BALL CLEAN FOR PLAYER(" + player.getId() + ")");
        Position checkingPosition;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) { //CHECKING MY PLAYERS
            checkingPosition = game.getMyTeam().getPlayer(i).getPosition();
            if (game.getMyTeam().getPlayer(i) == player)
                continue;
            if (checkingPosition.getX() == player.getPosition().getX() && checkingPosition.getY() == player.getPosition().getY())
                continue;
            if (!MHN_AI.isTheWayClean(wallStrikePoint, strikePlayerPosition, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                return false;
        }
//        System.out.println("THE PLAYER(" + player.getId() + ") DOESN'T COLLIDE WITH OUR PLAYERS IN WAY OF WALL TO THE BALL FOR ANGLE(" + playerShootAngle + ")!");
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) { //CHECKING ENEMY PLAYERS
            checkingPosition = game.getOppTeam().getPlayer(i).getPosition();
            if (game.getOppTeam().getPlayer(i) == player)
                continue;
            if (checkingPosition.getX() == player.getPosition().getX() && checkingPosition.getY() == player.getPosition().getY())
                continue;
            if (!MHN_AI.isTheWayClean(wallStrikePoint, strikePlayerPosition, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                return false;
        }
//        System.out.println("THE PLAYER(" + player.getId() + ") DOESN'T COLLIDE WITH ENEMY PLAYERS IN WAY OF WALL TO THE BALL FOR ANGLE(" + playerShootAngle + ")!");
//        System.out.println("THE WAY OF PLAYER(" + player.getId() + ") FROM THE WALL TO THE BALL IS TOTALLY CLEAN FOR ANGLE(" + playerShootAngle + ")!");
        return true;
    }

    public Ball getBall() {
        return ball;
    }

    public double getBallAngle() {
        return ballAngle;
    }

    public Player getPlayer() {
        return player;
    }

    public Position getStrikePlayerPosition() {
        return strikePlayerPosition;
    }

    public Position getWallStrikePoint() {
        return wallStrikePoint;
    }

    public double getPlayerShootAngle() {
        return playerShootAngle;
    }
}
