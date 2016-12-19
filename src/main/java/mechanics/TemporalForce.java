package mechanics;

import utils.Timer;

import java.util.concurrent.TimeUnit;

/**
 * Created by eugene on 12/18/16.
 */
public abstract class TemporalForce extends Timer implements Force {
    protected TemporalForce(long lifeTime, TimeUnit unit) {
        super(lifeTime, unit);
    }
}
