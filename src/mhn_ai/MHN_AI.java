package mhn_ai;

import uiai.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MHN_AI {
    protected final Triple act;
    protected final Game game;
    protected final HSide myHSide, enemyHSide;
    protected final Ball ball;
    protected static final int POWER_MAX = 100;
    protected static final int ANGLE_MAX = 359;
    protected static final int ANGLE_MIN = 0;
    protected static final float BALL_DIAMETER = 0.5f; //ORIGINAL
    //    protected static final float BALL_DIAMETER = 0.6f;
    protected static final float PLAYER_DIAMETER = 1f; //ORIGINAL
    //    protected static final float PLAYER_DIAMETER = 0.8f;
    protected static final float INDIRECT_SHOOT_CHECK_STEP = 0.4f;
    protected static final float DIRECT_SHOOT_CHECK_STEP = 0.4f;
    protected static final float TARGET_TOP_Y = 1.4f;
    protected static final float TARGET_BOTTOM_Y = -1.4f;
    protected static final int TARGET_LEFT_X = -7;
    protected static final int TARGET_RIGHT_X = 7;
    protected static final int FIELD_MIN_X = -7;
    protected static final int FIELD_MAX_X = 7;
    protected static final int FIELD_MIN_Y = -4;
    protected static final int FIELD_MAX_Y = 4;
    private static final int DANGER_ZONE_MAX_X = -5;
    protected static final int FAILED_CODE = -1;
    protected static final int PLAYERS_COUNT_IN_EACH_TEAM = 5;
    protected static final float DIRECT_SHOOT_THRESHOLD_ANGLE = 60f;
    protected static final float MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER = (BALL_DIAMETER + PLAYER_DIAMETER) / 2;
    protected static final float MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS = PLAYER_DIAMETER;

    //TODO--> THESE ARE WHAT I NEED TO DO:
    //TODO-->(DONE)           1-REMOVE INDIRECT STRIKE FROM TAKING THE BALL AWAY
    //TODO-->           2-ADD SUPER DEFENCE
    //TODO-->           3-MODIFY PARK THE BUS
    //TODO-->           4-CHANGE THE FORMATION TO ONE THAT CAN GOAL AT THE FIRST SHOOT
    //TODO-->           5-CAN THE ENEMY PLAYER GOAL FOR ME! (O.G)

    public MHN_AI(Triple act, Game game) {
        this.act = act;
        this.game = game;
        this.ball = game.getBall();
        this.myHSide = HSide.LEFT;
        this.enemyHSide = HSide.RIGHT;
        System.out.println("==================================== CYCLE: " + game.getCycle() + "====================================");
        System.out.println("MY PLAYERS POSITION:");
        for (int i = 0; i < 5; i++) {
            System.out.println("PLAYER(" + i + ") :     " + game.getMyTeam().getPlayer(i).getPosition().toString());
        }
        System.out.println("BALL POSITION:  " + ball.getPosition().toString());
        System.out.println("ENEMY PLAYERS POSITION:");
        for (int i = 0; i < 5; i++) {
            System.out.println("PLAYER(" + i + ") :     " + game.getOppTeam().getPlayer(i).getPosition().toString());
        }
        System.out.println("===================================================================================================================");
    }


    public void action() {
        if (canMakeADirectGoal()) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-GOAL-DIRECTLY!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        System.out.println("CAN NOT MAKE A DIRECT GOAL!");
        if (canMakeAnIndirectGoal()) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-GOAL-INDIRECTLY!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        System.out.println("CAN NOT MAKE AN INDIRECT GOAL!");
        if (canMakeAnOwnGoal()) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-MAKE-AN-OWN-GOAL!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        System.out.println("CAN NOT MAKE AN OWN GOAL!");
        if (canTakeTheBallAwayFromTarget()) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-TAKE-THE-BALL-AWAY-FROM-TARGET!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        System.out.println("CAN NOT TAKE THE BALL AWAY FROM TARGET!");
        if (canDefence()) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-PARK-THE-BUS!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        System.out.println("CAN NOT PARK THE BUS!");
        generateRandomShoot();


//        act.setAngle(45);
//        act.setPower(100);
//        act.setPlayerID(0);
    }

    private boolean canMakeADirectGoal() {
        int playerShootAngle;
        int power = POWER_MAX;
        int playerId;

        List<Double> anglesToGoal = calculateTheAnglesOfBallWithRespectToTheGoalForTeam(game.getMyTeam());
        if (anglesToGoal.size() == 0) {
            System.out.println("*************************CANNOT MAKE A DIRECT GOAL BECAUSE THE CLEAN ANGLES TO GOAL FROM BALL IS NULL!");
            return false;
        }
        System.out.println("ALL DIRECT SHOOT POSSIBLE ANGLES:");
        for (int i = 0; i < anglesToGoal.size(); i++)
            System.out.println("    --POSSIBLE-ANGLE:   " + anglesToGoal.get(i));
        List<DirectShoot> directShoots = new ArrayList<>();
//        List<DirectShoot> directShootsAvg;
        for (int i = 0; i < anglesToGoal.size(); i++)
            directShoots.addAll(whichPlayersCanStrikeThisDirectly(anglesToGoal.get(i), game.getMyTeam()));
        directShoots = thresholdDirectShoot(directShoots); //NEW NEW NEW NEW
        if (directShoots.size() == 0) {
            System.out.println("*********************************CANNOT MAKE A DIRECT GOAL BY DIRECT STRIKE BECAUSE THERE IS NO PLAYER TO SHOOT IT OUT!");
            //HERE IS THE START OF INDIRECT STRIKES...
            List<IndirectStrike> indirectStrikes = new ArrayList<>();
            for (int i = 0; i < anglesToGoal.size(); i++)
                indirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(anglesToGoal.get(i), game.getMyTeam()));
            if (indirectStrikes.size() == 0) {
                System.out.println("***********************************CANNOT MAKE A DIRECT GOAL BY AN INDIRECT STRIKE BECAUSE THERE IS NO PLAYER TO SHOOT IT OUT!");
                return false;
            }
            IndirectStrike chosenIndirectStrike = findTheBestIndirectStrike(indirectStrikes); //CHOOSER METHOD
            playerId = chosenIndirectStrike.getPlayer().getId();
            playerShootAngle = (int) Math.round(chosenIndirectStrike.getPlayerShootAngle());
            System.out.println("CHOSEN PLAYER(" + chosenIndirectStrike.getPlayer().getId() + ") FOR INDIRECT STRIKE FOR DIRECT SHOOT WITH ANGLE(" + chosenIndirectStrike.getPlayerShootAngle() + ") AND THE WALL-X(" + chosenIndirectStrike.getWallStrikePoint().getX() + ")");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE A DIRECT GOAL BY AN INDIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            System.out.println("TOTAL PURE DIRECT SHOOTS CAPABLE:");
            for (int i = 0; i < directShoots.size(); i++)
                System.out.println("    CAPABLE-SHOOT -->   " + directShoots.get(i).toString());
            directShoots = averageSequenceDirectShoots(directShoots);
            System.out.println("TOTAL PURE DIRECT SHOOTS CAPABLE(AFTER AVERAGE SEQUENCE DIRECT SHOOT):");
            for (int i = 0; i < directShoots.size(); i++)
                System.out.println("    CAPABLE-SHOOT -->   " + directShoots.get(i).toString());
            DirectShoot chosenDirectShoot = findTheBestDirectShoot(directShoots); //CHOOSER METHOD
            System.out.println("CHOSEN-SHOOT ==>    " + chosenDirectShoot.toString());
            playerId = chosenDirectShoot.getPlayer().getId();
            playerShootAngle = (int) Math.round(chosenDirectShoot.getPlayerShootAngle());
            System.out.println("REAL ANGLE (NOT-CASTED): " + chosenDirectShoot.getPlayerShootAngle());
            System.out.println("SETTING ANGLE TO(CASTED): " + playerShootAngle);
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE A DIRECT GOAL BY A DIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        act.setPlayerID(playerId);
        act.setPower(power);
        act.setAngle(playerShootAngle);
        return true;
    }

    private boolean canMakeAnIndirectGoal() {
        int playerId, power = 100, playerShootAngle;
        System.out.println("||||||||||||||||||||||||||||||||||TRYING TO MAKE AN INDIRECT SHOOT||||||||||||||||||||||||||||||||||");
        Position targetTopPosition = new Position(TARGET_RIGHT_X, TARGET_TOP_Y);
        Position targetBottomPosition = new Position(TARGET_RIGHT_X, TARGET_BOTTOM_Y);
        List<IndirectShoot> indirectShoots = new ArrayList<>();
        IndirectShoot indirectShoot;
        Position tempPosition;
        for (double i = targetBottomPosition.getY(); i <= targetTopPosition.getY(); i += INDIRECT_SHOOT_CHECK_STEP) {
            tempPosition = new Position(TARGET_RIGHT_X, i);
//            System.out.println("*****CHECKING INDIRECT SHOOTS FOR FINAL POINT:   " + tempPosition.toString());
            indirectShoot = new IndirectShoot(ball, tempPosition, game);
            if (indirectShoot.isItPossibleForTop() || indirectShoot.isItPossibleForBottom())
                indirectShoots.add(indirectShoot);
        }
        if (indirectShoots.size() == 0) {
            System.out.println("CANNOT MAKE AN INDIRECT GOAL BECAUSE THERE IS NO GOOD ANGLE FOR BALL!");
            return false;
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>ALL POSSIBLE INDIRECT SHOOTS!");
        for (IndirectShoot indirectShoot1 : indirectShoots) System.out.println(indirectShoot1.toString());
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        // NOW WE ARE SEARCHING FOR PLAYER TO SHOOT IT OUT!

        List<DirectShoot> directShoots = new ArrayList<>();
        for (int i = 0; i < indirectShoots.size(); i++) { //CAN STRIKE DIRECTLY?
            if (indirectShoots.get(i).isTopPossible())
                directShoots.addAll(whichPlayersCanStrikeThisDirectly(indirectShoots.get(i).getBallAngleTop(), game.getMyTeam()));
            if (indirectShoots.get(i).isBottomPossible())
                directShoots.addAll(whichPlayersCanStrikeThisDirectly(indirectShoots.get(i).getBallAngleBottom(), game.getMyTeam()));
//            System.out.println("----------------------");
        }
        directShoots = thresholdDirectShoot(directShoots);//NEW NEW NEW NEW
        if (directShoots.size() == 0) {
            List<IndirectStrike> indirectStrikes = new ArrayList<>();
            System.out.println("NO PLAYER CAN DIRECTLY STRIKE THIS INDIRECT-SHOOT!");
            for (int i = 0; i < indirectShoots.size(); i++) { //CAN STRIKE INDIRECTLY?
                if (indirectShoots.get(i).isTopPossible())
                    indirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(indirectShoots.get(i).getBallAngleTop(), game.getMyTeam()));
                if (indirectShoots.get(i).isBottomPossible())
                    indirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(indirectShoots.get(i).getBallAngleBottom(), game.getMyTeam()));
            }
            if (indirectStrikes.size() == 0) {
                System.out.println("NO PLAYER CAN INDIRECTLY STRIKE THIS INDIRECT-SHOOT!");
                return false;
            }
            IndirectStrike indirectStrike = findTheBestIndirectStrike(indirectStrikes); //CHOOSER METHOD
            playerId = indirectStrike.getPlayer().getId();
            playerShootAngle = (int) Math.round(indirectStrike.getPlayerShootAngle());
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE AN INDIRECT GOAL BY AN INDIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            System.out.println("ALL POSSIBLE DIRECT STRIKES:");
            for (DirectShoot directShoot : directShoots) System.out.println(directShoot.toString());
            DirectShoot directShoot = findTheBestDirectShoot(directShoots); //CHOOSER METHOD
            System.out.println("++++++++++++++++SELECTED DIRECT STRIKE FOR INDIRECT SHOOT++++++++++++");
            System.out.println(directShoot.toString());
            playerId = directShoot.getPlayer().getId();
            playerShootAngle = (int) Math.round(directShoot.getPlayerShootAngle());
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE AN INDIRECT GOAL BY A DIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        act.setPlayerID(playerId);
        act.setPower(power);
        act.setAngle(playerShootAngle);
        return true;
    }

    private boolean canMakeAnOwnGoal() {
        //TODO--> DEVELOP THIS METHOD (USING THE 'OwnGoal' CLASS)
        return false;
    }

    private boolean canTakeTheBallAwayFromTarget() {
        int power = POWER_MAX, playerAngle, playerId;
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||TRYING TO TAKE THE BALL AWAY FROM OUT TARGET||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        List<DirectShoot> directShoots = new ArrayList<>();
        for (int i = 0, j = 359; i < 70; i++, j--) {
//            System.out.println("CHECKING TAKING THE BALL AWAY DIRECTLY FOR ANGLE: " + i + " AND " + j);
            directShoots = whichPlayersCanStrikeThisDirectly(i, game.getMyTeam());
            directShoots.addAll(whichPlayersCanStrikeThisDirectly(j, game.getMyTeam()));
            if (directShoots.size() > 0)
                break;
        }
        if (directShoots.size() == 0) {  //If there is no player to take the ball away directly
//            List<IndirectStrike> indirectStrikes = new ArrayList<>();
//            for (int i = 0, j = 359; i < 60; i++, j--) {
////                System.out.println("CHECKING TAKING THE BALL AWAY INDIRECTLY FOR ANGLE: " + i + " AND " + j);
//                indirectStrikes = whichPlayersCanStrikeThisIndirectly(i, game.getMyTeam());
//                indirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(j, game.getMyTeam()));
//                if (indirectStrikes.size() > 0)
//                    break;
//            }
//            if (indirectStrikes.size() == 0) {
//                System.out.println("CANNOT TAKE THE BALL AWAY BECAUSE THERE IS NO PLAYER TO SHOOT IT OUT!");
//                return false;
//            }
//            System.out.println("ALL INDIRECT SHOTS FOR TAKING THE BALL AWAY :\n");
//            for (IndirectStrike indirectStrike1 : indirectStrikes) System.out.println(indirectStrike1.toString());
//            IndirectStrike indirectStrike = findTheBestIndirectStrike(indirectStrikes); //CHOOSER METHOD
//            playerAngle = (int) Math.round(indirectStrike.getPlayerShootAngles());
//            playerId = indirectStrike.getPlayer().getId();
//            System.out.println("SELECTED INDIRECT SHOOT FOR TAKING THE BALL AWAY :\n" + indirectStrike.toString());
            System.out.println("THERE IS NOT PLAYER TO TAKE THE BALL AWAY DIRECTLY!");
            return false;
        } else {
            System.out.println("ALL DIRECT SHOOTS FOR TAKING THE BALL AWAY:");
            for (DirectShoot directShoot1 : directShoots) System.out.println(directShoot1.toString());
            DirectShoot directShoot = findTheBestDirectShoot(directShoots); //CHOOSER METHOD
            playerAngle = (int) Math.round(directShoot.getPlayerShootAngle());
            playerId = directShoot.getPlayer().getId();
            System.out.println("SELECTED DIRECT SHOOT FOR TAKING THE BALL AWAY :\n" + directShoot.toString());
        }
        act.setAngle(playerAngle);
        act.setPower(power);
        act.setPlayerID(playerId);
        return true;
    }

    private boolean canDefence() { //THE SAME 'PARK THE BUS'
        //TODO--> COMPLETE THIS METHOD

        return false;
    }

    private void removeSimilar(List<Integer> source, List<Integer> from) {
        for (int i = 0; i < from.size(); i++) {
            source.remove(from.get(i));
        }
    }

    private Player findThePlayerWithWorstPosition(Team team) {
        double maxRate = -1;
        Player resultPlayer = null;
        double temp;
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            temp = ratePosition(team.getPlayer(i).getPosition());
            if (temp > maxRate) {
                maxRate = temp;
                resultPlayer = team.getPlayer(i);
            }
        }
        return resultPlayer;
    }

    private boolean isTheBallInDangerZone() {
        return ball.getPosition().getX() < DANGER_ZONE_MAX_X;
    }

    private double ratePosition(Position position) {
        return Math.sqrt(Math.pow(position.getX(), 2) + Math.pow(position.getY(), 2));
    }

    private List<DirectShoot> whichPlayersCanStrikeThisDirectly(double ballAngle, Team team) {
        List<DirectShoot> result = new ArrayList<>();
        DirectShoot playerDirectShoot;
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            playerDirectShoot = calculateTheAngleOfPlayerStrikingTheBallDirectlyForBallAngle(team.getPlayer(i), ballAngle, ball);
            if (playerDirectShoot.getPlayerShootAngle() == FAILED_CODE) {
//                System.out.println("&&&&&&&&&&&THE PLAYER(" + i + ") CANNOT SHOOT DIRECTLY BECAUSE THE POSITION IS BAD!");
                continue;
            }
            System.out.println("&&&&&&&&&&&&&&&CALCULATED PLAYER SHOOT ANGLE FOR DIRECT STRIKE FOR BALL ANGLE(" + ballAngle + " IS:    " + playerDirectShoot);
//            DirectShoot directShoot = new DirectShoot(team.getPlayer(i), playerDirectShoot, ballAngle, team);
            if (!isTheWayOfPlayerToBallCleanForDirectShoot(playerDirectShoot)) {
//                System.out.println("&&&&&&&&&&&THE PLAYER(" + i + ") CANNOT SHOOT BECAUSE THE WAY TO THE BALL IS BLOCKED!");
                continue;
            }
            result.add(playerDirectShoot);
        }
        return result;
    }

    private List<IndirectStrike> whichPlayersCanDefendIndirectly(Team team) {
        List<IndirectStrike> resultIndirectStrikes = new ArrayList<>();
        for (int i = 0, j = 359; i < 180; i++, j--) {
            resultIndirectStrikes = whichPlayersCanStrikeThisIndirectly(i, team);
            if (resultIndirectStrikes.size() > 0)
                break;
            resultIndirectStrikes = whichPlayersCanStrikeThisIndirectly(j, team);
            if (resultIndirectStrikes.size() > 0)
                break;
        }
        return resultIndirectStrikes;
    }

    private List<DirectShoot> whichPlayerCanDefendDirectly(Team team) {
        List<DirectShoot> resultDirectShoots = new ArrayList<>();
        double playerShootAngle;
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            if (team.getPlayer(i).getPosition().getX() > ball.getPosition().getX())
                continue;
            playerShootAngle = calculateTheAngleFromTo(team.getPlayer(i).getPosition(), ball.getPosition());
            resultDirectShoots.add(new DirectShoot(team.getPlayer(i), playerShootAngle));
        }
        return resultDirectShoots;
    }

    private List<IndirectStrike> whichPlayersCanStrikeThisIndirectly(double ballAngle, Team team) {
//        System.out.println("+++++++CHECKING WITCH PLAYER CAN SHOOT THE BALL INDIRECTLY FOR ANGLE(" + ballAngle + ")+++++++");
        List<IndirectStrike> result = new ArrayList<>();
//        double playerShootAngle;
        IndirectStrike indirectStrike;
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            indirectStrike = new IndirectStrike(ball, ballAngle, team.getPlayer(i));
            indirectStrike.calculatePlayerShootAngle();
            if (!indirectStrike.canThePlayerStrikeIndirectly()) {
//                System.out.println("THE PLAYER(" + i + ") CANNOT STRIKE THE BALL BECAUSE OF THE BAD POSITION!");
                continue;
            }
            if (!indirectStrike.isThePlayerWayToTheBallClean(game)) {
//                System.out.println("THE PLAYER(" + i + ") CANNOT STRIKE THE BALL BECAUSE THE WAY IS BLOCKED!");
                continue;
            }
            System.out.println("THE PLAYER(" + i + ") CAN STRIKE THE BALL INDIRECTLY FOR BALL ANGLE:" + ballAngle + "!-------------------");
            result.add(indirectStrike);
        }
        return result;
    }

    private boolean isTheWayOfPlayerToBallCleanForDirectShoot(DirectShoot directShoot) {
        Position playerPosition;
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) { //CHECKING ALL PLAYERS
            playerPosition = game.getMyTeam().getPlayer(i).getPosition();
            if (game.getMyTeam().getPlayer(i) == directShoot.getPlayer())
                continue;
            if (directShoot.getPlayer().getPosition().getX() == playerPosition.getX() && directShoot.getPlayer().getPosition().getY() == playerPosition.getY())
                continue;
            if (!isTheWayClean(directShoot.getPlayer().getPosition(), directShoot.getPlayerStrikePoint(), playerPosition, MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS)) {
//                System.out.println("THE PLAYER(" + directShoot.getPlayer().getId() + ") COLLIDES WITH AN OUR PLAYER(" + i + ")");
                return false;
            }
        }

        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            playerPosition = game.getOppTeam().getPlayer(i).getPosition();
            if (game.getOppTeam().getPlayer(i) == directShoot.getPlayer())
                continue;
            if (directShoot.getPlayer().getPosition().getX() == playerPosition.getX() && directShoot.getPlayer().getPosition().getY() == playerPosition.getY())
                continue;
            if (!isTheWayClean(directShoot.getPlayer().getPosition(), directShoot.getPlayerStrikePoint(), playerPosition, MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS)) {
//                System.out.println("THE PLAYER(" + directShoot.getPlayer().getId() + ") COLLIDES WITH AN ENEMY PLAYER(" + i + ")");
                return false;
            }
        }
