import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MHN_AI {
    protected static final int POWER_MAX = 100;
    protected static final int ANGLE_MAX = 359;
    protected static final int ANGLE_MIN = 0;
    protected static final float BALL_DIAMETER = 0.5f; //ORIGINAL
    //    protected static final float BALL_DIAMETER = 0.6f;
    protected static final float PLAYER_DIAMETER = 1f; //ORIGINAL
    //    protected static final float PLAYER_DIAMETER = 1.1f;
    //        protected static final float PLAYER_DIAMETER = 1.04f;
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
    protected static final int EMPTY_CODE = 204;
    protected static final int FAILED_CODE = -1;
    protected static final int PLAYERS_COUNT_IN_EACH_TEAM = 5;
    protected static final float DIRECT_SHOOT_THRESHOLD_ANGLE = 60f;
    protected static final float MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER = (BALL_DIAMETER + PLAYER_DIAMETER) / 2;
    protected static final float MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS = PLAYER_DIAMETER;
    private static final int DANGER_ZONE_MAX_X = -5;
    private static final int DANGER_ZONE_PLUS_MAX_X = -4;
    private static final int ENEMY_DANGER_ZONE_MIN_X = 5;
    private static final float OWN_GOAL_INDIRECT_STRIKE_THRESHOLD = 3f;
    //    private static final float DISTANCE_PER_100POWER = 15.75f;
    private static final float DISTANCE_PER_100POWER = 9.183f;
    private static final float BALL_THRESHOLD_FOR_INDIRECT_DEFENCE = -6.0f;
    private static final Position RIGHT_TARGET_CENTER = new Position(TARGET_RIGHT_X, 0);
    protected static final DirectShoot FAILED_DIRECT_SHOOT = new DirectShoot(null, FAILED_CODE, FAILED_CODE);
    protected final Triple act;
    protected final Game game;
    protected final HSide myHSide, enemyHSide;
    protected final Ball ball;

    //TODO--> THESE ARE WHAT I NEED TO DO:
    //TODO-->(DONE)   I- FIX THE FUCKING INDIRECT DEFENCE
    //TODO-->(DONE)  II- ADD INDIRECT OWN GOAL
    //TODO-->(DONE) III- FIND THE EXACT PLAYER DIAMETER
    //TODO-->(DONE)  IV- CHANGE THE FORMATION
    //TODO-->         V- ADD FRACTION [HUGE]    ):
    //TODO-->(DONE)  VI- OWN GOAL CLASS CLEAN WAY CHECKING HAS SOME ISSUES
    //TODO-->(DONE) VII- CHANGE TAKE THE BALL TO THE CORNER THRESHOLD
    //TODO-->        IX- IN DIRECT STRIKE CHANGE PLAYER SHOOT ANGLE IF THE BALL IS SO CLOSE TO THE WALL
    //TODO-->(DONE)   X- IF THE BALL ANGLES WITH RESPECT TO THE GOAL (ENEMY GOAL) IS IN 2 OR 3 PART JUST HIT THE BALL
    //TODO-->(DONE)  XI- REDUCE INDIRECT OWN GOAL THRESHOLD
    //TODO-->       XII- INCREASE SUPER DEFENCE THRESHOLD
    //TODO-->       XIV-


    public MHN_AI(Triple act, Game game) {
        this.act = act;
        this.game = game;
        this.ball = game.getBall();
        this.myHSide = HSide.LEFT;
        this.enemyHSide = HSide.RIGHT;
        printStatus();
    }

    public void action() {

        if (canMakeADirectGoal()) {
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-GOAL-DIRECTLY!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
//        System.out.println("CAN NOT MAKE A DIRECT GOAL!");
        if (canMakeAnIndirectGoal()) {
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-GOAL-INDIRECTLY!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
//        System.out.println("CAN NOT MAKE AN INDIRECT GOAL!");
        if (canTakeTheBallAwayFromTarget()) {
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-TAKE-THE-BALL-AWAY-FROM-TARGET!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        if (canTakeTheBallToTheCorner()) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-TAKE-THE-BALL-TO-THE-CORNER!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
//        System.out.println("CAN NOT TAKE THE BALL AWAY FROM TARGET!");
        if (canParkTheBus()) {
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!CAN-PARK-THE-BUS!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        System.out.println("TEARING THE DEFENCE DOWN");
        if (canTearEnemyDefenceDown()) {
            return;
        }
//        System.out.println("CAN NOT PARK THE BUS!");
//        generateRandomShoot();
        act.setAngle(0);
        act.setPlayerID(0);
        act.setPower(0);
//        System.out.println("!DON'T MOVE!");
    }

    private boolean canMakeADirectGoal() {
        int playerShootAngle;
        int power = POWER_MAX;
        int playerId;

        List<Double> anglesToGoal = calculateTheAnglesOfBallWithRespectToTheGoalForTeam(game.getMyTeam());
        if (anglesToGoal.size() == 0) {
//            System.out.println("*************************CANNOT MAKE A DIRECT GOAL BECAUSE THE CLEAN ANGLES TO GOAL FROM BALL IS NULL!");
            return false;
        }

        for (int i = 0; i < anglesToGoal.size(); i++) {
            if (anglesToGoal.get(i) >= 90 && anglesToGoal.get(i) <= 270) {
                anglesToGoal = getAnglesToGoalTargetLine();
                break;
            }
        }
//        System.out.println("ALL DIRECT SHOOT POSSIBLE ANGLES:");
//        for (int i = 0; i < anglesToGoal.size(); i++)
//            System.out.println("    --POSSIBLE-ANGLE:   " + anglesToGoal.get(i));

        List<DirectShoot> directShoots = new ArrayList<>();
//        List<DirectShoot> directShootsAvg;
        for (int i = 0; i < anglesToGoal.size(); i++)
            directShoots.addAll(whichPlayersCanStrikeThisDirectly(anglesToGoal.get(i), game.getMyTeam()));
        directShoots = thresholdDirectShoot(directShoots); //NEW NEW NEW NEW
        if (directShoots.size() == 0) {
//            System.out.println("*********************************CANNOT MAKE A DIRECT GOAL BY DIRECT STRIKE BECAUSE THERE IS NO PLAYER TO SHOOT IT OUT!");
            //HERE IS THE START OF INDIRECT STRIKES...
            List<IndirectStrike> indirectStrikes = new ArrayList<>();
            for (int i = 0; i < anglesToGoal.size(); i++)
                indirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(anglesToGoal.get(i), game.getMyTeam()));
            if (indirectStrikes.size() == 0) {
//                System.out.println("***********************************CANNOT MAKE A DIRECT GOAL BY AN INDIRECT STRIKE BECAUSE THERE IS NO PLAYER TO SHOOT IT OUT!");
//                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!TRYING TO MAKE AN OWN GOAL!!!!!!!!!!!!!!!!!!!!!!!!");
                return canMakeAnOwnGoal(anglesToGoal, false);
            }
            IndirectStrike chosenIndirectStrike = findTheBestIndirectStrike(indirectStrikes); //CHOOSER METHOD
            playerId = chosenIndirectStrike.getPlayer().getId();
            playerShootAngle = getIntAngle(chosenIndirectStrike.getPlayerShootAngle());
//            System.out.println("CHOSEN PLAYER(" + chosenIndirectStrike.getPlayer().getId() + ") FOR INDIRECT STRIKE FOR DIRECT SHOOT WITH ANGLE(" + chosenIndirectStrike.getPlayerShootAngle() + ") AND THE WALL-X(" + chosenIndirectStrike.getWallStrikePoint().getX() + ")");
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE A DIRECT GOAL BY AN INDIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
//            System.out.println("TOTAL PURE DIRECT SHOOTS CAPABLE:");
//            for (int i = 0; i < directShoots.size(); i++)
//                System.out.println("    CAPABLE-SHOOT -->   " + directShoots.get(i).toString());
            directShoots = averageSequenceDirectShoots(directShoots);
//            System.out.println("TOTAL PURE DIRECT SHOOTS CAPABLE(AFTER AVERAGE SEQUENCE DIRECT SHOOT):");
//            for (int i = 0; i < directShoots.size(); i++)
//                System.out.println("    CAPABLE-SHOOT -->   " + directShoots.get(i).toString());
            DirectShoot chosenDirectShoot = findTheBestDirectShoot(directShoots); //CHOOSER METHOD
//            System.out.println("CHOSEN-SHOOT ==>    " + chosenDirectShoot.toString());
            playerId = chosenDirectShoot.getPlayer().getId();
            playerShootAngle = getIntAngle(chosenDirectShoot.getPlayerShootAngle());
//            System.out.println("REAL ANGLE (NOT-CASTED): " + chosenDirectShoot.getPlayerShootAngle());
//            System.out.println("SETTING ANGLE TO(CASTED): " + playerShootAngle);
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE A DIRECT GOAL BY A DIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        act.setPlayerID(playerId);
        act.setPower(power);
        act.setAngle(playerShootAngle);
        return true;
    }

    private boolean canMakeAnIndirectGoal() {
        int playerId, power = 100, playerShootAngle;
//        System.out.println("||||||||||||||||||||||||||||||||||TRYING TO MAKE AN INDIRECT SHOOT||||||||||||||||||||||||||||||||||");
        Position targetTopPosition = new Position(TARGET_RIGHT_X, TARGET_TOP_Y);
        Position targetBottomPosition = new Position(TARGET_RIGHT_X, TARGET_BOTTOM_Y);
        final List<IndirectShoot> indirectShoots = new ArrayList<>();
        IndirectShoot indirectShoot;
        Position tempPosition;
        for (double i = targetBottomPosition.getY(); i <= targetTopPosition.getY(); i += INDIRECT_SHOOT_CHECK_STEP) {
            tempPosition = new Position(TARGET_RIGHT_X, i);
//            System.out.println("*****CHECKING INDIRECT SHOOTS FOR FINAL POINT:   " + tempPosition.toString());
            indirectShoot = new IndirectShoot(tempPosition, game);
            if (indirectShoot.isItPossibleForTop() || indirectShoot.isItPossibleForBottom())
                indirectShoots.add(indirectShoot);
        }
        if (indirectShoots.size() == 0) {
//            System.out.println("CANNOT MAKE AN INDIRECT GOAL BECAUSE THERE IS NO GOOD ANGLE FOR BALL!");
            return false;
        }
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>ALL POSSIBLE INDIRECT SHOOTS!");
//        for (IndirectShoot indirectShoot1 : indirectShoots) System.out.println(indirectShoot1.toString());
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

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
//            System.out.println("NO PLAYER CAN DIRECTLY STRIKE THIS INDIRECT-SHOOT!");
            for (int i = 0; i < indirectShoots.size(); i++) { //CAN STRIKE INDIRECTLY?
                if (indirectShoots.get(i).isTopPossible())
                    indirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(indirectShoots.get(i).getBallAngleTop(), game.getMyTeam()));
                if (indirectShoots.get(i).isBottomPossible())
                    indirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(indirectShoots.get(i).getBallAngleBottom(), game.getMyTeam()));
            }
            if (indirectStrikes.size() == 0) { //TRYING TO STRIKE DIRECTLY AND SHOOT INDIRECTLY TO OWN GOAL!
                System.out.println("TRYING TO MAKE AN INDIRECT SHOOT BY OWN PLAYERS!!!");
                List<Double> ballAnglesForIndirectOwnGoal = new ArrayList<>();
                IndirectShoot indirectShootTemp;
                for (int i = 0; i < indirectShoots.size(); i++) {
                    indirectShootTemp = indirectShoots.get(i);
                    if (indirectShootTemp.isTopPossible())
                        ballAnglesForIndirectOwnGoal.add(indirectShootTemp.getBallAngleTop());
                    if (indirectShootTemp.isBottomPossible())
                        ballAnglesForIndirectOwnGoal.add(indirectShootTemp.getBallAngleBottom());
                }
                if (ballAnglesForIndirectOwnGoal.size() == 0)
                    return false; //Just to make sure there will be no 'NullPointerException'
                return canMakeAnOwnGoal(ballAnglesForIndirectOwnGoal, true);
//                List<DirectShoot> directShootsOwn = new ArrayList<>();
//                for (int i = 0; i < indirectShoots.size(); i++) {
//                    if (indirectShoots.get(i).isTopPossible())
//                        directShootsOwn.addAll(whichPlayersCanStrikeThisDirectly(indirectShoots.get(i).getBallAngleTop(), game.getOppTeam()));
//                    if (indirectShoots.get(i).isBottomPossible())
//                        directShootsOwn.addAll(whichPlayersCanStrikeThisDirectly(indirectShoots.get(i).getBallAngleBottom(), game.getOppTeam()));
//                }
//                if (directShootsOwn.size() == 0) return false;
//                directShoots = new ArrayList<>();
//                Position finalPosition;
//                for (int i = 0; i < directShootsOwn.size(); i++) {
//                    finalPosition = calculateTheExpectedPlayerStrikePoint(directShootsOwn.get(i).getPlayer(), directShootsOwn.get(i).getPlayerShootAngle());
//                    directShoots.addAll(whichPlayersCanStrikeThisDirectly(finalPosition, game.getMyTeam()));
//                }
//                if (directShoots.size() == 0) return false;
//                DirectShoot directShoot = findTheBestDirectShoot(directShoots);
//                act.setAngle(getIntAngle(directShoot.getPlayerShootAngle()));
//                act.setPlayerID(directShoot.getPlayer().getId());
//                act.setPower(POWER_MAX);
//                System.out.println("DIRECT STRIKE INDIRECT SHOOT OWN GOAL");
//                return true;
            }
            IndirectStrike indirectStrike = findTheBestIndirectStrike(indirectStrikes); //CHOOSER METHOD
            playerId = indirectStrike.getPlayer().getId();
            playerShootAngle = getIntAngle(indirectStrike.getPlayerShootAngle());
            power = POWER_MAX;
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE AN INDIRECT GOAL BY AN INDIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
//            System.out.println("ALL POSSIBLE DIRECT STRIKES:");
//            for (DirectShoot directShoot : directShoots) System.out.println(directShoot.toString());
            DirectShoot directShoot = findTheBestDirectShoot(directShoots); //CHOOSER METHOD
//            System.out.println("++++++++++++++++SELECTED DIRECT STRIKE FOR INDIRECT SHOOT++++++++++++");
//            System.out.println(directShoot.toString());
            playerId = directShoot.getPlayer().getId();
            playerShootAngle = getIntAngle(directShoot.getPlayerShootAngle());
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE AN INDIRECT GOAL BY A DIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        act.setPlayerID(playerId);
        act.setPower(power);
        act.setAngle(playerShootAngle);
        return true;
    }

    private boolean canTakeTheBallAwayFromTarget() {
        if (ball.getPosition().getX() > 0 && !canTeamGoalDirectly(game.getOppTeam())) return false;
        int power = POWER_MAX, playerAngle, playerId;
        System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||TRYING TO TAKE THE BALL AWAY FROM OUT TARGET||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        List<DirectShoot> directShoots = new ArrayList<>();
        for (int i = 0, j = 359; i < 80; i++, j--) {
//            System.out.println("CHECKING TAKING THE BALL AWAY DIRECTLY FOR ANGLE: " + i + " AND " + j);
            directShoots.addAll(whichPlayersCanStrikeThisDirectly(i, game.getMyTeam()));
            directShoots.addAll(whichPlayersCanStrikeThisDirectly(j, game.getMyTeam()));
//            if (directShoots.size() > 0)
//                break;
        }
        for (int i = 0; i < directShoots.size(); i++) { //FILTERING DIRECT_SHOOTS
            if ((directShoots.get(i).getPlayer().getPosition().getX() + (PLAYER_DIAMETER / 2)) < FIELD_MIN_X && directShoots.get(i).getPlayerShootAngle() > 60 && directShoots.get(i).getPlayerShootAngle() < 300)
                directShoots.remove(i);
        }
        List<DirectShoot> directShootsClone = new ArrayList<>(directShoots);
        for (int i = 0; i < directShoots.size(); i++) {
            if (directShoots.get(i).getPlayerShootAngle() > 60 && directShoots.get(i).getPlayerShootAngle() < 300)
                directShoots.remove(i);
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
//            System.out.println("THERE IS NOT PLAYER TO TAKE THE BALL AWAY DIRECTLY!");
            if (ball.getPosition().getX() < MHN_AI.DANGER_ZONE_MAX_X) {
                System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||TRYING TO DO A SUPER DEFENCE||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
                SuperDefence superDefence;
                List<SuperDefence> superDefences = new ArrayList<>();
                for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
                    superDefence = new SuperDefence(game.getMyTeam().getPlayer(i), game);
                    if (superDefence.isThePlayerCapableOfSuperDefence()) superDefences.add(superDefence);
                }
                if (superDefences.size() > 0) {
                    superDefence = SuperDefence.findTheBestSuperDefence(superDefences, ball);
                    act.setPower(POWER_MAX);
                    act.setPlayerID(superDefence.getPlayer().getId());
                    act.setAngle(getIntAngle(superDefence.getPlayerShootAngle()));
                    System.out.println("SUPER DEFENCE SUCCESSFUL!");
                    return true;
                }
                System.out.println("NO PLAYER CAN DO SUPER DEFENCE!");
            } //If The SuperDefence can't be happened!
            if (directShootsClone.size() > 0) {
                DirectShoot directShoot = findTheBestDirectShoot(directShootsClone);
                act.setAngle(getIntAngle(directShoot.getPlayerShootAngle()));
                act.setPlayerID(directShoot.getPlayer().getId());
                act.setPower(POWER_MAX);
                return true;
            } //If there is no directShoots at all...
//            if (ball.getPosition().getX() > BALL_THRESHOLD_FOR_INDIRECT_DEFENCE) { //MIGHT BE RIGHT! :)
            if (ball.getPosition().getX() > (FIELD_MIN_X + (PLAYER_DIAMETER / 2))) {
                List<IndirectStrike> indirectStrikes = whichPlayersCanDefendIndirectly(game.getMyTeam());
                filterIndirectDefence(indirectStrikes);
                if (indirectStrikes.size() == 0) return false;
                IndirectStrike finalIndirectStrike = findTheBestIndirectStrike(indirectStrikes);
//                power = POWER_MAX;
//                playerId = finalIndirectStrike.getPlayer().getId();
//                playerAngle = (int) Math.round(finalIndirectStrike.getPlayerShootAngle());
                act.setPower(POWER_MAX);
                act.setPlayerID(finalIndirectStrike.getPlayer().getId());
                act.setAngle(getIntAngle(finalIndirectStrike.getPlayerShootAngle()));
                return true;
            }
            return false;
        } else {
            DirectShoot directShoot = findTheBestDirectShoot(directShoots); //CHOOSER METHOD
            playerAngle = getIntAngle(directShoot.getPlayerShootAngle());
            playerId = directShoot.getPlayer().getId();
            System.out.println("SELECTED DIRECT SHOOT FOR TAKING THE BALL AWAY :\n" + directShoot.toString());
        }
        act.setAngle(playerAngle);
        act.setPower(power);
        act.setPlayerID(playerId);
        return true;
    }

    private boolean canTeamGoalDirectly(Team team) {
        List<Double> ballAngles = calculateTheAnglesOfBallWithRespectToTheGoalForTeam(team);
        List<DirectShoot> directShoots = new ArrayList<>();
        if (ballAngles.size() == 0)
            return false;
        for (int i = 0; i < ballAngles.size(); i++) {
            directShoots.addAll(whichPlayersCanStrikeThisDirectly(ballAngles.get(i), team));
            if (directShoots.size() > 0)
                return true;
        }
        return false;
    }

    private boolean canParkTheBus() { //THE SAME 'PARK THE BUS'
        ParkTheBus parkTheBus = new ParkTheBus(game);
        ParkTheBus.Defence defence = parkTheBus.getBestDefence();
        if (defence == null)
            return false;
        act.setPower(defence.getPlayerShootPower());
        act.setPlayerID(defence.getPlayer().getId());
        act.setAngle(defence.getPlayerShootAngleInt());
//        System.out.println(">>>>>>>>>>>>>SELECTED-PARK-THE-BUS:\n" + defence.toString());
        return true;
    }

    private boolean canTearEnemyDefenceDown() {
        Player player = null;
        Player tempPlayer;
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            tempPlayer = game.getMyTeam().getPlayer(i);
            if (calculateDistanceBetweenTwoPoints(tempPlayer.getPosition(), RIGHT_TARGET_CENTER) <= 7) {
                player = tempPlayer;
            }
        }
        if (player == null) return false;
        Player targetPlayer = findOneDefencePlayer(game.getOppTeam());
        if (targetPlayer == null) return false;
        final double playerShootAngle = calculateTheAngleFromTo(player.getPosition(), targetPlayer.getPosition());
        act.setPower(POWER_MAX);
        act.setPlayerID(player.getId());
        act.setAngle(getIntAngle(playerShootAngle));
        return true;
    }

    private boolean canMakeAnOwnGoal(List<Double> ballShootAngles, boolean isIndirect) {
        List<DirectShoot> ownGoalDirectShoots = new ArrayList<>();
        ballShootAngles = averageSequences(ballShootAngles);
        OwnGoal ownGoal;
//        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
//            ownGoal = new OwnGoal(game, game.getOppTeam().getPlayer(i), ballShootAngles);
//            if (ownGoal.isItPossible()) {
//                DirectShoot directShoot = ownGoal.getTheBestStrikerPlayersDirectShoot();
//                act.setAngle((int) Math.round(directShoot.getPlayerShootAngle()));
//                act.setPlayerID(directShoot.getPlayer().getId());
//                act.setPower(POWER_MAX);
////                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!<<CAN MAKE A DIRECT OWN GOAL BY A DIRECT STRIKE>>!!!!!!!!!!!!!!!!!!!!!!!!");
//                return true;
//            }
//        }

        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            ownGoal = new OwnGoal(game, game.getOppTeam().getPlayer(i), ballShootAngles);
            if (!ownGoal.isItPossible()) continue;
            ownGoalDirectShoots.add(ownGoal.getTheBestStrikerPlayersDirectShoot());
        }
        DirectShoot directShoot;
        if (isIndirect) {
            for (int i = 0; i < ownGoalDirectShoots.size(); i++) {
                directShoot = ownGoalDirectShoots.get(i);
                if (
                        calculateDistanceBetweenTwoPoints(directShoot.getPlayerStrikePoint(), directShoot.getPlayer().getPosition()) > OWN_GOAL_INDIRECT_STRIKE_THRESHOLD
                                ||
                                calculateDistanceBetweenTwoPoints(directShoot.getPlayerStrikePoint(), ball.getPosition()) > OWN_GOAL_INDIRECT_STRIKE_THRESHOLD
                        )
                    ownGoalDirectShoots.remove(i);
            }
        }
        if (ownGoalDirectShoots.size() == 0) return false;
        DirectShoot finalDirectShoot = findTheBestDirectShoot(ownGoalDirectShoots);
        act.setAngle(getIntAngle(finalDirectShoot.getPlayerShootAngle()));
        act.setPlayerID(finalDirectShoot.getPlayer().getId());
        act.setPower(POWER_MAX);
        return true;
    }

    private boolean canTakeTheBallToTheCorner() {
        if ((ball.getPosition().getX() > DANGER_ZONE_PLUS_MAX_X) || (ball.getPosition().getY() < TARGET_TOP_Y && ball.getPosition().getY() > TARGET_BOTTOM_Y) || ((ball.getPosition().getX() - (BALL_DIAMETER / 2)) < TARGET_LEFT_X))
            return false;
        List<DirectShoot> directShoots;
        if (ball.getPosition().getY() > TARGET_TOP_Y) {
            directShoots = whichPlayersCanStrikeThisDirectly(90, game.getMyTeam());
        } else {
            directShoots = whichPlayersCanStrikeThisDirectly(270, game.getMyTeam());
        }
        for (int i = 0; i < directShoots.size(); i++)
            if (!(directShoots.get(i).getPlayerShootAngle() > 135 && directShoots.get(i).getPlayerShootAngle() < 225))
                directShoots.remove(i);
        if (directShoots.size() == 0) return false;
        DirectShoot directShoot = findTheBestDirectShoot(directShoots);
        act.setPower(getPowerByDistance(calculateDistanceBetweenTwoPoints(directShoot.getPlayer().getPosition(), ball.getPosition())));
        act.setPlayerID(directShoot.getPlayer().getId());
        act.setAngle(getIntAngle(directShoot.getPlayerShootAngle()));
        return true;
    }

    private List<Double> getAnglesToGoalTargetLine() {
        List<Double> result = new ArrayList<>();
        int counter = 89;
        for (int i = 0, j = 359; counter > 0; counter--, j--, i++) {
            result.add((double) i);
            result.add((double) j);
        }
        return result;
    }

    protected static DirectShoot calculateTheDirectShootOfPlayerStrikingTheBallDirectlyForBallAngle(Player player, double ballAngle, Ball ball) {
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
        final Position playerStrikePoint = calculateTheExpectedPlayerStrikePoint(ball, ballAngle); //original
//        System.out.println("PLAYER STRIKE POINT: " + playerStrikePoint.toString());
        final double playerShootAngle = calculateTheAngleFromTo(player.getPosition(), playerStrikePoint);
//        System.out.println("THE FINAL RESULT OF PLAYER(" + player.getId() + ") ANGLE IS:    " + playerShootAngle);
        if (playerShootAngle >= 90 && playerShootAngle <= 270 && (ballAngle < 90 || ballAngle > 270)) {
//            System.out.println("PLAYER(" + player.getId() + ") BAD POSITION! [STOPPED BY THRESHOLD]");
            return new DirectShoot(player, FAILED_CODE, ballAngle);
        }
        return new DirectShoot(player, playerShootAngle, ballAngle, playerStrikePoint);
    }

    protected static DirectShoot calculateTheDirectShootOfPlayerForPosition(Player player, Position finalPosition) {
        final double playerShootAngle = calculateTheAngleFromTo(player.getPosition(), finalPosition);
        return new DirectShoot(player, playerShootAngle);
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

    protected static Position calculateTheExpectedPlayerStrikePoint(Ball ball, double ballAngle) {
//        double xPos = ball.getPosition().getX() - (Math.cos(Math.toRadians(ballAngle)) * (BALL_DIAMETER) / 2);
//        double yPos = ball.getPosition().getY() - (Math.sin(Math.toRadians(ballAngle)) * (BALL_DIAMETER) / 2);
        final double xPos = ball.getPosition().getX() - (Math.cos(Math.toRadians(ballAngle)) * MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        final double yPos = ball.getPosition().getY() - (Math.sin(Math.toRadians(ballAngle)) * MINIMUM_COLLISION_DISTANCE_FOR_BALL_AND_PLAYER_FROM_CENTER);
        return new Position(xPos, yPos);
    }

    protected static Position calculateTheExpectedPlayerStrikePoint(Player player, double playerShootAngle) {
        final double xPos = player.getPosition().getX() - (Math.cos(Math.toRadians(playerShootAngle)) * MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS);
        final double yPos = player.getPosition().getY() - (Math.sin(Math.toRadians(playerShootAngle)) * MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS);
        return new Position(xPos, yPos);
    }

    protected static double calculateTheMBetween2Points(Position p1, Position p2) {
        return ((p1.getY() - p2.getY()) / (p1.getX() - p2.getX()));
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
        if (calculateTheMinimumDistanceBetweenThePointAndTheLine(from, destination, object) <= minDistance)
            return false;
        return true;
    }

    protected static double calculateDistanceBetweenTwoPoints(Position point1, Position point2) {
        return Math.sqrt(Math.pow(point1.getX() - point2.getX(), 2) + Math.pow(point1.getY() - point2.getY(), 2));
    }

    protected static DirectShoot findTheBestDirectShoot(List<DirectShoot> input) {
        double min = Double.MAX_VALUE;
        DirectShoot minP = null;
        double temp;
        for (int i = 0; i < input.size(); i++) {
            temp = Math.abs(input.get(i).getPlayerShootAngle() - input.get(i).getBallShootAngle());
            if (temp > 180)
                temp = 360 - temp;
            if (temp < min) {
                minP = input.get(i);
                min = temp;
            }
        }
        return minP;
    }

    protected static DirectShoot findTheNearestDirectShootByBallPlayer(List<DirectShoot> input) {
        double min = Double.MAX_VALUE;
        double temp;
        DirectShoot minDirectShoot = null;
        for (int i = 0; i < input.size(); i++) {
            temp = calculateDistanceBetweenTwoPoints(input.get(i).getPlayer().getPosition(), input.get(i).getBallPlayer().getPosition());
            if (temp < min) {
                min = temp;
                minDirectShoot = input.get(i);
            }
        }
        return minDirectShoot;
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

    protected static double getDistanceByPower(int power) {
        return (power * DISTANCE_PER_100POWER / 100);
    }

    protected static int getPowerByDistance(double distance) {
        return getIntAngle(distance * 100 / DISTANCE_PER_100POWER);
    }

    protected static int getIntAngle(double angle) {
        return ((int) Math.round(angle));
    }

    private void printStatus() {
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


    private Player findOneDefencePlayer(Team team) {
        Player player = null, tempPlayer;
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            tempPlayer = team.getPlayer(i);
            if (isDefencingForEnemy(tempPlayer)) {
                player = tempPlayer;
                break;
            }
        }
        return player;
    }

    private boolean isDefencingForEnemy(Player player) {
        return player.getPosition().getX() >= 5 && player.getPosition().getY() <= 1.5 && player.getPosition().getY() >= -1.5;
    }

//    protected static Position calculateTheExpectedPlayerStrikePoint(Player player, int ballAngle) {
//        double xPos = player.getPosition().getX() + (Math.cos(Math.toRadians(ballAngle)) * PLAYER_DIAMETER / 2);
//        double yPos = player.getPosition().getY() + (Math.sin(Math.toRadians(ballAngle)) * PLAYER_DIAMETER / 2);
//        return new Position(xPos, yPos);
//    }

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
            playerDirectShoot = calculateTheDirectShootOfPlayerStrikingTheBallDirectlyForBallAngle(team.getPlayer(i), ballAngle, ball);
            if (playerDirectShoot.getPlayerShootAngle() == FAILED_CODE) continue;
            if (!isTheWayOfPlayerToBallCleanForDirectShoot(playerDirectShoot)) continue;
            result.add(playerDirectShoot);
        }
        return result;
    }

    private void filterIndirectDefence(List<IndirectStrike> input) {
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).getStrikePlayerPosition().getX() < (FIELD_MIN_X + (PLAYER_DIAMETER / 2)))
                input.remove(i);
        }
    }

    private List<DirectShoot> whichPlayersCanStrikeThisDirectly(Position shooterFinalPosition, Team team) {
        List<DirectShoot> resultDirectShoots = new ArrayList<>();
//        DirectShoot directShoot;
        Player checkingPlayer;
        Player player;
        boolean capable;
        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            player = team.getPlayer(i);
            capable = true;
            for (int j = 0; j < PLAYERS_COUNT_IN_EACH_TEAM; j++) {
                checkingPlayer = game.getMyTeam().getPlayer(j);
                if (checkingPlayer != player)
                    if (!isTheWayClean(player.getPosition(), shooterFinalPosition, checkingPlayer.getPosition(), MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS)) {
                        capable = false;
                        break;
                    }
                checkingPlayer = game.getOppTeam().getPlayer(j);
                if (checkingPlayer != player)
                    if (!isTheWayClean(player.getPosition(), shooterFinalPosition, checkingPlayer.getPosition(), MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS)) {
                        capable = false;
                        break;
                    }
            }
            if (capable)
                resultDirectShoots.add(calculateTheDirectShootOfPlayerForPosition(player, shooterFinalPosition));
        }
        return resultDirectShoots;
    }

    private List<IndirectStrike> whichPlayersCanDefendIndirectly(Team team) {
        List<IndirectStrike> resultIndirectStrikes = new ArrayList<>();
        for (int i = 0, j = 359; i < 70; i++, j--) {
            resultIndirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(i, team));
            resultIndirectStrikes.addAll(whichPlayersCanStrikeThisIndirectly(j, team));
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
            if (!indirectStrike.canThePlayerStrikeIndirectly()) continue;
            if (!indirectStrike.isThePlayerWayToTheBallClean(game)) continue;
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
            if (!isTheWayClean(directShoot.getPlayer().getPosition(), directShoot.getPlayerStrikePoint(), playerPosition, MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                return false;
        }

        for (int i = 0; i < PLAYERS_COUNT_IN_EACH_TEAM; i++) {
            playerPosition = game.getOppTeam().getPlayer(i).getPosition();
            if (game.getOppTeam().getPlayer(i) == directShoot.getPlayer())
                continue;
            if (directShoot.getPlayer().getPosition().getX() == playerPosition.getX() && directShoot.getPlayer().getPosition().getY() == playerPosition.getY())
                continue;
            if (!isTheWayClean(directShoot.getPlayer().getPosition(), directShoot.getPlayerStrikePoint(), playerPosition, MINIMUM_COLLISION_DISTANCE_FOR_2_PLAYERS))
                return false;
        }
        return true;
    }

    private boolean isTheWayOfPlayerToPointClean() {
        return false;
    }

    private boolean thresholdDirectShoot(DirectShoot directShoot) {
        return (Math.abs(directShoot.getPlayerShootAngle() - directShoot.getBallShootAngle()) < DIRECT_SHOOT_THRESHOLD_ANGLE
                ||
                ball.getPosition().getX() > ENEMY_DANGER_ZONE_MIN_X
        );
    }

    protected List<DirectShoot> thresholdDirectShoot(List<DirectShoot> directShoots) {
        List<DirectShoot> resultDirectShoots = new ArrayList<>();
        for (int i = 0; i < directShoots.size(); i++) {
            if (thresholdDirectShoot(directShoots.get(i)))
                resultDirectShoots.add(directShoots.get(i));
        }
        return resultDirectShoots;
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
        act.setPower(50);
    }

    private List<Double> calculateTheAnglesOfBallWithRespectToTheGoalForTeam(Team team) {
        double topAngle = calculateTheAngleOfBallWithRespectToTheGoal(getOppositeHSideOfTeam(team), VSide.TOP);
        double bottomAngle = calculateTheAngleOfBallWithRespectToTheGoal(getOppositeHSideOfTeam(team), VSide.BUTTON);
//        System.out.println("CASTED ANGLES OF BALL TO TARGET --> START: " + topAngle + " , END: " + bottomAngle);
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

    private int findTheNearestPlayerToTheBall(Team team) {
//        System.out.println("FINDING THE NEAREST PLAYER OF TEAM WITH SCORE(" + team.getScore() + ") AND THE BALL");
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < 5; i++) {
            double temp = calculateDistanceBetweenPlayerAndTheBall(team.getPlayer(i));
            if (temp < min) {
                min = temp;
                minIndex = i;
            }
        }
//        System.out.println("THE NEAREST PLAYER FROM TEAM WITH SCORE(" + team.getScore() + ") TO THE BALL IS PLAYER(" + team.getPlayer(minIndex).getName() + ")");
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

    protected static boolean isTheSamePlayerByLocation(Player player1, Player player2) {
        return (player1.getPosition().getX() == player2.getPosition().getX() && player1.getPosition().getY() == player2.getPosition().getY());
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
}
