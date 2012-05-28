/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.qsoft.gcviewer;

import org.openide.util.Exceptions;

/**
 *
 * @author qdlt
 */
public class GcPauseMonitor implements Runnable {

    private final GcDataCollector dataCollector;

    GcPauseMonitor(final GcDataCollector dataCollector) {
        this.dataCollector = dataCollector;
    }

    @Override
    public void run() {
        while (true) {
            dataCollector.checkGc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
