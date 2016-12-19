package mechanics.forces;

import mechanics.Force;
import model.Cell;
import utils.MathVector;

/**
 * Created by eugene on 12/18/16.
 */
public class NoForce implements Force {
    @Override
    public MathVector force(Cell cell) {
        return new MathVector(2);
    }
}
