

import java.util.Date;

/**
 * @author mdsinalpha
 */

public class Strategy {
    public static Player[] init_players() {
        Player[] players = new Player[5];
        /*
        Here you can set each of your player's name and your team formation.
        In case of setting wrong position, server will set default formation for your team.
         */

        players[0] = new Player("MHN-1", new Position(-6.5, 0));
        players[1] = new Player("MHN-2", new Position(-1, 0));
//        players[1] = new Player("MHN-2", new Position(-2, 1));//OR
//        players[1] = new Player("MHN-2", new Position(-1.5, 2));
        players[2] = new Player("MHN-3", new Position(-5, -2));
        players[3] = new Player("MHN-4", new Position(-5, 2));
//        players[4] = new Player("MHN-5", new Position(-2, -1));//OR
//        players[4] = new Player("MHN-5", new Position(-1.5, -2));
        players[4] = new Player("MHN-5", new Position(-2.5, 0));
//        players[0] = new Player("R. Ahmadi", new Position(-6.5, 0));
//        players[1] = new Player("E. Hajisafi", new Position(-2, 1));
//        players[2] = new Player("M. Karimi", new Position(-5, -2));
//        players[3] = new Player("M. Navidkia", new Position(-5, 2));
//        players[4] = new Player("H. Aghili", new Position(-2, -1));
        return players;
    }


    public static Triple do_turn(Game game) {
        Triple act = new Triple();
            /*
            Write your code here
            At the end you have to set 3 parameter:
                player id -> act.setPlayerID()
                angle -> act.setAngle()
                power -> act.setPower()
             */

//        //Sample code for shooting a random player in the ball direction with the maximum power:
//
//        Random rnd = new Random();
//        int player_id = Math.abs(rnd.nextInt() % 5);
//        act.setPlayerID(player_id); //Choose a random player
//
//
//        double x1, x2, y1, y2;
//        x1 = game.getMyTeam().getPlayer(player_id).getPosition().getX();
//        y1 = game.getMyTeam().getPlayer(player_id).getPosition().getY();
//        x2 = game.getBall().getPosition().getX();
//        y2 = game.getBall().getPosition().getY();
//        int angle = Math.abs((int) Math.toDegrees(Math.atan((y2 - y1) / (x2 - x1)))); //Calculate the angle from the chosen player to the ball
//        if (x2 > x1) {
//            if (y2 < y1)
//                angle = 360 - angle;
//        } else {
//            if (y2 < y1)
//                angle += 180;
//            else
//                angle = 180 - angle;
//        }
//        act.setAngle(angle);
//
//        act.setPower(100);


//        act.setPlayerID(new Random().nextInt() % 5);
//        act.setAngle(new Random().nextInt() % 360);
//        act.setPower(100);

        System.out.println(new Date().getTime());
        MHN_AI ai = new MHN_AI(act, game);
        ai.action();
        System.out.println("COMMANDED--> ANGLE: " + act.getAngle() + " , POWER: " + act.getPower() + " , PLAYER: " + act.getPlayer_id());
        System.out.println(new Date().getTime());
        return act;
    }
}
