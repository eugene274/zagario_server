package mechanics.forces;

import mechanics.Force;
import model.Cell;
import model.Field;
import utils.MathVector;

import static java.lang.Math.pow;
import static mechanics.MechanicConstants.RETURNING_FORCE;

/**
 * Created by eugene on 12/18/16.
 */
public class ReturningForce implements Force {
    private final int width;
    private final int height;

    public ReturningForce(Field field){
        width = field.getWidth();
        height = field.getHeight();
    }

    @Override
    public MathVector force(Cell cell) {
        float rfX = (float) (- RETURNING_FORCE*pow(cell.getX() - width/2f,3.0)/pow(width, 4.0));
        float rfY = (float) (- RETURNING_FORCE*pow(cell.getY() - height/2f,3.0)/pow(height, 4.0));
        return new MathVector(new double[]{rfX, rfY});
    }
}
