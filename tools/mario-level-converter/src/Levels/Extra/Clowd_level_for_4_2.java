package Levels.Extra;

import Levels.BasicLevel;
import java.awt.Point;

public class Clowd_level_for_4_2 extends BasicLevel {

    public Clowd_level_for_4_2() {

        BackGroundColor = "Blue";

        Time = "400";
        type = "OrangeAndMushroom";
        pos = new Point(10, 12);
        BackGroundImage = "Clouds";
        attribute = "Ground";
        LevelLength = 64 * 32;
        level_Name = "OrangePump";


        this.AddStone(0, 13, 2, 80);

//        this.AddCoin(7, 5, 1, 27);
//        this.AddCoin(27, 3, 1, 30);
//        this.AddCoin(30, 4, 1, 50);
//        this.AddCoin(50, 3, 1, 53);

        this.AddCoin(12, 8, 1, 15);
        this.AddTree(12, 9, 4, 15);
        

        this.AddCoin(16, 4, 1, 19);
        this.AddTree(16, 5, 8, 19);

        this.AddTree(18, 11, 8, 21);

        this.AddCoin(22, 4, 1, 25);
        this.AddTree(22, 5, 8, 25);

        this.AddTree(22, 11, 8, 27);

        
        this.AddCoin(26, 6, 1, 29);
        this.AddTree(26, 7, 8, 29);
        
        this.AddCoin(30, 3, 1, 35);
        this.AddTree(30, 4, 9, 35);
        
        this.AddCoin(35, 9, 1, 37);
        this.AddTree(31, 10, 8, 38);
        
        for (int i = 0; i <= 8; i++) {
            
        this.AddChocolate(40+i, 12-i, 1, 49);
        }

        this.AddChocolate(49, 4, 1, 60);
        
        this.AddChocolate(62, 2, 11, 65);
        
        this.AddCheckPoints_InsidePumpvertically(50, 10 , 81 , new Point(2 , 5) );
        this.AddPump(50, 10, 3);
        
        this.AddCheckPoints_InsidePumpvertically(54, 10 , 71 , new Point(2 , 5) );
        this.AddPump(54, 10, 3);
        
        this.AddCheckPoints_InsidePumpvertically(58, 10 , 61 , new Point(2 , 5) );
        this.AddPump(58, 10, 3);
        // 3 check point

//        this.AddClowd_CheckPoints(80, 14 , 21, new Point(165, -2));
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
