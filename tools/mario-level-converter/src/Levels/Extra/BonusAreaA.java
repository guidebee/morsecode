
package Levels.Extra;

import Levels.BasicLevel;
import java.awt.Point;


public class BonusAreaA  extends BasicLevel{

    
    public BonusAreaA() {
        
        BackGroundColor = "Black";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "" ;
        attribute       = "UnderGround" ;
        LevelLength     = 640 ; 
        
        this.AddBrick(0, 2, 12, 1);
        this.AddBrick(4, 2, 1, 15);
        this.AddBrick(4, 10, 3, 15);
        
        this.AddCoin(5, 5, 1, 14);
        this.AddCoin(4, 7, 1, 15);
        this.AddCoin(4, 9, 1, 15);
        
        
        this.AddStone(0, 13, 2, 20);
        
        this.AddPumpImage(19, 2, 9);
        
        this.AddHoriImage(17, 11);
        
        
    }


}
