package utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by eugene on 12/17/16.
 */
public class Timer {
    private long startedAt = System.nanoTime();
    private long lifeTime = 0;

    public Timer(long lifeTime, TimeUnit unit) {
        this.lifeTime = TimeUnit.NANOSECONDS.convert(lifeTime, unit);
    }

    public boolean isExpired(){
        return (System.nanoTime() - startedAt) > lifeTime;
    }
}
