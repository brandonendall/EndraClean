package com.endra.clean;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/** Scalable three-head Hydra illustration using the same navy-panel design as EndraLink. */
public final class HydraArtworkView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public HydraArtworkView(Context context) { super(context); setContentDescription("Gold three-headed Hydra emblem"); }

    @Override protected void onMeasure(int w, int h) {
        setMeasuredDimension(MeasureSpec.getSize(w), (int) (MeasureSpec.getSize(w) * .52f));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save(); canvas.scale(getWidth() / 400f, getHeight() / 208f);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(200, 108, 170, new int[]{0x444E3610, 0x112A251B, 0x0004111F}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, 400, 208, paint); paint.setShader(null);
        paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(0xFF8A5A0C); paint.setStrokeWidth(24); paint.setStyle(Paint.Style.STROKE);
        Path necks = new Path(); necks.moveTo(190, 183); necks.cubicTo(170, 120, 110, 150, 90, 82);
        necks.moveTo(200, 180); necks.cubicTo(190, 115, 205, 108, 199, 51);
        necks.moveTo(210, 183); necks.cubicTo(225, 122, 290, 150, 316, 82);
        canvas.drawPath(necks, paint);
        paint.setColor(0xFFF6C84A); paint.setStrokeWidth(14); canvas.drawPath(necks, paint);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 3; i++) {
            float x = i == 0 ? 85 : i == 1 ? 198 : 316;
            float y = i == 1 ? 48 : 79;
            Path head = new Path(); head.moveTo(x - 23, y + 6); head.lineTo(x - 18, y - 13);
            head.lineTo(x - 4, y - 8); head.lineTo(x + 2, y - 26);
            head.lineTo(x + 15, y - 10); head.lineTo(x + 30, y + 1);
            head.lineTo(x + 7, y + 13); head.lineTo(x - 15, y + 15); head.close();
            paint.setColor(0xFFFFD35B); canvas.drawPath(head, paint);
            paint.setColor(0xFF5C3804); canvas.drawCircle(x + 8, y, 3, paint);
        }
        paint.setColor(0xFFC58A1E); paint.setStrokeWidth(3); paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(200, 106, 96, paint);
        canvas.restore();
    }
}
