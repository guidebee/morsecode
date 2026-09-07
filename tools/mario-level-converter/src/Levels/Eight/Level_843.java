
package Levels.Eight;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_843 extends BasicLevel{

    public Level_843() {
        BackGroundColor = "Black";
        
        Time            = "300" ;
        type            = "" ; // if OrangeAndMushroom then red treen
        pos             = new Point(10 , 12);
        BackGroundImage = "" ;
        attribute       = "Castle" ;
        LevelLength          = 3700 ;
        FlyingFishes = true ;
        FlyingFishesLength = 200*32 ;
        
        this.AddStone(0,2, 1, 100);
        
        this.AddStone(0 , 10 , 5 , 19+4);
        this.AddStone(25+4 , 10 , 5 , 31+4);
        this.AddStone(33+4 , 10 , 5 , 39+4);
        this.AddStone(41+4 , 10 , 5 , 47+4);
        this.AddStone(51+4 , 10 , 5 , 55+4);
        this.AddStone(57+4 , 10 , 5 , 100+4);
        this.AddStone(19+4 , 13 , 3 , 22+4);
        this.AddStone(24+4 , 13 , 3 , 25+4);
        
        this.AddPump(22+4, 11, 4);
        
        this.AddPump(31+4, 8, 15);
        this.AddPump(39+4, 7, 15);
        this.AddPump(55+4, 8, 15);
        
        this.AddLava(47+4, 51+4);
        
        this.AddCheckPoints_InsidePumpvertically(39+4, 7, 841, new Point(19 , 9));
        this.AddCheckPoints_InsidePumpvertically(55+4, 8, 844, new Point(3 , 9));
        
        this.AddTeleport(79, 3, 10, 3);
        this.AddTeleport(79, 6, 10, 6);
        this.AddTeleport(79, 9, 10, 9);
    }
    
    
}
