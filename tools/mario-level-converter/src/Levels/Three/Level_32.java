
package Levels.Three;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_32 extends BasicLevel{

    public Level_32() {
        
        BackGroundColor = "Black";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "Fence" ;
        attribute       = "Ground" ;
        LevelLength     = 7000 ;

        this.AddStone(0, 13, 2, 80);
        this.AddStone( 82,13, 2, 123);
        this.AddStone( 125, 13,2, 128);
        this.AddStone( 130,13, 2, 250);
        
        this.AddCastle(0, 8, "small");
        
        this.AddTurtle(15, 12);
        
        this.AddEnemyMushroom(23, 12);
        this.AddEnemyMushroom(24, 12);
        this.AddEnemyMushroom(25, 12);
        
        this.AddTurtle(32, 11);
        this.AddTurtle(34, 11);
        
        this.AddTurtle(42, 11);
        this.AddTurtle(44, 11);
        
        this.AddChocolate(49, 12);
        
        this.AddCoin(65, 9, 1, 68);
        
        this.AddChocolate(60, 10, 3, 61);
        
        this.AddQuestionMarkWithMushroom(60, 6);
        
        this.AddTurtle(65, 11);
        
        this.AddEnemyMushroom(68, 12);
        this.AddEnemyMushroom(69, 12);
        this.AddEnemyMushroom(70, 12);
        
        this.AddChocolate(75, 11, 2, 76);
        this.AddTurtle(77, 11);
        this.AddChocolate(79, 11, 2, 80);
        
        this.AddBank(77, 9);
        
        this.AddBrickWithStar(77, 5);
        
        this.AddFlyingTurtle(92, 9);
        
        this.AddTurtle(109, 2);
        
        this.AddEnemyMushroom(117, 12);
        this.AddEnemyMushroom(118, 12);
        this.AddEnemyMushroom(119, 12);
        
        this.AddChocolate(126, 11 , 2 , 127);
        
        this.AddBrick(126, 6);
        
        this.AddTurtle(132, 12);
        this.AddTurtle(139, 12);
        this.AddTurtle(140, 12);
        this.AddTurtle(141, 12);
        this.AddTurtle(150, 12);
        this.AddTurtle(160, 12);
        this.AddTurtle(161, 12);
        this.AddTurtle(162, 12);
        
        this.AddCoin(168, 6, 1, 172);
        
        this.AddEnemyMushroom(177, 12);
        this.AddEnemyMushroom(178, 12);
        this.AddEnemyMushroom(179, 12);
        this.AddEnemyMushroom(186, 12);
        this.AddEnemyMushroom(187, 12);
        
        this.AddChocolate(192, 12, 1, 201);
        this.AddChocolate(193, 11, 1, 201);
        this.AddChocolate(194, 10, 1, 201);
        this.AddChocolate(195, 9, 1, 201);
        this.AddChocolate(196, 8, 1, 201);
        this.AddChocolate(197, 7, 1, 201);
        this.AddChocolate(198, 6, 1, 201);
        this.AddChocolate(199, 5, 1, 201);
        
        this.AddChocolate(209, 12);
        
        this.AddCheckPointsFlag(209, 3);
        
        this.AddCastle(213, 8, "small");
        
        this.AddCheckPoints(216, 12, 33, new Point(2,3));
        
        this.AddPump(169, 10, 3);
        
    }
    
    
}
