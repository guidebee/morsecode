
package Levels.Eight;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_844 extends BasicLevel{

    public Level_844() {
        BackGroundColor = "DarkBlue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "" ;
        attribute       = "Sea" ;
        LevelLength     = 2304 ;
        
        this.AddHoriImage(68 , 7);
        this.AddCheckPoints_InsidePumpHorzontally(68, 8, 845, new Point(3 , 9));
        
        this.AddStone(0, 2 , 15 , 2);
        this.AddStone(2, 13 , 2 , 3);
        this.AddStone(5, 13 , 2 , 6);
        this.AddStone(6, 10 , 5 , 18);
        this.AddStone(11, 9 , 1 , 18);
        this.AddStone(6, 2 , 3 , 18);
        this.AddStone(11, 5 , 1 , 18);
        this.AddStone(18, 2 , 1 , 90);
        this.AddStone(18, 13 , 2 , 90);
        this.AddStone(39, 3 , 3 , 42);
        this.AddStone(39, 10 , 3 , 42);
        this.AddStone(67, 3 , 3 , 90);
        this.AddStone(69, 6 , 3 , 90);
        this.AddStone(67, 9 , 4 , 90);
        
        this.AddPump(3, 11, 4);
        
        this.AddFireBar(18, 9, 6 , "AWC");
        this.AddFireBar(38, 8, 6 , "AWC");
        this.AddFireBar(29, 12, 6 , "AWC");
        this.AddFireBar(54, 10, 6 , "AWC");
        this.AddFireBar(62, 9, 6 , "AWC");
        
        this.AddOctoPussy(28, 9);
        this.AddOctoPussy(53, 11);
        this.AddOctoPussy(57, 4);
        
        
        
        
        
        
        
        
    }
    
    
}
