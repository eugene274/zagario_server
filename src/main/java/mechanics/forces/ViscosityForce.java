package mechanics.forces;

import mechanics.Force;
import mechanics.MechanicConstants;
import model.Cell;
import utils.MathVector;

/**
 * Created by eugene on 12/18/16.
 */
public class ViscosityForce implements Force {
    @Override
    public MathVector force(Cell cell) {
        return cellSpeed(cell).scale(1.0/MechanicConstants.VISCOSITY_DECREMENT);
    }
}
