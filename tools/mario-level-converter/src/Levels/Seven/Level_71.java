
package Levels.Seven;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_71 extends BasicLevel{

    public Level_71() {
        
        BackGroundColor = "Blue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "Fence" ;
        attribute       = "Ground" ;
        LevelLength     = 7000 ;
         
        this.AddCastle(-2, 2, "big");
        
        this.AddStone(0, 13, 2, 73);
        
        this.AddRocketLauncher(19, 11, 2);
        this.AddRocketLauncher(28, 10, 2);
        this.AddRocketLauncher(28, 12 , 1 );
        
        this.AddBrickWithMushroom(27, 5);
        this.AddBrick(28, 5, 1, 30);
        
        this.AddFlyingTurtle(24, 9);
        this.AddFlyingTurtle(38, 9);
        this.AddFlyingTurtle(52, 9);
        
        this.AddRocketLauncher(36, 11, 2);
        this.AddRocketLauncher(46, 10, 3);
        this.AddRocketLauncher(56, 10, 2);
        this.AddRocketLauncher(56, 12, 1);
        this.AddRocketLauncher(68, 11, 2);
        this.AddRocketLauncher(64, 7, 2);
        
        this.AddQuestionMark(39, 9, 1, 43);
        
        this.AddBrick(62, 9, 1, 64);
        this.AddChocolate(64, 9);
        this.AddBank(65, 9);
        this.AddBrick(66, 9);
        
        this.AddStone(75, 13, 2, 152);
        this.AddPump(76, 10, 3);
        
        this.AddBrick(82, 9, 1, 89);
        this.AddBrick(82, 5, 1, 89);
        
        this.AddMonkey(86, 2);
        this.AddMonkey(84, 7);
        
        this.AddPump(93, 10, 3);
        
        this.AddInvisibleBrckWith1Up(93, 5);
        
        this.AddRocketLauncher(104, 11, 2);
        
        this.AddPump(109, 10, 3);
        
        this.AddTurtle(111, 12);
        
        this.AddPump(115, 11, 2);
        
        this.AddRocketLauncher(122, 11, 2);
        
        this.AddPump(128, 11, 2);
        
        this.AddBrick(134, 5, 1, 139);
        this.AddBrick(134, 9, 1, 139);
        
        this.AddMonkey(135, 6);
        this.AddMonkey(137, 10);
        
        this.AddChocolate(141, 10, 3, 142);
        
        this.AddRocketLauncher(146, 10, 2);
        this.AddRocketLauncher(146, 12, 1);
        
        this.AddBrick(149, 8, 1, 151);
        this.AddBrick(153, 6, 1, 155);
        
        this.AddBrickWithMushroom(151, 2);
        
        this.AddStone(153, 13 , 2 , 200);
        
        for (int i = 0; i < 6; i++) {
        this.AddChocolate(153+i, 12-i, 1, 159);
        }
        
        for (int i = 0; i < 8; i++) {
        this.AddChocolate(162+i, 12-i, 1, 171);
        }
        
        this.AddHelmet(170, 4);
        
        this.AddChocolate(179, 12);
        
        this.AddCheckPointsFlag(179, 3);
        
        this.AddCastle(183, 8, "small");
        
        this.AddCheckPoints(186, 12, 72, new Point(2 , 3));
        
        this.AddCheckPoints_InsidePumpvertically(93, 10, 'l', new Point(2,3));
        
    }
    
    
}