//        System.out.println("&&&&&&&&&&&&&&THE WAY OF PLAYER(" + directShoot.getPlayer().getId() + ") TO THE BALL IS TOTALLY CLEAN!");
        return true;
    }

    private boolean isTheWayOfPlayerToPointClean() {
        return false;
    }

    protected static DirectShoot calculateTheAngleOfPlayerStrikingTheBallDirectlyForBallAngle(Player player, double ballAngle, Ball ball) {
//        System.out.println("CALCULATING THE ANGLE OF PLAYER(ID=" + player.getId() + ") STRIKING DIRECTLY THE BALL FOR BALL ANGLE(" + ballAngle + ")");
        if (ballAngle > 90 && ballAngle < 270 && player.getPosition().getX() < ball.getPosition().getX())
            return new DirectShoot(player, FAILED_CODE, ballAngle);
        if ((ballAngle < 90 || ballAngle > 270) && player.getPosition().getX() > ball.getPosition().getX())
            return new DirectShoot(player, FAILED_CODE, ballAngle);
        if (ballAngle < 180 && player.getPosition().getY() > ball.getPosition().getY())
            return new DirectShoot(player, FAILED_CODE, ballAngle);
        if (ballAngle > 180 && player.getPosition().getY() < ball.getPosition().getY())
            return new DirectShoot(player, FAILED_CODE, ballAngle);
//        System.out.println("PLAYER POSITION:    " + player.getPosition().toString());
//        System.out.println("BALL POSITION:      " + ball.getPosition());
//        System.out.println("BALL ANGLE:         " + ballAngle);
        Position playerStrikePoint = calculateTheExpectedPlayerStrikePoint(ball, ballAngle); //original
//        System.out.println("PLAYER STRIKE POINT: " + playerStrikePoint.toString());
        final double playerShootAngle = calculateTheAngleFromTo(player.getPosition(), playerStrikePoint);
//        System.out.println("THE FINAL RESULT OF PLAYER(" + player.getId() + ") ANGLE IS:    " + playerShootAngle);
        if (playerShootAngle >= 90 && playerShootAngle <= 270 && (ballAngle < 90 || ballAngle > 270)) {
//            System.out.println("PLAYER(" + player.getId() + ") BAD POSITION! [STOPPED BY THRESHOLD]");
            return new DirectShoot(player, FAILED_CODE, ballAngle);
        }
        return new DirectShoot(player, playerShootAngle, ballAngle, playerStrikePoint);
    }

    protected static double calculateTheAngleFromTo(Position from, Position destination) {
        final double gradient = calculateTheMBetween2Points(from, destination);
        double angle = Math.abs(Math.toDegrees(Math.atan(gradient)));
        if (destination.getX() > from.getX()) {
            if (destination.getY() < from.getY())
                angle = 360 - angle;
        } else {
            if (destination.getY() < from.getY())
                angle += 180;
            else
                angle = 180 - angle;
        }
        return angle;
    }

    protected boolean thresholdDirectShoot(DirectShoot directShoot) {
        return Math.abs(directShoot.getPlayerShootAngle() - directShoot.getBallShootAngle()) < DIRECT_SHOOT_THRESHOLD_ANGLE;
    }

    protected List<DirectShoot> thresholdDirectShoot(List<DirectShoot> directShoots) {
        List<DirectShoot> resultDirectShoots = new ArrayList<>();
        for (int i = 0; i < directShoots.size(); i++) {
            if (thresholdDirectShoot(directShoots.get(i)))
                resultDirectShoots.add(directShoots.get(i));
        }
        return resultDirectShoots;
    }

    protected static Position calculateTheExpectedPlayerStrikePoint(Ball ball, double ballAngle) {
//        double xPos = ball.getPosition().getX() - (Math.cos(Math.toRadians(ballAngle)) * (BALL_DIAMETER) / 2);
//        double yPos = ball.getPosition().getY() - (Math.sin(Math.toRadians(ballAngle)) * (BALL_DIAMETER) / 2);
        double xPos = ball.getPosition().getX() - (Math.cos(Math.toRadians(ballAngle)) * MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        double yPos = ball.getPosition().getY() - (Math.sin(Math.toRadians(ballAngle)) * MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        return new Position(xPos, yPos);
    }

//    protected static Position calculateTheExpectedPlayerStrikePoint(Player player, int ballAngle) {
//        double xPos = player.getPosition().getX() + (Math.cos(Math.toRadians(ballAngle)) * PLAYER_DIAMETER / 2);
//        double yPos = player.getPosition().getY() + (Math.sin(Math.toRadians(ballAngle)) * PLAYER_DIAMETER / 2);
//        return new Position(xPos, yPos);
//    }

    protected static double calculateTheMBetween2Points(Position p1, Position p2) {
        return ((p1.getY() - p2.getY()) / (p1.getX() - p2.getX()));
    }

    private IndirectStrike findTheBestIndirectStrike(List<IndirectStrike> input) {
        IndirectStrike resultIndirectStrike = null;
        double min = Double.MAX_VALUE;
        double temp;
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).getPlayerShootAngle() > 180)
                temp = Math.abs(360 - input.get(i).getPlayerShootAngle() - input.get(i).getBallAngle());
            else
                temp = Math.abs(input.get(i).getPlayerShootAngle() - input.get(i).getBallAngle());
            if (temp < min) {
                min = temp;
                resultIndirectStrike = input.get(i);
            }
        }
        return resultIndirectStrike;
    }

    private List<DirectShoot> calculateThePlayersAnglesForShootBallInAngle(int ballAngle) {

        return null;
    }

    private void generateRandomShoot() {
        System.out.println("GENERATING A RANDOM SHOOT!");
//        int player_id = findTheNearestPlayerToTheBall(game.getMyTeam());
        int player_id = Math.abs(new Random().nextInt()) % PLAYERS_COUNT_IN_EACH_TEAM;
        act.setPlayerID(player_id); //Choose a random player
        double x1, x2, y1, y2;
        x1 = game.getMyTeam().getPlayer(player_id).getPosition().getX();
        y1 = game.getMyTeam().getPlayer(player_id).getPosition().getY();
        x2 = game.getBall().getPosition().getX();
        y2 = game.getBall().getPosition().getY();
        int angle = Math.abs(new Random().nextInt() % 90);
        if (angle > 45)
            angle -= 45;
        act.setAngle(angle);
        act.setPower(25);
    }

    private List<Double> calculateTheAnglesOfBallWithRespectToTheGoalForTeam(Team team) {
        double topAngle = calculateTheAngleOfBallWithRespectToTheGoal(getOppositeHSideOfTeam(team), VSide.TOP);
        double bottomAngle = calculateTheAngleOfBallWithRespectToTheGoal(getOppositeHSideOfTeam(team), VSide.BUTTON);
        System.out.println("CASTED ANGLES OF BALL TO TARGET --> START: " + topAngle + " , END: " + bottomAngle);
        List<Double> result = new ArrayList<>();
        if (topAngle < 90 && bottomAngle > 270) { //CONDITION-1
            int counter = (int) ((topAngle + (360 - bottomAngle)) / DIRECT_SHOOT_CHECK_STEP);
            for (double i = bottomAngle; counter > 0; i += DIRECT_SHOOT_CHECK_STEP, counter--) {
                if (i >= 360)
                    i -= 360;
                if (isTheBallWayToGoalCleanForAngle(i, getOppositeHSideOfTeam(team)))
                    result.add(i);
            }
        } else if ((topAngle < 90 && bottomAngle < 90) || (topAngle > 270 && bottomAngle > 270)) { //CONDITION-2 & CONDITION-3
            int counter = (int) (Math.abs(topAngle - bottomAngle) / DIRECT_SHOOT_CHECK_STEP);
            for (double i = bottomAngle; counter > 0; i += DIRECT_SHOOT_CHECK_STEP, counter--)
                if (isTheBallWayToGoalCleanForAngle(i, getOppositeHSideOfTeam(team)))
                    result.add(i);
        } else if ((topAngle < 270 && bottomAngle < 270 && topAngle > 90 && bottomAngle > 90)) { //CONDITION-4 & CONDITION-5 & CONDITION-6
            int counter = (int) (Math.abs(topAngle - bottomAngle) / DIRECT_SHOOT_CHECK_STEP);
            for (double i = bottomAngle; counter > 0; i -= DIRECT_SHOOT_CHECK_STEP, counter--)
                if (isTheBallWayToGoalCleanForAngle(i, getOppositeHSideOfTeam(team)))
                    result.add(i);
        }
        return result;
    }

    private static List<Double> averageSequences(List<Double> input) {
        int counter = 0;
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < input.size(); ) {
//            System.out.println("I --> " + i);
//            System.out.println("RESULT LIST IS-->   " + Arrays.toString(result.toArray()));
            counter = 1;
            for (int j = i; j < input.size() - 1; j++, counter++) {
                if (input.get(j) + DIRECT_SHOOT_CHECK_STEP != input.get(j + 1))
                    break;
            }
//            System.out.println("COUNTER: " + counter);
            if (counter > 1) {
                double[] temp = new double[counter];
                for (int j = i, k = 0; k < counter; j++, k++) {
                    temp[k] = input.get(j);
                }
//                System.out.println("TEMP -->    " + Arrays.toString(temp));
//                System.out.println("AVERAGE IS: " + average(temp));
//                result.add(average(temp));
                result.add(temp[temp.length / 2]);
                i += counter;
                continue;
            }
            result.add(input.get(i));
            i++;
        }
        return result;
    }

    private List<DirectShoot> averageSequenceDirectShoots(List<DirectShoot> input) {
        int counter;
        List<DirectShoot> result = new ArrayList<>();
        List<List<DirectShoot>> separatedShoots = new ArrayList<>();
        // hard coding :)
        separatedShoots.add(new ArrayList<>());  //0
        separatedShoots.add(new ArrayList<>());  //1
        separatedShoots.add(new ArrayList<>());  //2
        separatedShoots.add(new ArrayList<>());  //3
        separatedShoots.add(new ArrayList<>());  //4

        for (int i = 0; i < input.size(); i++)
            separatedShoots.get(input.get(i).getPlayer().getId()).add(input.get(i));

        List<Double> temp = new ArrayList<>();
        for (int i = 0; i < separatedShoots.size(); i++) {
            temp.clear();
            for (int j = 0; j < separatedShoots.get(i).size(); j++)
                temp.add(separatedShoots.get(i).get(j).getBallShootAngle());
            temp = averageSequences(temp);
            for (int j = 0; j < temp.size(); j++) {
                for (int k = 0; k < separatedShoots.get(i).size(); k++) {
                    if (separatedShoots.get(i).get(k).getBallShootAngle() == temp.get(j)) {
                        result.add(separatedShoots.get(i).get(k));
                    }
                }
            }
        }

//        for (int i = 0; i < input.size(); ) {
//            counter = 1;
//            for (int j = i; j < input.size() - 1; j++, counter++) {
//                if (input.get(i).getPlayer().equals(input.get(i + 1).getPlayer())) {
//                    if (input.get(j).getBallShootAngle() + 1 != input.get(j + 1).getBallShootAngle())
//                        break;
//                }
//            }
//            if (counter > 1) {
//                DirectShoot[] temp = new DirectShoot[counter];
//                for (int j = i, k = 0; k < counter; j++, k++) {
//                    temp[k] = input.get(j);
//                }
//                result.add(average(temp));
//                i += counter;
//                continue;
//            }
//            result.add(input.get(i));
//            i++;
//        }
        return result;
    }

    private List<IndirectShoot> averageSequenceIndirectShoots(List<IndirectShoot> input) {
        int counter;
        List<IndirectShoot> result = new ArrayList<>();
        for (int i = 0; i < input.size(); ) {
            counter = 1;
            for (int j = i; j < input.size(); j++, counter++) {
                if (input.get(j).getFinalPoint().getY() + 1 != input.get(j + 1).getFinalPoint().getY())
                    break;
            }
            if (counter > 1) {
                int index = i + (counter / 2);
                result.add(input.get(index));
                i += counter;
                continue;
            }
            result.add(input.get(i));
            i++;
        }
        return result;
    }

    private static double average(double[] input) {
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i];
        }
        return (sum / input.length);
    }

    private static DirectShoot average(DirectShoot[] input) {
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i].getBallShootAngle();
        }
        return new DirectShoot(input[0].getPlayer(), input[0].getPlayerShootAngle(), (sum / input.length), input[0].getTeam());
    }

    private int calculateTheBestAngleOfPlayerForBallWithAngle(Player player, int ballAngle) {
        return 0;
    }

    private boolean isTheBallWayToGoalCleanForAngle(double angle, HSide side) {
        final double x = (side == enemyHSide ? TARGET_RIGHT_X : TARGET_LEFT_X);
        final double y = calculateTheYOnX(ball.getPosition(), angle, x);
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) { //CHECKING ALL PLAYERS
            if (calculateTheMinimumDistanceBetweenThePointAndTheLine(ball.getPosition(), new Position(x, y), game.getOppTeam().getPlayer(i).getPosition()) <= MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER) {
//                System.out.println("PLAYER(" + i + ") OF ENEMY TEAM IS BLOCKING THE BALL FOR ANGLE: " + angle);
                return false;
            }
            if (calculateTheMinimumDistanceBetweenThePointAndTheLine(ball.getPosition(), new Position(x, y), game.getMyTeam().getPlayer(i).getPosition()) <= MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER) {
//                System.out.println("PLAYER(" + i + ") OF OUR TEAM IS BLOCKING THE BALL FOR ANGLE: " + angle);
                return false;
            }
        }
