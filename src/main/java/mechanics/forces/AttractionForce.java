package mechanics.forces;

import mechanics.Force;
import model.Cell;
import utils.MathVector;

import java.util.Collection;
import java.util.List;

import static mechanics.MechanicConstants.ATTRACTION_DECREMENT;

/**
 * Created by eugene on 12/18/16.
 */
public class AttractionForce implements Force {
    private final MathVector avg;

    public AttractionForce(Collection<? extends Cell> cells) {
        avg = new MathVector(2);
        for (Cell cell: cells){
            avg.plus(cellPosition(cell));
        }
        avg.scale(1.0/cells.size());
    }

    @Override
    public MathVector force(Cell cell) {
        return avg.minus(cellPosition(cell)).scale(1.0/ATTRACTION_DECREMENT);
    }
}
