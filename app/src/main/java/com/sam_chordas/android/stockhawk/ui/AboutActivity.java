package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Prithvi on 7/30/2016.
 */
public class AboutActivity extends AppCompatActivity{

    private TextView textGooglePlus, textAppName, textCoder, textGithub;
    private ImageView back;
    private Typeface typeface;
    private SpannableString ss;
    private ClickableSpan span;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        typeface = Typeface.createFromAsset(getAssets(), "fonts/Geomanist_Regular.otf");

        textAppName = (TextView) findViewById(R.id.tvAppName);
        textGooglePlus = (TextView) findViewById(R.id.tvGooglePlus);
        textCoder = (TextView) findViewById(R.id.tvCoder1);
        textGithub = (TextView) findViewById(R.id.tvGithub);
        back = (ImageView) findViewById(R.id.ivBack);

        textAppName.setTypeface(typeface);
        textGooglePlus.setTypeface(typeface);
        textCoder.setTypeface(typeface);
        textGithub.setTypeface(typeface);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ss = new SpannableString(getResources().getString(R.string.github));

        span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/prithvibhola"));
                startActivity(intent);
            }
        };

        ss.setSpan(span, 32, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textGithub.setText(ss);
        textGithub.setMovementMethod(LinkMovementMethod.getInstance());

        String htmlString = "<u>" + getResources().getString(R.string.google) +"</u>";
        textGooglePlus.setText(Html.fromHtml(htmlString));
        textGooglePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://plus.google.com/u/1/111463277012289416232"));
                startActivity(intent);
            }
        });
    }
}
