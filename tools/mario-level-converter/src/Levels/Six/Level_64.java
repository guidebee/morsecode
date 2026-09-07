
package Levels.Six;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_64 extends BasicLevel{

    public Level_64() {
        
        
        BackGroundColor = "Black";
        
        Time            = "300" ;
        type            = "" ; // if OrangeAndMushroom then red treen
        pos             = new Point(10 , 12);
        BackGroundImage = "" ;
        attribute       = "Castle" ;
        LevelLength          = 5200 ;
        
        Add("stone" , 0 , 10 , 13 , 5 ) ;
        Add("stone" , 0 , 7 , 3 , 1 ) ;
        Add("stone" , 0 , 8 , 4 , 1 ) ;
        Add("stone" , 0 , 9 , 5 , 1 ) ;
        
        Add("stone" , 0 , 2 , 23 , 3 ) ;
        Add("stone" , 23 , 2 , 1 , 4 ) ;
        Add("stone" , 24 , 2 , 13 , 1 ) ;
        
        Add("stone" , 15 , 10 , 11 , 5 ) ;
        
        Add("stone" , 29 , 10 , 1 , 1 ) ;
        Add("stone" , 31 , 10 , 1 , 1 ) ;
        Add("stone" , 29 , 11 , 3 , 5 ) ;
        
        Add("stone" , 35 , 9 , 37 , 6 ) ;
        
        Add("stone" , 37 , 2 , 35 , 4 ) ;
        
        this.AddStone(72, 2, 1, 500);
//        Add("stone" , 72 , 2 , 100 , 1 ) ;
        
        Add("stone" , 72 , 10 , 32 , 6 ) ;
        
        
        Add("stone" , 80 , 3 , 1 , 1 ) ;
        
        Add("stone" , 88 , 3 , 1 , 1 ) ;
        
        Add("stone" , 97 , 3 , 7 , 2 ) ;
        
        Add("stone" , 104 , 23 , 1 , 2 ) ;
        
        Add("stone" , 116 , 10 , 4 , 3 ) ;
        
        Add("stone" , 104 , 13 , 24 , 3 ) ;
        
        Add("stone" , 123 , 10 , 5 , 3 ) ;
        
        Add("stone" , 123 , 3 , 5 , 2 ) ;
        
        Add("Brick" , 128 , 10 , 13 , 1 ) ; // bridge
        
        this.AddAxe(141 , 8);
        
        Add("stone" , 142 , 3 , 2 , 3 ) ;
        
        Add("stone" , 141 , 9 , 3 , 6 ) ;
        
        this.AddStone(144, 13, 2, 300);
        
        this.AddWhyYouDOThis(153, 11, 71, new Point(3 , 2));
        
//        this.AddWhyYouDOThis(153 , 11 );
        
        this.AddIron(23 , 6 ); // default color is red even in castle
        
        this.AddIron(30 , 10 );
        
        this.AddQuestionMarkWithMushroom(30, 6);
        
        this.AddIron(37 , 6 );
        
        this.AddIron(49 , 6 );
        this.AddIron(60 , 6 );
        this.AddIron(67 , 6 );
        
        this.AddIron(80 , 4 );
        this.AddIron(88 , 4 );
        
        this.AddIron(76 , 9 );
        this.AddIron(84 , 9 );
        this.AddIron(92 , 9 );
        
        this.AddInvisibleBrckWithCoin(106, 9);
        this.AddInvisibleBrckWithCoin(109, 9);
        this.AddInvisibleBrckWithCoin(112, 9);
        
        this.AddInvisibleBrckWithCoin(107, 5);
        this.AddInvisibleBrckWithCoin(110, 5);
        this.AddInvisibleBrckWithCoin(113, 5);
        
        
        this.AddFireBar(23, 6, 6 , "ACW");
        
        this.AddFireBar(37, 6, 6 , "ACW");
        
        this.AddFireBar(80, 4, 6 , "ACW");
        
        this.AddFireBar(92, 9, 6 );
        
        
        
        
        this.AddFireBar(30, 10, 6 );
        
        this.AddFireBar(49, 6, 6, "ACW");
        
        this.AddFireBar(60, 6, 6, "ACW");
        
        this.AddFireBar(67, 6, 6, "ACW");
        
        this.AddFireBar(76, 9, 6);
        
        this.AddFireBar(84, 9, 6);
        
        
        this.AddFireBar(88, 4, 6 , "ACW");
        
        this.AddBossHammer(135, 7 , 141);
        
        this.AddBridgeBloks(128 , 10 , 1 , 141);
        
        this.AddFire(109 , 7);
        
        this.AddFire(113 , 9);
        
        
        this.AddFire(117 , 7);
        
        this.AddFire(129 , 7);
        
        
        this.AddLavaUP(13, 15);
        
        this.AddLava(26 , 29);
        
        this.AddLava(32 , 35);
        
        this.AddLava(128 , 141);
        
        this.AddLift_LeftRight(137, 6 , 4);
        
        this.AddLavaBall(131);
        
        
        
        
    }
    
    
}
