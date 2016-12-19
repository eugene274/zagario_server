package mechanics.forces;

import mechanics.MechanicConstants;
import mechanics.TemporalForce;
import model.Cell;
import utils.MathVector;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.pow;
import static mechanics.MechanicConstants.REPULSION_FORCE_SCALING;

/**
 * Created by eugene on 12/18/16.
 */
public class RepulsionForce extends TemporalForce {
    private boolean multipleReferences;
    private Collection<? extends Cell> cells;
    private Cell referencedCell;

    public RepulsionForce(Cell referencedCell) {
        super(MechanicConstants.REUNION_TIMEOUT, TimeUnit.MILLISECONDS);
        multipleReferences = false;
        this.referencedCell = referencedCell;
    }

    public RepulsionForce(Collection<? extends Cell> cells){
        super(0, TimeUnit.MILLISECONDS);
        multipleReferences = true;
        this.cells = cells;
    }

    @Override
    public MathVector force(Cell cell) {
        if(multipleReferences){
            MathVector force = new MathVector(2);
            for (Cell c: cells){
                if(c.getRepulsionForce() != null && c != cell){
                    force.plus(c.getRepulsionForce().force(cell));
                }
            }
            return force;
        }
        else {
            if(isExpired()){
                return new MathVector(2);
            }

            MathVector dR = cellPosition(cell).minus(cellPosition(referencedCell));
            return dR.scale(REPULSION_FORCE_SCALING/pow(dR.magnitude(),7));
        }
    }
}
