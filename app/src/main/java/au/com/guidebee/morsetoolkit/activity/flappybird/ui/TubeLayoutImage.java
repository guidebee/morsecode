/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
//--------------------------------- PACKAGE ------------------------------------
package au.com.guidebee.morsetoolkit.activity.flappybird.ui;

//--------------------------------- IMPORTS ------------------------------------

import android.util.Log;

import com.guidebee.game.files.FileHandle;
import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.ui.Image;
import com.guidebee.utils.collections.Array;

import au.com.guidebee.morsetoolkit.activity.flappybird.actor.Numbers;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;
import au.com.guidebee.morsetoolkit.activity.flappybird.state.TubePosition;

import static com.guidebee.game.GameEngine.assetManager;
import static com.guidebee.game.GameEngine.files;


//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Tube design layout image.
 * @author James Shen <james.shen@guidebee.com>
 */
public class TubeLayoutImage extends Image {


    /**
     * Default tube file name.
     */
    private final static String filename = "guidebeeflappybird.tube";
    private final TextureRegion bottomTubeTextRegion;
    private final TextureRegion topTubeTextRegion;
    private final TextureRegion groundTextRegion;
    private final Numbers numbers = new Numbers();
    private final Numbers currentNumbers = new Numbers("levelnum");
    private final DesignWindow designWindow;
    private int offset;
    private int currentTubeIndex = 0;
    private int scale = 2;
    /**
     * all tube positions.
     */
    private Array<TubePosition> tubePositionArray = new Array<TubePosition>();

    /**
     * Constructor.
     * @param designWindow
     */
    public TubeLayoutImage(DesignWindow designWindow) {
        this.designWindow = designWindow;
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);

