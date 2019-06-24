package com.sharing.files.geoshare.app;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
public class Features extends AppCompatActivity {
    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;
    private TextView[] mDots;
    private SliderAdapter sliderAdapter;
    private FloatingActionButton mNextBtn;
    private FloatingActionButton mBackBtn;
    private int mCurrentPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);
        mSlideViewPager = (ViewPager) findViewById(R.id.slideViewPager);
        mDotLayout = (LinearLayout) findViewById(R.id.dotsLayout);
        mNextBtn = (FloatingActionButton) findViewById(R.id.nextBtn);
        mBackBtn = (FloatingActionButton) findViewById(R.id.prevBtn);
        sliderAdapter = new SliderAdapter(this);
        mSlideViewPager.setAdapter(sliderAdapter);
        addDotsIndicator(0);
        mSlideViewPager.addOnPageChangeListener(viewListener);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((mCurrentPage+1)==3) {
                    Intent i = new Intent(getBaseContext(), Login.class);
                    startActivity(i);
                    finish();
                }
                mSlideViewPager.setCurrentItem(mCurrentPage + 1);
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((mCurrentPage-1)==-1) {
                    Intent i = new Intent(getBaseContext(),Login.class);
                    startActivity(i);
                    finish();
                }
                mSlideViewPager.setCurrentItem(mCurrentPage - 1);
            }
        });
    }
    public void addDotsIndicator(int position) {
        mDots = new TextView[3];
        mDotLayout.removeAllViews();
        for(int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.white));
            mDotLayout.addView(mDots[i]);
        }
        if(mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.black));
        }
    }
    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {
        }
        @Override
        public void onPageSelected(int i) {
            addDotsIndicator(i);
            mCurrentPage = i;
            if(i==0) {
                mNextBtn.setEnabled(true);
                mBackBtn.setEnabled(true);
                mNextBtn.setVisibility(View.VISIBLE);
                mBackBtn.setVisibility(View.VISIBLE);
                mNextBtn.setImageResource(R.mipmap.next);
                mBackBtn.setImageResource(R.mipmap.skip);
            } else if(i == mDots.length - 1) {
                mNextBtn.setEnabled(true);
                mBackBtn.setEnabled(true);
                mNextBtn.setVisibility(View.VISIBLE);
                mBackBtn.setVisibility(View.VISIBLE);
                mNextBtn.setImageResource(R.mipmap.finish);
                mBackBtn.setImageResource(R.mipmap.back);
            } else {
                mNextBtn.setEnabled(true);
                mBackBtn.setEnabled(true);
                mNextBtn.setVisibility(View.VISIBLE);
                mBackBtn.setVisibility(View.VISIBLE);
                mNextBtn.setImageResource(R.mipmap.next);
                mBackBtn.setImageResource(R.mipmap.back);
            }
        }
        @Override
        public void onPageScrollStateChanged(int i) {
        }
    };
}