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
    private double playerStrikerShootAngle;
    private Player playerStriker;
    private List<Position> playerStrikerStrikePoints;
    private List<Position> playerStrikePoints = new ArrayList<>();
    private List<DirectShoot> playerDirectShoots = new ArrayList<>();


    //This Class Gets a list of averaged ball angles 'ballAnglesAvg' and a player
    //Calculates a list of 'playerStrikePositions' to hit the ball and goal
    //Calculates a list of 'playerShootAngles' to hit the ball in the 'playerStrikePoints'
    //Checks for each 'playerShootAngles' that if the way of player to the ball is clean
    //Calculates the 'playerStrikerStrikePoints' to hit the player and the player hit the ball and goal
    //Checks for each of my team players that if the way of checking player to the 'playerStrikerStrikePoint' is clean

    ////<<NOTE: EACH OBJECT OF THIS CLASS IS ABOUT ONLY ONE OF THE ENEMY'S PLAYERS>>

    public OwnGoal(Ball ball, Game game, Player player, List<Double> ballAnglesAvg) {
        this.ball = ball;
        this.game = game;
        this.player = player;
        this.ballAnglesAvg = ballAnglesAvg;
    }

    private void calculatePlayerStrikePoints() {
        DirectShoot directShoot;
        for (int i = 0; i < ballAnglesAvg.size(); i++) {
            directShoot = MHN_AI.calculateTheAngleOfPlayerStrikingTheBallDirectlyForBallAngle(player, ballAnglesAvg.get(i), ball);
            if (directShoot.getPlayerShootAngle() != MHN_AI.FAILED_CODE)
                playerDirectShoots.add(directShoot);
        }
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

    public Player getPlayerStriker() {
        return playerStriker;
    }
}
