package Levels.One;

import Levels.BasicLevel;
import java.awt.Point;

public class Level_12 extends BasicLevel {

    public Level_12() {

        BackGroundColor = "Black";

        Time = "400";
        type = "GreenAndTrees";
        pos = new Point(10, 12);
        BackGroundImage = "";
        attribute = "UnderGround";
        LevelLength = 6176;

        Add("stone", 0, 13, 80, 2);

        Add("Brick", 0, 2, 1, 11);

        Add("Brick", 6, 2, 83, 1);

        this.AddQuestionMarkWithMushroom(10, 9);
        this.AddQuestionMark(11, 9, 1, 15);

        Add("Brick", 90, 2, 48, 1);

        Add("chocolate", 17, 12, 1, 1);

        Add("chocolate", 19, 11, 1, 2);

        Add("chocolate", 21, 10, 1, 3);

        Add("chocolate", 23, 9, 1, 4);

        Add("chocolate", 25, 9, 1, 4);

        Add("chocolate", 27, 10, 1, 3);

        Add("chocolate", 31, 10, 1, 3);

        Add("chocolate", 33, 11, 1, 2);

        this.AddCoin(40, 8);
        this.AddCoin(41, 5, 1, 45);
        this.AddCoin(45, 8);

        this.AddBrickWithStar(46, 7);

        Add("Brick", 39, 7, 1, 3);
        Add("Brick", 41, 7, 1, 3);
        Add("Brick", 44, 7, 1, 3);
        Add("Brick", 46, 8, 1, 2);

        Add("Brick", 40, 9, 1, 1);
        Add("Brick", 42, 7, 2, 1);
        Add("Brick", 45, 9, 1, 1);


        Add("Brick", 54, 3, 2, 2);
        Add("Brick", 52, 5, 2, 5);
        Add("Brick", 54, 9, 2, 3);

        this.AddCoin(58, 8, 1, 62);

        Add("Brick", 58, 3, 6, 2);
        Add("Brick", 62, 5, 2, 4);
        Add("Brick", 58, 9, 6, 1);

        Add("Brick", 66, 3, 4, 2);
        Add("Brick", 67, 5, 1, 4);
        Add("Brick", 67, 9, 3, 1);
        this.AddCoin(68, 8);
        this.AddBrickWithMushroom(69, 8);

        Add("Brick", 72, 5, 2, 3);
        Add("Brick", 72, 8, 1, 1);
        Add("Brick", 72, 9, 2, 1);
        this.AddBank(73, 8);

        Add("Brick", 76, 3, 4, 2);

        Add("Brick", 76, 9, 4, 1);

        Add("Brick", 84, 7, 6, 2);
        this.AddCoin(84, 5, 1, 90);

        this.AddBrickWith1UP(89, 2);

        Add("Brick", 122, 10, 2, 3);


        Add("stone", 83, 13, 37, 2);

        Add("stone", 122, 13, 2, 2);

        Add("stone", 126, 13, 12, 2);

        Add("chocolate", 133, 12, 5, 1);
        Add("chocolate", 134, 11, 4, 1);
        Add("chocolate", 135, 10, 3, 1);
        Add("chocolate", 136, 9, 2, 1);

        Add("Brick", 145, 8, 5, 1);

        Add("stone", 145, 13, 8, 2);

        Add("Brick", 160, 10, 17, 3);
        Add("stone", 160, 13, 150, 2);

        this.AddBrick(161, 2, 1, 168);

        Add("Brick", 170, 2, 7, 8);

        Add("Brick", 177, 2, 10, 1);

        Add("Brick", 190, 2, 50, 11);

        this.AddHoriImage(166, 8);

        this.AddPumpImage(168, 2, 6);

        this.AddCheckPoints_InsidePumpHorzontally(166, 9, 13, new Point(3, 4));

        this.AddEnemyMushroom(13, 12);
        this.AddEnemyMushroom(14, 12);
        this.AddEnemyMushroom(29, 11);

        this.AddTurtle(41, 11);
        this.AddTurtle(43, 11);
        this.AddTurtle(53, 11);
        this.AddEnemyMushroom(60, 12);
        this.AddEnemyMushroom(63, 12);

        this.AddEnemyMushroom(73, 4);

        this.AddEnemyMushroom(77, 8);
        this.AddEnemyMushroom(79, 8);

        this.AddEnemyMushroom(96, 12);
        this.AddEnemyMushroom(97, 12);
        this.AddEnemyMushroom(98, 12);

        this.AddEnemyMushroom(112, 12);

        this.AddEnemyMushroom(134, 10);
        this.AddEnemyMushroom(136, 8);

        this.AddBank(29, 8);

        this.AddBrickWithMushroom(150, 8);

        this.AddPump(103, 10, 3);

        this.AddPump(109, 9, 4);

        this.AddPump(115, 11, 2);

        this.AddLiftDown(140, 0);

        this.AddLiftDown(140, 7);

        this.AddLiftDown(140, 15);

        this.AddLiftUP(155, 0);

        this.AddLiftUP(155, 7);

        this.AddLiftUP(155, 15);

        this.AddCheckPoints_InsidePumpvertically(103, 10, 'b', new Point(2, 3));

        this.AddEnemyTurtlePatrol(145, 12, 7);

        this.AddPumpWarp(178, 10, 3);
        this.AddCheckPoints_InsidePumpvertically(178, 10, 41, new Point(2, 3));
        this.AddPumpWarp(182, 10, 3);
        this.AddCheckPoints_InsidePumpvertically(182, 10, 31, new Point(2, 3));
        this.AddPumpWarp(186, 10, 3);
        this.AddCheckPoints_InsidePumpvertically(186, 10, 21, new Point(2, 3));














    }
}
