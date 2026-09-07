
package Levels.Eight;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_842 extends BasicLevel{

    public Level_842() {
        BackGroundColor = "Black";
        
        Time            = "300" ;
        type            = "" ; // if OrangeAndMushroom then red treen
        pos             = new Point(10 , 12);
        BackGroundImage = "" ;
        attribute       = "Castle" ;
        LevelLength          = 3700 ;
        
        this.AddHelmet(37, 12);
        this.AddHelmet(38, 12);
        this.AddFlyingTurtle(51, 9);
        this.AddFlyingTurtle(70, 10);
        this.AddFlyingTurtle(75, 9);
        
        this.AddStone(0,2, 1, 100);
        
        this.AddStone(0 , 13 , 5 , 28);
        this.AddStone(30 , 13 , 5 , 35);
        this.AddStone(37 , 13 , 5 , 45);
        this.AddStone(47 , 13 , 5 , 55);
        this.AddStone(60 , 13 , 5 , 100);
        
        this.AddStone(65 , 9 , 1 , 67);
        
        this.AddPump(28, 11, 4);
        
        this.AddPump(35, 10, 15);
        this.AddPump(45, 11, 15);this.AddCheckPoints_InsidePumpvertically(45, 11, 841, new Point(19 , 9));
        this.AddPump(55, 10, 15);
        this.AddPump(65, 6, 3);this.AddCheckPoints_InsidePumpvertically(65, 6, 843, new Point(22+4 , 9));
        
        this.AddLava(57, 60);
        
        this.AddInvisibleBrckWithCoin(63, 9);
        
        this.AddTeleport(79, 3, 10, 3);
        this.AddTeleport(79, 6, 10, 6);
        this.AddTeleport(79, 9, 10, 9);
    }
    
   
}
