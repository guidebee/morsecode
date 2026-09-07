
package Levels.Extra;

import Levels.BasicLevel;
import java.awt.Point;


public class BonusAreaG  extends BasicLevel{

    public BonusAreaG() {
    
    
    BackGroundColor = "DarkBlue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "" ;
        attribute       = "Sea" ;
        LevelLength     = 65*32 ;
        
        this.AddHoriImage(62, 7);
        
        this.AddStone(0, 13, 2, 22);
        this.AddStone(26, 10, 5, 28);
        this.AddStone(32, 10, 5, 34);
        this.AddStone(34, 13, 2, 38);
        
        this.AddStone(40, 13, 2, 42);
        this.AddStone(44, 13, 2, 70);
        
        this.AddStone(59, 12, 1, 70);
        this.AddStone(60, 11, 1, 70);
        this.AddStone(61, 9, 2, 70);
        this.AddStone(63, 6, 3, 70);
        this.AddStone(61, 2, 4, 70);
        
        this.AddStone(11, 6, 1, 16);
        this.AddStone(26, 2, 3, 28);
        this.AddStone(32, 2, 2, 34);
        
        this.AddStone(38, 4, 1, 44);
        
        this.AddStone(50, 9, 1, 54);
        
        this.AddStone(54, 6, 1, 58);
        
        this.AddBrick(15, 4, 2, 16);
        this.AddBrick(12, 10, 3, 13);
        this.AddBrick(20, 9, 4, 21);
        this.AddBrick(38, 5, 4, 39);
        this.AddBrick(43, 5, 4, 44);
        this.AddBrick(46, 11, 2, 47);
        
        this.AddCoin(22, 7, 1, 32);
        this.AddCoin(40, 12, 1, 42);
        this.AddCoin(50, 8, 1, 54);
        this.AddCoin(54, 5, 1, 58);
        
        this.AddOctoPussy(14, 7);
        this.AddOctoPussy(29, 8);
        this.AddOctoPussy(40, 6);
        
        this.AddFishGrey(34, 5);
        this.AddFishGrey(43, 7);
        this.AddFishGrey(48, 4);
        
        this.AddFishRed(55, 11);
        this.AddFishRed(60, 2);
        
        this.AddLiftDown(22, 4 , 4);
        this.AddLiftDown(28, 6 , 4);
    }
}