//        System.out.println("THE WAY IS CLEAN FOR BALL TO TARGET WITH ANGLE: " + angle);
        return true;
    }

    protected static double calculateTheYOnX(Position point, double angle, double x) {
        final double gradient = Math.tan(Math.toRadians(angle));
        final double deltaX = point.getX() - x;
        final double deltaY = gradient * deltaX;
        return point.getY() - deltaY;
    }

    protected static double calculateTheMinimumDistanceBetweenThePointAndTheLine(Position pointL1, Position pointL2, Position point) {
        final double A = point.getX() - pointL1.getX();
        final double B = point.getY() - pointL1.getY();
        final double C = pointL2.getX() - pointL1.getX();
        final double D = pointL2.getY() - pointL1.getY();
        final double dotProduct = (A * C) + (B * D);
        final double lengthSq = (C * C) + (D * D);
        double param = -1;
        double xx, yy;
        if (lengthSq != -1)
            param = dotProduct / lengthSq;
        if (param < 0) {
            xx = pointL1.getX();
            yy = pointL1.getY();
        } else if (param > 1) {
            xx = pointL2.getX();
            yy = pointL2.getY();
        } else {
            xx = pointL1.getX() + (param * C);
            yy = pointL1.getY() + (param * D);
        }
        double dx = point.getX() - xx;
        double dy = point.getY() - yy;
        double result = Math.sqrt((dx * dx) + (dy * dy));
//        System.out.println("CALCULATED MINIMUM DISTANCE IS: " + result);
        return result;
    }

    protected static boolean isTheWayClean(Position from, Position destination, Position object, double minDistance) {
//        System.out.println("CHECKING IS THE WAY CLEAN...");
        if (from.getX() > destination.getX() && destination.getX() > (object.getX() + minDistance))
            return true;
        if (from.getX() < destination.getX() && destination.getX() < (object.getX() - minDistance))
            return true;
        if (from.getY() > destination.getY() && destination.getY() > (object.getY() + minDistance))
            return true;
        if (from.getY() < destination.getY() && destination.getY() < (object.getY() - minDistance))
            return true;
        if (calculateTheMinimumDistanceBetweenThePointAndTheLine(from, destination, object) <= minDistance) {
//            System.out.println("THERE IS A COLLISION!");
            return false;
        }
        return true;
    }

    private double calculateTheAngleOfBallWithRespectToTheGoal(HSide goalHSide, VSide goalVSide) {
        int goalXPos = (goalHSide == enemyHSide ? FIELD_MAX_X : FIELD_MIN_X);
        double goalYPos = (goalVSide == VSide.TOP ? TARGET_TOP_Y : TARGET_BOTTOM_Y);
//        System.out.println("GOAL-XPOS: " + goalXPos + " , GOAL-YPOS: " + goalYPos);
//        double m = ((ball.getPosition().getY() - goalYPos) / (ball.getPosition().getX() - goalXPos));
        double m = calculateTheMBetween2Points(ball.getPosition(), new Position(goalXPos, goalYPos));
//        System.out.println("CALCULATED GRADIENT OF BALL WITH RESPECT TO THE GOAL IS: " + m);
        double angle = Math.toDegrees(Math.atan(m));
        double uAngle = Math.abs(angle);
//        System.out.println("THE PURE ANGLE:     " + angle);
//        System.out.println("THE UNSIGNED ANGLE: " + uAngle);
        if (goalXPos > ball.getPosition().getX()) {
            if (goalYPos < ball.getPosition().getY())
                uAngle = 360 - uAngle;
        } else {
            if (goalYPos < ball.getPosition().getY())
                uAngle += 180;
            else
                uAngle = 180 - uAngle;
        }
//        double angle = calculateAngleOfTwoPoints(ball.getPosition(), new Position(goalXPos, goalYPos));
//        System.out.println("ANGLE IS: " + angle);
//        if (angle < 0)
//            angle += 360;
//        System.out.println("FINAL ANGLE IS: " + uAngle);
        return uAngle;
    }

    private double calculateDistanceBetweenPlayerAndTheBall(Player player) {
//        System.out.println("CALCULATING THE DISTANCE BETWEEN PLAYER(" + player.getName() + ") AND THE BALL");
        return Math.sqrt(Math.pow(player.getPosition().getX() - ball.getPosition().getX(), 2) + Math.pow(player.getPosition().getY() + ball.getPosition().getY(), 2));
    }

    protected static double calculateDistanceBetweenTwoPoints(Position point1, Position point2) {
        return Math.sqrt(Math.pow(point1.getX() - point2.getX(), 2) + Math.pow(point1.getY() - point2.getY(), 2));
    }

    private int findTheNearestPlayerToTheBall(Team team) {
        System.out.println("FINDING THE NEAREST PLAYER OF TEAM WITH SCORE(" + team.getScore() + ") AND THE BALL");
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < 5; i++) {
            double temp = calculateDistanceBetweenPlayerAndTheBall(team.getPlayer(i));
            if (temp < min) {
                min = temp;
                minIndex = i;
            }
        }
        System.out.println("THE NEAREST PLAYER FROM TEAM WITH SCORE(" + team.getScore() + ") TO THE BALL IS PLAYER(" + team.getPlayer(minIndex).getName() + ")");
        return minIndex;
    }

    private DirectShoot findTheNearestPlayerToTheBall(List<DirectShoot> input) {
        double min = Double.MAX_VALUE;
        DirectShoot minP = null;
        double temp;
        for (int i = 0; i < input.size(); i++) {
            temp = calculateDistanceBetweenTwoPoints(ball.getPosition(), input.get(i).getPlayer().getPosition());
            if (temp < min) {
                minP = input.get(i);
                min = temp;
            }
        }
        return minP;
    }

    private List<DirectShoot> filterDirectShoots(List<DirectShoot> input) {
        List<DirectShoot> resultDirectShoots = new ArrayList<>();


        return resultDirectShoots;
    }

    protected static DirectShoot findTheBestDirectShoot(List<DirectShoot> input) {
        //TODO--> THIS METHOD DOES WRONG WHEN THE ANGLES ARE AROUND 0 OR 360
        double min = Double.MAX_VALUE;
        DirectShoot minP = null;
        double temp;
        for (int i = 0; i < input.size(); i++) {
            temp = Math.abs(input.get(i).getPlayerShootAngle() - input.get(i).getBallShootAngle());
            if (temp < min) {
                minP = input.get(i);
                min = temp;
            }
        }
        return minP;
    }

    private int findTheNearestPlayerToTheBall(List<Integer> chosenPlayers, Team team) {
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < chosenPlayers.size(); i++) {
            double temp = calculateDistanceBetweenPlayerAndTheBall(team.getPlayer(chosenPlayers.get(i)));
            if (temp < min) {
                min = temp;
                minIndex = i;
            }
        }
        return chosenPlayers.get(minIndex);
    }

    private HSide getHSideOfTeam(Team team) {
        if (team.equals(game.getMyTeam()))
            return myHSide;
        return enemyHSide;
    }

    private HSide getOppositeHSideOfTeam(Team team) {
        if (getHSideOfTeam(team) == HSide.RIGHT)
            return HSide.LEFT;
        return HSide.RIGHT;
    }

    private static double calculateAngleOfTwoPoints(Position p1, Position p2) {
        double angle = Math.abs(Math.toDegrees(Math.atan((p2.getY() - p1.getY()) / (p2.getX() - p1.getX()))));
        if (p2.getX() > p1.getX()) {
            if (p2.getY() < p1.getY())
                angle = 360 - angle;
        } else {
            if (p2.getY() < p1.getY())
                angle += 180;
            else
                angle = 180 - angle;
        }
        return angle;
    }
}