        groundTextRegion = textureAtlas.findRegion("ground_small");
        bottomTubeTextRegion = textureAtlas.findRegion("bottomtube_small");
        topTubeTextRegion = textureAtlas.findRegion("toptube_small");
        generateLevelData(100);

    }

    /**
     * Get all tube layout.
     * @return
     */
    public Array<TubePosition> getTubePositionArray() {
        TubePosition.resetTubePosition(tubePositionArray);

        return tubePositionArray;
    }

    /**
     * Set current tube's new size.
     * @param topBlockSize
     * @param bottomBlockSize
     */
    public void setCurrentTube(int topBlockSize, int bottomBlockSize) {
        if (currentTubeIndex < tubePositionArray.size) {
            TubePosition tubePosition = tubePositionArray.get(currentTubeIndex);
            tubePosition.topTubeHeight = topBlockSize * TubePosition.BLOCK_SIZE;
            tubePosition.bottomTubeHeight = bottomBlockSize * TubePosition.BLOCK_SIZE;

        }
    }


    /**
     * random generate the tube positions.
     */
    public void generateLevelData(int num) {
        tubePositionArray.clear();
        tubePositionArray.addAll(TubePosition.randomTubes(num));


    }

    /**
     * Move the layout with given distance.
     * @param distance distance to move, can be positive and negative.
     */
    public void move(int distance) {

        for (int i = 0; i < tubePositionArray.size; i++) {
            TubePosition tubePosition = tubePositionArray.get(i);

            tubePosition.posX -= distance;
            if (tubePosition.powerUp != null) {
                tubePosition.powerUp.posX -= distance;
            }
        }
    }

    /**
     * Get current tube in middle.
     * @return
     */
    public TubePosition getMiddleTube() {
        if (currentTubeIndex < tubePositionArray.size) {
            return tubePositionArray.get(currentTubeIndex);
        }
        return null;
    }

    /**
     * Get the middle tube index,
     * @return
     */
    public int getMiddleIndex() {
        return currentTubeIndex;
    }

    /**
     * Save tube layout to file.
     */
    public void saveTubeFile() {
        FileHandle fileHandle = null;
        if (files.isExternalStorageAvailable()) {
            fileHandle = files.external(filename);
        } else if (files.isLocalStorageAvailable()) {
            fileHandle = files.local(filename);
        }

        if (fileHandle != null) {
            fileHandle.delete();

            if (files.isExternalStorageAvailable()) {
                fileHandle = files.external(filename);
            } else if (files.isLocalStorageAvailable()) {
                fileHandle = files.local(filename);
            }

            for (int i = 0; i < tubePositionArray.size; i++) {
                TubePosition tubePosition = tubePositionArray.get(i);
                fileHandle.writeString(tubePosition.toString() + "\r\n", true);
            }
        }

    }

    /**
     * Read tube layout from file.
     */
    public void readTubeFile() {
        FileHandle fileHandle = null;
        if (files.isExternalStorageAvailable()) {
            fileHandle = files.external(filename);
        } else if (files.isLocalStorageAvailable()) {
            fileHandle = files.local(filename);
        }
        if (fileHandle != null) {
            tubePositionArray.clear();
            String fileContent = fileHandle.readString();
            String[] lines = fileContent.split("\r");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                Log.d("line", i + "," + line);
                String[] values = line.split(",");
                if (values.length == 4) {
                    TubePosition tubePosition = new TubePosition();
                    tubePosition.posX = Integer.parseInt(values[0]);
                    tubePosition.topTubeHeight = Integer.parseInt(values[1]);
                    tubePosition.bottomTubeHeight = Integer.parseInt(values[2]);
                    tubePosition.width = Integer.parseInt(values[3]);
                    tubePosition.drawNumber = false;

                    tubePositionArray.add(tubePosition);
                }
            }
            TubePosition.resetTubePosition(tubePositionArray);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int backWidth = groundTextRegion.getRegionWidth();
        int size = Configuration.SCREEN_WIDTH / backWidth;
        if (size * backWidth < Configuration.SCREEN_WIDTH) size += scale;
        offset = 0;
        int offsetY = (int) getY();
        for (int i = 0; i < (size + 1) * scale; i++) {
            batch.draw(groundTextRegion, (offset + i * backWidth) / scale, offsetY,
                    groundTextRegion.getRegionWidth(),
                    groundTextRegion.getRegionHeight());
        }


        for (int i = 0; i < tubePositionArray.size; i++) {
            TubePosition tubePosition = tubePositionArray.get(i);
            if (tubePosition.posX > -bottomTubeTextRegion.getRegionWidth()
                    && tubePosition.posX / scale < Configuration.SCREEN_WIDTH) {

                batch.draw(bottomTubeTextRegion, tubePosition.posX / scale,
                        offsetY + groundTextRegion.getRegionHeight(),
                        bottomTubeTextRegion.getRegionWidth(),
                        tubePosition.bottomTubeHeight / scale);

                batch.draw(topTubeTextRegion, tubePosition.posX / scale,
                        (Configuration.SCREEN_HEIGHT
                                - tubePosition.topTubeHeight) / scale + offsetY,
                        topTubeTextRegion.getRegionWidth(),
                        topTubeTextRegion.getRegionHeight());
                int num = String.valueOf(i + 1).length();
                if (Math.abs(tubePosition.posX / scale - Configuration.SCREEN_WIDTH / 2) < 20) {
                    currentTubeIndex = i;
                    designWindow.updateCurrentTubeInfo();
                }
                if (i == currentTubeIndex) {
                    currentNumbers.drawNumber(batch, i + 1,
                            (tubePosition.posX
                                    + (bottomTubeTextRegion.getRegionWidth()
                                    - 28 * scale * num) / 2) / scale,

                            (Configuration.SCREEN_HEIGHT - tubePosition.topTubeHeight +
                                    tubePosition.bottomTubeHeight)
                                    / (scale * 2) + offsetY);
                } else {
                    numbers.drawNumber(batch, i + 1,
                            (tubePosition.posX
                                    + (bottomTubeTextRegion.getRegionWidth()
                                    - 28 * scale * num) / 2) / scale,

                            (Configuration.SCREEN_HEIGHT - tubePosition.topTubeHeight +
                                    tubePosition.bottomTubeHeight)
                                    / (scale * 2) + offsetY);
                }

            }

        }

    }


    @Override
    public float getImageHeight() {
        return Configuration.SCREEN_HEIGHT / 2;
    }

    @Override
    public float getImageWidth() {
        return Configuration.SCREEN_WIDTH;
    }

    @Override
    public float getPrefHeight() {
        return getImageHeight();
    }
}
