/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.qsoft.gcviewer;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.tools.jvmstat.JvmstatModel;
import com.sun.tools.visualvm.tools.jvmstat.JvmstatModelFactory;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import org.openide.util.Utilities;

class GCViewerView extends DataSourceView {

    private static final String IMAGE_PATH = "com/sun/tools/visualvm/coredump/resources/coredump.png"; // NOI18N
    private final Application application;
    private final Map<SimpleXYChartSupport, String[]> charts = new HashMap<SimpleXYChartSupport, String[]>();
    private DataViewComponent dvc;

    public GCViewerView(Application application) {
        super(application, "GC viewer", new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 60, false);
        this.application = application;
    }

    private SimpleXYChartSupport createChart(DataTypeEnum dataType,
            ChartTypeEnum chartType, String name, String[] lineItems,
            String xDesc, String yDesc, String[] dataItemKeys) {

        SimpleXYChartDescriptor description;
        if (dataType == DataTypeEnum.DECIMAL) {
            description = SimpleXYChartDescriptor.decimal(0, true, 1000);
        } else {
            description = SimpleXYChartDescriptor.bytes(0, true, 1000);
        }

        description.setChartTitle(name);

        if (chartType == ChartTypeEnum.LINE) {
            description.addLineItems(lineItems);
        } else {
            description.addFillItems(lineItems);
        }

        description.setXAxisDescription(xDesc);
        description.setYAxisDescription(yDesc);
        final SimpleXYChartSupport chart = ChartFactory.createSimpleXYChart(description);
        charts.put(chart, dataItemKeys);
        return chart;
    }

    @Override
    protected DataViewComponent createComponent() {
        JEditorPane generalDataArea = new JEditorPane();
        generalDataArea.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
        JPanel panel = new JPanel();

        DataViewComponent.MasterView masterView = new DataViewComponent.MasterView("GC Performance Overview", null, generalDataArea);
        DataViewComponent.MasterViewConfiguration masterConfiguration =
                new DataViewComponent.MasterViewConfiguration(false);

        dvc = new DataViewComponent(masterView, masterConfiguration);

        SimpleXYChartSupport pauseChart = createChart(DataTypeEnum.DECIMAL, ChartTypeEnum.LINE, "GC pause times",
                new String[]{"Minor GC pause", "Major GC pause"}, "<html>Time</html>", "<html>GC  pause  in  \u00B5s</html>",
                new String[]{GcDataCollector.MINOR_GC_TIME_KEY, GcDataCollector.MAJOR_GC_TIME_KEY});
        dvc.addDetailsView(new DataViewComponent.DetailsView("Gc pause times", "description", 0, pauseChart.getChart(), null), DataViewComponent.TOP_LEFT);

        SimpleXYChartSupport survivorChart = createChart(DataTypeEnum.BYTES, ChartTypeEnum.LINE, "Promoted vs Survived",
                new String[]{"Promoted", "Survived"}, "<html>Time</html>", "<html>Bytes</html>",
                new String[]{GcDataCollector.PROMOTED_KEY, GcDataCollector.SURVIVED_KEY});
        dvc.addDetailsView(new DataViewComponent.DetailsView("Promoted vs Survived", "description", 0, survivorChart.getChart(), null), DataViewComponent.TOP_RIGHT);

        SimpleXYChartSupport gcCostChart = createChart(DataTypeEnum.DECIMAL, ChartTypeEnum.LINE, "GC cost",
                new String[]{"Minor GC cost", "Major GC cost"}, "<html>Time</html>", "<html>Cost</html>",
                new String[]{GcDataCollector.MINOR_COST_KEY, GcDataCollector.MAJOR_COST_KEY});
        dvc.addDetailsView(new DataViewComponent.DetailsView("GC cost", "description", 0, gcCostChart.getChart(), null), DataViewComponent.BOTTOM_LEFT);

        SimpleXYChartSupport freeLiveChart = createChart(DataTypeEnum.BYTES, ChartTypeEnum.LINE, "Free & live space",
                new String[]{"Free space", "Live space"}, "<html>Time</html>", "<html>Bytes</html>",
                new String[]{GcDataCollector.FREE_SPACE_KEY, GcDataCollector.LIVE_SPACE_KEY});
        dvc.addDetailsView(new DataViewComponent.DetailsView("Free & live space", "description", 0, freeLiveChart.getChart(), null), DataViewComponent.BOTTOM_RIGHT);

        final JvmstatModel model = JvmstatModelFactory.getJvmstatFor(application);
        final GcPauseMonitor monitor = new GcPauseMonitor(new GcDataCollector(model, charts));
        new Thread(monitor).start();

        return dvc;
    }
}
