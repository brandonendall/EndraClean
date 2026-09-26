package com.endra.clean;
import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Responsive EndraClean cave containment artwork: exactly three Hydra heads. */
public final class HydraArtworkView extends View {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 public HydraArtworkView(Context c){super(c);setContentDescription("Three headed Endra Hydra inside a gold containment chamber");}
 @Override protected void onMeasure(int w,int h){int W=MeasureSpec.getSize(w);setMeasuredDimension(W,(int)(W*.48f));}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float sx=getWidth()/500f,sy=getHeight()/240f;c.save();c.scale(sx,sy);
  p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,0,0,240,0xFF050403,0xFF171006,Shader.TileMode.CLAMP));c.drawRect(0,0,500,240,p);p.setShader(null);
  // cave silhouettes
  p.setColor(0xFF241707);Path cave=new Path();cave.moveTo(0,0);cave.lineTo(80,0);cave.lineTo(48,42);cave.lineTo(88,70);cave.lineTo(30,105);cave.lineTo(65,145);cave.lineTo(0,190);cave.close();c.drawPath(cave,p);
  c.save();c.scale(-1,1,250,0);c.drawPath(cave,p);c.restore();
  // wide containment chamber
  RectF tube=new RectF(52,30,448,212);p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,30,0,212,new int[]{0x55291A05,0xAA080704,0x662A1A05},null,Shader.TileMode.CLAMP));c.drawRoundRect(tube,42,42,p);p.setShader(null);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);p.setColor(0xFFF6C84A);c.drawRoundRect(tube,42,42,p);p.setStrokeWidth(2);p.setColor(0xFFFFE58B);c.drawRoundRect(new RectF(61,39,439,203),35,35,p);
  // hydra necks
  p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(28);p.setColor(0xFF8B590A);
  Path n=new Path();n.moveTo(240,184);n.cubicTo(200,142,145,157,127,98);n.moveTo(250,184);n.cubicTo(248,140,250,112,250,72);n.moveTo(260,184);n.cubicTo(302,142,355,157,373,98);c.drawPath(n,p);p.setStrokeWidth(18);p.setColor(0xFFFFC83D);c.drawPath(n,p);
  // little tail/body
  Path tail=new Path();tail.moveTo(238,184);tail.cubicTo(205,203,188,191,172,202);tail.cubicTo(198,207,222,214,253,196);p.setStrokeWidth(13);p.setColor(0xFFFFC83D);c.drawPath(tail,p);
  p.setStyle(Paint.Style.FILL);float[] xs={123,250,377},ys={94,67,94};for(int i=0;i<3;i++){float x=xs[i],y=ys[i];Path h=new Path();h.moveTo(x-28,y+8);h.lineTo(x-22,y-17);h.lineTo(x-7,y-11);h.lineTo(x,y-31);h.lineTo(x+15,y-12);h.lineTo(x+34,y+2);h.lineTo(x+10,y+16);h.lineTo(x-19,y+17);h.close();p.setColor(0xFFFFD85C);c.drawPath(h,p);p.setColor(0xFF4C2B00);c.drawCircle(x+10,y,3.5f,p);}
  c.restore();
 }
}