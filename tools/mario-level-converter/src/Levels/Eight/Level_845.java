
package Levels.Eight;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_845 extends BasicLevel{

    public Level_845() {
        BackGroundColor = "Black";
        
        Time            = "300" ;
        type            = "" ; // if OrangeAndMushroom then red treen
        pos             = new Point(10 , 12);
        BackGroundImage = "" ;
        attribute       = "Castle" ;
        LevelLength          = 2200 ;
        
        this.AddStone(0, 2, 1, 100);
        
        this.AddStone(0, 13, 2, 3);
        this.AddStone(5, 13, 2, 10);
        this.AddStone(12, 13, 2, 21);
        this.AddStone(26, 3, 2, 32);
        this.AddStone(46, 3, 3, 48);
        this.AddStone(45, 9, 4, 48);
        this.AddStone(26, 10, 5, 32);
        this.AddStone(45, 13, 2, 100);
        
        this.AddPump(3, 11, 5);
        this.AddPump(10, 11, 5);
        this.AddCheckPoints_InsidePumpvertically(10, 11, 841, new Point(19 , 9)); //  Game Complete
        this.AddMonkey(18, 9);
        
        this.AddLavaBall(23);
        
        this.AddLava(21, 26);
        this.AddLava(32, 45);
        
        this.AddBridgeBloks(32, 10, 1, 45);
        this.AddBossHammer(39, 7 , 45);
        
        this.AddAxe(45, 8);
        
        this.AddPrincess(57, 11);
        
    }
    
    
}
