

import java.util.ArrayList;
import java.util.List;

public class OwnGoal {
    private final Ball ball;
    private final Game game;
    private final Player player; //Player that hits the ball
    private final List<Double> ballAnglesAvg;
    private List<DirectShoot> playerDirectShoots = new ArrayList<>();
    private List<DirectShoot> strikerPlayerDirectShoots = new ArrayList<>();

    ////<<NOTE: EACH OBJECT OF THIS CLASS IS ABOUT ONLY ONE OF THE ENEMY'S PLAYERS>>

    public OwnGoal(Game game, Player player, List<Double> ballAnglesAvg) {
        this.ball = game.getBall();
        this.game = game;
        this.player = player;
        this.ballAnglesAvg = ballAnglesAvg;

        calculateAllPlayerDirectShoots();
        filterPlayerDirectShoots();
        calculateAllStrikerPlayerDirectShoots();
        filterStrikerPlayerDirectShoots();
    }

    //CHECKED
    private void calculateAllPlayerDirectShoots() {
        DirectShoot directShoot;
        for (int i = 0; i < ballAnglesAvg.size(); i++) {
            directShoot = MHN_AI.calculateTheDirectShootOfPlayerStrikingTheBallDirectlyForBallAngle(player, ballAnglesAvg.get(i), ball);
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

    protected static DirectShoot calculateThePlayerToPlayerDirectShoot(Player strikerPlayer, Player player, double playerShootAngle) {
        if (playerShootAngle > 90 && playerShootAngle < 270 && strikerPlayer.getPosition().getX() < player.getPosition().getX())
            return new DirectShoot(null, MHN_AI.FAILED_CODE, MHN_AI.FAILED_CODE);
        if ((playerShootAngle < 90 || playerShootAngle > 270) && strikerPlayer.getPosition().getX() > player.getPosition().getX())
            return new DirectShoot(null, MHN_AI.FAILED_CODE, MHN_AI.FAILED_CODE);
        if (playerShootAngle < 180 && strikerPlayer.getPosition().getY() > player.getPosition().getY())
            return new DirectShoot(null, MHN_AI.FAILED_CODE, MHN_AI.FAILED_CODE);
        if (playerShootAngle > 180 && strikerPlayer.getPosition().getY() < player.getPosition().getY())
            return new DirectShoot(null, MHN_AI.FAILED_CODE, MHN_AI.FAILED_CODE);

        final Position strikerPlayerStrikePoint = calculateTheExpectedStrikerPlayerStrikePoint(player, playerShootAngle);
        final double strikerPlayerShootAngle = MHN_AI.calculateTheAngleFromTo(strikerPlayer.getPosition(), strikerPlayerStrikePoint);
        if (strikerPlayerShootAngle >= 90 && strikerPlayerShootAngle <= 270 && (playerShootAngle < 90 || playerShootAngle > 270))
            return new DirectShoot(null, MHN_AI.FAILED_CODE, MHN_AI.FAILED_CODE);
        return new DirectShoot(strikerPlayer, player, strikerPlayerShootAngle, playerShootAngle, strikerPlayerStrikePoint);
    }


    private void filterPlayerDirectShoots() {
        for (int i = 0; i < playerDirectShoots.size(); i++) {
            if (!isTheWayCleanForPlayerToPoint(player, playerDirectShoots.get(i).getPlayerStrikePoint()))
                playerDirectShoots.remove(i);
        }
    }

    private void filterStrikerPlayerDirectShoots() {
        for (int i = 0; i < strikerPlayerDirectShoots.size(); i++) {
            if (!isTheWayCleanForPlayerToPoint(strikerPlayerDirectShoots.get(i).getPlayer(), strikerPlayerDirectShoots.get(i).getPlayerStrikePoint()))
                strikerPlayerDirectShoots.remove(i);
        }
    }

    public boolean isItPossible() {
        return strikerPlayerDirectShoots.size() != 0;
    }

    public DirectShoot getTheBestStrikerPlayersDirectShoots() { //returns null if the list is empty, so the method 'isItPossible' must be called!
//        return MHN_AI.findTheBestDirectShoot(strikerPlayerDirectShoots);
        return MHN_AI.findTheNearestDirectShootByBallPlayer(strikerPlayerDirectShoots);
    }


    private boolean isTheWayCleanForPlayerToPoint(Player playerFrom, Position destinationPoint) {
        //TODO==> THROWS NULL POINTER EXCEPTION
        Position checkingPosition;
        Player checkingPlayer;
        for (int i = 0; i < MHN_AI.PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            checkingPlayer = game.getMyTeam().getPlayer(i);
            if (playerFrom != checkingPlayer && player != checkingPlayer) {
                checkingPosition = checkingPlayer.getPosition();
                if (!MHN_AI.isTheWayClean(playerFrom.getPosition(), destinationPoint, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
            checkingPlayer = game.getOppTeam().getPlayer(i);
            if (playerFrom != checkingPlayer && player != checkingPlayer) {
                checkingPosition = checkingPlayer.getPosition();
                if (!MHN_AI.isTheWayClean(playerFrom.getPosition(), destinationPoint, checkingPosition, MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                    return false;
            }
        }
        return true;
    }


    protected static Position calculateTheExpectedStrikerPlayerStrikePoint(Player player, double playerShootAngle) {
        double xPos = player.getPosition().getX() - (Math.cos(Math.toRadians(playerShootAngle)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS);
        double yPos = player.getPosition().getY() - (Math.sin(Math.toRadians(playerShootAngle)) * MHN_AI.MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS);
        return new Position(xPos, yPos);
    }

    public Player getPlayer() {
        return player;
    }
}
