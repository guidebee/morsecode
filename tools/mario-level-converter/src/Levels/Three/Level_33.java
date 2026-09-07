
package Levels.Three;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_33 extends BasicLevel{

    public Level_33() {
        
        BackGroundColor = "Black";
        
        Time            = "300" ;
        type            = "GreenAndTrees" ; // if OrangeAndMushroom then red treen
        pos             = new Point(10 , 12);
        BackGroundImage = "Clouds" ;
        attribute       = "Ground" ;
        LevelLength          = 5500 ;
        
        this.AddStone(0, 13, 2, 16);
        
        this.AddTree(18, 10, 5, 23);
        this.AddTree(22, 7, 8, 28);
        
        this.AddTree(30, 13, 2, 33);
        
        this.AddTree(36, 12, 3, 43);
        
        this.AddEnemyMushroom(25, 6);
        
        this.AddCoin(31, 12);
        this.AddCoin(32, 12);
        this.AddCoin(37, 6);
        
        this.AddCoin(42, 6, 1, 45);
        
        this.AddTree(43, 9, 15, 47);
        this.AddTree(46, 11, 4, 56);
        this.AddTree(47, 7, 4, 53);
        this.AddTree(55, 3, 15, 59);
        this.AddCoin(56, 2, 1, 58);
        
        this.AddQuestionMarkWithMushroom(49, 3);
        
        this.AddEnemyTurtlePatrol(47, 6 , 5);
        this.AddEnemyTurtlePatrol(46, 10 , 9);
        
        this.AddTree(65, 7, 6, 68);
        this.AddTree(69, 7, 6, 72);
        this.AddTree(73, 7, 6, 76);
        this.AddTree(77, 4, 9, 80);
        this.AddTree(65, 13, 2, 81);
        
        this.AddCoin(66, 6);
        this.AddCoin(70, 6);
        this.AddCoin(74, 6);
        this.AddCoin(78, 3);
        this.AddCoin(81, 5);
        this.AddCoin(88, 5);
        this.AddCoin(90, 5);
        this.AddEnemyTurtlePatrol(65, 11, 15);
        
        this.AddTree(84, 11, 4, 88);
        this.AddTree(97, 9, 6, 100);
        this.AddTree(104, 5, 15, 108);
        this.AddTree(108, 7, 15, 111);
        this.AddTree(107, 11, 15, 111);
        this.AddTree(116, 12, 10, 119);
        this.AddTree(119, 10, 10, 131);
        
        this.AddFlyingTurtlePatrol(114, 2, 4);
        
        this.AddEnemyTurtlePatrol(119, 9, 11);
        this.AddEnemyTurtlePatrol(119, 9, 11);
        
        this.AddCoin(105, 4, 1, 107);
        this.AddCoin(109, 6);
        this.AddCoin(108, 10);
        
        this.AddChocolate(151, 12);
        this.AddCheckPointsFlag(151, 3);
        
        this.AddCastle(154, 2, "big");
        this.AddStone(144, 13, 2, 170);
        this.AddCastle(0, 8, "small");
        
        this.AddCheckPoints(159, 12, 34, new Point(6,6));
        
        this.AddBalenceLift(81 ,6 , 7);
        
        this.AddLift_LeftRight(29, 4, 5);
        this.AddLift_LeftRight(32, 8, 6);
        
        this.AddLiftFall(60, 6);
        
        this.AddLift_LeftRight(92, 9, 6);
        this.AddLift_LeftRight(96, 5, 6);
        this.AddLift_LeftRight(101, 11, 6);
        this.AddLift_LeftRight(129, 6, 6);
        
        
        this.AddBalenceLift(136 ,5 , 4);
        
    }

    
    
}
