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

        calculateAllPlayerDirectShoots();
        filterPlayerDirectShoots();
        calculateAllStrikerPlayerDirectShoots();
        filterStrikerPlayerDirectShoots();
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
            for (int j = 0; j < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; j++) {
                directShoot = calculateThePlayerToPlayerDirectShoot(
                        game.getMyTeam().getPlayer(j), //StrikerPlayer (that hits the player)
                        player, //Player (that hits the ball)
                        playerDirectShoots.get(i).getPlayerShootAngle()); //Player (that hits the ball) Shoot Angle
                if (directShoot.getPlayerShootAngle() != MHN_AI.FAILED_CODE)
                    strikerPlayerDirectShoots.add(directShoot);
            }
        }
    }

    private void filterPlayerDirectShoots() {
        for (int i = 0; i < playerDirectShoots.size(); i++) {
            if (!isTheWayCleanForPlayerToPoint(player, playerDirectShoots.get(i).getPlayerStrikePoint()))
                playerDirectShoots.remove(i);
        }
    }

    private void filterStrikerPlayerDirectShoots() {
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            if (!isTheWayCleanForPlayerToPoint(strikerPlayerDirectShoots.get(i).getPlayer(), strikerPlayerDirectShoots.get(i).getPlayerStrikePoint()))
                strikerPlayerDirectShoots.remove(i);
        }
    }

    public boolean isItPossible() {
        return strikerPlayerDirectShoots.size() != 0;
    }

    public DirectShoot getTheBestStrikerPlayersDirectShoots() { //returns null if the list is empty, so the method 'isItPossible' must be called!
        return MHN_AI.findTheBestDirectShoot(strikerPlayerDirectShoots);
    }

    private boolean isTheWayCleanForPlayerToPoint(Player player, Position position) {
        Position checkingPosition;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            if (player != game.getMyTeam().getPlayer(i)) {
                checkingPosition = game.getMyTeam().getPlayer(i).getPosition();
                if (!MHN_AI.isTheWayClean(player.getPosition(), position, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
            if (player != game.getOppTeam().getPlayer(i)) {
                checkingPosition = game.getOppTeam().getPlayer(i).getPosition();
                if (!MHN_AI.isTheWayClean(player.getPosition(), position, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
        }
        return true;
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

        Position strikerPlayerStrikePoint = calculateTheExpectedStrikePlayerStrikePoint(player, playerShootAngle);
        final double strikerPlayerShootAngle = MHN_AI.calculateTheAngleFromTo(strikerPlayer.getPosition(), strikerPlayerStrikePoint);
        if (strikerPlayerShootAngle >= 90 && strikerPlayerShootAngle <= 270 && (playerShootAngle < 90 || playerShootAngle > 270))
            return new DirectShoot(player, MHN_AI.FAILED_CODE, playerShootAngle);
        return new DirectShoot(strikerPlayer, player, strikerPlayerShootAngle, playerShootAngle);
    }

    protected static Position calculateTheExpectedStrikePlayerStrikePoint(Player player, double playerShootAngle) {
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
}
