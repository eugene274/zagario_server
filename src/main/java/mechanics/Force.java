package mechanics;

import model.Cell;
import utils.MathVector;


/**
 * Created by eugene on 12/18/16.
 */
public interface Force {
    default MathVector cellPosition(Cell cell){
        return new MathVector(new double[]{cell.getX(),cell.getY()});
    }

    default MathVector cellSpeed(Cell cell){
        return new MathVector(new double[]{cell.getSpeedX(),cell.getSpeedY()});
    }

    MathVector force(Cell cell);
}
