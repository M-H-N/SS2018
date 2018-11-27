package mhn_ai;

import uiai.Ball;
import uiai.Game;
import uiai.Player;

import java.util.List;

public class OwnGoal {
    private final Ball ball;
    private final Game game;
    private final Player player;
    private final List<Double> ballAngles;
    private double playerShootAngle;
    private Player playerStriker;


    public OwnGoal(Ball ball, Game game, Player player, List<Double> ballAngles) {
        this.ball = ball;
        this.game = game;
        this.player = player;
        this.ballAngles = ballAngles;
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

    public List<Double> getBallAngles() {
        return ballAngles;
    }

    public double getPlayerShootAngle() {
        return playerShootAngle;
    }

    public Player getPlayerStriker() {
        return playerStriker;
    }
}
