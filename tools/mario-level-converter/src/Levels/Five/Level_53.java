
package Levels.Five;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_53 extends BasicLevel{

    public Level_53() {
        
        BackGroundColor = "Blue";
        
        Time            = "300" ;
        type            = "GreenAndTrees" ; // if OrangeAndMushroom then red treen
        pos             = new Point(10 , 12);
        BackGroundImage = "Clouds" ;
        attribute       = "Ground" ;
        LevelLength          = 5500 ;
        Bombs = true ;
        BombsTurnOff = 128 ;
        
        
        this.AddCoin(27, 4, 1, 30);
        
        this.AddCoin(33, 11);
        
        this.AddCoin(37, 3, 1, 39);
        
        this.AddCoin(50, 6, 1, 52);
        
        this.AddCoin(60, 4, 1, 64);
        
        this.AddCoin(85, 5, 1, 87);
        
        this.AddCoin(93, 4, 1, 95);
        
        this.AddCoin(97, 4, 1, 99);
        
        this.AddCoin(113, 12, 1, 116);
        
        this.AddCoin(120, 5, 1, 122);
        
        
        this.AddCastle(0, 8, "small");
        
        Add("stone" , 0 , 13 , 16 , 2 ) ;
        
        
        Add("tree" , 18 , 12 , 3 , 3 ) ;
        Add("tree" , 26 , 5 , 5 , 4 ) ;
        Add("tree" , 24 , 9 , 8 , 6  ) ;
        Add("tree" , 32 , 12 , 3 , 3 ) ;
        Add("tree" , 35 , 8 , 5, 7 ) ;
        Add("tree" , 40 , 4 , 7 , 11 ) ;
        Add("tree" , 50 , 13 , 4 , 2 ) ;
        Add("tree" , 60 , 5 , 4 , 8  ) ;
        Add("tree" , 59 , 13 , 5 , 2 ) ;
        Add("tree" , 65 , 13 , 5 , 2 ) ;
        Add("tree" , 70 , 9 , 3 ,6  ) ;
        Add("tree" , 76 , 6 , 6 , 9 ) ;
        Add("tree" , 98 , 11 , 4 , 4 ) ;
        Add("tree" , 104 , 7 , 8 , 8 ) ;
        Add("tree" , 113 , 13 , 3 , 2 ) ;
        Add("tree" , 116 , 9 , 4 , 6 ) ;
        Add("tree" , 122 , 9 , 4 , 6 ) ;
        
//        Add("stone" , 86 , 7 , 10 , 1 ) ; // no lifter just for testing
        
        this.AddStone(129, 13, 2, 300);
//        Add("stone" , 129 , 13 , 150 , 2 ) ;
        
        Add("chocolate" , 138 , 9 , 6 , 4 ) ;
        Add("chocolate" , 140 , 7 , 4 , 3 ) ;
        Add("chocolate" , 142 , 5 , 2 , 2 ) ;
        
        Add("chocolate" , 152 , 12 , 1 , 1 ) ; // flag
        
        this.AddCastle( 155 , 2 , "big"  ) ;
        
        this.AddCheckPoints(160, 12, 54, new Point(2 , 6));
        
        this.AddEnemyTurtlePatrol(26, 3 , 4);
        
        this.AddFlyingTurtlePatrol(74 , 2 , 4);
        
        this.AddEnemyTurtlePatrol(104, 6 , 7);
        
        this.AddFlyingTurtlePatrol(114 , 2 , 4);
        
        this.AddEnemyTurtlePatrol(129, 12 , 8);
        
        this.AddLift_UpDown(55 , 10 , 4 );
        
        this.AddLift_LeftRight(84 , 8, 4);
        
        this.AddLift_LeftRightInvert(92 , 9, 4);
        
        this.AddLift_LeftRight(129 , 6, 4);
        
        this.AddEnemyMushroom(42, 3);
        
        this.AddEnemyMushroom(46, 3);
        
        this.AddQuestionMarkWithMushroom(59, 9);
        
        this.AddEnemyMushroom(81, 5);
        
        this.AddCheckPointsFlag(152, 3);
        
        
    }
    
    
}
