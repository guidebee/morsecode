package CheckPoint;

import com.golden.gamedev.object.Sprite;
import java.awt.Point;

public class CheckPoints extends Sprite implements BasicCheckPoints {

    public int NextLevel;

    public Point NextLocation;

    /**
     *
     * @param Mario_X_Location_when_he_is_dead when mario dies and is back alive
     * , where should we put him , in what x and y coordinate , so this will be
     * that x location , its just temporary value as when the mario walks , new
     * autosave value gets saved in this locatin
     * @param Mario_Y_Location_when_he_is_dead when mario dies and is back alive
     * , where should we put him , in what x and y coordinate , so this will be
     * that y location
     * @param Level_Number check Mario.java then SelectLevel() method , all
     * levels codes are written there
     * @param MarioStartingLocation the locatin where mario should be when level
     * is start (not when mario is dead and back alive but completly first time
     * start of level)
     */
    public CheckPoints(double Mario_X_Location_when_he_is_dead, double Mario_Y_Location_when_he_is_dead, int Level_Number, Point MarioStartingLocation) {
        super(Mario_X_Location_when_he_is_dead, Mario_Y_Location_when_he_is_dead);
        this.NextLevel = Level_Number;
        this.NextLocation = MarioStartingLocation;
//        this.MarioStatus = MarioStatus;
        // System.out.println(x +" "+ y);
        this.setID(23);

    }

    @Override
    public void update(long elapsedTime) {
        super.update(elapsedTime);
    }

    @Override
    public int GetNextLevel() {
        return NextLevel;
    }

    @Override
    public Point GetNextLocation() {
        return NextLocation;
    }

}
