
package Levels.Two;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_22 extends BasicLevel{

    public Level_22() {
        
        BackGroundColor = "DarkBlue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(10 , 12);
        BackGroundImage         = "" ;
        attribute       = "Sea" ;
        LevelLength     = 192*32 ;
        
        
        this.AddHoriImage(189, 7);
        this.AddCheckPoints_InsidePumpHorzontally(189, 8, 23, new Point(2,3));
        
        this.AddStone(0, 13 , 2, 66);
        this.AddStone(71, 13 , 2, 131);
        this.AddStone(140, 13 , 2, 157);
        this.AddStone(164, 13 , 2, 200);
        
        this.AddStone(18, 9 , 1, 21);
        this.AddStone(42, 9 , 1, 44);
        this.AddStone(64, 10 , 3, 66);
        this.AddStone(65, 8 , 2, 66);
        
        this.AddStone(71, 8 , 2, 72);
        this.AddStone(71, 10 , 3, 73);
        
        this.AddStone(78, 2 , 3, 80);
        this.AddStone(78, 10 , 3, 80);
        
        this.AddStone(82, 5 , 1, 85);
        
        this.AddStone(102, 9 , 1, 104);
        
        this.AddStone(115, 8 , 1, 117);
        
        this.AddStone(129, 9 , 2, 130);
        this.AddStone(129, 11 , 2, 131);
        
        this.AddStone(141, 9 , 2, 142);
        this.AddStone(140, 11 , 2, 142);
        
        this.AddStone(131, 2 , 2, 132);
        this.AddStone(131, 4 , 1, 140);
        
        this.AddStone(156, 5 , 1, 159);
        this.AddStone(156, 6 , 7, 157);
        
        this.AddStone(162, 5 , 1, 165);
        this.AddStone(164, 6 , 7, 165);
        
        this.AddStone(172, 5 , 1, 177);
        this.AddStone(172, 9 , 1, 177);
        this.AddStone(180, 5 , 1, 184);
        this.AddStone(180, 9 , 1, 184);
        
        this.AddStone(188, 2 , 4, 200);
        this.AddStone(190, 6 , 4, 200);
        this.AddStone(188, 9 , 4, 200);
        
        this.AddStone(187, 10 , 3, 188);
        this.AddStone(186, 11 , 2, 187);
        this.AddStone(185, 12 , 1, 186);
        
        this.AddBrick( 11 , 10 );
        
this.AddBrick( 11 , 11 );
this.AddBrick( 11 , 12 );
this.AddBrick( 33 , 8 );
this.AddBrick( 33 , 9 );
this.AddBrick( 33 , 10 );
this.AddBrick( 33 , 11 );
this.AddBrick( 33 , 12 );
this.AddBrick( 42 , 7 );
this.AddBrick( 42 , 8 );
this.AddBrick( 50 , 9 );
this.AddBrick( 50 , 10 );
this.AddBrick( 50 , 11 );
this.AddBrick( 50 , 12 );
this.AddBrick( 83 , 3 );
this.AddBrick( 83 , 4 );
this.AddBrick( 89 , 10 );
this.AddBrick( 89 , 11 );
this.AddBrick( 89 , 12 );
this.AddBrick( 102 , 5 );
this.AddBrick( 102 , 6 );
this.AddBrick( 102 , 7 );
this.AddBrick( 102 , 8 );
this.AddBrick( 120 , 9 );
this.AddBrick( 120 , 10 );
this.AddBrick( 120 , 11 );
this.AddBrick( 120 , 12 );
this.AddBrick( 147 , 11 );
this.AddBrick( 147 , 12 );
this.AddBrick( 149 , 10 );
this.AddBrick( 149 , 11 );
this.AddBrick( 149 , 12 );
this.AddBrick( 173 , 3 );
this.AddBrick( 173 , 4 );
        
this.AddCoin( 14 , 12 );
this.AddCoin( 15 , 12 );
this.AddCoin( 27 , 5 );
this.AddCoin( 28 , 5 );
this.AddCoin( 29 , 5 );
this.AddCoin( 36 , 12 );
this.AddCoin( 37 , 12 );
this.AddCoin( 38 , 12 );
this.AddCoin( 67 , 10 );
this.AddCoin( 68 , 10 );
this.AddCoin( 69 , 10 );
this.AddCoin( 101 , 11 );
this.AddCoin( 102 , 11 );
this.AddCoin( 103 , 11 );
this.AddCoin( 113 , 6 );
this.AddCoin( 114 , 6 );
this.AddCoin( 115 , 6 );
this.AddCoin( 133 , 11 );
this.AddCoin( 134 , 12 );
this.AddCoin( 135 , 12 );
this.AddCoin( 136 , 12 );
this.AddCoin( 137 , 11 );
this.AddCoin( 159 , 9 );
this.AddCoin( 159 , 12 );
this.AddCoin( 160 , 9 );
this.AddCoin( 160 , 12 );
this.AddCoin( 161 , 9 );
this.AddCoin( 161 , 12 );



        this.AddOctoPussy(10 , 10);
        this.AddOctoPussy(44 , 8);
        this.AddOctoPussy(63 , 6);
        this.AddOctoPussy(81 , 8);
        this.AddOctoPussy(93 , 6);
        
        this.AddFishGrey(74, 7);
        this.AddFishGrey(77, 8);
        this.AddFishGrey(92, 3);
        this.AddFishGrey(95, 4);
        this.AddFishGrey(99, 4);
        this.AddFishGrey(127, 3);
        this.AddFishGrey(137, 2);
        this.AddFishGrey(145, 6);
        this.AddFishGrey(162, 6);
        this.AddFishGrey(170, 2);
        this.AddFishGrey(174, 7);
        this.AddFishGrey(179, 5);
        
        this.AddFishRed(79 , 5);
        this.AddFishRed(86 , 3);
        this.AddFishRed(101 , 10);
        this.AddFishRed(113 , 11);
        this.AddFishRed(123 , 6);
        this.AddFishRed(151 , 3);
        
        
        
        
        
        
        
        
        
        
        
        
    }
}
