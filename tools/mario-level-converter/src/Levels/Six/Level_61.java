
package Levels.Six;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_61 extends BasicLevel{

    public Level_61() {
        
        BackGroundColor = "Black";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "Mountain" ;
        attribute       = "Ground" ;
        LevelLength     = 6700 ;
                
        this.AddCastle(-2, 2, "big");
        this.AddQuestionMark(16, 9, 1, 18);
        this.AddStone(0, 13, 2, 20);
        
        this.AddSonOfABuitch(32, 2);
        this.AddChocolate(26, 12, 1, 32);
        this.AddChocolate(29, 11, 1, 34);
        this.AddChocolate(31, 10, 1, 36);
        this.AddChocolate(35, 9, 1, 38);
        this.AddBrickWithMushroom(36, 5);
        this.AddBrick(37, 5);
        this.AddStone(23, 13, 2, 31);
        
        this.AddStone(37, 13 , 2 , 39);
        
        this.AddBrick(41, 9, 1, 43);
        this.AddBank(43, 9);
        this.AddStone(41, 13 , 2 , 57);
        
        this.AddCoin(62, 9, 1, 65);
        for (int i = 0; i < 4; i++) { 
        this.AddChocolate(69+i, 12-i, 1, 74);
        }
        this.AddStone(59, 13, 2, 74);
        
        this.AddCoin(76, 7, 1, 78);
        
        this.AddStone(77, 13, 2, 92);
        
        this.AddChocolate(84, 11, 1, 86);
        this.AddChocolate(85, 10);
        this.AddChocolate(87, 8, 5, 90);
        this.AddChocolate(88, 7, 1, 90);
        this.AddChocolate(89, 6);
        
        this.AddBrick(90, 6, 1, 93);
        this.AddInvisibleBrckWith1Up(90, 8);
        this.AddBrick(92, 10, 1, 95);
        
        this.AddStone(96, 13, 2, 127);
        this.AddPump(102, 10, 3);
        this.AddCoin(105, 8, 1, 108);
        
        this.AddInvisibleBrckWithCoin(113, 5);
        this.AddInvisibleBrckWithCoin(113, 9);
        
        for (int i = 0; i < 5; i++) {
            this.AddChocolate(122+i, 12-i, 1, 127);
        }
        this.AddBrick(127, 8, 1, 129);
        
        this.AddBrickWithMushroom(130, 8);
        this.AddQuestionMark(131, 8);
        this.AddBrick(130, 12, 1, 135);
        this.AddStone(134, 13, 2, 136);
        
        this.AddStone(137, 13, 2, 149);
        
        for (int i = 0; i < 6; i++) {
            this.AddChocolate(143+i, 12-i, 1, 149);
        }
        
        this.AddBrick(149, 7, 1, 151);
        
        this.AddBrick(151, 9);
        this.AddBank(152, 9);
        
        this.AddBrick(152, 11, 1, 155);
        
        this.AddStone(155, 13, 2, 164);
        
        this.AddStone(167, 13, 2, 174);
        for (int i = 0; i < 5; i++) {
            this.AddChocolate(169+i, 12-i, 1, 174);
        }
        
        this.AddStone(176, 13, 2, 220);
        this.AddChocolate(176, 5, 8, 178);
        
        this.AddCheckPointsFlag(186, 3);
        this.AddChocolate(186, 12);
        
        this.AddCastle(192, 8, "small");
        this.AddCheckPoints(195, 12, 62, new Point(2 , 3));
    }
    
    
}
