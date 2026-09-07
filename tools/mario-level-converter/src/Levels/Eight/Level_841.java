
package Levels.Eight;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_841 extends BasicLevel{

    public Level_841() {
        
        BackGroundColor = "Black";
        
        Time            = "300" ;
        type            = "" ; // if OrangeAndMushroom then red treen
        pos             = new Point(10 , 12);
        BackGroundImage = "" ;
        attribute       = "Castle" ;
        LevelLength          = 10000 ;
        
        this.AddStone(0, 2, 1, 120);
        this.AddStone(0, 7, 10, 3);
        this.AddStone(3, 8, 10, 4);
        this.AddStone(4, 9, 10, 5);
        this.AddStone(5, 10, 10, 6);
        
        this.AddLava(6, 11);
        this.AddLava(66, 75);
        
        this.AddStone(11, 13, 5, 19);
        this.AddStone(21, 13, 5, 51);
        this.AddStone(53, 13, 5, 66);
        this.AddStone(62, 10, 3, 66);
        this.AddStone(75, 10, 5, 81);
        this.AddStone(78, 10, 5, 89);
        this.AddStone(76, 6, 1, 80);
        this.AddStone(83, 10, 5, 90);
        this.AddStone(92, 10, 5, 96);
        
        this.AddPump(19, 11, 5);
        
        this.AddPump(51, 11, 5); this.AddCheckPoints_InsidePumpvertically(51, 11, 841, new Point(19 , 9));
        this.AddPump(81, 8, 15); this.AddCheckPoints_InsidePumpvertically(81, 8, 842, new Point(28 , 8));
        
        this.AddPump(90, 7, 15);
        
        this.AddEnemyMushroom(56, 12);
        this.AddEnemyMushroom(57, 12);
        this.AddEnemyMushroom(58, 12);
        
        this.AddStone(96, 13, 5, 120);
        
        this.AddTeleport(108, 3,  35, 3);
        this.AddTeleport(108, 6,  35, 6);
        this.AddTeleport(108, 9,  35, 9);
        this.AddTeleport(108, 12,  35, 12);
        
        this.AddLift_LeftRight(69, 13);
    }
    
    
}
