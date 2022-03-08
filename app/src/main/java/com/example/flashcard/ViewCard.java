package com.example.flashcard;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.awt.font.NumericShaper;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewCard extends AppCompatActivity implements View.OnClickListener {

    private List<List<String>> cards = new ArrayList<>();
    private TextView tvShuffle, tvOrder, tvInfo, tvTitle, tvCard;
    private ImageView ivShuffle, ivOrder, ivInfo;
    private List currCard;
    private boolean front = true;
    private static int f = 0, b = 1;
    private Deck thisDeck;
    private int inc = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);

        tvShuffle = (TextView) findViewById(R.id.tvShuffle);
        tvOrder = (TextView) findViewById(R.id.tvOrder);
        tvInfo = (TextView) findViewById(R.id.tvInfo);
        tvTitle = (TextView) findViewById(R.id.tvCardTitle);
        tvCard = (TextView) findViewById(R.id.tvCard) ;



        try {
            thisDeck = (Deck) getIntent().getSerializableExtra("Deck");
            cards = thisDeck.getCards();
            currCard = cards.get(inc);
            tvTitle.setText(thisDeck.title);
            tvCard.setText(currCard.get(f).toString());
            tvCard.setTypeface(Typeface.DEFAULT_BOLD);
        }
        catch(Exception e){
            Log.d("debug", "Empty deck");
        }

        tvCard.setOnTouchListener(new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeLeft () {
                    front = true;
                    inc++;
                    tvCard.setBackgroundColor(Color.parseColor("#A3FFFFFF"));
                    tvCard.setTypeface(Typeface.DEFAULT_BOLD);
                    try {
                        currCard = cards.get(inc);
                        tvCard.setText(currCard.get(f).toString());
                    } catch(Exception e) {
                        inc = 0;
                        currCard = cards.get(inc);
                        tvCard.setText(currCard.get(f).toString());
                        Toast.makeText(ViewCard.this,"Done", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSwipeRight () {
                    front = true;
                    inc--;
                    tvCard.setBackgroundColor(Color.parseColor("#A3FFFFFF"));
                    tvCard.setTypeface(Typeface.DEFAULT_BOLD);
                    try {
                        currCard = cards.get(inc);
                        tvCard.setText(currCard.get(f).toString());
                    } catch (Exception e) {
                        inc = cards.size() - 1;
                        currCard = cards.get(inc);
                        tvCard.setText(currCard.get(f).toString());
                    }
                }
                @Override
                public void onClick(){
                    if (front){
                        front = false;
                        tvCard.setText(currCard.get(b).toString());
                        tvCard.setBackgroundColor(Color.parseColor("#d3d3d3"));
                        tvCard.setTypeface(Typeface.DEFAULT);

                        Log.d("dhk", currCard.get(b).toString());
                    }
                    else{
                        front = true;
                        tvCard.setTypeface(Typeface.DEFAULT_BOLD);
                        tvCard.setBackgroundColor(Color.parseColor("#A3FFFFFF"));
                        tvCard.setText(currCard.get(f).toString());
                        Log.d("dhk", currCard.get(f).toString());
                    }
                }
        });

    }


    @Override
    public void onClick(View v){

        switch (v.getId()){
        }
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }
        public void onClick(){

        }

        public void onSwipeLeft() {
        }

        public void onSwipeRight() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onClick();
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
                return false;
            }
        }
    }
}