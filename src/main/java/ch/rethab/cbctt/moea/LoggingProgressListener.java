package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.Logger;
import org.moeaframework.util.progress.ProgressEvent;
import org.moeaframework.util.progress.ProgressListener;

/**
 * @author Reto Habluetzel, 2015
 */
public class LoggingProgressListener implements ProgressListener {

    private final Logger.Level level;

    public LoggingProgressListener(Logger.Level level) {
        this.level = level;
    }

    @Override
    public void progressUpdate(ProgressEvent event) {
        Logger.log(level, String.format("Progress Update: NFE=%d, Seed=%d, ElapsedTime=%2.2fs, PercentComplete=%2.3f%%",
                event.getCurrentNFE(), event.getCurrentSeed(),
                event.getElapsedTime(), event.getPercentComplete() ));
    }

}
