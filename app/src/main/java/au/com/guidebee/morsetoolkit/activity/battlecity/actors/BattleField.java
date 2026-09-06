/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
//--------------------------------- PACKAGE ------------------------------------
package au.com.guidebee.morsetoolkit.activity.battlecity.actors;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.GameEngine;
import com.guidebee.game.microedition.TiledLayer;
import com.guidebee.utils.StringBuilder;

import au.com.guidebee.morsetoolkit.activity.battlecity.LedLetters;
import au.com.guidebee.morsetoolkit.activity.battlecity.ResourceManager;
import au.com.guidebee.morsetoolkit.activity.battlecity.actors.tank.EnemyTank;
import au.com.guidebee.morsetoolkit.activity.battlecity.actors.tank.PlayerTank;
import au.com.guidebee.morsetoolkit.helper.MorseHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

//[------------------------------ MAIN CLASS ----------------------------------]


/**
 * This class defines the battle field for the game.
 * <p/>
 *
 * @author James Shen
 */
public class BattleField extends TiledLayer {
    /**
     * No direction.
     */
    public static final int NONE = -1;

    /**
     * Heading north.
     */
    public static final int NORTH = 0;

    /**
     * Heading east.
     */
    public static final int EAST = 1;

    /**
     * Heading south.
     */
    public static final int SOUTH = 2;

    /**
     * Heading west.
     */
    public static final int WEST = 3;

    /**
     * Snow tile type.
     */
    private static final int SNOW = 1;

    /**
     * Brick wall tile type.
     */
    private static final int BRICK_WALL = 2;

    /**
     * Forest tile type.
     */
    private static final int FOREST = 3;

    /**
     * Concrete wall tile type.
     */
    private static final int CONCRETE_WALL = 6;

    /**
     * water animation.
     */
    private static final int WATER1 = 4;


    /**
     * water animation.
     */
    private static final int WATER2 = 5;

    private int waterFramesIndex = -1;

    /**
     * water animation frames.
     */
    private static int[][] waterFrames = {{4, 5}, {5, 4}};

    /**
     * default number of tiles in each direction.
     */
    private static final int NUMBER_IN_TILES = 26;
    /**
     * the number of horizontal tiles
     */
    public static int WIDTH_IN_TILES = 26;
    /**
     * the number of vertical tiles.
     */
    private static int HEIGHT_IN_TILES = 26;

    /**
     * Random used to create randome position pair for powerups.
     */
    private static Random rnd = new Random();

    /**
     * where enemy tanks appears ,left, middle, right
     */
    private static int[][] enemyPos = new int[3][2];

    /**
     * tick used to control the water animation speed.
     */
    private int tickCount = 0;

    /**
     * change enemy tank apprears position in sequence.
     */
    private static int nextEnemyPos = 0;

    /**
     * The player's home became concrete wall time.
     */
    private long concreteWallStartTime = 0;

    /**
     * how long player's home concrete wall can be
     */
    private static long concreteWallPeriod = 30000;


    private static int leftLetterArea = 24;
    /**
     * Constructor used to create a battle fields.
     *
     * @param xTiles the number of tiles in width.
     * @param yTiles the number of tiles in height.
     */
    public BattleField(int xTiles, int yTiles) {
        //When read from file, each number stand for 2X2 tiles
        super(xTiles * 2, yTiles * 2, ResourceManager.getInstance().getTileImage(),
                ResourceManager.TILE_WIDTH / 2, ResourceManager.TILE_WIDTH / 2);
        createAnimatedTile(waterFrames[0][0]); // tile -1
        createAnimatedTile(waterFrames[1][0]); // tile -2
        WIDTH_IN_TILES = xTiles * 2;
        HEIGHT_IN_TILES = yTiles * 2;
        if (xTiles * 2 < NUMBER_IN_TILES || xTiles * 2 < NUMBER_IN_TILES) {
            throw new IllegalArgumentException("Tiles shall be greater than 13");
        }
        //Initialized array which stores enemy appears start position.
        //Left
        enemyPos[0][0] = leftLetterArea*ResourceManager.TILE_WIDTH;
        enemyPos[0][1] = (yTiles - 1) * ResourceManager.TILE_WIDTH;
        //Middle
        enemyPos[1][0] = xTiles / 2 * ResourceManager.TILE_WIDTH;
        enemyPos[1][1] = (yTiles - 1) * ResourceManager.TILE_WIDTH;
        //Right
        enemyPos[2][0] = (xTiles - 1) * ResourceManager.TILE_WIDTH;
        enemyPos[2][1] = (yTiles - 1) * ResourceManager.TILE_WIDTH;

    }

