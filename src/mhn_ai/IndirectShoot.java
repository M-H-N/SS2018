package mhn_ai;

import uiai.Ball;
import uiai.Game;
import uiai.Player;
import uiai.Position;

public class IndirectShoot {
    private Ball ball;
    private double ballAngleTop;
    private double ballAngleBottom;
    private Position bottomWallStrikePoint;
    private Position topWallStrikePoint;
    private Position finalPoint;
    private Position playerStrikePointTop;
    private Position playerStrikePointBottom;
    private Game game;
    private boolean topPossible, bottomPossible;

    public IndirectShoot(Ball ball, Position finalPosition, Game game) {
        this.ball = ball;
        this.finalPoint = finalPosition;
        this.game = game;
    }

    public boolean isItPossibleForTop() {
        topPossible = calculatePlayerStrikePointTop();
        return topPossible;
    }

    public boolean isItPossibleForBottom() {
        bottomPossible = calculatePlayerStrikePointBottom();
        return bottomPossible;
    }

    private boolean calculatePlayerStrikePointTop() {
        if (!calculateBallTopIndirectShootAngle())
            return false;
        final double xPos = ball.getPosition().getX() - (Math.cos(Math.toRadians(ballAngleTop)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        final double yPos = ball.getPosition().getY() - (Math.sin(Math.toRadians(ballAngleTop)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        playerStrikePointTop = new Position(xPos, yPos);
//        System.out.println("THE CALCULATED PLAYER STRIKE FOR TOP-INDIRECT-SHOOT FOR FINAL POINT(" + finalPoint.toString() + ") IS:  " + playerStrikePointTop.toString());
        return true;
    }

    private boolean calculatePlayerStrikePointBottom() {
        if (!calculateBallBottomIndirectShootAngle())
            return false;
        final double xPos = ball.getPosition().getX() - (Math.cos(Math.toRadians(ballAngleBottom)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        final double yPos = ball.getPosition().getY() - (Math.sin(Math.toRadians(ballAngleBottom)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        playerStrikePointBottom = new Position(xPos, yPos);
//        System.out.println("THE CALCULATED PLAYER STRIKE FOR BOTTOM-INDIRECT-SHOOT FOR FINAL POINT(" + finalPoint.toString() + ") IS:  " + playerStrikePointBottom.toString());
        return true;
    }

    private boolean calculateBallTopIndirectShootAngle() {
//        System.out.println("CALCULATING THE BALL TOP INDIRECT SHOOT ANGLE FOR FINAL-POINT:      " + finalPoint.toString());
        calculateTopWallStrikePoint();
        if (!isTheWayForTopIndirectShootClean()) {
//            System.out.println("THE WAY TO THE FINAL POINT FROM TOP-INDIRECT-SHOOT IS BLOCKED!");
            return false;
        }
        ballAngleTop = MHN_AI.calculateTheAngleFromTo(ball.getPosition(), topWallStrikePoint);
//        System.out.println("THE BALL TOP-INDIRECT-SHOOT ANGLE IS:   " + ballAngleTop);
        return true;
    }

    private boolean calculateBallBottomIndirectShootAngle() {
//        System.out.println("CALCULATING THE BALL BOTTOM INDIRECT SHOOT ANGLE FOR FINAL-POINT:   " + finalPoint.toString());
        calculateBottomWallStrikePoint();
        if (!isTheWayForBottomIndirectShootClean()) {
//            System.out.println("THE WAY TO THE FINAL POINT FROM BOTTOM-INDIRECT-SHOOT IS BLOCKED!");
            return false;
        }
        ballAngleBottom = MHN_AI.calculateTheAngleFromTo(ball.getPosition(), bottomWallStrikePoint);
//        System.out.println("THE BALL BOTTOM-INDIRECT-SHOOT ANGLE IS:   " + ballAngleTop);
        return true;
    }

    private void calculateBottomWallStrikePoint() {
//        System.out.println("CALCULATING THE BOTTOM WALL-STRIKE-POINT FOR BALL AND FINAL POINT:     " + finalPoint.toString());
        final double distanceA = Math.abs(Math.abs(MHN_AI.FIELD_MIN_Y - ball.getPosition().getY()) - (MHN_AI.BALL_DIAMETER / 2));
        final double distanceB = Math.abs(Math.abs(MHN_AI.FIELD_MIN_Y - finalPoint.getY()) - (MHN_AI.BALL_DIAMETER / 2));
        final double yWallStrikePoint = MHN_AI.FIELD_MIN_Y + (MHN_AI.BALL_DIAMETER / 2);
        final double xWallStrikePoint = ((ball.getPosition().getX() * distanceB) + (finalPoint.getX() * distanceA)) / (distanceA + distanceB);
        bottomWallStrikePoint = new Position(xWallStrikePoint, yWallStrikePoint);
//        System.out.println("        D1:     " + distanceA);
//        System.out.println("        D2:     " + distanceB);
//        System.out.println("        YWall:  " + yWallStrikePoint);
//        System.out.println("        XWall:  " + xWallStrikePoint);
//        System.out.println("THE CALCULATE BOTTOM WALL STRIKE POINT IS:     " + bottomWallStrikePoint.toString());
    }

    private void calculateTopWallStrikePoint() {
//        System.out.println("CALCULATING THE TOP WALL-STRIKE-POINT FOR BALL AND FINAL POINT:     " + finalPoint.toString());
        final double distanceA = Math.abs(Math.abs(MHN_AI.FIELD_MAX_Y - ball.getPosition().getY()) - (MHN_AI.BALL_DIAMETER / 2));
        final double distanceB = Math.abs(Math.abs(MHN_AI.FIELD_MAX_Y - finalPoint.getY()) - (MHN_AI.BALL_DIAMETER / 2));
        final double yWallStrikePoint = MHN_AI.FIELD_MAX_Y - (MHN_AI.BALL_DIAMETER / 2);
        final double xWallStrikePoint = ((ball.getPosition().getX() * distanceB) + (finalPoint.getX() * distanceA)) / (distanceA + distanceB);
        topWallStrikePoint = new Position(xWallStrikePoint, yWallStrikePoint);
//        System.out.println("        D1:     " + distanceA);
//        System.out.println("        D2:     " + distanceB);
//        System.out.println("        YWall:  " + yWallStrikePoint);
//        System.out.println("        XWall:  " + xWallStrikePoint);
//        System.out.println("THE CALCULATED TOP WALL STRIKE POINT IS:     " + topWallStrikePoint.toString());
    }

    private boolean isTheWayForTopIndirectShootClean() {
        Position playerPosition;
        final double threshold = (MHN_AI.PLAYER_DIAMETER + MHN_AI.BALL_DIAMETER) / 2;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) { //CHECKING ALL PLAYERS
            playerPosition = game.getMyTeam().getPlayer(i).getPosition();
            if (!MHN_AI.isTheWayClean(ball.getPosition(), topWallStrikePoint, playerPosition, threshold)) //BALL TO THE WALL
                return false;
            if (!MHN_AI.isTheWayClean(topWallStrikePoint, finalPoint, playerPosition, threshold)) //WALL TO THE FINAL
                return false;
            playerPosition = game.getOppTeam().getPlayer(i).getPosition();
            if (!MHN_AI.isTheWayClean(ball.getPosition(), topWallStrikePoint, playerPosition, threshold)) //BALL TO THE WALL
                return false;
            if (!MHN_AI.isTheWayClean(topWallStrikePoint, finalPoint, playerPosition, threshold)) //WALL TO THE FINAL
                return false;
        }
//        System.out.println("THE WAY FOR TOP-INDIRECT-SHOOT WITH ANGLE(" + ballAngleTop + ") IS TOTALLY CLEAN!");
        return true;
    }

    private boolean isTheWayForBottomIndirectShootClean() {
        Position playerPosition;
        final double threshold = (MHN_AI.PLAYER_DIAMETER + MHN_AI.BALL_DIAMETER) / 2;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) { //CHECKING ALL PLAYERS
            playerPosition = game.getMyTeam().getPlayer(i).getPosition();
            if (!MHN_AI.isTheWayClean(ball.getPosition(), bottomWallStrikePoint, playerPosition, threshold)) //BALL TO THE WALL
                return false;
            if (!MHN_AI.isTheWayClean(bottomWallStrikePoint, finalPoint, playerPosition, threshold)) //WALL TO THE FINAL
                return false;
            playerPosition = game.getOppTeam().getPlayer(i).getPosition();
            if (!MHN_AI.isTheWayClean(ball.getPosition(), bottomWallStrikePoint, playerPosition, threshold)) //BALL TO THE WALL
                return false;
            if (!MHN_AI.isTheWayClean(bottomWallStrikePoint, finalPoint, playerPosition, threshold)) //WALL TO THE FINAL
                return false;
        }
//        System.out.println("THE WAY FOR BOTTOM-INDIRECT-SHOOT WITH ANGLE(" + ballAngleBottom + ") IS TOTALLY CLEAN!");
        return true;
    }

    public Ball getBall() {
        return ball;
    }

    public double getBallAngleTop() {
        return ballAngleTop;
    }

    public double getBallAngleBottom() {
        return ballAngleBottom;
    }

    public Position getBottomWallStrikePoint() {
        return bottomWallStrikePoint;
    }

    public Position getTopWallStrikePoint() {
        return topWallStrikePoint;
    }

    public Position getFinalPoint() {
        return finalPoint;
    }

    public Position getPlayerStrikePointTop() {
        return playerStrikePointTop;
    }

    public Position getPlayerStrikePointBottom() {
        return playerStrikePointBottom;
    }

    public Game getGame() {
        return game;
    }

    public boolean isTopPossible() {
        return topPossible;
    }

    public boolean isBottomPossible() {
        return bottomPossible;
    }

    @Override
    public String toString() {
        String result = "";
        if (topPossible)
            result += "TOP ANGLE: " + ballAngleTop + "\nTOP WALL STRIKE POINT: " + topWallStrikePoint.toString() + "\nFINAL POINT: " + finalPoint.toString() + "\nPLAYER STRIKE POINT TOP: " + playerStrikePointTop.toString();
        if (bottomPossible)
            result += "BOTTOM ANGLE: " + ballAngleBottom + "\nBOTTOM WALL STRIKE POINT: " + bottomWallStrikePoint.toString() + "\nFINAL POINT: " + finalPoint.toString() + "\nPLAYER STRIKE POINT BOTTOM: " + playerStrikePointBottom.toString();
        return result;
    }
}
