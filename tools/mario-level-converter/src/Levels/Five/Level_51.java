
package Levels.Five;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_51 extends BasicLevel{

    public Level_51() {
        BackGroundColor = "Blue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "Fence" ;
        attribute       = "Ground" ;
        LevelLength     = 7000 ;
          
        this.AddCastle(0, 8, "small");
        this.AddStone(0, 13, 2, 49);
        this.AddStone(51, 13, 2, 92);
        this.AddStone(96, 13, 2, 114);
        this.AddStone(116, 13, 2, 152);
        this.AddStone(155, 13, 2, 230);
        
        this.AddPump(44, 10, 3);
        this.AddPump(51, 10, 3);
        this.AddPump(156, 7, 2);
        this.AddPump(163, 11, 2);
        
        this.AddTurtle(15, 11);
        this.AddTurtle(38, 11);
        this.AddTurtle(126, 11);
        this.AddTurtle(144, 11);
        this.AddTurtle(145, 11);
        
        this.AddEnemyMushroom(18, 12);
        this.AddEnemyMushroom(19, 12);
        this.AddEnemyMushroom(20, 12);
        this.AddEnemyMushroom(29, 12);
        this.AddEnemyMushroom(30, 12);
        this.AddEnemyMushroom(31, 12);
        this.AddEnemyMushroom(63, 12);
        this.AddEnemyMushroom(64, 12);
        this.AddEnemyMushroom(65, 12);
        this.AddEnemyMushroom(72, 12);
        this.AddEnemyMushroom(73, 12);
        this.AddEnemyMushroom(74, 12);
        this.AddEnemyMushroom(102, 12);
        this.AddEnemyMushroom(103, 12);
        this.AddEnemyMushroom(104, 12);
        this.AddEnemyMushroom(119, 12);
        this.AddEnemyMushroom(120, 12);
        this.AddEnemyMushroom(121, 12);
        this.AddEnemyMushroom(133, 12);
        
        this.AddFlyingTurtle(57, 9);
        this.AddFlyingTurtle(83, 9);
        this.AddFlyingTurtle(175, 9);
        this.AddFlyingTurtle(180, 9);
        
        this.AddChocolate(89, 9, 4, 90);
        this.AddChocolate(90, 9, 1, 94);
        this.AddChocolate(116, 10, 3, 117);
        this.AddChocolate(147, 9, 4, 148);
        this.AddChocolate(156, 9, 1, 158);
        for (int i = 0; i < 5; i++) {
        this.AddChocolate(182+i, 12-i, 1, 187);
        }
        this.AddChocolate(189, 5, 6, 191);
        this.AddChocolate(199, 12);
        
        this.AddBrick(90, 5);
        this.AddBrickWithStar(91, 5);
        this.AddBrick(92, 5);
        this.AddInvisibleBrckWith1Up(148, 9);
        this.AddBrick(149, 9);
        this.AddBrick(150, 9);
        
        this.AddRocketLauncher(111, 11, 2);
        this.AddRocketLauncher(159, 11, 2);
        this.AddRocketLauncher(170, 11, 2);
        
        this.AddCheckPointsFlag(199, 3);
        
        this.AddCastle(203, 8, "small");
        this.AddCheckPoints(206, 12, 52, new Point(2,3));
        
        this.AddCheckPoints_InsidePumpvertically(156, 6, 'g', new Point(2,3));
    }
    
    
}