    /**
     * Check if given rectangle contains impassable area.
     *
     * @param x      x coordinate.
     * @param y      y coordinate.
     * @param width  the width of given area.
     * @param height the height of given area.
     * @return true contains impassable area.
     */
    public boolean containsImpassableArea(int x, int y, int width, int height) {
        int TILE_WIDTH = ResourceManager.TILE_WIDTH / 2;
        int rowMin = y / TILE_WIDTH;
        int rowMax = (y + height - 1) / TILE_WIDTH;
        if (rowMax >= HEIGHT_IN_TILES) {
            rowMax = HEIGHT_IN_TILES - 1;
        }
        int columnMin = x / TILE_WIDTH;
        if (x < 0 || y < 0 || columnMin > WIDTH_IN_TILES - 1 ||
                rowMin > HEIGHT_IN_TILES - 1) {
            return true;
        }
        rowMin = Math.min(rowMin, getRows() - 1);
        columnMin = Math.min(columnMin, getColumns() - 1);
        int columnMax = (x + width - 1) / TILE_WIDTH;
        if (columnMax >= WIDTH_IN_TILES) {
            columnMax = WIDTH_IN_TILES - 1;
        }
        for (int row = rowMin; row <= rowMax; ++row) {
            for (int column = columnMin; column <= columnMax; ++column) {
                int cell = getCell(column, row);
                if ((cell < 0) || (cell == BRICK_WALL)
                        || (cell == CONCRETE_WALL)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * check if one snow filed, if on snow, the tank move a bit faster.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @return true on the snow field.
     */
    public boolean isOnSnow(int x, int y) {
        int TILE_WIDTH = ResourceManager.TILE_WIDTH / 2;
        int row = y / TILE_WIDTH;
        int column = x / TILE_WIDTH;
        if (x < 0 || y < 0 || column > WIDTH_IN_TILES - 1 ||
                row > HEIGHT_IN_TILES - 1) {
            return false;
        }
        row = Math.min(row, getRows() - 1);
        column = Math.min(column, getColumns() - 1);
        int cell = getCell(column, row);
        return cell == SNOW;
    }

    /**
     * Check if given point hit wall in the battle field. If hits wall, the wall
     * will be destoryed if with enough strength.
     *
     * @param x        x coordinate.
     * @param y        y coordinate.
     * @param strength the strength of the the hitting object.
     * @return true hit the wall.
     */
    public boolean hitWall(int x, int y, int strength) {
        boolean bRet = false;
        int TILE_WIDTH = ResourceManager.TILE_WIDTH /2;
        int[] col = new int[6];
        int[] row = new int[6];
        int maxRows = getRows() - 1;
        int maxCols = getColumns() - 1;

        for(int i=0;i<6;i++){
            for(int j=0;j<6;j++){
                col[i] = Math.min( (x - TILE_WIDTH*3/4  +i*TILE_WIDTH/4) / TILE_WIDTH, maxCols);
                row[j] = Math.min((y - TILE_WIDTH*3/4  + j*TILE_WIDTH/4) / TILE_WIDTH, maxRows);
            }
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int cell = getCell(col[i], row[j]);
                if (cell == BRICK_WALL && strength > 0) {
                    setCell(col[i], row[j], 0);
                    bRet = true;
                } else if (cell == CONCRETE_WALL) {
                    if (strength > Bullet.GRADE_DEFAULT)
                        setCell(col[i], row[j], 0);
                    bRet = true;
                } else if (cell == FOREST || cell < 0 || cell == SNOW) {
                    //here a bullet can destory water, snow field and forest
                    //which is unrealistic:) just for fun.
                    if (strength > Bullet.GRADE_BREAK_CONCRETE_WALL) {
                        setCell(col[i], row[j], 0);
                        bRet = true;
                    }
                }
            }
        }
        return bRet;
    }

    /**
     * Initialize the Enemy's start position.
     *
     * @param tank the Enemy's tank.
     */
    public void initEnemyTankPos(EnemyTank tank) {
        nextEnemyPos %= 3;
        int x = enemyPos[nextEnemyPos][0];
        int y = enemyPos[nextEnemyPos][1];
        tank.setPosition(x, y);
        nextEnemyPos++;
    }

    /**
     * duplicate adjacent cell with given value. The reason for this ,the width
     * for each image tile is 6X6 ,when design the battle fields, for simplicity
     * we combine adjacent 2X2 cell to stand for a 12X12 area,i.e the 4 cells
     * store the same value.
     *
     * @param x     the x index of the cell
     * @param y     the y index of the cell
     * @param value the value for the cell
     */
    private void duplicateCell(int x, int y, int value) {
        int maxCols = getColumns() - 1;
        int maxRows = getRows() - 1;
        if (x < 0 || x > maxCols || y < 0 || y > maxRows)
            return;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                setCell(x + i, y + j, value);
            }
        }
    }

    private void setSingleCell(int x, int y, int value) {
        int maxCols = getColumns() - 1;
        int maxRows = getRows() - 1;
        if (x < 0 || x > maxCols || y < 0 || y > maxRows)
            return;
        setCell(x , y , value);
    }


    /**
     * Read the battle from an input stream.
     *
     * @param is input stream stores the battle field information.
     */
    public synchronized void initBattlefield(java.io.InputStream is)
            throws IOException {
        Random rnd = new Random();
        drawLeftArea();
        drawRandomArea(rnd);
        try {
            if (is != null) {
                readBattlefieldSingleCell(is);
            }
        } catch (Exception e) {
        }
        makeHomeBrickWall();
    }

    private void drawRandomArea(Random rnd) {
        //Clear the whole play area to the right of the morse-code display
        //first (it used to only be filled near the bottom, leaving most of
        //the field an empty void), then scatter obstacle clusters on a
        //coarse grid so tanks and bullets always keep clear corridors
        //between them instead of the field reading as pure noise.
        int startX = leftLetterArea + 4;
        int endX = WIDTH_IN_TILES;
        int endY = HEIGHT_IN_TILES;
        for (int i = startX; i < endX; i += 2) {
            for (int j = 0; j < endY; j += 2) {
                duplicateCell(i, j, 0);
            }
        }

        //3 full tiles between cluster anchors leaves room to drive around
        //them; the loop bounds also keep both the home approach (low j)
        //and the enemy spawn row (near endY) clear of clutter.
        int clusterStride = 6;
        for (int j = clusterStride; j < endY - 2; j += clusterStride) {
            for (int i = startX; i < endX - 2; i += clusterStride) {
                if (rnd.nextInt(100) >= 45) continue;
                int roll = rnd.nextInt(100);
                int tile;
                if (roll < 55) {
                    tile = BRICK_WALL;
                } else if (roll < 75) {
                    tile = FOREST;
                } else if (roll < 92) {
                    tile = -1 - ((i ^ j) & 1); //water
                } else if (roll < 97) {
                    tile = SNOW;
                } else {
                    tile = CONCRETE_WALL;
                }
                duplicateCell(i, j, tile);
                //grow most clusters by one tile so cover reads as a
                //deliberate obstacle rather than a single random speck.
                if (tile != CONCRETE_WALL && rnd.nextBoolean()) {
                    int di = rnd.nextBoolean() ? 2 : 0;
                    int dj = di == 0 ? 2 : 0;
                    duplicateCell(i + di, j + dj, tile);
                }
            }
        }
    }

    private void drawLeftArea() {

        char letter=generateRandomLetter();
        for (int i = 0; i < leftLetterArea; i += 1) {
            for (int j = 0; j < HEIGHT_IN_TILES; j += 1) {
                setSingleCell(i, j, -1 - ((i ^ j) & 1));
            }
        }
        for (int i = 1; i < leftLetterArea-1; i += 1) {
            for (int j = 1; j < HEIGHT_IN_TILES-1; j += 1) {
                setSingleCell(i, j, CONCRETE_WALL);
            }
        }

        for (int i = 2; i < leftLetterArea-2; i += 1) {
            for (int j = 2; j < HEIGHT_IN_TILES-2; j += 1) {
                setSingleCell(i, j, 0);
            }
        }

        String dotMatrix=LedLetters.LedDotMatrices.get(letter);
        int x0 = (leftLetterArea-16)/2;
        int y0 = (HEIGHT_IN_TILES-16)/2-2;
        int x = 0, y = 0;
        for(int i=0;i<dotMatrix.length();i++){
            char c=dotMatrix.charAt(i);
            switch(c){
                case ' ':
                case '0':
                    //setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), 0);
                    x += 1;
                    break;
                case '1':
                    setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), CONCRETE_WALL);
                    x += 1;
                    break;
                case '\n'://new line
                    y += 1;
                    x = 0;
                    break;
                default:
                    break;
            }
        }

