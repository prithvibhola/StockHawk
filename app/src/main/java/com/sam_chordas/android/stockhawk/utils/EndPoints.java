package com.sam_chordas.android.stockhawk.utils;

/**
 * Created by Prithvi on 7/28/2016.
 */
public class EndPoints {

    public static final String BASE_URL = "https://query.yahooapis.com/v1/public/yql?q=";
    public static final String QUOTES_SELECT = "select * from yahoo.finance.quotes where symbol ";
    public static final String GRAPH_SELECT = "select * from yahoo.finance.historicaldata where symbol ";
    public static final String URL_END = "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
            + "org%2Falltableswithkeys&callback=";

}
