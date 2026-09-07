
package Levels.Extra;

import Levels.BasicLevel;
import java.awt.Point;


public class BonusAreaC  extends BasicLevel{

    public BonusAreaC() {
        
        BackGroundColor = "Black";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "" ;
        attribute       = "UnderGround" ;
        LevelLength     = 640 ; 
        
        this.AddBrick(0, 2, 12, 1);
        
        this.AddBrick(5, 5, 2, 7);
        this.AddBrick(5, 7, 2, 6);
        this.AddBrick(13, 5, 2, 15);
        this.AddBrick(14, 7, 2, 15);
        
        this.AddBrick(6, 9);
        this.AddBrick(13, 9);
        this.AddBrick(7, 8);
        this.AddBrick(12, 8);
        this.AddBrick(8, 7);
        this.AddBrick(11, 7);
        this.AddBrick(9, 5 , 2 , 11);
        
        this.AddBrickWithMushroom(7, 5);
        this.AddBrick(12, 5);
        
        this.AddCoin(9, 3 , 1 , 11);
        this.AddCoin(8, 4 , 1 , 12);
        this.AddCoin(6 ,8 );
        this.AddCoin(13, 8);
        this.AddCoin(7, 7);
        this.AddCoin(12, 7);
        this.AddCoin(8, 6);
        this.AddCoin(11, 6);
        this.AddStone(0, 13, 2, 20);
        
        this.AddPumpImage(19, 2, 9);
        
        this.AddHoriImage(17, 11);
        
        
    }


}
