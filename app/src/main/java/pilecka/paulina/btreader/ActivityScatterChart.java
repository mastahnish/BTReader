package pilecka.paulina.btreader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ActivityScatterChart extends Activity implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {

    private ScatterChart chart;
    private SeekBar seekBarX;
    private TextView tvX;
    private final SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private ArrayList<String> results;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scatterchart);
        Calendar calendar = Calendar.getInstance();

        setTitle(date_format.format(calendar.getTime()));

        results = getDataFromFile();

        tvX = findViewById(R.id.tvXMax);

        seekBarX = findViewById(R.id.seekBar1);
        seekBarX.setOnSeekBarChangeListener(this);

        chart = findViewById(R.id.chart1);
        chart.getDescription().setEnabled(false);
        chart.setOnChartValueSelectedListener(this);

        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setMaxHighlightDistance(50f);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        chart.setMaxVisibleValueCount(200);
        chart.setPinchZoom(true);

        seekBarX.setMax(results.size());
        seekBarX.setProgress(results.size());

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);

        l.setXOffset(5f);

        YAxis yl = chart.getAxisLeft();

        yl.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);

        XAxis xl = chart.getXAxis();
        xl.setDrawGridLines(false);
    }

    private ArrayList<String> getDataFromFile() {
        FileHelper helper = new FileHelper(this);
        return helper.readFromFile();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText(String.valueOf(seekBarX.getProgress()) + " [s]");

        ArrayList<Entry> values1 = new ArrayList<>();

        for (int i = 0; i < seekBarX.getProgress(); i++) {
            String stringValue = results.get(i);
            float val = 0.0f;

            if(stringValue!=null && !stringValue.isEmpty()){
                val = Float.parseFloat(stringValue);
            }

            values1.add(new Entry(i, val));
        }


        // create a dataset and give it a type
        ScatterDataSet set1 = new ScatterDataSet(values1, "Pomiar");
        set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);


        set1.setScatterShapeSize(8f);

        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets


        // create a data object with the data sets
        ScatterData data = new ScatterData(dataSets);

        chart.setData(data);
        chart.invalidate();
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", xIndex: " + e.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}