package com.sensorberg.android.ui;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.LimitLine;
import com.sensorberg.android.sensorscanner.BeaconScanObject;
import com.sensorberg.android.sensorscanner.SensorScanner;
import com.sensorberg.android.sensorscanner.filter.BeaconIdFilter;
import com.sensorberg.sdk.cluster.BeaconId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Plotfragment extends Fragment implements SensorScanner.Listener {


    private static final long SAMPLERATE = 5;
    private static final long SECONDS_TO_SHOW = 5;
    private LineChart chart;
    private SensorScanner scanner;
    private ArrayDeque<BeaconScanObject.BeaconScanDistance > readings;
    private BeaconId myBeaconId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chart = new LineChart(getActivity());
        scanner = new SensorScanner(getActivity());
        scanner.addFilter(new BeaconIdFilter(myBeaconId));
        scanner.setSampleRate(SAMPLERATE);
        readings = new ArrayDeque<>();
        return chart;
    }

    @Override
    public void onResume() {
        super.onResume();
        scanner.setListener(this);
        scanner.start();
    }

    @Override
    public void onPause() {
        scanner.stop();
        super.onPause();
    }

    @Override
    public void updateUI(List<BeaconScanObject> beacons) {


        BeaconScanObject.BeaconScanDistance reading = beacons.size() == 1 ? beacons.get(0).getLastDistanceCalculation() : new BeaconScanObject.BeaconScanDistance(0,0,0);
        readings.add(new BeaconScanObject.BeaconScanDistance(reading));
        if (readings.size() > SECONDS_TO_SHOW * SAMPLERATE){
            readings.removeFirst();
        }


        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> averageRssi = new ArrayList<Entry>();
        ArrayList<Entry> distanceInMeters =  new ArrayList<>();
        ArrayList<Entry> samplecount =  new ArrayList<>();

        int i = 0;
        for (BeaconScanObject.BeaconScanDistance beaconScanDistance : readings) {
            xVals.add(String.valueOf(SECONDS_TO_SHOW * SAMPLERATE + 1 - i));
            averageRssi.add(new Entry((float) -beaconScanDistance.averageRssi, i));
            distanceInMeters.add(new Entry((float) beaconScanDistance.distanceInMeters, i));
            samplecount.add(new Entry((float) beaconScanDistance.samplecount, i));
            i++;
        }

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(getDataSet(averageRssi, "Average Rssi", Color.BLACK));
        dataSets.add(getDataSet(distanceInMeters, "Distance", Color.RED));
        dataSets.add(getDataSet(samplecount, "Sample Count", Color.GREEN));


        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        LimitLine calRssi = new LimitLine(-beacons.get(0).calRssi);
        calRssi.setLineWidth(4f);
        calRssi.enableDashedLine(10f, 10f, 0f);
        calRssi.setDrawValue(true);
        calRssi.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT);


        data.addLimitLine(calRssi);
        chart.setData(data);
        chart.invalidate();
    }

    private LineDataSet getDataSet(ArrayList<Entry> values, String name, int color) {
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, name);
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10f, 5f, 0f);
        set1.setColor(color);
        set1.setCircleColor(color);
        set1.setLineWidth(1f);
        set1.setCircleSize(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(color);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));
        return set1;
    }


    public Plotfragment setMyBeaconId(BeaconId myBeaconId) {
        this.myBeaconId = myBeaconId;
        return this;
    }
}