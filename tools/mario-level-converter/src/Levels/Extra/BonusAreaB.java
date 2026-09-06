
package Levels.Extra;

import Levels.BasicLevel;
import java.awt.Point;


public class BonusAreaB  extends BasicLevel{

    public BonusAreaB() {
        
        BackGroundColor = "Black";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "" ;
        attribute       = "UnderGround" ;
        LevelLength     = 640 ; 
        
        this.AddBrick(0, 2, 12, 1);
        this.AddBrick(3, 2, 4, 19);
        this.AddBrick(17, 6, 5, 19);
        
        this.AddBrick(3, 9, 1, 16);
        this.AddBank(16, 9);
        
        this.AddCoin(4, 8, 1, 16);
        this.AddCoin(3, 12, 1, 16);
        
        
        this.AddStone(0, 13, 2, 20);
        
        this.AddPumpImage(19, 2, 9);
        
        this.AddHoriImage(17, 11);
       
        
    }


}
