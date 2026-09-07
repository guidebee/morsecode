
package Levels.Four;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_43 extends BasicLevel{

    public Level_43() {
        BackGroundColor = "Blue";
        
        Time            = "300" ;
        type            = "OrangeAndMushroom" ; // if OrangeAndMushroom then red treen
        pos             = new Point(10 , 12);
        BackGroundImage = "Clouds" ;
        attribute       = "Ground" ;
        LevelLength          = 5500 ;
        
        this.AddStone(0, 13, 2, 15);
        this.AddCastle(0, 8, "small");
        
        this.AddTree(16, 13, 20, 21);
        this.AddTree(19, 5, 20, 24);
        this.AddTree(23, 9, 20, 30);
        this.AddTree(32, 4, 20, 35);
        this.AddTree(39, 5, 20, 44);
        this.AddTree(36, 12, 20, 43);
        this.AddTree(44, 9, 20, 47);
        this.AddTree(51, 8, 20, 54);
        
        
        this.AddTree(67, 7, 20, 70);
        this.AddTree(65, 13, 20, 70);
        this.AddTree(70, 3, 20, 73);
        this.AddTree(72, 9, 20, 75);
        this.AddTree(74, 5, 20, 79);
        this.AddTree(84, 9, 20, 87);
        
        this.AddTree(99, 11, 20, 102);
        this.AddTree(105, 10, 20, 108);
        this.AddTree(113, 9, 20, 118);
        this.AddTree(117, 6, 20, 120);
        this.AddTree(121, 13, 20, 128);
        
        this.AddTree(130, 10, 20, 135);
        
        this.AddCoin(20, 4 , 1 , 23);
        this.AddCoin(24, 8 , 1 , 29);
        
        this.AddEnemyTurtlePatrol(23, 8, 6);
        this.AddEnemyTurtlePatrol(23, 8, 6);
        this.AddEnemyTurtlePatrol(36, 11, 6);
        
        this.AddFlyingTurtlePatrol(36, 1, 4);
        
        this.AddQuestionMarkWithMushroom(43, 2);
        
        this.AddCoin(48, 7);
        
        this.AddBalenceLift(48, 5 , 7);
        
        this.AddCoin(57, 3);
        
        this.AddLift_UpDown(58, 8);
        this.AddLift_UpDown(62, 6);
        
        this.AddCoin(67, 6, 1, 70);
        this.AddCoin(70, 2, 1, 73);
        this.AddCoin(73, 8);
        
        this.AddEnemyTurtlePatrol(74, 4, 5);
        
        this.AddBalenceLift(80, 5 , 9);
        
        this.AddCoin(96, 7);
        
        this.AddBalenceLift(91, 5 , 6);
        this.AddBalenceLift(102, 5 , 7);
        
        this.AddCoin(113, 8, 1, 118);
        
        this.AddLift_UpDown(136, 7);
        
        this.AddStone(141, 13, 2, 180);
        this.AddChocolate(147, 12);
        this.AddCheckPointsFlag(147, 3);
        
        this.AddCastle(150, 2, "big");
        
        this.AddCheckPoints(155, 12, 44, new Point(5,7));
        
        
    }
    
    
}
