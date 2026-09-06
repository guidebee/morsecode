package Levels;

import CheckPoint.CheckPoints;
import CheckPoint.ClowdGoUP_CheckPoint;
import CheckPoint.Clowd_CheckPoint;
import CheckPoint.InsidePumpHorzontally;
import CheckPoint.InsidePumpvertically;
import CheckPoint.Princess;
import CheckPoint.WhyYouDOThis;
import Gears.Construct;
import Teleport.Teleport;
import java.awt.Point;
import java.util.Random;

public abstract class BasicLevel {

    public int LevelLength;
    // Nothing , Water , Wall ,overWater , Night , Snow , NightAndSnow , Castle
    public String BackGroundColor;
    // Not Set , 400 , 300 , 200
    public String Time;
    // GreenAndTrees , OrangeAndMushroom , Guns , Clouds 
    public String type;
    public Point pos;
    // Nothing$ , Clouds$$Day::Night , Mountain$ ,Fence$$Day::Night // 3 Pages Loop
    public String BackGroundImage;  // DONE
    //Sea , Ground ,UnderGround , Castle 
    // here Bricks will change Skin and Enemy too..
    // 1 brick 4 Skin :Brown,Blue,Grew,Green
    // Clowd for bonus sky level
    public String attribute;
    public boolean Bombs = false;
    public int BombsTurnOff = -1;
    public boolean FlyingFishes = false;
    public int FlyingFishesLength = -1;
    public Construct[] constructors;
    CheckPoints[] checkPoints;
    Teleport[] teleport;
    public int Items = 0;
    public int checkPoints_Amount = 0;
    public int Teleport_Items = 0;
    // normal means pump which are not orange and has plants
    // "OrangePump" = no plants in it and orange color
    public String level_Name = "normal";

    public BasicLevel() {
        constructors = new Construct[5000];;
        checkPoints = new CheckPoints[20];
        teleport = new Teleport[20];
    }

    public int Get_CheckPoints_Amount() {

        return constructors.length;

    }

    public CheckPoints get_CheckPoints_Number(int i) {

        return checkPoints[i];

    }

    public Teleport get_Teleport_Number(int i) {
        return teleport[i];
    }

    public int Get_Items_Amount() {

        return constructors.length;

    }

    public Construct get_Item_Number(int i) {

        return constructors[i];

    }

    public void AddCheckPoints(int x, int y, int nextlevel, Point LOC) {

        CheckPoints tempcheckPoints = new CheckPoints((x * 32) - 4, y * 32, nextlevel, LOC);

        checkPoints[this.checkPoints_Amount] = tempcheckPoints;
//        // System.out.println(Items);

        checkPoints_Amount++;

    }

    public void AddWhyYouDOThis(int x, int y, int nextlevel, Point LOC) {

        WhyYouDOThis tempcheckPoints = new WhyYouDOThis(x * 32, y * 32, nextlevel, LOC);

        checkPoints[this.checkPoints_Amount] = tempcheckPoints;
//        // System.out.println(Items);

        checkPoints_Amount++;

    }

    public void AddPrincess(int x, int y) {

        Princess princess = new Princess(x * 32, y * 32, 11, new Point(3, 6));

        checkPoints[this.checkPoints_Amount] = princess;
//        // System.out.println(Items);

        checkPoints_Amount++;

    }

    public void AddClowd_CheckPoints(int x, int y, int level, Point point) {

        Clowd_CheckPoint clowd = new Clowd_CheckPoint(x * 32, y * 32, level, point);

        checkPoints[this.checkPoints_Amount] = clowd;
//        // System.out.println(Items);

        checkPoints_Amount++;

    }

    public void AddClowdGoUP_CheckPoints(int x, int y, int level, Point point) {

        ClowdGoUP_CheckPoint clowdfoup = new ClowdGoUP_CheckPoint(x * 32, (y * 32) + 16, level, point);

        checkPoints[this.checkPoints_Amount] = clowdfoup;
//        // System.out.println(Items);

        checkPoints_Amount++;

    }

    public void Add(String type, int x, int y, int LenX, int LenY, int Length) {

        Construct temp = new Construct();
        temp.setItem_Type(type);
        temp.setX(x);
        temp.setY(y);
        temp.setLength_X(LenX);
        temp.setLength_Y(LenY);
        temp.setPatrolLength(Length);

        constructors[Items] = temp;
//        // System.out.println(Items);

        Items++;

    }

