
package Levels.Extra;

import Levels.BasicLevel;
import java.awt.Point;


public class Clowd_level_for_5_2 extends BasicLevel{

    public Clowd_level_for_5_2() {
        
        // id 90
        BackGroundColor = "Blue";

        Time = "400";
        type = "GreenAndTrees";
        pos = new Point(10, 12);
        BackGroundImage = "Nothing$";
        attribute = "Clowd";
        LevelLength = 77 * 32;

        this.AddLiftCar(16, 10);
        this.AddStone(0, 13, 1, 62);


        this.AddCoin(15, 6, 1, 31);
        this.AddCoin(32, 4, 1, 35);
        this.AddCoin(36, 5, 1, 52);
        this.AddCoin(53, 4, 1, 56);
        
        this.AddClowd_CheckPoints(62, 14 , 52, new Point(141, 0));
//        this.AddCheckPoints(2, 11 , 21 , new Point(2 , 3));
//        
//        this.AddBrick(0, 0, 13, 1);
        
//        this.AddBrick(30, 0, 13, 31);
        
//        this.AddBrick(2, 10, 1, 5);
        
//        this.AddBouncer( 11 , 11 );
        
        
        
//        this.AddBrickWithMushroom(6, 9);
//        
//        this.AddBrickWithMushroom(9, 9);
        
        
//        this.AddLift( 3 , 9);
        
//        this.AddFlag(6, 3);
//        this.AddChocolate(6, 12);
        
//        this.AddEnemyTurtlePatrol(5, 5 , 4);
//        
//        this.AddEnemyTurtlePatrol(7, 5 , 4);
//        
//        this.AddFlyingTurtle(9, 12);
//        
//        this.AddFlyingTurtlePatrol(11, 4 , 3);
//        
//        this.AddFlyingTurtlePatrol(15, 4 , 3);
////        
//        this.AddBrick(20, 11, 2, 21);
//        this.AddMonkey(15, 12);
//        
//        this.AddMonkey(17, 12);
//        
//        this.AddTurtle(19, 12);
//        
//        this.AddMonkey(21, 10);
    }

    

    

    

    

    
}
