package Levels.Five;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_52 extends BasicLevel {

    public Level_52() {
        BackGroundColor = "Blue";

        Time = "400";
        type = "GreenAndTrees";
        pos = new Point(10, 12);
        BackGroundImage = "Fence";
        attribute = "Ground";
        LevelLength = 7000;

        this.AddCastle(0, 8, "small");
        this.AddStone(0, 13, 2, 26);
        this.AddStone(29, 13, 2, 66);

        for (int i = 0; i < 3; i++) {

            this.AddChocolate(12 + i, 12 - i, 1, 16);
        }
        this.AddChocolate(16, 9, 1, 19);
        this.AddRocketLauncher(17, 7, 2);

        this.AddTurtle(20, 11);

        this.AddBouncer(25, 11);

        this.AddCoin(21, 4, 1, 24);
        this.AddCoin(29, 8, 1, 32);

        this.AddBrick(29, 5, 1, 34);
        this.AddBrickWithMushroom(34, 5);
        this.AddBrick(29, 9, 1, 35);

        this.AddFlyingTurtle(38, 9);

        this.AddMonkey(45, 9);

        for (int i = 0; i < 3; i++) {

            this.AddChocolate(44 + i, 12 - i, 1, 49);
        }
        this.AddChocolate(49, 11, 2, 50);

        this.AddPump(55, 10, 3);

        for (int i = 0; i < 4; i++) {

            this.AddChocolate(62 + i, 12 - i, 1, 66);
        }
        this.AddChocolate(68, 8, 5, 71);
        this.AddChocolate(69, 7, 1, 71);

        this.AddEnemyMushroom(59, 12);
        this.AddEnemyMushroom(60, 12);

        this.AddQuestionMark(78, 9, 1, 83);

        this.AddInvisibleBrckWithCoin(84, 9);

        this.AddMonkey(82, 10);

        this.AddClowdGoUP_CheckPoints(85, 3, 95, new Point(5, 5));
        this.AddQuestionMark(85, 5);

        this.AddBrick(86, 5, 1, 88);
        this.AddBrick(89, 8, 1, 92);

        this.AddStone(68, 13, 2, 92);

        this.AddCoin(86, 4, 1, 88);
        this.AddCoin(89, 12, 1, 91);

        this.AddStone(96, 13, 2, 127);

        this.AddFlyingTurtle(105, 9);

        this.AddRocketLauncher(107, 11, 2);

        this.AddPump(115, 11, 2);

        this.AddBrick(118, 5, 1, 125);
        this.AddBrickWithStar(125, 5);
        this.AddBrick(118, 9, 1, 126);

        this.AddStone(129, 13, 2, 144);
        this.AddChocolate(129, 10, 3, 130);

        this.AddMonkey(120, 2);
        this.AddMonkey(123, 7);

        this.AddHelmet(134, 12);
        this.AddHelmet(135, 12);
        this.AddHelmet(136, 12);

        this.AddBank(141, 11);
        this.AddBrickWithMushroom(142, 11);

        this.AddBrick(147, 9, 1, 150);

        this.AddBrick(153, 5, 1, 158);
        this.AddEnemyTurtlePatrol(153, 4, 4);

        this.AddStone(151, 13, 2, 170);

        this.AddEnemyMushroom(154, 12);
        this.AddEnemyMushroom(155, 12);

        this.AddFlyingTurtle(164, 9);
        this.AddFlyingTurtle(165, 9);

        this.AddBrickWithMushroom(168, 9);

        this.AddStone(173, 13, 2, 175);
        this.AddPump(173, 11, 2);

        this.AddBrick(172, 5, 1, 176);

        this.AddCoin(172, 4, 1, 174);

        this.AddStone(177, 13, 2, 185);
        this.AddChocolate(183, 12, 1, 185);
        this.AddChocolate(184, 11);

        this.AddStone(186, 13, 2, 188);
        this.AddChocolate(186, 9, 4, 188);
        this.AddChocolate(187, 8);

        this.AddStone(189, 13, 2, 250);
        this.AddChocolate(189, 6, 7, 192);
        this.AddChocolate(190, 5, 1, 192);

        this.AddChocolate(200, 12);

        this.AddCheckPointsFlag(200, 3);

        this.AddCastle(204, 8, "small");
        this.AddCheckPoints(207, 12, 53, new Point(2, 3));

        this.AddCheckPoints_InsidePumpvertically(55, 10, 'h', new Point(2, 3));
    }
}