    public void Add(String type, int x, int y, int LenX, int LenY) {

        Construct temp = new Construct();
        temp.setItem_Type(type);
        temp.setX(x);
        temp.setY(y);
        temp.setLength_X(LenX);
        temp.setLength_Y(LenY);

        constructors[Items] = temp;
//        // System.out.println(Items);

        Items++;

    }

    public void Add(String type, int x, int y, int LenX, int LenY, String rotation) {
        Construct temp = new Construct();
        temp.setItem_Type(type);
        temp.setX(x);
        temp.setY(y);
        temp.setLength_X(LenX);
        temp.setLength_Y(LenY);
        temp.ExtraInfo = rotation;

        constructors[Items] = temp;
//        // System.out.println(Items);

        Items++;

    }

    public void AddTeleport(int INx, int INy, int OUTx, int OUTy) {
        Teleport temp = new Teleport();
        temp.setINx(INx * 32);
        temp.setINy(INy * 32);
        temp.setOUTx(OUTx * 32);
        temp.setOUTy(OUTy * 32);
        System.out.println(Items);
        teleport[Teleport_Items] = temp;

        Teleport_Items++;

    }

    public void AddCastle(int x, int y, String size) {
        if ("small".equals(size)) {
            Add("SmallCastle", x, y, 1, 1);
        } else if ("big".equals(size)) {
            Add("BigCastle", x, y, 1, 1);
        }
    }

    // x , y , height , EndAt
    public void AddBrick(int x, int y, int height, int EndAt) {

        Add("Brick", x, y, EndAt - x, height);

    }

    public void AddQuestionMark(int x, int y, int height, int EndAt) {
        Add("QuestionMark", x, y, EndAt - x, height);
    }

    public void AddQuestionMark(int x, int y) {
        Add("QuestionMark", x, y, 1, 1);
    }

    public void AddBrickWith1UP(int x, int y) {
        Add("BrickWith1UP", x, y, 1, 1);
    }

    public void AddBrickWithCoin(int x, int y) {
        Add("BrickWithCoin", x, y, 1, 1);
    }

    public void AddEnemyMushroom(int x, int y) {
        Add("EnemyMushroom", x, y, 1, 1);
    }

    // x , y , height , EndAt
    public void AddWall(int x, int y, int height, int EndAt) {

        Add("Wall", x, y, EndAt - x, height);

    }

    public void AddRocketLauncher(int x, int y, int height) {
        Add("RocketLauncher", x, y, 1, height);
    }

    public void AddFlyingTurtle(int x, int y) {
        Add("FlyingTurtle", x, y, 1, 1);
    }

    public void AddPump(int x, int y, int height) {
        Add("pump", x, y, 1, height);
    }

    public void AddPumpWarp(int x, int y, int height) {
        Add("PumpWarp", x, y, 1, height);
    }

    public void AddChocolate(int x, int y) {
        Add("chocolate", x, y, 1, 1);
    }

    public void AddChocolate(int x, int y, int height, int EndAt) {
        Add("chocolate", x, y, EndAt - x, height);
    }

    public void AddBrick(int x, int y) {
        Add("Brick", x, y, 1, 1);
    }

    public void AddBrickWithMushroom(int x, int y) {
        Add("BrickWithMushroom", x, y, 1, 1);
    }

    public void AddMonkey(int x, int y) {
        Add("Monkey", x, y, 1, 1);
    }

    public void AddTurtle(int x, int y) {
        Add("EnemyTurtle", x, y, 1, 1);
    }

    public void AddHelmet(int x, int y) {
        Add("Helmet", x, y, 1, 1);
    }

    public void AddBank(int x, int y) {
        Add("Bank", x, y, 1, 1);
    }

    public void AddStone(int x, int y, int height, int EndAt) {
        Add("stone", x, y, EndAt - x, height);
    }

    public void AddFireBar(int x, int y, int size) {

        if (size == 6) {
            Add("FireBar", x, y, 1, 1, "CW");
        } else if (size == 12) {
            Add("BigFireBar", x, y, 1, 1, "CW");
        }

    }

