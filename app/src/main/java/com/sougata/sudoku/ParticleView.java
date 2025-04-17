package com.sougata.sudoku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;

public class ParticleView extends View {

    private static final int NUM_PARTICLES = 25;
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint();
    private final Random random = new Random();
    private int width, height;
    private Bitmap particleBitmap;

    public ParticleView(Context context) {
        super(context);
        init();
    }

    public ParticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.light_particle);
        Bitmap original = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(original);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        particleBitmap = original;
    }

    private class Particle {
        float x, y, size, speedX, speedY;
        int fadingHeight;
        Bitmap particle;
        float alpha;
        boolean fadingOut = false, fadeIn = true;
        private final Paint bitmapPaint = new Paint();

        Particle(float x, float y, float size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speedY = random.nextFloat() * 3f + 2f;
            this.speedX = random.nextFloat() * 1.5f - 1.5f;
            this.particle = Bitmap.createScaledBitmap(particleBitmap, (int) size, (int) size, true);
            int min = (int) (height * 0.5);
            int max = (int) (height * 0.9);
            this.fadingHeight = height - (random.nextInt(max - min + 1) + min);
            this.alpha = 1;
        }

        void update() {
            y -= speedY;
            x += speedX;
            if (x < -size) {
                x = width + size;
            } else if (x > width + size) {
                x = -size;
            }
            if (y < -size || alpha == 0) {
                y = (random.nextFloat() * height * 0.75f) + (height * 0.25f);
//                x = (random.nextFloat() * (2 * width) / 3f) + (width / 3f);
                x = random.nextFloat() * width;
                size = random.nextFloat() * 15 + 10;
                speedY = random.nextFloat() * 3f + 2f;
                speedX = random.nextFloat() * 1.5f - 1.5f;
                alpha = 1;
                fadeIn = true;
            }
            if (y < fadingHeight) {
                fadingOut = true;
            }
            if (fadingOut) {
                alpha -= 10;
                if (alpha <= 0) {
                    fadingOut = false;
                    alpha = 0;
                }
            }
            if (fadeIn){
                alpha += 10;
                if (alpha >= 255) {
                    fadeIn = false;
                    alpha = 255;
                }
            }
        }

        void draw(Canvas canvas) {
            float halfSize = size / 2;
            bitmapPaint.setAlpha((int) alpha);
            canvas.drawBitmap(this.particle, x - halfSize, y - halfSize, bitmapPaint);
        }
    }

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < NUM_PARTICLES; i++) {
//            float x = (random.nextFloat() * (2 * width) / 3f) + (width / 3f);
            float x = random.nextFloat() *  width;
            float y = random.nextFloat() * height;
            float size = random.nextFloat() * 75 + 50;
            particles.add(new Particle(x, y, size));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        initParticles();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        for (Particle p : particles) {
            p.update();
            p.draw(canvas);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 300;
        int desiredHeight = 300;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
    }
}