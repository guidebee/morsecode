package Levels.Six;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_62 extends BasicLevel {

    public Level_62() {

        BackGroundColor = "Black";

        Time = "400";
        type = "GreenAndTrees";
        pos = new Point(10, 12);
        BackGroundImage = "Mountain";
        attribute = "Ground";
        LevelLength = 7700;

        this.AddStone(0, 13, 2, 123);
        this.AddStone(129, 13, 2, 141);
        this.AddStone(142, 13, 2, 143);
        this.AddStone(144, 13, 2, 152);
        this.AddStone(153, 13, 2, 270);

        this.AddCastle(0, 8, "small");

        this.AddBrick(10, 9, 1, 13);

        this.AddPump(19, 9, 4);
        this.AddBrick(23, 5);
        this.AddBank(24, 5);
        this.AddBrick(25, 5);
        this.AddInvisibleBrckWithCoin(24, 9);
        this.AddTurtle(24, 11);
        this.AddPump(28, 9, 4);

        this.AddPump(32, 6, 2);
        this.AddChocolate(32, 8, 1, 34);

        this.AddPump(35, 11, 2);
        this.AddPump(37, 11, 2);

        this.AddPump(46, 9, 4);
        this.AddBrick(51, 9);
        this.AddBrickWithMushroom(52, 9);
        this.AddPump(56, 8, 5);

        this.AddPump(62, 7, 2);
        this.AddChocolate(62, 9, 1, 64);

        this.AddPump(67, 11, 2);
        this.AddPump(74, 11, 2);

        this.AddBrick(67, 5, 1, 72);
        this.AddBrick(77, 5, 1, 81);
        this.AddQuestionMark(81, 5);

        this.AddPump(80, 10, 3);
        this.AddInvisibleBrckWithCoin(81, 9);

        this.AddClowdGoUP_CheckPoints(81, 3, 96, new Point(5, 5));

        this.AddPump(84, 11, 2);
        this.AddPump(87, 7, 6);
        this.AddPump(94, 10, 3);
        this.AddPump(102, 9, 4);
        this.AddPump(105, 11, 2);
        this.AddPump(111, 6, 3);
        this.AddChocolate(111, 9, 1, 113);
        this.AddPump(115, 11, 2);

        this.AddBrick(110, 9);
        this.AddBrick(113, 9);
        this.AddBrick(115, 5);

        this.AddHelmet(114, 11);

        this.AddBrick(119, 5, 1, 128);

        this.AddPump(131, 11, 2);
        this.AddPump(135, 11, 2);

        this.AddBrick(138, 8, 1, 140);
        this.AddBrick(145, 5, 1, 147);
        this.AddBrick(144, 9, 1, 147);
        this.AddBrickWithStar(140, 5);
        this.AddBrick(141, 5);

        this.AddChocolate(149, 12, 1, 152);
        this.AddChocolate(150, 11, 1, 152);
        this.AddChocolate(151, 10);

        this.AddPump(153, 10, 3);

        this.AddChocolate(156, 9, 4, 157);
        this.AddChocolate(157, 11, 2, 158);

        this.AddBrick(160, 5, 1, 163);
        this.AddBrick(160, 9, 1, 163);

        this.AddPump(167, 6, 3);
        this.AddChocolate(167, 9, 1, 169);

        this.AddPump(174, 9, 4);
        this.AddPump(179, 11, 2);
        this.AddPump(181, 10, 3);
        this.AddPump(183, 9, 4);

        this.AddPump(189, 10, 3);

        this.AddPump(201, 9, 4);

        this.AddChocolate(199, 12);
        this.AddChocolate(200, 11, 2, 201);

        for (int i = 0; i < 4; i++) {
            this.AddChocolate(203 + i, 8 - i, 5 + i, 208);
        }

        this.AddCheckPointsFlag(216, 3);
        this.AddChocolate(216, 12);

        this.AddCastle(220, 8, "small");

        this.AddCheckPoints(223, 12, 63, new Point(2, 3));

        this.AddHelmet(51, 12);
        this.AddHelmet(91, 12);
        this.AddHelmet(158, 12);

        this.AddEnemyMushroom(166, 12);

        this.AddFlyingTurtle(205, 2);

        this.AddCheckPoints_InsidePumpvertically(19, 9, 'i', new Point(2, 3));
        this.AddCheckPoints_InsidePumpvertically(56, 8, 'j', new Point(2, 3));
        this.AddCheckPoints_InsidePumpvertically(153, 10, 'k', new Point(2, 3));
    }
}
