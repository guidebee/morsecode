
package Levels.Seven;


import Levels.BasicLevel;
import java.awt.Point;

public class Level_73 extends BasicLevel{

    public Level_73() {
        
                
                
        BackGroundColor = "Blue";
        
        Time            = "400" ;
        type            = "GreenAndTrees" ;
        pos             = new Point(2 , 3);
        BackGroundImage         = "Clouds" ;
        attribute       = "Ground" ;
        LevelLength     = 8000 ;
        FlyingFishes = true ;
        FlyingFishesLength = 200*32 ;
        
        this.AddCastle(0, 8, "small");
        this.AddStone(0, 13 ,2 , 7);
        this.AddStone(207, 13 ,2 , 250);
        
        this.AddTree(8, 13, 2, 16);
        this.AddTree(112, 13, 2, 120);
        this.AddTree(192, 13, 2, 205);
        
        this.AddChocolate(10, 12, 1, 15);
        this.AddChocolate(11, 11, 1, 15);
        this.AddChocolate(12, 10, 1, 15);
        
        this.AddChocolate(31, 10, 5, 32);
        this.AddChocolate(47, 10, 5, 48);
        this.AddChocolate(63, 10, 5, 64);
        this.AddChocolate(68, 10, 5, 69);
        this.AddChocolate(79, 10, 5, 80);
        this.AddChocolate(84, 10, 5, 85);
        this.AddChocolate(95, 10, 5, 96);
        this.AddChocolate(99, 9, 6, 100);
        this.AddChocolate(105, 9, 6, 106);
        
        this.AddChocolate(127, 10, 5, 128);
        this.AddChocolate(143, 10, 5, 144);
        
        this.AddChocolate(146, 12, 3, 147);
        this.AddChocolate(155, 12, 3, 156);
        
        this.AddChocolate(159, 10, 5, 160);
        this.AddChocolate(168, 10, 5, 169);
        
        this.AddChocolate(183, 10, 5, 184);
        this.AddChocolate(193, 10, 3, 195);
        
        this.AddChocolate(195, 11, 2, 196);
        this.AddChocolate(196, 12);
        
        this.AddChocolate(208, 12 , 1 , 217);
        this.AddChocolate(209, 11 , 1 , 217);
        this.AddChocolate(210, 10 , 1 , 217);
        this.AddChocolate(211, 9 , 1 , 217);
        this.AddChocolate(212, 8 , 1 , 217);
        this.AddChocolate(213, 7 , 1 , 217);
        this.AddChocolate(214, 6 , 1 , 217);
        this.AddChocolate(215, 5 , 1 , 217);
        
        this.AddChocolate(225, 12 );
        
        this.AddCheckPointsFlag(225, 3);
        
        this.AddCastle(228, 2, "big");
        this.AddCheckPoints(233, 12, 74, new Point(2, 6));
        
        this.AddWoodenBridge(15 , 10 , 31);
        this.AddWoodenBridge(32 , 10 , 47);
        this.AddWoodenBridge(48 , 10 , 63);
        
        this.AddWoodenBridge(69 , 10 , 79);
        
        this.AddWoodenBridge(85 , 10 , 95);
        
        this.AddWoodenBridge(100 , 9 , 105);
        
        this.AddWoodenBridge(122 , 10 , 125);
        
        this.AddWoodenBridge(128 , 10 , 143);
        
        this.AddWoodenBridge(147 , 12 , 155);
        
        this.AddWoodenBridge(160 , 10 , 168);
        
        this.AddWoodenBridge(171 , 10 , 173);
        this.AddWoodenBridge(175 , 10 , 177);
        this.AddWoodenBridge(179 , 10 , 181);
        
        this.AddWoodenBridge(184 , 10 , 193);
        
        this.AddTurtle(47, 9);
        
        this.AddFlyingTurtle(60, 8);
        this.AddFlyingTurtle(143, 6);
        this.AddFlyingTurtle(155, 8);
        
        this.AddEnemyTurtlePatrol(68, 9, 11);
        this.AddEnemyTurtlePatrol(84, 9, 11);
        this.AddEnemyTurtlePatrol(112, 13, 7);
        
        
    }
    
    
}
