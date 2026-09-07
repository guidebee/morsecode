package Levels.Four;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_42 extends BasicLevel {

    public Level_42() {

        BackGroundColor = "Black";

        Time = "400";
        type = "GreenAndTrees";
        pos = new Point(10, 12);
        BackGroundImage = "";
        attribute = "UnderGround";
        LevelLength = 7400;

        this.AddBrick(0, 2, 11, 1);
        this.AddStone(0, 13, 2, 11);
        this.AddStone(13, 13, 2, 15);
        this.AddStone(17, 13, 2, 18);
        this.AddStone(21, 13, 2, 57);

        this.AddBrick(6, 2, 1, 20);
        this.AddBrick(20, 2, 4, 43);
        this.AddBrick(43, 2, 2, 44);
        this.AddBank(43, 4);
        this.AddBrick(43, 5);
        this.AddBrick(44, 2, 4, 48);
        this.AddBrick(48, 2, 1, 57);

        this.AddBrick(22, 9, 3, 27);
        this.AddBrick(27, 9);
        this.AddBrickWithMushroom(28, 9);

        this.AddBrick(30, 9, 4, 48);

        this.AddQuestionMark(50, 5, 1, 52);
        this.AddQuestionMark(50, 9, 1, 52);
        this.AddQuestionMark(54, 9);
        this.AddQuestionMarkWithMushroom(55, 9);
        this.AddQuestionMark(56, 9);

        this.AddCoin(27, 12, 1, 30);

        this.AddEnemyMushroom(42, 8);
        this.AddEnemyMushroom(43, 8);
        this.AddEnemyMushroom(44, 8);

        this.AddLiftDown(58, 4);

        this.AddInvisibleBrckWithCoin(63, 8);
        this.AddInvisibleBrckWithCoin(64, 7);
        this.AddInvisibleBrckWithCoin(65, 8);
        this.AddInvisibleBrckWithCoin(66, 9);

        this.AddInvisibleBrckWithCoin(64, 5);
        this.AddClowdGoUP_CheckPoints(64, 3, 94, new Point(5, 5));

        this.AddBrick(65, 5, 1, 67);

        this.AddStone(63, 13, 2, 105);

        this.AddBrick(67, 2, 1, 112);

        this.AddPump(72, 10, 3);

        this.AddTurtle(75, 11);

        this.AddBrick(76, 9);
        this.AddBank(77, 9);

        this.AddPump(78, 6, 7);

        this.AddBrick(80, 9);
        this.AddBrickWithStar(81, 9);

        this.AddPump(84, 10, 3);
        this.AddPump(89, 10, 3);

        this.AddHelmet(81, 12);
        this.AddHelmet(87, 12);

        this.AddBrick(87, 8);

        this.AddTurtle(99, 11);
        this.AddTurtle(100, 11);

        this.AddChocolate(103, 11, 2, 104);
        this.AddChocolate(104, 10, 3, 105);

        this.AddStone(107, 13, 2, 109);
        this.AddPump(107, 9, 4);

        this.AddStone(111, 13, 2, 113);
        this.AddChocolate(111, 10, 3, 113);

        this.AddLiftDown(114, 0);
        this.AddLiftDown(114, 7);

        this.AddStone(119, 13, 2, 123);

        this.AddBrick(119, 9, 1, 123);

        this.AddBrick(119, 5);
        this.AddBrickWithMushroom(120, 5);
        this.AddBrick(121, 5);

        this.AddBrick(119, 2, 1, 123);

        this.AddLiftUP(123, 0);
        this.AddLiftUP(123, 7);

        this.AddStone(129, 13, 2, 144);
        this.AddPump(131, 11, 2);
        this.AddTurtle(135, 11);
        this.AddPump(138, 10, 3);
        this.AddPump(142, 9, 4);

        this.AddBrick(128, 2, 1, 155);

        this.AddStone(146, 13, 2, 155);

        this.AddChocolate(152, 12, 1, 155);
        this.AddChocolate(153, 11, 1, 155);
        this.AddChocolate(154, 10);

        this.AddHelmet(154, 9);

        this.AddLiftDown(156, 0);
        this.AddLiftDown(156, 7);

        this.AddStone(160, 13, 2, 183);

        this.AddTurtle(167, 11);
        this.AddTurtle(168, 11);

        this.AddChocolate(173, 12, 1, 177);
        this.AddChocolate(174, 11, 1, 177);
        this.AddChocolate(175, 10, 1, 177);
        this.AddChocolate(176, 9);

        this.AddHelmet(178, 12);
        this.AddPump(180, 6, 7);

        this.AddBrickWithMushroom(161, 9);
        this.AddBrick(162, 9, 1, 172);
        this.AddCoin(162, 8, 1, 172);

        this.AddBrick(160, 6, 2, 161);
        this.AddBrick(160, 2, 4, 176);
        this.AddBrick(176, 2, 1, 189);

        this.AddStone(185, 13, 2, 250);
        this.AddBrick(185, 10, 3, 209);
        this.AddHoriImage(187, 8);
        this.AddPumpImage(189, 2, 6);

        this.AddBrick(191, 2, 8, 209);

        this.AddBrick(209, 2, 1, 219);

        this.AddPump(214, 10, 3);

        this.AddBrick(222, 2, 11, 250);

        this.AddCheckPoints_InsidePumpHorzontally(187, 9, 43, new Point(2, 3));

        this.AddCheckPoints_InsidePumpvertically(214, 10, 51, new Point(2, 3));

        this.AddCheckPoints_InsidePumpvertically(89, 10, 'f', new Point(2, 3));


    }
}
