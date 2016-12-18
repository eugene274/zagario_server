package mechanics.forces;

import mechanics.Force;
import model.Cell;
import utils.MathVector;

/**
 * Created by eugene on 12/18/16.
 */
public class MouseForce implements Force {
    private float vX;
    private float vY;

    public MouseForce(float vX, float vY) {
        this.vX = vX;
        this.vY = vY;
    }

    @Override
    public MathVector force(Cell cell) {
        return new MathVector(new double[]{vX, vY});
    }
}
