
package Levels.Eight;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_82 extends BasicLevel{

    public Level_82() {
        
        BackGroundColor = "Blue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "Fence2" ;
        attribute       = "Ground" ;
        LevelLength          = 7450 ;
              
        this.AddSonOfABuitch(33, 3);
        this.AddCastle(0, 8, "small");
        
        this.AddStone(0, 13, 2, 15);
        
        this.AddStone( 16, 13 , 2, 21);
        
        this.AddStone( 22, 13 , 2, 36);
        
        this.AddStone( 37, 13 , 2, 45);
        
        this.AddStone( 46, 13 , 2, 50);
        
        this.AddStone( 51, 13 , 2, 52);
        
        this.AddStone( 53, 13 , 2, 56);
        
        this.AddStone( 57, 13 , 2, 63);
        
        this.AddStone( 64, 13 , 2, 78);
        
        this.AddStone( 80, 13 , 2, 84);
        
        this.AddStone( 85, 13 , 2, 138);
        
        this.AddStone( 139, 13 , 2, 144);
        
        this.AddStone( 145, 13 , 2, 146);
        
        this.AddStone( 147, 13 , 2, 148);
        
        this.AddStone( 154, 13 , 2, 176);
        
        this.AddStone( 179, 13 , 2, 202);
        
        this.AddStone( 203, 13 , 2, 204);
        
        this.AddStone( 206, 13 , 2, 250);
        
        this.AddFlyingTurtle(15, 9);
        this.AddFlyingTurtle(23, 1);
        
        this.AddChocolate(17, 12, 1, 21);
        this.AddChocolate(18, 11, 1, 21);
        this.AddChocolate(19, 10, 1, 21);
        this.AddChocolate(20, 9, 1, 21);
        
        this.AddChocolate(22, 7, 6, 26);
        this.AddChocolate(23, 6, 1, 26);
        this.AddChocolate(24, 5, 1, 26);
        
        
//        this.AddBrickWithCoin(29, 9);
//        this.AddBrickWithMushroom(30, 9);
//        this.AddBrickWith1UP(31, 9);
        this.AddQuestionMark(29 , 9 , 1 , 33);
        
        this.AddBouncer(44, 11);
        
        this.AddBrick(43, 5);
        this.AddBrickWith1UP(44, 5);
        this.AddBrick(45, 5 , 1 , 76);
        
        this.AddFlyingTurtle(54, 9);
        this.AddFlyingTurtle(64, 9);
        this.AddFlyingTurtle(67, 9);
        this.AddFlyingTurtle(90, 10);
        this.AddFlyingTurtle(95, 10);
        
        this.AddHelmet(118 , 12 );
        this.AddHelmet(120 , 12 );
        
        this.AddBrick(77, 9 , 1 , 79);
        
        this.AddRocketLauncher(85, 11, 2);
        
        this.AddRocketLauncher(93, 10, 2);
        
        this.AddRocketLauncher(93, 12, 1);
        
        this.AddBank(99, 9);
        
        this.AddBrickWithMushroom(100, 9);
        
        this.AddRocketLauncher(105, 11, 2);
        
        this.AddBrick(110, 9 , 1 , 112);
        
        this.AddRocketLauncher(115, 12, 1);
        
        this.AddBrick(118, 9 );
        this.AddChocolate(119, 9);
        this.AddBrickWithCoin(120, 9);
        
        this.AddRocketLauncher(119, 8, 1);
        
        this.AddRocketLauncher(125, 10, 3);
        
        this.AddPump(131, 11, 2);
        
        this.AddFlyingTurtle(137, 10);
        
        this.AddPump(142, 11, 2);
        
        this.AddPump(156, 9, 4);
        this.AddPump(163, 11, 2);
        
        this.AddFlyingTurtle(167+3, 9);
        
        this.AddFlyingTurtle(169+3, 10);
        
        this.AddFlyingTurtle(172+3, 8);
        
        this.AddRocketLauncher(175, 11, 2);
        
        this.AddChocolate(182, 12, 1, 187);
        this.AddChocolate(183, 11, 1, 187);
        this.AddChocolate(184, 10, 1, 187);
        this.AddChocolate(185, 9, 1, 187);
        this.AddChocolate(186, 8, 1, 187);
        
        this.AddEnemyMushroom(182 , 10 );
        
        this.AddEnemyMushroom(185 , 8 );
        
        this.AddHelmet(187 , 12 );
        
        this.AddRocketLauncher(191, 10, 2);
        this.AddRocketLauncher(191, 12, 1);
        
        this.AddChocolate(199, 12, 1, 202);
        this.AddChocolate(200, 11, 1, 202);
        this.AddChocolate(201, 10, 1, 202);
        
        this.AddFlyingTurtle(202, 5);
        
        this.AddChocolate(203, 8, 5, 204);
        
        this.AddChocolate(206, 5, 8, 208);
        
        this.AddChocolate(216, 12);
        
        this.AddCastle(220, 8, "small");
        
        this.AddCheckPoints_InsidePumpvertically(156, 9, 'n', new Point(2 , 3));
        
        this.AddCheckPointsFlag(216, 3);
        
        this.AddCheckPoints(223, 12, 83, new Point(2,3));
        
        
        
        
        
        
        
        
    }



    

}
