package Levels.One;


import Levels.BasicLevel;
import java.awt.Point;


public class Level_11 extends BasicLevel {

    public Level_11() {
        
        BackGroundColor = "Blue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "Mountain" ;
        attribute       = "Ground" ;
        LevelLength     = 6768 ;
        
        	Add("stone" , 0 , 13 , 69 , 2 ) ;

		Add("stone" , 71 , 13 , 15 , 2 ) ;

		Add("stone" , 89 , 13 , 63 , 2 ) ;

		Add("stone" , 155 , 13 , 156 , 2 ) ;

		Add("pump" , 28 , 11 , 1 , 2 ) ;

		Add("pump" , 38 , 10 , 1 , 3 ) ;

		Add("pump" , 46 , 9 , 1 , 4 ) ;

		Add("pump" , 57 , 9 , 1 , 4 ) ;

		Add("Brick" , 20 , 9 , 1 , 1 ) ;
                
                Add("Brick" , 22 , 9 , 1 , 1 ) ;
                
                Add("Brick" , 24 , 9 , 1 , 1 ) ;
        
                Add("QuestionMarkWithMushroom" , 16 , 9 , 1 , 1 ) ;
                
                Add("QuestionMark" , 21 , 9 , 1 , 1 ) ;
                
                Add("QuestionMarkWithMushroom" , 23 , 9 , 1 , 1 ) ;
                
                Add("QuestionMark" , 22 , 5 , 1 , 1 ) ;
                
                this.AddInvisibleBrckWith1Up(64 , 8 );
                
                Add("Brick" , 77 , 9 , 1 , 1 ) ;
                Add("QuestionMarkWithMushroom" , 78 , 9 , 1 , 1 ) ;
                Add("Brick" , 79 , 9 , 1 , 1 ) ;
                
                Add("Brick" , 80 , 5 , 8 , 1 ) ;
                
                Add("Brick" , 91 , 5 , 3 , 1 ) ;
                Add("QuestionMark" , 94 , 5 , 1 , 1 ) ;
                
                this.AddBank( 94 , 9  ) ;
                
                Add("Brick" , 100 , 9 , 1 , 1 ) ;
                this.AddBrickWithStar( 101 , 9  ) ;
                
                Add("QuestionMark" , 106 , 9 , 1 , 1 ) ;
                Add("QuestionMark" , 109 , 9 , 1 , 1 ) ;
                Add("QuestionMark" , 112 , 9 , 1 , 1 ) ;
                
                this.AddQuestionMarkWithMushroom(109, 5);
                
                Add("Brick" , 118 , 9 , 1 , 1 ) ;
                
                Add("Brick" , 121 , 5 , 3 , 1 ) ;
                
                Add("Brick" , 128 , 5 , 1 , 1 ) ;
                Add("QuestionMark" , 129 , 5 , 2 , 1 ) ;
                Add("Brick" , 131 , 5 , 1 , 1 ) ;
                
                Add("Brick" , 129 , 9 , 2 , 1 ) ;
                
                Add("chocolate" , 134 , 12 , 4 , 1 ) ;
                Add("chocolate" , 135 , 11 , 3 , 1 ) ;
                Add("chocolate" , 136 , 10 , 2 , 1 ) ;
                Add("chocolate" , 137 , 9 , 1 , 1 ) ;
                
                Add("chocolate" , 140 , 9 , 1 , 1 ) ;
                Add("chocolate" , 140 , 10 , 2 , 1 ) ;
                Add("chocolate" , 140 , 11 , 3 , 1 ) ;
                Add("chocolate" , 140 , 12 , 4 , 1 ) ;
                
                
                Add("chocolate" , 148 , 12 , 4 , 1 ) ;
                Add("chocolate" , 149 , 11 , 3 , 1 ) ;
                Add("chocolate" , 150 , 10 , 2 , 1 ) ;
                Add("chocolate" , 151 , 9 , 1 , 1 ) ;
                
                Add("chocolate" , 155 , 9  , 1 , 1 ) ;
                Add("chocolate" , 155 , 10 , 2 , 1 ) ;
                Add("chocolate" , 155 , 11 , 3 , 1 ) ;
                Add("chocolate" , 155 , 12, 4 , 1 ) ;
                
                Add("pump" , 163 , 11 , 1 , 2 ) ;
                
                Add("Brick" , 168 , 9 , 2 , 1 ) ;
                Add("QuestionMark" , 170 , 9 , 1 , 1 ) ;
                Add("Brick" , 171 , 9 , 1 , 1 ) ;
                
                Add("pump" , 179 , 11 , 1 , 2 ) ;
                
                Add("chocolate" , 181 , 12 , 9 , 1 ) ;
                Add("chocolate" , 182 , 11 , 8 , 1 ) ;
                Add("chocolate" , 183 , 10 , 7 , 1 ) ;
                Add("chocolate" , 184 , 9 , 6 , 1 ) ;
                Add("chocolate" , 185 , 8 , 5 , 1 ) ;
                Add("chocolate" , 186 , 7 , 4 , 1 ) ;
                Add("chocolate" , 187 , 6 , 3 , 1 ) ;
                Add("chocolate" , 188 , 5 , 2 , 1 ) ;
                
                Add("chocolate" , 198 , 12 , 1 , 1 ) ;
                
                // 102 items 
                //////
//                Add("EnemyMushroom" , 22 , 8 , 1 , 1 ) ;
//                this.AddTurtle(20, 8);
                this.AddEnemyMushroom(21, 12);
//                this.AddEnemyMushroom(24, 8);
//                this.AddTurtle(26, 8);
//                this.AddBrick(19, 8);
//                this.AddBrick(25, 8);
                //////
//                Add("EnemyMushroom" , 22 , 8 , 1 , 1 ) ;
                
                Add("EnemyMushroom" , 40 , 12 , 1 , 1 ) ;
                
                Add("EnemyMushroom" , 53 , 12 , 2 , 1 ) ;
                
                Add("EnemyMushroom" , 80 , 4 , 1 , 1 ) ;
                
                Add("EnemyMushroom" , 83 , 4 , 1 , 1 ) ;
                
                Add("EnemyMushroom" , 95 , 12 , 2 , 1 ) ;
                
                Add("EnemyMushroom" , 123 , 12 , 2 , 1 ) ;
                Add("EnemyMushroom" , 127 , 12 , 2 , 1 ) ;
                
                Add("EnemyMushroom" , 173 , 12 , 2 , 1 ) ;
                
                Add("EnemyTurtle" , 106 , 11 , 1 , 1 ) ;
                
                this.AddCheckPointsFlag(198, 3);
                
                Add("SmallCastle" , 202 , 8 , 1 , 1 ) ;
                
                this.AddCheckPoints(205, 12 , 12 , new Point(2 , 3));
                
                this.AddCheckPoints_InsidePumpvertically(57, 9 , 'a' , new Point(2 , 3) );
                
    }

    

    

    

    


}