    public void AddFireBar(int x, int y, int size, String rotate) {
        // clockwisw
        if (size == 6) {
            Add("FireBar", x, y, 1, 1, rotate);
        } else if (size == 12) {
            Add("BigFireBar", x, y, 1, 1, rotate);
        }

    }

    public void AddSonOfABuitch(int x, int y) {
        Add("SonOfABuitch", x, y, 1, 1);
    }

    public void AddBouncer(int x, int y) {
        Add("Bouncer", x, y + 1, 1, 1);
    }

    public void AddCheckPointsFlag(int x, int y) {
        Add("Flag", x, y, 1, 1);
    }

    public void AddAxe(int x, int y) {
        Add("Axe", x, y, 1, 1);
    }

    public void AddWhyYouDOThis(int x, int y) {
        Add("WhyYouDOThis", x, y, 1, 1);
    }

    public void AddBoss(int x, int y, int maxx) {
        Add("Boss", x, y, 1, 1, maxx);
    }

    public void AddBossHammer(int x, int y, int maxx) {
        Add("BossHammer", x, y, 1, 1, maxx);
    }

    public void AddBridgeBloks(int x, int y, int height, int EndAt) {
        Add("BridgeBloks", x, y, EndAt - x, height);
    }

    public void AddInvisibleBrckWith1Up(int x, int y) {
        Add("InvisibleBrckWith1Up", x, y, 1, 1);
    }

    public void AddBrickWithStar(int x, int y) {
        Add("BrickWithStar", x, y, 1, 1);
    }

    public void AddQuestionMarkWithMushroom(int x, int y) {
        Add("QuestionMarkWithMushroom", x, y, 1, 1);
    }

    public void AddCoin(int x, int y) {
        Add("Coin", x, y, 1, 1);
    }

    public void AddCoin(int x, int y, int height, int EndAt) {
        Add("Coin", x, y, EndAt - x, height);
    }

//    public void AddCoinSimple(int x, int y, int height, int EndAt) {
//        Add("Coin" , x , y , EndAt- x , height) ;
//    }
    public void AddIron(int x, int y) {
        Add("Iron", x, y, 1, 1);
    }

    public void AddInvisibleBrckWithCoin(int x, int y) {
        Add("InvisibleBrckWithCoin", x, y, 1, 1);
    }

    public void AddFire(int x, int y) {
        Add("BossFire", x, y, 1, 1);
    }

    public void AddLava(int x, int Length) {
        Add("Lava", x, 13, Length - x, 1);
    }

    public void AddLavaUP(int x, int Length) {
        Add("Lava", x, 12, Length - x, 1);
    }

    public void AddHoriImage(int x, int y) {
        Add("HoriImage", x, y, 1, 1);
    }

    public void AddPumpImage(int x, int y, int height) {
        Add("PumpImage", x, y, 1, height);
    }

    public void AddCheckPoints_InsidePumpHorzontally(int x, int y, int nextlevel, Point point) {

        InsidePumpHorzontally tempcheckPoints = new InsidePumpHorzontally(x * 32 - 4, y * 32, nextlevel, point);

        checkPoints[this.checkPoints_Amount] = tempcheckPoints;
//        // System.out.println(Items);

        checkPoints_Amount++;

    }

    public void AddCheckPoints_InsidePumpvertically(int x, int y, int nextlevel, Point point) {

        InsidePumpvertically tempcheckPoints = new InsidePumpvertically((x * 32) + 16, y * 32 - 4, nextlevel, point);

        checkPoints[this.checkPoints_Amount] = tempcheckPoints;
//        // System.out.println(Items);

        checkPoints_Amount++;

    }

    public void AddLift(int x, int y) {
        Add("Lift", x, y, 1, 1, 6);
    }

    public void AddEnemyTurtlePatrol(int x, int y, int Length) {
        Add("EnemyTurtlePatrol", x, y, 1, 1, Length);
    }

    public void AddFlyingTurtlePatrol(int x, int y, int Length) {
        Add("FlyingTurtlePatrol", x, y, 1, 1, Length);
    }

    public void AddLift_UpDown(int x, int y) {
        Add("Lift_UpDown", x, y, 1, 1, 6);
    }

