package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteGraphColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.sam_chordas.android.stockhawk.utils.ResponseKeys.*;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject(QUERY);
        int count = Integer.parseInt(jsonObject.getString(COUNT));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject(RESULTS)
                  .getJSONObject(QUOTE);
          batchOperations.add(buildBatchOperation(jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject(RESULTS).getJSONArray(QUOTE);

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0, 1);
    String ampersand = "";
    if (isPercentChange) {
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(QuoteProvider.Quotes.CONTENT_URI);

    try {
      String change = jsonObject.getString(CHANGE);
      builder.withValue(QuoteColumns.NAME, jsonObject.getString(NAME));
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(SYMBOL));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString(BID)));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(jsonObject.getString(CHANGE_IN_PERCENT), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-') {
        builder.withValue(QuoteColumns.ISUP, 0);
      } else {
        builder.withValue(QuoteColumns.ISUP, 1);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return builder.build();
  }

  public static ContentProviderOperation buildGraphOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(QuoteProvider.QuoteGraph.CONTENT_URI);
    try {
      builder.withValue(QuoteGraphColumns.SYMBOL, jsonObject.getString(SYMBOL_GRAPH));
      builder.withValue(QuoteGraphColumns.BIDPRICE, jsonObject.getString(OPEN));
      builder.withValue(QuoteGraphColumns.DATE, jsonObject.getString(DATE));
      builder.withValue(QuoteGraphColumns.ISCURRENT, 1);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return builder.build();
  }

  public static ArrayList quoteJsonToGraphVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject(QUERY);

        resultsArray = jsonObject.getJSONObject(RESULTS).getJSONArray(QUOTE);

        if (resultsArray != null && resultsArray.length() != 0){
          for (int i = 0; i < resultsArray.length(); i++){
            jsonObject = resultsArray.getJSONObject(i);
            batchOperations.add(buildGraphOperation(jsonObject));
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }
}
