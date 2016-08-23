package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Prithvi on 7/29/2016.
 */
public class StockDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor mCursor;
    private RemoteViews mRemoteViews;

    public StockDataProvider(Context context){
        this.mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        setData();
    }

    @Override
    public void onDestroy() {
        if (mCursor != null){
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        String symbol = "";
        String bidPrice = "";
        String change = "";
        int isUp = 1;

        if(mCursor.moveToPosition(position)){
            symbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
            bidPrice = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE));
            change = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE));
            isUp = mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP));
        }

        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.stock_widget_item);
        mRemoteViews.setTextViewText(R.id.stock_symbol, symbol);
        mRemoteViews.setTextViewText(R.id.bid_price, bidPrice);
        mRemoteViews.setTextViewText(R.id.change, change);

        if(isUp == 1){
            mRemoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        }else{
            mRemoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        Intent intent = new Intent();
        intent.putExtra("SYMBOL", symbol);
        mRemoteViews.setOnClickFillInIntent(R.id.widget_item, intent);

        return mRemoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void setData(){

        if(mCursor != null){
            mCursor.close();
        }

        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{
                        QuoteColumns._ID,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE,
                        QuoteColumns.ISUP
                },
                QuoteColumns.ISCURRENT + " = ? ",
                new String[]{"1"},
                null);
    }
}
