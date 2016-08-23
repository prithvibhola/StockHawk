package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteGraphColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Prithvi on 7/21/2016.
 */
public class DetailStockActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener{

    private TextView textStockName, textStockSymbol, textStockBidPrice, textStockChange;
    private Spinner spinTime;
    private LineChartView lineChartView;
    public static final String ARG_SYMBOL = "SYMBOL";
    private static final int CURSOR_LOADER_ID = 1;
    private static final int CURSOR_LOADER_ID_LINE_CHART = 2;
    private String symbol;
    private int item = 0;
    private String sortOrder;
    private List<AxisValue> xAxisValues;
    private List<PointValue> pointValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_stock);

        textStockName = (TextView) findViewById(R.id.tvStockName);
        textStockSymbol = (TextView) findViewById(R.id.tvStockSymbol);
        textStockBidPrice = (TextView) findViewById(R.id.tvStockBidPrice);
        textStockChange = (TextView) findViewById(R.id.tvStockChange);
        lineChartView = (LineChartView) findViewById(R.id.lcvGraph);
        spinTime = (Spinner) findViewById(R.id.spinTime);

        spinTime.setOnItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        symbol = extras.getString(ARG_SYMBOL);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        getLoaderManager().initLoader(CURSOR_LOADER_ID_LINE_CHART, null, this);

        List<String> time = new ArrayList<>();
        time.add(getResources().getString(R.string.five_days));
        time.add(getResources().getString(R.string.two_weeks));
        time.add(getResources().getString(R.string.one_month));

        //Adapter for spinnerMoney
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(DetailStockActivity.this, R.layout.spinner_item, time);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTime.setAdapter(dataAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if(id == CURSOR_LOADER_ID){

            return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.NAME,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP
                    },
                    QuoteColumns.SYMBOL + " = \"" + symbol + "\"",
                    null,
                    null);
        }else if (id == CURSOR_LOADER_ID_LINE_CHART) {

            if (item == 0) {
                sortOrder = QuoteColumns._ID + " ASC LIMIT 5";
            }else if (item == 1) {
                sortOrder = QuoteColumns._ID + " ASC LIMIT 14";
            } else if (item == 2) {
                sortOrder = QuoteColumns._ID + " ASC LIMIT 30";
            }

            return new CursorLoader(this, QuoteProvider.QuoteGraph.CONTENT_URI,
                    new String[]{QuoteGraphColumns._ID,
                            QuoteGraphColumns.SYMBOL,
                            QuoteGraphColumns.BIDPRICE,
                            QuoteGraphColumns.DATE
                    },
                    QuoteGraphColumns.SYMBOL + " = \"" + symbol + "\"",
                    null,
                    sortOrder);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(loader.getId() == CURSOR_LOADER_ID && data != null && data.moveToFirst()) {

            String name = data.getString(data.getColumnIndex(QuoteColumns.NAME));
            textStockName.setText(name);

            String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
            textStockSymbol.setText(symbol);

            String bidPrice = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
            textStockBidPrice.setText(bidPrice);

            String change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));
            String percentChange = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
            String actualChange = change + " (" + percentChange + ")";
            textStockChange.setText(actualChange);
        }else if (loader.getId() == CURSOR_LOADER_ID_LINE_CHART && data != null && data.moveToFirst()) {
            displayChart(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private void displayChart(Cursor data) {

        xAxisValues = new ArrayList<>();
        pointValues = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        LineChartData lineChartData = new LineChartData();

        int c = -1;

        do {
            c++;

            String date = data.getString(data.getColumnIndex(QuoteGraphColumns.DATE));
            String bidPrice = data.getString(data.getColumnIndex(QuoteGraphColumns.BIDPRICE));

            int x = data.getCount() - 1 - c;

            PointValue pointValue = new PointValue(x, Float.valueOf(bidPrice));
            pointValue.setLabel(date);
            pointValues.add(pointValue);

            if (c != 0 && c % (data.getCount() / 3) == 0) {
                AxisValue xAxisValue = new AxisValue(x);
                xAxisValue.setLabel(date);
                xAxisValues.add(xAxisValue);
            }

        } while (data.moveToNext());

        Line line = new Line(pointValues).setColor(Color.WHITE).setCubic(false);
        lines.add(line);
        lineChartData.setLines(lines);

        Axis xAxis = new Axis(xAxisValues);
        xAxis.setHasLines(true);
        xAxis.setMaxLabelChars(4);
        lineChartData.setAxisXBottom(xAxis);

        Axis yAxis = new Axis();
        yAxis.setAutoGenerated(true);
        yAxis.setHasLines(true);
        yAxis.setMaxLabelChars(4);
        lineChartData.setAxisYLeft(yAxis);

        lineChartView.setInteractive(false);
        lineChartView.setLineChartData(lineChartData);
        lineChartView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Spinner spinner = (Spinner) parent;
        switch(spinner.getId()){
            case R.id.spinTime:
                item = position;
                getLoaderManager().restartLoader(CURSOR_LOADER_ID_LINE_CHART, null, this);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
