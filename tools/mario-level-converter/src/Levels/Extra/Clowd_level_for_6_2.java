
package Levels.Extra;

import Levels.BasicLevel;
import java.awt.Point;


public class Clowd_level_for_6_2 extends BasicLevel{

    public Clowd_level_for_6_2() {
        
        BackGroundColor = "Black";

        Time = "400";
        type = "GreenAndTrees";
        pos = new Point(10, 12);
        BackGroundImage = "Nothing$";
        attribute = "Clowd";
        LevelLength = 96 * 32;

        this.AddLiftCar(16, 10);

        this.AddStone(0, 13, 1, 83);

        this.AddStone(32, 8);

        this.AddStone(51, 7, 2, 52);
        this.AddStone(61, 7, 2, 62);
        this.AddStone(67, 6, 1, 69);
        this.AddStone(71, 6);
        this.AddStone(73, 6);
        this.AddStone(75, 6);
        this.AddStone(77, 6);
        this.AddStone(79, 6);


        this.AddCoin(15, 6, 1, 31);
        this.AddCoin(34, 6, 1, 50);
        this.AddCoin(53, 5, 1, 60);
        this.AddCoin(71, 5, 1, 81);
        
        this.AddClowd_CheckPoints(83, 14 , 62, new Point(162, 0));
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
