
package Levels.Six;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_63 extends BasicLevel{

    public Level_63() {
        
        BackGroundColor = "Black";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "CloudsNight" ;
        attribute       = "Ground" ;
        LevelLength     = 7700 ;
        Bombs = true ;
        BombsTurnOff = 156 ;
        
        this.AddCastle(0, 8, "small");
        
        this.AddStone(0 , 13, 2, 16);
        
        this.AddTree(18, 13, 10, 21);
        
        this.AddTree(21, 9, 10, 24);
        this.AddTree(24, 13, 10, 27);
        this.AddTree(31, 9, 10, 35);
        
        this.AddLift_UpDown(28, 8 , 4);
        
        this.AddCoin(28, 2, 1, 30);
        
        this.AddBouncer(38, 11);
        
        this.AddTree(37, 13, 10, 40);
        this.AddTree(43, 13, 10, 46);
        this.AddTree(49, 11, 10, 53);
        this.AddTree(65, 9, 10, 70);
        
        this.AddLift_LeftRight(40, 5, 4);
        this.AddLift_LeftRight(46, 7, 4);
        this.AddLift_LeftRight(52, 6, 4);
        this.AddLift_UpDown(60, 7, 4);
        
        this.AddQuestionMarkWithMushroom(55, 3);
        this.AddTree(65, 9, 10, 70);
        
        this.AddBalenceLift(70, 5, 4);
        this.AddBalenceLift(78, 5, 3);
        
        this.AddCoin(85, 2 , 1 , 88);
        this.AddTree(85, 3, 10, 88);
        this.AddTree(85, 10, 10, 90);
        this.AddTree(90, 8, 10, 93);
        this.AddTree(93, 13, 10, 96);
        this.AddTree(97, 13, 10, 101);
        this.AddTree(103, 13, 10, 106);
        this.AddCoin(100, 8, 1, 104);
        this.AddTree(107, 9, 10, 112);
        
        this.AddTree(113, 13, 10, 118);
        this.AddBouncer(116, 11);
        
        this.AddLift_LeftRight(120, 5);
        
        this.AddBalenceLift(126, 6, 4);
        this.AddCoin(128, 5, 1, 130);
        this.AddTree(132, 5, 10, 135);
        this.AddTree(132, 13, 10, 136);
        this.AddTree(135, 9, 10, 139);
        
        this.AddLiftFall(141, 7);
        this.AddLiftFall(145, 6);
        this.AddLiftFall(149, 8);
        this.AddLiftFall(153, 7);
        
        this.AddCoin(145, 7, 1, 147);
        this.AddCoin(154, 3, 1, 156);
        
        this.AddTree(156, 5 , 15, 159 );
        
        this.AddStone(160, 13, 2 , 200);
        
        this.AddCheckPointsFlag(167, 3);
        this.AddChocolate(167, 12);
        this.AddCastle(170, 2, "big");
        this.AddCheckPoints(175, 12, 64, new Point(5 , 6));
        
        
        
        
        
        
        
        
        
    }
    
    
}
