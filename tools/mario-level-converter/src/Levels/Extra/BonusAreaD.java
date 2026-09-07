
package Levels.Extra;

import Levels.BasicLevel;
import java.awt.Point;


public class BonusAreaD  extends BasicLevel{

    
    public BonusAreaD() {
        
        BackGroundColor = "Black";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "" ;
        attribute       = "UnderGround" ;
        LevelLength     = 640 ; 
        
        this.AddBrick(0, 2, 12, 1);
        this.AddBrick(3, 2, 1, 19);
        
        this.AddBrick(3, 9, 1, 15);
        this.AddBrick(3, 10, 2, 4);
        this.AddBrick(14, 10, 2, 15);
        
        this.AddCoin(3, 8, 1, 15);
        this.AddCoin(3, 12, 1, 15);
        
        this.AddBrickWithMushroom(17, 9);
        
        this.AddStone(0, 13, 2, 20);
        
        this.AddPumpImage(19, 2, 9);
        
        this.AddHoriImage(17, 11);
        
        
    }


}
