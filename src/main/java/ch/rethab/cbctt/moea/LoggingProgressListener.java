package ch.rethab.cbctt.moea;

import ch.rethab.cbctt.Logger;
import org.moeaframework.util.progress.ProgressEvent;
import org.moeaframework.util.progress.ProgressListener;

/**
 * @author Reto Habluetzel, 2015
 */
public class LoggingProgressListener implements ProgressListener {

    @Override
    public void progressUpdate(ProgressEvent event) {
        Logger.gibber(String.format("Progress Update: NFE=%d, Seed=%d, ElapsedTime=%ds, PercentComplete=%2.3f%%",
                event.getCurrentNFE(), event.getCurrentSeed(),
                (int) event.getElapsedTime(), event.getPercentComplete() ));
    }

}
