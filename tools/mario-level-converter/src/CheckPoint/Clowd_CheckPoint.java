
package CheckPoint;

import java.awt.Point;

public class Clowd_CheckPoint extends CheckPoints{

    
    public Clowd_CheckPoint( double x, double y , int NextLevel, Point NextLocation  ) {
        super(x, y+16 , NextLevel , NextLocation);
        this.NextLevel = NextLevel;
        this.NextLocation = NextLocation;
//        this.MarioStatus = MarioStatus;
        // System.out.println(x +" "+ y);
        this.setID(27);
        
        
    }



    @Override
    public int GetNextLevel() {
        return NextLevel ;
    }

    @Override
    public Point GetNextLocation() {
        return NextLocation;
    }

    
}