        String morse= MorseHelper.morseCodeData.get(letter);
        morse=morse.replace(".","60").replace("-","6660");

        int length1=morse.length();
        int left=(16-length1)/2;
        for(int i=0;i<left;i++){
            morse="0"+morse+"0";
        }
        y+=2;
        for(int i=0;i<morse.length();i++){
            char c=morse.charAt(i);
            switch(c){
                case ' ':
                case '0':
                    //setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), 0);
                    x += 1;
                    break;
                case '6':
                    setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), CONCRETE_WALL);
                    x += 1;
                    break;
                case '\n'://new line
                    y += 1;
                    x = 0;
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * Make home wall a brick wall.
     */
    private void makeHomeBrickWall() {
        //Draw the player's home area.
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                duplicateCell((WIDTH_IN_TILES - 2) / 2-i, j, 0);
            }
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                setCell(i + (WIDTH_IN_TILES / 2 - 3), j, BRICK_WALL);
            }
        }
        //this place will be placed with player's flag.
        duplicateCell((WIDTH_IN_TILES - 2) / 2, 0, 0);
        concreteWallStartTime = 0;
    }


    /**
     * Initialize the player's start position.
     *
     * @param tank the player's tank.
     */
    public void initPlayerTankPos(PlayerTank tank) {
        int x = (WIDTH_IN_TILES / 2 - 3) * ResourceManager.TILE_WIDTH / 2 - ResourceManager.TILE_WIDTH;
        int y = 0;
        //this place will be placed with player's tank.
        duplicateCell(x * 2 / ResourceManager.TILE_WIDTH,
                y * 2 / ResourceManager.TILE_WIDTH, 0);
        tank.setPosition(x, y);
        EnemyTank.explodeNearHomeEnemies();

    }


    /**
     * Initialize the powerup's random start position.
     *
     * @param powerup the powerup object.
     */
    public void initPowerupPos(Powerup powerup) {
        if (powerup.getType() == Powerup.HOME ||
                powerup.getType() == Powerup.HOME_DESTROYED) {
            int x = (WIDTH_IN_TILES / 4) * ResourceManager.TILE_WIDTH ;
            int y = 0;
            powerup.setPosition(x, y);
        } else {
            int x0 = (WIDTH_IN_TILES / 4) * ResourceManager.TILE_WIDTH;
            int y0 = (HEIGHT_IN_TILES / 2 - 1) * ResourceManager.TILE_WIDTH;
            int x = (Math.abs(rnd.nextInt()) % (WIDTH_IN_TILES / 2-leftLetterArea)+leftLetterArea)
                    * ResourceManager.TILE_WIDTH;
            int y = (Math.abs(rnd.nextInt()) % (WIDTH_IN_TILES / 2)-2)
                    * ResourceManager.TILE_WIDTH;
            //aovid the home cell.
            while (x == x0 && y == y0) {
                x = (Math.abs(rnd.nextInt()) % (WIDTH_IN_TILES / 2-leftLetterArea)+leftLetterArea)
                        * ResourceManager.TILE_WIDTH;
                y = (Math.abs(rnd.nextInt()) % (WIDTH_IN_TILES / 2)-2)
                        * ResourceManager.TILE_WIDTH;

            }
            powerup.setPosition(x, y);
        }

    }


    /**
     * Make home wall a concrete wall.
     */
    public void makeHomeConcreteWall() {
        //Draw the player's home area.
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                setCell(i + (WIDTH_IN_TILES / 2 - 3), j, CONCRETE_WALL);
            }
        }
        //this place will be placed with player's flag.
        duplicateCell((WIDTH_IN_TILES - 2) / 2, 0, 0);
        concreteWallStartTime = System.currentTimeMillis();
    }


    private char generateRandomLetter(){
        Random random=new Random();
        int letterIndex=random.nextInt(26) %26;
        char letter=(char)('a'+letterIndex);
        if(letter=='w') letter='a';
        return  letter;
    }

    public void readBattlefieldFromLedLetter(){
        try {
            char letter1=generateRandomLetter();
            char letter2=generateRandomLetter();
            char replaceLetter='2';
            String dotMatrixString1 = LedLetters.LedDotMatrices.get(letter1);
            String dotMatrixString2 = LedLetters.LedDotMatrices.get(letter2);
            StringBuilder stringBuilder=new StringBuilder();
            String [] lines1 = dotMatrixString1.split("\n");
            String [] lines2 = dotMatrixString2.split("\n");
            for(int i=0;i<lines1.length;i++){
                stringBuilder.append(lines1[i]+"00"+lines2[i]+"\n");
            }

            String morse1= MorseHelper.morseCodeData.get(letter1);
            String morse2= MorseHelper.morseCodeData.get(letter2);
            morse1=morse1.replace(".","60").replace("-","6660");
            int length1=morse1.length();
            int left=(16-length1)/2;
            for(int i=0;i<left;i++){
                morse1="0"+morse1+"0";
            }

            morse2=morse2.replace(".","60").replace("-","6660");
            int length2=morse2.length();
            int right=(16-length2)/2;
            for(int i=0;i<right;i++){
                morse2="0"+morse2+"0";
            }
            stringBuilder.append("00\n");
            stringBuilder.append("00\n");
            stringBuilder.append(morse1+"00"+morse2+"\n");
            String dotMatrixString=stringBuilder.toString();

            dotMatrixString = dotMatrixString.replace('1', replaceLetter);
            InputStream is = new ByteArrayInputStream(dotMatrixString.getBytes());
            initBattlefield(is);
        }catch (Exception e){
            //ignore
        }
    }


    /**
     * read battle field from HZK.
     *
     * @param gameLevel the current game level.
     */
    public void readBattlefieldFromHZK(int gameLevel) {
        InputStream is = GameEngine.files.internal("hzk12").read();
        int[] buffer = new int[24];
        int[] hzData = new int[13 * 13 + 4];
        int index = 0;
        try {
            is.skip(gameLevel * 24);
            for (int i = 0; i < 24; i++) buffer[i] = is.read();
            int tempChar;
            int tempBit;
            index = 0;
            for (int i = 0; i < 12; i++) {
                for (int j = 0; j < 2; j++) {
                    tempChar = buffer[i * 2 + j];
                    tempBit = 0x80;
                    for (int k = 0; k < 8; k++) {
                        if (j == 1 && k > 3) break;
                        if ((tempBit & tempChar) != 0) {
                            hzData[index++] = '2';
                        } else {
                            hzData[index++] = '0';
                        }
                        tempBit = tempBit >> 1;
                    }
                }
                hzData[index++] = '0';
                hzData[index++] = '\n';
            }
            byte[] byteArray = new byte[hzData.length];
            for (int i = 0; i < byteArray.length; i++) {
                byteArray[i] = (byte) hzData[i];
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);

            initBattlefield(bais);
        } catch (Exception e) {
            //inglore the exception.
        }
    }

    /**
     * Read the battle from an input stream.
     *
     * @param is input stream stores the battle field information.
     */
    private void readBattlefield(java.io.InputStream is) throws IOException {
        int c = -1;
        int x0 = (WIDTH_IN_TILES - NUMBER_IN_TILES+leftLetterArea) / 2;
        int y0 = (HEIGHT_IN_TILES - NUMBER_IN_TILES) / 2;
        int x = 0, y = 0;
        while ((c = is.read()) != -1 && y < NUMBER_IN_TILES) {
            switch (c) {
                case ' '://empty
                case '0':
                    duplicateCell(x + x0, HEIGHT_IN_TILES - (y + y0), 0);
                    x += 2;
                    break;
                case '1'://snow field
                    duplicateCell(x + x0, HEIGHT_IN_TILES - (y + y0), SNOW);
                    x += 2;
                    break;
                case '2'://brick wall
                    duplicateCell(x + x0, HEIGHT_IN_TILES - (y + y0), BRICK_WALL);
                    x += 2;
                    break;
                case '3'://forest
                    duplicateCell(x + x0, HEIGHT_IN_TILES - (y + y0), FOREST);
                    x += 2;
                    break;
                case '4':
                case '5'://water
                    duplicateCell(x + x0, HEIGHT_IN_TILES - (y + y0), -1 - ((x ^ y) & 1));
                    x += 2;
                    break;
                case '6': //Concrete wall
                    duplicateCell(x + x0, HEIGHT_IN_TILES - (y + y0), CONCRETE_WALL);
                    x += 2;
                    break;
                case '\n'://new line
                    y += 2;
                    x = 0;
                    break;
                default:
            }
        }
    }

    /**
     * Read the battle from an input stream.
     *
     * @param is input stream stores the battle field information.
     */
    private void readBattlefieldSingleCell(java.io.InputStream is) throws IOException {
        int c = -1;
        int x0 = 8+leftLetterArea;
        int y0 = 4;
        int x = 0, y = 0;
        while ((c = is.read()) != -1 && y < NUMBER_IN_TILES*2) {
            switch (c) {
                case ' '://empty
                case '0':
                    setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), 0);
                    x += 1;
                    break;
                case '1'://snow field
                    setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), SNOW);
                    x += 1;
                    break;
                case '2'://brick wall
                    setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), BRICK_WALL);
                    x += 1;
                    break;
                case '3'://forest
                    setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), FOREST);
                    x += 1;
                    break;
                case '4':
                case '5'://water
                    setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), -1 - ((x ^ y) & 1));
                    x += 1;
                    break;
                case '6': //Concrete wall
                    setSingleCell(x + x0, HEIGHT_IN_TILES - (y + y0), CONCRETE_WALL);
                    x += 1;
                    break;
                case '\n'://new line
                    y += 1;
                    x = 0;
                    break;
                default:
            }
        }
    }

    private float animatedTime = 0;

    @Override
    public void act(float alpha) {
        animatedTime += alpha;
        if (animatedTime > 0.5f) {
            animatedTime = 0;
            if (getAnimatedTile(waterFramesIndex) == WATER1) {
                setAnimatedTile(waterFramesIndex, WATER2);
            } else {
                setAnimatedTile(waterFramesIndex, WATER1);
            }
        }
        int tickState = (tickCount++ >> 3); // slow down x8
        int tile = tickState % 2;
        setAnimatedTile(-1 - tile, waterFrames[tile][(tickState % 4) / 2]);
        if(concreteWallStartTime>0){
            long tickTime=System.currentTimeMillis();
            if(tickTime-concreteWallStartTime>concreteWallPeriod){
                makeHomeBrickWall();
                concreteWallStartTime=0;
            }
        }
    }

}
