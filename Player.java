import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {


    public static int BASE_X = 0;
    public static int BASE_Y = 0;

    public static int MAX_X = 16000;
    public static int MAX_Y = 9000;
    public static int STEPS;

    public static boolean FOLLOW_MY_HUNTER = true;



    public static void main(String args[]) {

        class Point{
            public int x;
            public int y;
            public Point(int x, int y){
                this.x = x;
                this.y = y;
            }
        }

        class State{
            public  int idle = 0;
            public int carryingGhost = 1;
            public int stunned = 2;
            public int trapping = 3;
            public int busting = 4;
        }


        class Creature {
            State state_Enum = new State();
            public int id;
            public int x;
            public int y;

            public double getDistance(int tx, int ty){
                double temp = (tx - this.x)*(tx - this.x) + (ty - this.y)*(ty - this.y);
                return Math.sqrt(temp);
            }
            public double getDistance(Creature other){
                double temp = (other.x - this.x)*(other.x - this.x) + (other.y - this.y)*(other.y - this.y);
                return Math.sqrt(temp);
            }

        }

        class Ghost extends Creature{
            public int stamina; // the state value of the ghost
            public int attemptBusters;
            public Ghost(int id,int x, int y, int state, int value){
                this.id = id;
                this.x = x;
                this.y = y;
                this.stamina = state;
                this.attemptBusters = value;
            }
            public String toString(){
                return " id:"+this.id + " x:"+ this.x + " y:"+this.y;
            }
        }


        class Buster extends  Creature{
            public Integer state;
            public Integer value;
            public String actionStr;
            public Buster lastSelf;
            public String do_Move_To(int tx, int ty){
                return "MOVE" + " " + tx + " "+ ty;
            }

            public String do_Move_Random(){

                int tx = (this.x + new Random().nextInt(MAX_X))%MAX_X;
                int ty = (this.y + new Random().nextInt(MAX_Y))%MAX_Y;

                return "MOVE" + " " + tx + " "+ ty;
            }

            public String do_Stay(){
                return do_Move_To(this.x,this.y);
            }

            public Ghost findNearestGhost(List<Ghost> ghosts){
                double minDice = 16001*9001;
                Ghost nearestGhost = null;
                for(Ghost ghost : ghosts){
                    if(minDice > this.getDistance(ghost)){
                        minDice = this.getDistance(ghost);
                        nearestGhost = ghost;
                    }
                }
                return nearestGhost;
            }

            public Ghost findLeastStaminaGhost(List<Ghost> ghosts){
                if(ghosts.size() <= 0){
                    return null;
                }else{
                    Ghost leastGhost = ghosts.get(0);
                    for(Ghost ghost : ghosts){
                        if(leastGhost.stamina > ghost.stamina){
                            leastGhost = ghost;
                        }
                    }
                    return  leastGhost;
                }

            }

            public List<Ghost> findAttackableGhost(List<Ghost> ghosts){
                double minDice = 900;
                double maxDice = 1760;
                List<Ghost> res = new ArrayList<>();
                for(Ghost ghost : ghosts){
                    double dice = this.getDistance(ghost);
                    if(minDice < dice && maxDice > dice){
                        res.add(ghost);
                    }
                }
                return res;
            }



        }

        class Hunter extends Buster{


            public Hunter(int id,int x, int y, int state, int value, Buster lastSelf){
                this.id = id;
                this.x = x;
                this.y = y;
                this.state = state;
                this.value = value;
            }



            public String do_BUST(Ghost ghost){
                String actStr = null;
                if(ghost != null){
                    actStr = "BUST" + " " + ghost.id;
                }else{
                    actStr = do_Stay();
                }
                return  actStr;
            }

            public String do_FollowTheGhost(Ghost ghost){
                String actStr = null;
                if(ghost != null){
                    actStr = this.do_Move_To(ghost.x, ghost.y);
                }else{
                    actStr = this.do_Stay();
                }
                return actStr;
            }

            public String do_Hunter_Strategy(List<Ghost> ghosts,Queue<Ghost> ghostQueue){

//                Ghost nearestGhost = findNearestGhost(ghosts);

                List<Ghost> reachableGhost = findAttackableGhost(ghosts);
                Ghost leastGhost = findLeastStaminaGhost(reachableGhost);
                String actStr = null;

                if(leastGhost != null){
                    if(leastGhost.stamina <=2 && this.state != state_Enum.stunned){
                        FOLLOW_MY_HUNTER = true;
                    }else{
                        FOLLOW_MY_HUNTER = false;
                    }
                    actStr = do_BUST(leastGhost);
                }else if(ghosts.size() > 0){
                    Ghost nearestGhost = findNearestGhost(ghosts);
                    actStr = do_FollowTheGhost(nearestGhost);
                }else if(ghostQueue.size() > 0){
                    if(this.getDistance(ghostQueue.peek()) > 1600){

                        Ghost tGhost = ghostQueue.peek();
                        System.err.println("Move to ghost:"  + tGhost.toString());
                        actStr = do_Move_To(tGhost.x,tGhost.y);
                    }else{
                        Ghost tGhost = ghostQueue.poll();
                        System.err.println("Move to ghost:" + tGhost.toString());
                        actStr = do_Move_To(tGhost.x,tGhost.y);
                    }
                }else{
                    actStr = do_Move_Random();
                }


//                if(nearestGhost == null){
//                    actStr = this.do_Move_Random();
//                }else if(this.getDistance(nearestGhost) > 1760){
//                    actStr = do_FollowTheGhost(nearestGhost);
//                }else if(this.getDistance(nearestGhost) >= 900 && this.getDistance(nearestGhost) <= 1760 && nearestGhost.stamina> 0 && nearestGhost.stamina < 15){
//                    actStr = do_BUST(nearestGhost);
//                }else{
//                    List<Ghost> attackableGhosts = findAttackableGhost(ghosts);
//                    if(attackableGhosts.size() >0){
//                        Ghost attackGhost = findNearestGhost(attackableGhosts);
//                        actStr = do_BUST(attackGhost);
//                    }else{
//                        actStr = do_Move_Random();
//                    }
//                }

                return actStr;
            }
        }

        class Catcher extends Buster{

            public Catcher(int id,int x, int y, int state, int value,Buster lastSelf){
                this.id = id;
                this.x = x;
                this.y = y;
                this.state = state;
                this.value = value;
            }

            public String do_Trap(Ghost ghost){
                String actStr = null;
                if(ghost != null ){
                    actStr = "TRAP" + " " + ghost.id;
                }else{
                    if(ghost == null){
                        System.err.println("Error, Trap null ghost, stay ");
                    }else{
                        System.err.println("Error, ghost of none zero ghost, stay");
                    }
                    actStr = this.do_Stay();
                }
                return  actStr;
            }

            public String do_Release(){
                return "RELEASE";
            }

            public String do_Catcher_Strategy(List<Ghost> ghosts, Buster myBuster,Hunter enemyHunter, Catcher enemyCatcher){
                String actStr = null;
                if(this.state == state_Enum.carryingGhost){
                    if(this.getDistance(BASE_X,BASE_Y) > 0.0){
                        actStr = do_Move_To(BASE_X,BASE_Y);
                    }else{
                        actStr = do_Release();
                    }
                }else {
                    Ghost leastGhost = this.findLeastStaminaGhost(ghosts);
                    if(leastGhost == null){
                        actStr = this.do_Move_To(myBuster.x,myBuster.y);
                    }else{
                        if(this.getDistance(leastGhost) > 900 && this.getDistance(leastGhost) <= 1760){
                            actStr = this.do_Trap(leastGhost);
                        }else {
                            List<Ghost> reachableGhost = this.findAttackableGhost(ghosts);
                            Ghost reachableLeastGhost = this.findLeastStaminaGhost(reachableGhost);
                            if(reachableLeastGhost != null){
                                actStr = this.do_Trap(reachableLeastGhost);
                            }else{
                                if( (enemyHunter!= null || enemyCatcher!=null) &&  FOLLOW_MY_HUNTER == false){

                                    if(enemyHunter!= null){
                                        actStr = this.do_Move_To(enemyHunter.x,enemyHunter.y);
                                    }else{
                                        actStr = this.do_Move_To(enemyCatcher.x,enemyCatcher.y);
                                    }

                                }else{
                                    actStr = this.do_Move_To(myBuster.x,myBuster.y);
                                }

                            }
                        }
                    }
                }
                return actStr;
            }

        }

        class Support extends Buster{

            public Integer stun_steps;
            public Support(int id,int x, int y, int state, int value,Support lastSelf){
                this.id = id;
                this.x = x;
                this.y = y;
                this.state = state;
                this.value = value;
                this.lastSelf = lastSelf;
            }


            public String do_Radar(){
                return "RADAR";
            }
            public String do_Stun(Buster enemy){
                return "STUN" + " " + enemy.id;
            }
            public List<Buster> getReachableEnemies(List<Buster> enemys){
                List<Buster> reachableEnemy = new LinkedList<>();
                for(Buster enemy : enemys){
                    if(this.getDistance(enemy) <= 1760){
                        reachableEnemy.add(enemy);
                    }
                }
                return reachableEnemy;
            }

            public Point pathGenerator(int x, int y){
                int midX = MAX_X/2;
                int midY = MAX_Y/2;

                if (x<midX && y<midY){
                    return new Point(14429,1571);
                }
                if (x>midX && y>midY){
                    return new Point(1571,7429);
                }

                if(x>midX && y<midY){
                    return new Point(14429,7429);
                }

                if(x<midX && y>midY){
                    return new Point(1571,1571);
                }
                return new Point(1571,1571);
            }

            public String do_Support_Strategy(List<Ghost> ghosts, List<Buster> enemys, List<Ghost> ghostQueue){
                for(Ghost ghost : ghosts){
                    boolean seen = false;
                    for(Ghost former : ghostQueue){
                        if(former.id == ghost.id){
                            former.x = ghost.x;
                            former.y = ghost.y;
                            seen = true;
                        }
                    }
                    if(seen == false){
                        System.err.println("Add ghost to queue, id:"+ghost.id);
                        ghostQueue.add(ghost);
                    }
                }
                if(enemys.size() > 0){
                    List<Buster> reachable  = getReachableEnemies(enemys);
                    if(reachable.size() >0){
                        Buster stunEnemy = null;
                        for(Buster enemy : reachable){
                            if(enemy instanceof Catcher){
                                stunEnemy = enemy;
                                break;
                            }
//                            if(enemy instanceof  Hunter){
//                                stunEnemy = enemy;
//                                break;
//                            }
                        }
//                        if(stunEnemy == null){
//                            stunEnemy = reachable.get(0);
//                        }
//                        return do_Stun(stunEnemy);
                        if(stunEnemy != null ){
                            if(stunEnemy.state!=state_Enum.stunned){
                                return do_Stun(stunEnemy);
                            }else{
                                return do_Move_To(stunEnemy.x,stunEnemy.y);
                            }

                        }else{
                            if(lastSelf == null){
                                return do_Move_Random();
                            }else{
                                Point nextP = pathGenerator(lastSelf.x,lastSelf.y);
                                return do_Move_To(nextP.x,nextP.y);
                            }
                        }
                    }else{
                        Buster followEnemy = null;
                        for(Buster enemy : enemys){
                            if(enemy instanceof Catcher){
                                followEnemy = enemy;
                                break;
                            }
//                            if(enemy instanceof  Hunter){
//                                followEnemy = enemy;
//                                break;
//                            }
                        }
                        if(followEnemy != null){
                            return do_Move_To(followEnemy.x,followEnemy.y);
                        }else{
                            if(lastSelf == null){
                                return do_Move_Random();
                            }else{
                                Point nextP = pathGenerator(lastSelf.x,lastSelf.y);
                                return do_Move_To(nextP.x,nextP.y);
                            }
                        }
                    }
                }else{
                    if(lastSelf == null){
                        return do_Move_Random();
                    }else{
                        Point nextP = pathGenerator(lastSelf.x,lastSelf.y);
                        return do_Move_To(nextP.x,nextP.y);
                    }
                }


            }
        }

        Scanner in = new Scanner(System.in);
        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        int ghostCount = in.nextInt(); // the amount of ghosts on the map
        int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
        BASE_X = myTeamId==0? 0 : 16000;
        BASE_Y = myTeamId==0?0 : 9000;
        // game loop
        Hunter hunter = null;
        Catcher catcher = null;
        Support support = null;

        Hunter lastHunter = null;
        Catcher lastCatcher = null;
        Support lastSupport = null;

        Hunter enemyHunter = null;
        Catcher enemyCatcher = null;
        Support enemySupport = null;

        Hunter lastEnemyHunter = null;
        Catcher lastEnemyCatcher = null;
        Support lastEnemySupport = null;



        while (true) {
            int entities = in.nextInt(); // the number of busters and ghosts visible to you

            String HunterActStr = null;
            String CatcherActStr = null;
            String SupportActStr = null;

            ArrayList<Ghost> ghostLs = new ArrayList<>();
            ArrayList<Buster> enemyLs = new ArrayList<>();
            LinkedList<Ghost> ghostQueue = new LinkedList<>();

            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // buster id or ghost id
                int x = in.nextInt();
                int y = in.nextInt(); // position of this buster / ghost
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int entityRole = in.nextInt(); // -1 for ghosts, 0 for the HUNTER, 1 for the GHOST CATCHER and 2 for the SUPPORT
                int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points.
                int value = in.nextInt(); // For busters: Ghost id being carried/busted or number of turns left when stunned. For ghosts: number of busters attempting to trap this ghost.

                 //my team
                if(entityType == myTeamId){

                    if(entityRole == 0){ //hunter
                        hunter = new Hunter(entityId, x,y,state,value,lastHunter);
                    }
                    if(entityRole == 1){//Catcher
                        catcher = new Catcher(entityId,x,y,state,value,lastCatcher);
                    }
                    if(entityRole == 2){// Support
                        support = new Support(entityId,x,y,state,value,lastSupport);
                    }

                }else  if(entityType == -1){//catch a ghost

                    ghostLs.add(new Ghost(entityId,x,y,state,value));

                }else{
                    // the other team
                    if(entityRole == 0){ //hunter
                        enemyHunter = new Hunter(entityId, x,y,state,value,lastEnemyHunter);
                        enemyLs.add(enemyHunter);
                    }
                    if(entityRole == 1){//Catcher
                        enemyCatcher = new Catcher(entityId,x,y,state,value,lastEnemyCatcher);
                        enemyLs.add(enemyCatcher);
                    }
                    if(entityRole == 2){// Support
                        enemySupport = new Support(entityId,x,y,state,value,lastEnemySupport);
                        enemyLs.add(enemySupport);
                    }
                }
            }

            System.err.println("catch creature finished");
            System.err.println("catch "+ghostLs.size() + "ghosts");


            HunterActStr = hunter.do_Hunter_Strategy(ghostLs,ghostQueue);
            System.err.println("Hunter strategy: " + HunterActStr);
            CatcherActStr = catcher.do_Catcher_Strategy(ghostLs,hunter,enemyHunter,enemyCatcher);
            SupportActStr = support.do_Support_Strategy(ghostLs,enemyLs,ghostQueue);
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            // First the HUNTER : MOVE x y | BUST id
            // Second the GHOST CATCHER: MOVE x y | TRAP id | RELEASE
            // Third the SUPPORT: MOVE x y | STUN id | RADAR
            System.out.println(HunterActStr);
            System.out.println(CatcherActStr);
            System.out.println(SupportActStr);

            lastHunter = hunter;
            lastCatcher = catcher;
            lastSupport = support;
            lastEnemyHunter = enemyHunter;
            lastEnemyCatcher = enemyCatcher;
            lastEnemySupport = enemySupport;
            STEPS = (STEPS + 1)%(Integer.MAX_VALUE - 1);

        }
    }
}