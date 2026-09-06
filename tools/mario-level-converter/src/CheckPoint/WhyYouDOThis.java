
package CheckPoint;

import java.awt.Point;


public class WhyYouDOThis   extends CheckPoints {

    // NOTE (mario-level-converter): the original file declared an unused
    // `Mario game;` field here, which pulled in a compile dependency on the
    // entire SandBox.Mario class (2,348 lines, ~60 transitive imports) for a
    // field that is never read or assigned. Dropped for the converter build;
    // behavior is unaffected. See docs/MARIO_PORT_PLAN.md Step 1.

    public WhyYouDOThis( double x, double y , int NextLevel, Point NextLocation  ) {
        super(x, y , NextLevel , NextLocation );
        this.setID(16);

    }


}
