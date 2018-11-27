package mhn_ai;

import uiai.*;

public class DirectShoot {
    private static final double DISTANCE_TO_BALL_SCORE = 1d;
    private static final double DIGRESSION_ANGLE_SCORE = 2d;
    private Player player;
    private double playerShootAngle;
    private double ballShootAngle;
    private Game game;
    private Team team;
    private float negativeRate = -1f;
    private double distanceToBall = -1f;
    private double digressionAngle = -1d;
    private Position playerStrikePoint;


    public DirectShoot(Player player, double playerShootAngle) {
        this.player = player;
        this.playerShootAngle = playerShootAngle;
    }

    public DirectShoot(Player player, double playerShootAngle, double ballShootAngle) {
        this.player = player;
        this.playerShootAngle = playerShootAngle;
        this.ballShootAngle = ballShootAngle;
    }

    public DirectShoot(Player player, double playerShootAngle, double ballShootAngle, Team team) {
        this.player = player;
        this.playerShootAngle = playerShootAngle;
        this.ballShootAngle = ballShootAngle;
        this.team = team;
    }

    public DirectShoot(Player player, double playerShootAngle, double ballShootAngle, Position playerStrikePoint) {
        this.player = player;
        this.playerShootAngle = playerShootAngle;
        this.ballShootAngle = ballShootAngle;
        this.playerStrikePoint = playerStrikePoint;
    }

    public void calculateRate(Ball ball) {
        calculateDigressionAngle(ball);
        calculateDistanceToBall(ball);
        negativeRate = (float) ((distanceToBall * DISTANCE_TO_BALL_SCORE) + (digressionAngle * DIGRESSION_ANGLE_SCORE));
    }

    public void calculateDistanceToBall(Ball ball) {
        distanceToBall = MHN_AI.calculateDistanceBetweenTwoPoints(player.getPosition(), ball.getPosition());
    }

    public void calculateDigressionAngle(Ball ball) {
        if (playerShootAngle < 270 && playerShootAngle > 360)
            digressionAngle = 360 - playerShootAngle;
        else
            digressionAngle = playerShootAngle;
    }


    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public double getPlayerShootAngle() {
        return playerShootAngle;
    }

    public void setPlayerShootAngle(double playerShootAngle) {
        this.playerShootAngle = playerShootAngle;
    }

    public double getBallShootAngle() {
        return ballShootAngle;
    }

    public void setBallShootAngle(double ballShootAngle) {
        this.ballShootAngle = ballShootAngle;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public float getNegativeRate() {
        return negativeRate;
    }

    public DirectShoot setNegativeRate(float negativeRate) {
        this.negativeRate = negativeRate;
        return this;
    }

    public double getDistanceToBall() {
        return distanceToBall;
    }

    public DirectShoot setDistanceToBall(double distanceToBall) {
        this.distanceToBall = distanceToBall;
        return this;
    }

    public double getDigressionAngle() {
        return digressionAngle;
    }

    public DirectShoot setDigressionAngle(double digressionAngle) {
        this.digressionAngle = digressionAngle;
        return this;
    }

    public Team getTeam() {
        return team;
    }

    public DirectShoot setTeam(Team team) {
        this.team = team;
        return this;
    }

    public Position getPlayerStrikePoint() {
        return playerStrikePoint;
    }

    public DirectShoot setPlayerStrikePoint(Position playerStrikePoint) {
        this.playerStrikePoint = playerStrikePoint;
        return this;
    }

    @Override
    public String toString() {
        return "Player-Id: " + player.getId() + " ,Player-DirectShoot-Angle: " + playerShootAngle + " ,Ball-DirectShoot-Angle: " + ballShootAngle;
    }
}
