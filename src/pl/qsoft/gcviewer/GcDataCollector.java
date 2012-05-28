/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.qsoft.gcviewer;

import com.sun.tools.visualvm.charts.SimpleXYChartSupport;
import com.sun.tools.visualvm.tools.jvmstat.JvmstatModel;
import com.sun.tools.visualvm.tools.jvmstat.MonitoredValue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author qdlt
 */
public class GcDataCollector {

    public static final String MINOR_GC_TIME_KEY = "sun.gc.collector.0.time";
    public static final String MAJOR_GC_TIME_KEY = "sun.gc.collector.1.time";
    public static final String PROMOTED_KEY = "sun.gc.policy.promoted";
    public static final String SURVIVED_KEY = "sun.gc.policy.survived";
    public static final String MINOR_COST_KEY = "sun.gc.policy.minorGcCost";
    public static final String MAJOR_COST_KEY = "sun.gc.policy.majorGcCost";
    public static final String FREE_SPACE_KEY = "sun.gc.policy.freeSpace";
    public static final String LIVE_SPACE_KEY = "sun.gc.policy.liveSpace";

    private final Map<SimpleXYChartSupport, String[]> charts;
    private final MonitoredValue minorTime;
    private final MonitoredValue majorTime;
    private final JvmstatModel vm;

    private long lastMinorTime = 0L, lastMajorTime = 0L, currentMinorTime, currentMajorTime;
    private Map<String, Pair<MonitoredValue, AtomicLong>> monitors = new HashMap<String, Pair<MonitoredValue, AtomicLong>>();

    
    public GcDataCollector(final JvmstatModel vm, final Map<SimpleXYChartSupport, String[]> charts) {
        this.vm = vm;
        this.charts = charts;
        minorTime = vm.findMonitoredValueByName(MINOR_GC_TIME_KEY);
        majorTime = vm.findMonitoredValueByName(MAJOR_GC_TIME_KEY);
        initializeMonitors();
    }

    public void checkGc() {
        currentMinorTime = (Long) minorTime.getValue();
        currentMajorTime = (Long) majorTime.getValue();
        if (currentMinorTime - lastMinorTime != 0
                || currentMajorTime - lastMajorTime != 0) {
            onGc();
            lastMinorTime = currentMinorTime;
            lastMajorTime = currentMajorTime;
        }
    }

    private void onGc() {
        final long now = System.currentTimeMillis();
        for (final Map.Entry<SimpleXYChartSupport, String[]> entry : charts.entrySet()) {
            final String[] dataItems = entry.getValue();
            final long[] dataPoints = new long[dataItems.length];
            for (int i = 0; i < dataItems.length; i++) {
                long value;
                final Pair<MonitoredValue, AtomicLong> monitorPair = monitors.get(dataItems[i]);
                final AtomicLong previousValue = monitorPair.getRight();
                Long currentValue = 0L;
                try {
                currentValue = (Long) monitors.get(dataItems[i]).getLeft().getValue();
                } catch (Exception e) {
                    System.out.println("");
                }
                if (previousValue != null) {
                    value = currentValue - previousValue.get();
                    if (previousValue.getAndSet(currentValue) == Long.MIN_VALUE) break;
                } else {
                    value = currentValue;
                }
                dataPoints[i] = value;
            }
            entry.getKey().addValues(now, dataPoints);
        }
    }

    private void initializeMonitors() {
        monitors.put(MINOR_GC_TIME_KEY, new Pair<MonitoredValue, AtomicLong>(minorTime, new AtomicLong(Long.MIN_VALUE)));
        monitors.put(MAJOR_GC_TIME_KEY, new Pair<MonitoredValue, AtomicLong>(majorTime, new AtomicLong(Long.MIN_VALUE)));
        initMonitor(PROMOTED_KEY, null);
        initMonitor(SURVIVED_KEY, null);
        initMonitor(MINOR_COST_KEY, null);
        initMonitor(MAJOR_COST_KEY, null);
        initMonitor(FREE_SPACE_KEY, null);
        initMonitor(LIVE_SPACE_KEY, null);
    }

    private void initMonitor(final String name, final AtomicLong previousValue) {
        monitors.put(name, new Pair<MonitoredValue, AtomicLong>(vm.findMonitoredValueByName(name), previousValue));
    }
}
