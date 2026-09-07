package Levels.Three;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_31 extends BasicLevel {

    public Level_31() {


        BackGroundColor = "Black";

        Time = "400";
        type = "GreenAndTrees";
        pos = new Point(10, 12);
        BackGroundImage = "Fence";
        attribute = "Ground";
        LevelLength = 7000;

        this.AddCastle(-2, 2, "big");

        


        this.AddStone(0, 13, 2, 45);
        this.AddStone(48, 13, 2, 77);
        this.AddStone(85, 13, 2, 86);
        this.AddStone(88, 13, 2, 128);
        this.AddStone(132, 13, 2, 142);
        this.AddStone(144, 13, 2, 177);
        this.AddStone(180, 13, 2, 240);

        this.AddQuestionMark(16, 9);
        this.AddQuestionMark(19, 8);
        this.AddQuestionMarkWithMushroom(22, 8);
        this.AddBrick(26, 9, 1, 29);

        this.AddPump(32, 10, 3);
        this.AddPump(38, 9, 4); // checkpoint C
        this.AddCheckPoints_InsidePumpvertically(38, 9, 'd', new Point(2, 3));

        this.AddPump(57, 10, 3);

        this.AddBrick(61, 9);

        this.AddPump(67, 11, 2);

        this.AddChocolate(73, 12, 1, 77);
        this.AddChocolate(74, 11, 1, 77);
        this.AddChocolate(75, 10, 1, 77);
        this.AddChocolate(76, 9);

        this.AddWoodenBridge(77, 9, 85);

        this.AddInvisibleBrckWith1Up(82, 5);

        this.AddChocolate(85, 9, 4, 86);

        this.AddChocolate(88, 9, 4, 89);
        this.AddChocolate(89, 11, 2, 90);

        this.AddBrickWithStar(90, 5);
        this.AddBrick(91, 5, 1, 93);

        this.AddPump(103, 9, 4);


        this.AddBrick(111, 5, 1, 113);
        this.AddQuestionMark(113, 5);
        this.AddBrick(114, 5, 1, 117);
        this.AddQuestionMarkWithMushroom(117, 5);
        this.AddBrick(118, 5, 1, 121);

        this.AddBrick(111, 9, 1, 122);

        this.AddBouncer(126, 11);

        this.AddBrick(129, 5, 1, 131);
        this.AddBrick(129, 8, 1, 132);

        this.AddQuestionMark(131, 5);
        this.AddClowdGoUP_CheckPoints(131, 3, 93, new Point(5, 5));
        
        this.AddChocolate(136, 12, 1, 142);
        this.AddChocolate(137, 11, 1, 142);
        this.AddChocolate(138, 10, 1, 142);
        this.AddChocolate(139, 9, 1, 142);
        this.AddChocolate(140, 8, 1, 142);
        this.AddChocolate(141, 7, 1, 142);

        this.AddBrick(150, 5);
        this.AddQuestionMark(151, 5);
        this.AddBrick(152, 5);
        this.AddBrick(155, 5);
        this.AddQuestionMark(156, 5);
        this.AddBrick(157, 5);

        this.AddBrick(150, 9);
        this.AddQuestionMark(151, 9);
        this.AddBrick(152, 9);
        this.AddBrick(155, 9);
        this.AddQuestionMark(156, 9);
        this.AddBrick(157, 9);


        this.AddBrick(166, 9);
        this.AddBank(167, 9);
        this.AddBrick(168, 9, 1, 171);

        this.AddChocolate(174, 10, 3, 175);
        this.AddChocolate(175, 7, 6, 176);

        this.AddChocolate(183, 12, 1, 192);
        this.AddChocolate(184, 11, 1, 192);
        this.AddChocolate(185, 10, 1, 192);
        this.AddChocolate(186, 9, 1, 192);
        this.AddChocolate(187, 8, 1, 192);
        this.AddChocolate(188, 7, 1, 192);
        this.AddChocolate(189, 6, 1, 192);
        this.AddChocolate(190, 5, 1, 192);

        this.AddChocolate(200, 12);

        this.AddCheckPointsFlag(200, 3);

        this.AddCastle(204, 8, "small");
        this.AddCheckPoints(207, 12, 32, new Point(2, 3));

        this.AddWater(77, 12, 88);

        this.AddFlyingTurtle(18, 9);
        this.AddFlyingTurtle(25, 9);

        this.AddEnemyMushroom(36, 12);
        this.AddEnemyMushroom(50, 12);
        this.AddEnemyMushroom(52, 12);
        this.AddEnemyMushroom(54, 12);

        this.AddTurtle(60, 11);

        this.AddEnemyMushroom(80, 8);
        this.AddEnemyMushroom(82, 8);
        this.AddEnemyMushroom(84, 8);

        this.AddEnemyMushroom(93, 12);
        this.AddEnemyMushroom(94, 12);

        this.AddTurtle(98, 11);

        this.AddMonkey(116, 8);
        this.AddMonkey(113, 12);

        this.AddEnemyMushroom(170, 7);
        this.AddEnemyMushroom(138, 9);

        this.AddTurtle(147, 12);
        this.AddTurtle(151, 3);

        this.AddEnemyMushroom(154, 12);
        this.AddEnemyMushroom(156, 12);
        this.AddEnemyMushroom(158, 12);

        this.AddFlyingTurtle(162, 9);
        this.AddFlyingTurtle(166, 10);
        this.AddFlyingTurtle(170, 10);

        this.AddTurtle(168, 7);
        this.AddTurtle(184, 9);
        this.AddTurtle(187, 6);




    }
}
