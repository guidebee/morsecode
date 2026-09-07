package Levels.Eight;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_81 extends BasicLevel {

    public Level_81() {

        BackGroundColor = "Blue";

        Time = "400";
        type = "GreenAndTrees";
        pos = new Point(10, 12);
        BackGroundImage = "Fence2";
        attribute = "Ground";
        LevelLength = 12700;

        this.AddStone(0, 13, 2, 46);
        this.AddStone(47, 13, 2, 48);
        this.AddStone(49, 13, 2, 51);
        this.AddStone(52, 13, 2, 54);
        this.AddStone(55, 13, 2, 57);
        this.AddStone(58, 13, 2, 169);
        this.AddStone(170, 13, 2, 171);
        this.AddStone(172, 13, 2, 174);
        this.AddStone(175, 13, 2, 176);
        this.AddStone(177, 13, 2, 179);
        this.AddStone(180, 13, 2, 197);
        this.AddStone(198, 13, 2, 199);
        this.AddStone(200, 13, 2, 201);
        this.AddStone(202, 13, 2, 220);
        this.AddStone(227, 13, 2, 237);
        this.AddStone(238, 13, 2, 240);
        this.AddStone(242, 13, 2, 244);
        this.AddStone(246, 13, 2, 290);
        this.AddStone(293, 13, 2, 295);
        this.AddStone(298, 13, 2, 314);
        this.AddStone(319, 13, 2, 320);
        this.AddStone(324, 13, 2, 359);
        this.AddStone(360, 13, 2, 361);
        this.AddStone(362, 13, 2, 363);
        this.AddStone(364, 13, 2, 365);
        this.AddStone(366, 13, 2, 400);

        this.AddCastle(-2, 2, "big");

        this.AddHelmet(13, 12);
        this.AddHelmet(80, 12);
        this.AddHelmet(251, 12);
        this.AddHelmet(281, 12);

        this.AddEnemyMushroom(21, 12);
        this.AddEnemyMushroom(22, 12);
        this.AddEnemyMushroom(23, 12);
        this.AddEnemyMushroom(31, 12);
        this.AddEnemyMushroom(72, 12);
        this.AddEnemyMushroom(73, 12);
        this.AddEnemyMushroom(74, 12);
        this.AddEnemyMushroom(107, 12);
        this.AddEnemyMushroom(108, 12);
        this.AddEnemyMushroom(108, 12);
        this.AddEnemyMushroom(147, 12);
        this.AddEnemyMushroom(148, 12);
        this.AddEnemyMushroom(149, 12);
        this.AddEnemyMushroom(232, 12);
        this.AddEnemyMushroom(233, 12);
        this.AddEnemyMushroom(234, 12);
        this.AddEnemyMushroom(256, 12);
        this.AddEnemyMushroom(257, 12);
        this.AddEnemyMushroom(258, 12);
        this.AddEnemyMushroom(261, 12);
        this.AddEnemyMushroom(271, 12);

        this.AddPump(246, 8, 5);
        this.AddPump(35, 9, 4);
        this.AddPump(76, 9, 4);
        this.AddPump(94, 9, 4);
        this.AddPump(104, 9, 4);
        this.AddPump(242, 9, 4);
        this.AddPump(82, 10, 3);
        this.AddPump(140, 10, 3);
        this.AddPump(238, 10, 3);
        this.AddPump(344, 10, 3);
        this.AddPump(115, 11, 2);
        this.AddPump(355, 11, 2);

        this.AddTurtle(41, 11);
        this.AddTurtle(43, 11);
        this.AddTurtle(65, 11);
        this.AddTurtle(123, 11);
        this.AddTurtle(128, 11);
        this.AddTurtle(129, 11);
        this.AddTurtle(130, 11);
        this.AddTurtle(205, 11);
        this.AddTurtle(207, 11);
        this.AddTurtle(304, 11);
        this.AddTurtle(331, 11);
        this.AddTurtle(338, 11);
        this.AddTurtle(339, 11);
        this.AddTurtle(340, 11);

        this.AddInvisibleBrckWith1Up(80, 8);

        this.AddCoin(64, 8);
        this.AddCoin(89, 8);
        this.AddCoin(98, 8);
        this.AddCoin(283, 8);
        this.AddCoin(284, 8);
        this.AddCoin(291, 8);
        this.AddCoin(296, 8);
        this.AddCoin(316, 8);
        this.AddCoin(317, 8);
        this.AddCoin(321, 8);
        this.AddCoin(322, 8);
        this.AddCoin(109, 4);
        this.AddCoin(110, 4);
        this.AddCoin(223, 8, 1, 225);

        this.AddChocolate(153, 9, 4, 154);
        this.AddChocolate(163, 9, 4, 164);
        this.AddChocolate(210, 11, 2, 211);
        this.AddChocolate(303, 11, 2, 304);
        this.AddChocolate(307, 11, 2, 308);
        this.AddChocolate(360, 11, 2, 361);
        this.AddChocolate(362, 9, 4, 363);
        this.AddChocolate(364, 7, 6, 365);
        this.AddChocolate(366, 5, 8, 368);

        this.AddInvisibleBrckWithCoin(158, 9);
        this.AddBrick(154, 5, 1, 158);
        this.AddBank(158, 5);
        this.AddBrick(159, 5, 1, 162);

        this.AddFlyingTurtle(158, 11);
        this.AddFlyingTurtle(171, 11);
        this.AddFlyingTurtle(176, 11);

        this.AddBrick(184, 8, 1, 186);
        this.AddBrickWithStar(186, 8);
        this.AddBrick(187, 8, 1, 191);

        for (int i = 0; i < 6; i++) {
            this.AddChocolate(275 + i, 12 - i, 1, 281);
        }

        this.AddCheckPointsFlag(376, 3);
        this.AddChocolate(376, 12);

        this.AddCastle(380, 8, "small");
        this.AddCheckPoints(383, 12, 82, new Point(2, 3));

        this.AddCheckPoints_InsidePumpvertically(104, 9, 'm', new Point(2, 3));
    }

}
