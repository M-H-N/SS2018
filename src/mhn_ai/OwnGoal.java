package mhn_ai;

import uiai.Ball;
import uiai.Game;
import uiai.Player;
import uiai.Position;

import java.util.ArrayList;
import java.util.List;

public class OwnGoal {
    private final Ball ball;
    private final Game game;
    private final Player player;
    private final List<Double> ballAnglesAvg;
    private List<Double> playerShootAngles;
    private double strikerPlayerShootAngle;
    private Player strikerPlayer;
    private List<Position> strikerPlayerStrikePoints;
    private List<Position> playerStrikePoints = new ArrayList<>();
    private List<DirectShoot> playerDirectShoots = new ArrayList<>();
    private List<DirectShoot> strikerPlayerDirectShoots = new ArrayList<>();


    //This Class Gets a list of averaged ball angles 'ballAnglesAvg' and a player
    //Calculates a list of 'playerStrikePositions' to hit the ball and goal
    //Calculates a list of 'playerShootAngles' to hit the ball in the 'playerStrikePoints'
    //Checks for each 'playerShootAngles' that if the way of player to the ball is clean
    //Calculates the 'strikerPlayerStrikePoints' to hit the player and the player hit the ball and goal
    //Checks for each of my team players that if the way of checking player to the 'playerStrikerStrikePoint' is clean

    ////<<NOTE: EACH OBJECT OF THIS CLASS IS ABOUT ONLY ONE OF THE ENEMY'S PLAYERS>>

    public OwnGoal(Ball ball, Game game, Player player, List<Double> ballAnglesAvg) {
        this.ball = ball;
        this.game = game;
        this.player = player;
        this.ballAnglesAvg = ballAnglesAvg;
    }

    private void calculateAllPlayerDirectShoots() {
        DirectShoot directShoot;
        for (int i = 0; i < ballAnglesAvg.size(); i++) {
            directShoot = MHN_AI.calculateTheAngleOfPlayerStrikingTheBallDirectlyForBallAngle(player, ballAnglesAvg.get(i), ball);
            if (directShoot.getPlayerShootAngle() != MHN_AI.FAILED_CODE)
                playerDirectShoots.add(directShoot);
        }
    }

    private void calculateAllStrikerPlayerDirectShoots() {
        DirectShoot directShoot;
        for (int i = 0; i < playerDirectShoots.size(); i++) {
            directShoot = calculateThePlayerToPlayerDirectShoot(playerDirectShoots.get(i).getPlayer()
                    , playerDirectShoots.get(i).getBallPlayer()
                    , playerDirectShoots.get(i).getBallShootAngle());
            if (directShoot.getPlayerShootAngle() != MHN_AI.FAILED_CODE)
                strikerPlayerDirectShoots.add(directShoot);
        }
    }

    protected static DirectShoot calculateThePlayerToPlayerDirectShoot(Player strikerPlayer, Player player, double playerShootAngle) {
        if (playerShootAngle > 90 && playerShootAngle < 270 && strikerPlayer.getPosition().getX() < player.getPosition().getX())
            return new DirectShoot(player, MHN_AI.FAILED_CODE, playerShootAngle);
        if ((playerShootAngle < 90 || playerShootAngle > 270) && strikerPlayer.getPosition().getX() > player.getPosition().getX())
            return new DirectShoot(player, MHN_AI.FAILED_CODE, playerShootAngle);
        if (playerShootAngle < 180 && strikerPlayer.getPosition().getY() > player.getPosition().getY())
            return new DirectShoot(player, MHN_AI.FAILED_CODE, playerShootAngle);
        if (playerShootAngle > 180 && strikerPlayer.getPosition().getY() < player.getPosition().getY())
            return new DirectShoot(player, MHN_AI.FAILED_CODE, playerShootAngle);

        Position strikerPlayerStrikePoint = calculateTheExcpectedStrikePlayerStrikePoint(player, playerShootAngle);
        final double strikerPlayerShootAngle = MHN_AI.calculateTheAngleFromTo(strikerPlayer.getPosition(), strikerPlayerStrikePoint);
        if (strikerPlayerShootAngle >= 90 && strikerPlayerShootAngle <= 270 && (playerShootAngle < 90 || playerShootAngle > 270))
            return new DirectShoot(player, MHN_AI.FAILED_CODE, playerShootAngle);
        return new DirectShoot(strikerPlayer, player, strikerPlayerShootAngle, playerShootAngle);
    }

    protected static Position calculateTheExcpectedStrikePlayerStrikePoint(Player player, double playerShootAngle) {
        double xPos = player.getPosition().getX() - (Math.cos(Math.toRadians(playerShootAngle)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS);
        double yPos = player.getPosition().getY() - (Math.sin(Math.toRadians(playerShootAngle)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS);
        return new Position(xPos, yPos);
    }

    public Ball getBall() {
        return ball;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Double> getBallAnglesAvg() {
        return ballAnglesAvg;
    }

    public List<Double> getPlayerShootAngles() {
        return playerShootAngles;
    }

    public Player getStrikerPlayer() {
        return strikerPlayer;
    }
}
