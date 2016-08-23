package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteGraphColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.sam_chordas.android.stockhawk.utils.EndPoints.*;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {

  private String LOG_TAG = StockTaskService.class.getSimpleName();
  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;
  String getResponse;
  String graphGetResponse;
  String urlString, graphUrlString;
  int result = GcmNetworkManager.RESULT_FAILURE;

  public StockTaskService(){

  }

  public StockTaskService(Context context) {
    mContext = context;
  }

  String fetchData(String url) throws IOException {
    Request request = new Request.Builder()
            .url(url)
            .build();
    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  @Override
  public int onRunTask(TaskParams params) {

    Cursor initQueryCursor;
    if (mContext == null) {
      mContext = this;
    }

    StringBuilder urlStringBuilder = new StringBuilder();
    StringBuilder graphUrlStringBuilder = new StringBuilder();

    try {

      // Base URL for the Yahoo query
      urlStringBuilder.append(BASE_URL);
      urlStringBuilder.append(URLEncoder.encode(QUOTES_SELECT
              + "in (", "UTF-8"));

      // Base URL for the Yahoo Graph query
      graphUrlStringBuilder.append(BASE_URL);
      graphUrlStringBuilder.append(URLEncoder.encode(GRAPH_SELECT
              + "in (", "UTF-8"));

    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    if (params.getTag().equals(mContext.getResources().getString(R.string.string_init)) || params.getTag().equals(mContext.getResources().getString(R.string.string_periodic))) {

      isUpdate = true;
      initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
              new String[]{"Distinct " + QuoteColumns.SYMBOL},
              null,
              null,
              null);

      if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
        // Init task. Populates DB with quotes for the symbols seen below
        try {
          urlStringBuilder.append(
                  URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));

          graphUrlStringBuilder.append(
                  URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));

        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      } else if (initQueryCursor != null) {
        DatabaseUtils.dumpCursor(initQueryCursor);
        initQueryCursor.moveToFirst();
        for (int i = 0; i < initQueryCursor.getCount(); i++) {
          mStoredSymbols.append("\"" + initQueryCursor.getString(initQueryCursor.getColumnIndex(QuoteColumns.SYMBOL)) + "\",");
          initQueryCursor.moveToNext();
        }
        mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");

        try {

          urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
          graphUrlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));

        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
    } else if (params.getTag().equals(mContext.getResources().getString(R.string.string_add))) {
      isUpdate = false;
      // get symbol from params.getExtra and build query
      String stockInput = params.getExtras().getString(mContext.getResources().getString(R.string.string_symbol));
      try {

        urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
        graphUrlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));

      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      Intent intent = new Intent();
      intent.setAction("com.sam_chordas.android.stockhawk.ACTION.DATA.UPDATED");
      mContext.sendBroadcast(intent);

    }

    // finalize the URL for the API query.
    urlStringBuilder.append(URL_END);

    // finalize the URL for the Graph API query.
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    Date currentDate = new Date();

    Calendar calEnd = Calendar.getInstance();
    calEnd.setTime(currentDate);
    calEnd.add(Calendar.DATE, 0);

    Calendar calStart = Calendar.getInstance();
    calStart.setTime(currentDate);
    calStart.add(Calendar.MONTH, -1);

    String startDate = dateFormat.format(calStart.getTime());
    String endDate = dateFormat.format(calEnd.getTime());
    graphUrlStringBuilder.append(" and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\"" + URL_END);

    if (urlStringBuilder != null) {
      urlString = urlStringBuilder.toString();
      fetchQuote(urlString);
    }

    if (graphUrlStringBuilder != null) {
      graphUrlString = graphUrlStringBuilder.toString();
      fetchGraph(graphUrlString);
    }
    return result;
  }

  private void fetchQuote(String urlString){
    try {
      getResponse = fetchData(urlString);
      result = GcmNetworkManager.RESULT_SUCCESS;
      try {

        ContentValues contentValues = new ContentValues();
        if (isUpdate) {
          contentValues.put(QuoteColumns.ISCURRENT, 0);
          mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                  null, null);
        }

        ArrayList<ContentProviderOperation> contents = null;

        try {
          contents = Utils.quoteJsonToContentVals(getResponse);
        } catch (Exception ex) {
          Log.e(LOG_TAG, "Invalid json response.");
          result = GcmNetworkManager.RESULT_FAILURE;
        }
        if (contents != null) {
          mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, contents);
        }
      } catch (RemoteException | OperationApplicationException e) {
        Log.e(LOG_TAG, "Error applying batch insert", e);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void fetchGraph(String graphUrlString){
    try {
      graphGetResponse = fetchData(graphUrlString);
      result = GcmNetworkManager.RESULT_SUCCESS;
      try {

        ContentValues contentGraphValues = new ContentValues();

        if (isUpdate) {
          contentGraphValues.put(QuoteGraphColumns.ISCURRENT, 0);
          mContext.getContentResolver().update(QuoteProvider.QuoteGraph.CONTENT_URI, contentGraphValues,
                  null, null);
        }

        ArrayList<ContentProviderOperation> contentsGraph = null;

        try {
          contentsGraph = Utils.quoteJsonToGraphVals(graphGetResponse);
        } catch (Exception ex) {
          Log.e(LOG_TAG, "Invalid json response.");
          result = GcmNetworkManager.RESULT_FAILURE;
        }
        if (contentsGraph != null) {
          mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, contentsGraph);
        }
      } catch (RemoteException | OperationApplicationException e) {
        Log.e(LOG_TAG, "Error applying batch insert", e);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