    public void AddLift_UpDown(int x, int y, int Points) {
        Add("Lift_UpDown", x, y, 1, 1, Points);
    }

    public void AddLift_LeftRight(int x, int y) {
        Add("Lift_LeftRight", x, y, 1, 1, 6);
    }

    public void AddLift_LeftRight(int x, int y, int Points) {
        Add("Lift_LeftRight", x, y, 1, 1, Points);
    }

    public void AddLift_LeftRightInvert(int x, int y) {
        Add("Lift_LeftRightInvert", x, y, 1, 1, 6);
    }

    public void AddLift_LeftRightInvert(int x, int y, int Points) {
        Add("Lift_LeftRightInvert", x, y, 1, 1, Points);
    }

    public void AddLiftUP(int x, int y) {
        Add("LiftUP", x, y, 1, 1, 6);
    }

    public void AddLiftUP(int x, int y, int Points) {
        Add("LiftUP", x, y, 1, 1, Points);
    }

    public void AddLiftDown(int x, int y) {
        Add("LiftDown", x, y, 1, 1, 6);
    }

    public void AddLiftDown(int x, int y, int Points) {
        Add("LiftDown", x, y, 1, 1, Points);
    }

    public void AddLiftCar(int x, int y) {
        Add("LiftCar", x, y, 1, 1, 6);
    }

    public void AddLiftFall(int x, int y) {
        Add("LiftFall", x, y, 1, 1, 6);
    }

    public void AddLiftFall(int x, int y, int Points) {
        Add("LiftFall", x, y, 1, 1, Points);
    }

    public void AddStone(int x, int y) {
        Add("stone", x, y, 1, 1);
    }

    public void AddOctoPussy(int x, int y) {
        Add("OctoPussy", x, y, 1, 1);
    }

    public void AddFishGrey(int x, int y) {
        Random r = new Random();
        if (r.nextBoolean()) {
            Add("FishGrey", x, y, 1, 1);
        } else {
            Add("FishGreyUpDown", x, y, 1, 1);
        }
    }

    public void AddFishRed(int x, int y) {
        Random r = new Random();
        if (r.nextBoolean()) {
            Add("FishRed", x, y, 1, 1);
        } else {
            Add("FishRedUpDown", x, y, 1, 1);
        }
    }

    public void AddFishGreyUpDown(int x, int y) {
        Add("FishGreyUpDown", x, y, 1, 1);
    }

    public void AddFishRedUpDown(int x, int y) {
        Add("FishRedUpDown", x, y, 1, 1);
    }

    public void AddTree(int x, int y, int height, int EndAt) {
        Add("tree", x, y, EndAt - x, height);
    }

    public void AddWoodenBridge(int x, int y, int EndAt) {
        Add("WoodenBridge", x, y, EndAt - x, 1);
    }

    public void AddLavaBall(int x) {
        Add("LavaBall", x, 6, 1, 1);
    }

    public void AddWhiteLine(int x) {
        Add("WhiteLine", x, 2, 1, 1);
    }

    public void AddWater(int x, int y, int EndAt) {
        Add("Water", x, y, EndAt - x, 1);
    }

    public void AddBalenceLift(int x, int y, int Distance) {

        Construct temp = new Construct();
        temp.setItem_Type("BalenceLift");
        temp.setX(x);
        temp.setY(y);
        temp.setLength_X(1);
        temp.setLength_Y(1);
        temp.setPatrolLength(1);
        temp.setBridgeLength(Distance);

        constructors[Items] = temp;
//        // System.out.println(Items);

        Items++;

    }
//   
//       public Level_11(int i) throws IOException, ClassNotFoundException {
//        
//        Load_level(i);
//        
//    }
//  
//       
//    public  void Load_level(int i) throws IOException, ClassNotFoundException {
//        
//        ObjectInputStream in = new ObjectInputStream(new FileInputStream(i+".lvl"));
//
//        constructors =  (Construct[]) in.readObject();
//        
//        in.close();
//    }
//    
//    public void Save_Level() throws FileNotFoundException, IOException{
//        int level_no = 11 ;
//        
//        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(level_no+".lvl"));
//        
//        out.writeObject(constructors);
//        out.flush();
//        out.close();
//        
//    }
}
