package mechanics;

/**
 * Created by eugene on 11/22/16.
 */


public interface MechanicConstants {
    int MINIMAL_MASS = 16;
    int EJECTED_MASS = 10;

    long EJECTION_TIME = 1000;

    float MINIMAL_SPEED = 0.1f;
    float MAXIMAL_SPEED = 20f;
    float SPLIT_SPEED = 15f;

    float EJECT_SPEED = 18f;

    float VISCOSITY_DECREMENT = 1f; // milliseconds
    float ATTRACTION_DECREMENT = 300f;
    float VISCOSITY_SCALING = 100f;
    float RETURNING_FORCE = 10f;

    float MOUSE_FORCE_SCALING = 0.5f;
    float TIME_SCALE = 1f;

    long REPULSION_TIMEOUT = 30_000;
    float REPULSION_FORCE_SCALING = 100f;
}
