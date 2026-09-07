
package Levels.Eight;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_83 extends BasicLevel{

    public Level_83() {
        
        BackGroundColor = "Blue";
        
        Time            = "400" ;
        type            = "Guns" ; // actually not useful
        pos             = new Point(10 , 12);
        BackGroundImage         = "Fence2" ;
        attribute       = "Ground" ;
        LevelLength          = 7450 ;
        
        this.AddCastle( 0 , 8 , "small");
        
        this.AddStone( 0 , 13 , 2 , 69 ) ;  // x , y , height , EndAt
                
        this.AddWall(24 , 7 , 6 , 32 ) ;
        
        this.AddRocketLauncher(18 , 11 , 2);
        
        this.AddFlyingTurtle( 28 , 11 );
        
        this.AddRocketLauncher(34, 10 , 3);
        
        this.AddWall(37, 7, 6, 51);
        
        this.AddPump(53 , 9 , 4 ); // only in level pump does not have plants
        
        this.AddBrick(60, 5, 1, 66);
        this.AddBrickWithMushroom(66, 5);
        this.AddBrick(67, 5);
        
        this.AddBrick(60, 9, 1, 68);
        
        this.AddChocolate(71 , 9); // single
        
        this.AddChocolate(71 , 10 , 3 , 73); 
        
        this.AddChocolate(73 , 11 , 2 , 74); 
        
        this.AddChocolate(74 , 12); // single
        
        this.AddStone(71, 13, 2, 75);
        
        this.AddStone(77, 13, 2, 124);
        
        this.AddStone(126, 13, 2, 128);
        
        this.AddStone(130, 13, 2, 197);
        
        this.AddStone(208, 13, 2, 250);
        
        this.AddWall(79, 7, 6, 85);
        
        this.AddMonkey(64 , 7);
        
        this.AddMonkey(65 , 11);
        
        this.AddWall(88, 7, 6, 94);
        
        this.AddWall(97, 7, 6, 107);
        
        this.AddRocketLauncher(86, 11, 2);
        
        this.AddFlyingTurtle(91, 10);
        
        this.AddChocolate(95, 10, 3, 96);
        
        this.AddChocolate(109, 9, 4, 111);
        
        this.AddBrick(115, 5);
        
        this.AddBrickWithMushroom(116, 5);
        
        this.AddBrick(117, 5 , 1 , 123);
        
        this.AddBrick(115, 9 , 1 , 123);
        
        this.AddMonkey(120 , 7);
        
        this.AddMonkey(118 , 11);
        
        this.AddPump(126, 9, 4);
        
        this.AddWall(132, 7, 6, 166);
        
        this.AddWall(172, 7, 6, 192);
        
        this.AddTurtle(136 , 11);
        
        this.AddMonkey(145 , 11);
        
        this.AddMonkey(160 , 10);
        
        this.AddPump(168, 10, 3);
        
        this.AddMonkey(177 , 11);
        
        this.AddMonkey(185 , 11);
        
        this.AddBank(190 , 9); // now Bank is always brick skin
        
        this.AddChocolate(195, 11, 2, 196);
        
        this.AddChocolate(198, 11);
        
        this.AddChocolate(200, 9);
        
        this.AddChocolate(202, 7);
        
        this.AddChocolate(204, 5 , 1 , 206);
        
        this.AddChocolate(214, 12);
        
        this.AddCheckPointsFlag(214 , 3);
        
        this.AddCastle(220, 2, "big");
        
        this.AddCheckPoints(225, 12, 841, new Point(2 , 5));
        
        
        
        
        
        
        
        
        
        
        
        
    }

    

    


}
