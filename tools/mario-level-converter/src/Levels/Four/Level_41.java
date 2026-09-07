
package Levels.Four;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_41 extends BasicLevel{

    public Level_41() {
        
        BackGroundColor = "Blue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "Mountain" ;
        attribute       = "Ground" ;
        LevelLength     = 8000 ;
                
        this.AddSonOfABuitch(33, 3);
        this.AddCastle(-1, 2, "big");
        
        this.AddStone(0, 13, 2, 32);
        this.AddPump(21, 10, 3);
        this.AddQuestionMark(25, 5);
        this.AddQuestionMarkWithMushroom(25, 9);
        
        this.AddStone(34 , 13, 2, 78);
        this.AddCoin(41, 9);
        this.AddCoin(42, 8 , 1 , 44);
        this.AddCoin(44, 9);
        
        this.AddQuestionMark(64, 5);
        this.AddQuestionMark(66, 5);
        this.AddQuestionMark(64, 9);
        this.AddQuestionMark(66, 9);
        
        this.AddStone(82, 13, 2, 149);
        this.AddQuestionMark(90, 9, 1, 94);
        this.AddInvisibleBrckWith1Up(92, 5);
        
        this.AddChocolate(103, 10, 3, 104);
        this.AddCoin(105, 6, 1, 109);
        
        this.AddPump(116, 9, 4);
        this.AddPump(132, 9, 4);
        this.AddCoin(119, 6, 1, 123);
        this.AddCoin(135, 6, 1, 139);
        
        this.AddQuestionMark(148, 5, 1, 152);
        this.AddQuestionMark(146, 9, 1, 148);this.AddQuestionMarkWithMushroom(148, 9);
        
        this.AddBrick(149, 9, 1, 151);
        this.AddQuestionMark(151, 9, 1, 154);
        
        this.AddPump(163, 11, 2);
        
        this.AddStone(151, 13, 2, 174);
        this.AddStone(177, 13, 2, 180);
        this.AddStone(182, 13, 2, 190);
        
        this.AddChocolate(189, 10, 3, 190);
        
        this.AddStone(192, 13, 2, 280);
        
        for (int i = 0; i < 8; i++) {
            
        this.AddChocolate(208+i, 12-i, 1, 217);
        }
        
        this.AddBank(220, 9);
        
        this.AddCheckPointsFlag(225, 3);
        this.AddChocolate(225, 12);
        
        this.AddCastle(229, 8, "small");
        this.AddCheckPoints(232, 12, 42, new Point(2 , 3));
        
        
        this.AddCheckPoints_InsidePumpvertically(132, 9, 'e', new Point(2,3));
        
        
        
        
        
        
    }
    
    
}
