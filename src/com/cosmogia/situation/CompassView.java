package com.cosmogia.situation;

import com.cosmogia.situation.R;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.util.AttributeSet;
import android.content.res.Resources;
import java.lang.Math;

public class CompassView extends View {

	  private Paint markerPaint;
	  private Paint textPaint;
	  private Paint textPaintAngle;
	  private Paint circlePaint;
	  private Paint lubberPaint, deviationPaint1, deviationPaint2, velocityPaint, dVelocityPaint, velocityBoxPaint, glideslopePaint;
	  private String northString;
	  private String eastString;
	  private String southString;
	  private String westString;
	  private int textHeight;
	  
	  private static final int RED = -65536;
	  private static final int WHITE = -1;
	  private static final int GREEN = -16711936;
	  @SuppressWarnings("unused")
	  private static final int BLUE = -16776961;
	  private static final int BLACK = -16777216;
	  private static final int YELLOW = -256;
	  @SuppressWarnings("unused")
	  private static final double VMAX = 200;  
	  private static final double DVMAX = 15; // miles per hour
	  private static double DEVMAX = 1000;
	  private static final double GLIDEMAX = 30;
	  
	  private double glide = 50; // meters
	  private double dev = 50; // positive is to the left
	  private double courseAngle = 3.14/4; // in radians
	  private double velocityExcess = 100;
	  private double velocityAngle =273; // degrees
	  private double desiredVelocity = 110;
	  private double dVelocityAngle = 275; // degrees
	  private double bearing = Math.toRadians(0); // degrees
	  private double time = 0;
	  

	  public void setBearing(double _bearing) {
	    bearing = Math.toRadians(_bearing);
	  }
	  
	  public void setGlide(double _glide) {
		  glide = _glide;
		  if(glide > GLIDEMAX) {
			  glide = GLIDEMAX;
		  }
	  }
	  
	  public void setVelocityExcess(double _vel) {
		  velocityExcess = _vel;
	  }
	  
	  public void setCourseDeviation(double _dev) {
		  dev = _dev;
		  if(dev > DEVMAX) {
			  dev = DEVMAX;
		  }
	  }
	  
	  // CHECK THIS!!
	  private double scaleTime(double time) {
		  double t0 = -120;
		  double t1 = 0;
		  double d0 = 1000;
		  double d1 = 100;
		  if (time < t0)
			  return d0;
		  if (time > t1)
			  return d1;
		  return d0 + (d1-d0)*(time - t0)/(t1-t0);
	  }
	  
	  public void setCourseBearing(double angle) {
		  courseAngle = Math.toRadians(angle);
	  }
	  
	  public void setVelocityExcessAngle(double angle) {
		  velocityAngle = angle;
	  }
	  
	  public void setDesiredVelocity(double vel) {
		  desiredVelocity = vel;
	  }
	  
	  public void setDVelocityAngle(double angle) {
		  dVelocityAngle = angle;
	  }
	  
	  public void setTime(double _time) {
		  time = _time;
	  }
		
	  public CompassView(Context context) {
	    super(context);
	    initCompassView();
	  }
	
	  public CompassView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    initCompassView();
	  }
	
	  public CompassView(Context context, 
	                     AttributeSet ats, 
	                     int defaultStyle) {
	    super(context, ats, defaultStyle);
	    initCompassView();
	  }

  protected void initCompassView() {
    setFocusable(true);

    Resources r = this.getResources();

    circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    circlePaint.setColor(r.getColor(R.color.background_color));
    circlePaint.setStrokeWidth(10);
    circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

    northString = r.getString(R.string.cardinal_north);
    eastString = r.getString(R.string.cardinal_east);
    southString = r.getString(R.string.cardinal_south);
    westString = r.getString(R.string.cardinal_west);

    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setColor(r.getColor(R.color.text_color));
    textPaint.setTextAlign(Paint.Align.CENTER);
    textPaintAngle = new Paint();
    
    lubberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    lubberPaint.setColor(BLACK);
    lubberPaint.setStrokeWidth(3);
    
    deviationPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    deviationPaint1.setColor(WHITE);
    deviationPaint1.setStrokeWidth(10);
    
    deviationPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    deviationPaint2.setColor(GREEN);
    deviationPaint2.setStrokeWidth(7);
    
    velocityPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    velocityPaint.setColor(RED);
    velocityPaint.setStrokeWidth(5);
    
    dVelocityPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    dVelocityPaint.setColor(GREEN);
    dVelocityPaint.setStrokeWidth(10);
    
    
    velocityBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    velocityBoxPaint.setColor(WHITE);
    velocityBoxPaint.setStrokeWidth(2);
    velocityBoxPaint.setStyle(Paint.Style.STROKE);
    
    glideslopePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    glideslopePaint.setColor(YELLOW);
    glideslopePaint.setStrokeWidth(9);
    glideslopePaint.setStyle(Paint.Style.FILL);

    textHeight = (int)textPaint.measureText("yY");

    markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    markerPaint.setColor(r.getColor(R.color.marker_color));
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
    // The compass is a circle that fills as much space as possible.
    // Set the measured dimensions by figuring out the shortest boundary,
    // height or width.
    int measuredWidth = measure(widthMeasureSpec);
    int measuredHeight = measure(heightMeasureSpec);

    int d = Math.min(measuredWidth, measuredHeight);

    setMeasuredDimension(d, measuredHeight);

  }

  private int measure(int measureSpec) {
    int result = 0; 

    // Decode the measurement specifications.
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec); 

    if (specMode == MeasureSpec.UNSPECIFIED) {
      // Return a default size of 200 if no bounds are specified. 
      result = 200;
    } else {
      // As you want to fill the available space
      // always return the full available bounds.
      result = specSize;
    } 
    return result;
  }
  
  @Override 
  protected void onDraw(Canvas canvas) {
	  DEVMAX = scaleTime(time);
	  setKeepScreenOn(true);

    int mMeasuredWidth = getMeasuredWidth();
    int mMeasuredHeight = getMeasuredHeight();


//    circle
    int px = getMeasuredWidth() / 2;
    int py = getMeasuredWidth() /2 ;
    int radius = (int) (py*0.7);
    py = (int)(radius*1/0.8);
    
    // glideslope ticks
    int tickLength = (int) ((mMeasuredWidth - 2*radius)*0.8/2);
    int tickSpacing = (int) (2*radius/5.0);
    int tickStart = py-radius+(int)(tickSpacing/2.0);
    
    // glideslope
    int glideHeight = py - (int)(glide*radius/GLIDEMAX);
    int glideThick = 5;
    int glideLength = (int)(tickLength*0.8);
    
//    velocity arrows
    int vSpace = mMeasuredHeight - radius - py-20; 
    int vOriginX = px;
    int vOriginY = mMeasuredHeight - vSpace + (int) (vSpace/2.0);
    int lx = vOriginX - 5;
    int rx = vOriginX + 5;
        
    dVelocityAngle = dVelocityAngle - velocityAngle;
    velocityAngle = 0;
    
    double vx = velocityExcess*Math.sin(Math.toRadians(velocityAngle));
    double vy = velocityExcess*Math.cos(Math.toRadians(velocityAngle));
    double dx = desiredVelocity*Math.sin(Math.toRadians(dVelocityAngle));
    double dy = desiredVelocity*Math.cos(Math.toRadians(dVelocityAngle));
    
    double er = Math.hypot(dx-vx, dy-vy);
    if(er>DVMAX) {
    	er = vSpace/2.0;
    }
    else {
    	er = er*vSpace/2.0/DVMAX;
    }
    double ea = Math.atan2(dx-vx, dy-vy);
    
    int ex = (int) (vOriginX + er*Math.sin(ea));
    int ey = (int) (vOriginY - er*Math.cos(ea));
    

    courseAngle = courseAngle - bearing;
    
//    course select pointer
    int course1Startx = (int) (px + ((int) radius*.6*Math.sin(courseAngle)));
    int course1Starty = (int) (py - ((int) radius*.6*Math.cos(courseAngle)));
    int course1Endx = (int) (px + ((int) radius*Math.sin(courseAngle)));
    int course1Endy = (int) (py - ((int) radius*Math.cos(courseAngle)));
    
    int course2Startx = (int) (px - ((int) radius*.6*Math.sin(courseAngle)));
    int course2Starty = (int) (py + ((int) radius*.6*Math.cos(courseAngle)));
    int course2Endx = (int) (px - ((int) radius*Math.sin(courseAngle)));
    int course2Endy = (int) (py + ((int) radius*Math.cos(courseAngle)));
    
//    course deviation indicator
    int courseDevStartx = (int) (course1Startx - ((int) 50*dev/DEVMAX*Math.cos(courseAngle)));
    int courseDevStarty = (int) (course1Starty - ((int) 50*dev/DEVMAX*Math.sin(courseAngle)));
    int courseDevEndx = (int) (course2Startx - ((int) 50*dev/DEVMAX*Math.cos(courseAngle)));
    int courseDevEndy = (int) (course2Starty - ((int) 50*dev/DEVMAX*Math.sin(courseAngle)));
    
    System.out.println("deviation: " + dev);
      
    // Draw the background
    canvas.drawCircle(px, py, radius, circlePaint);
    // Rotate our perspective so that the ‘top’ is
    // facing the current bearing.
    
    canvas.save();
    
    System.out.println("bearing: " + Math.toDegrees(bearing));
    canvas.rotate((float)-Math.toDegrees(bearing), px, py);
    
    @SuppressWarnings("unused")
	int textWidth = (int)textPaint.measureText("W");
    int cardinalX = px;//-20;//(int)(textWidth/1);
    int cardinalY = py-radius+textHeight+25;

    textPaint.setTextSize(50f);
    textPaintAngle.set(textPaint); 
    textPaintAngle.setTextSize(25f);
    textPaintAngle.setTextAlign(Paint.Align.CENTER);

    // Draw the marker every 15 degrees and text every 45.
    for (int i = 0; i < 24; i++) {
      // Draw a marker.
      canvas.drawLine(px, py-radius, px, py-radius+10, markerPaint);

      canvas.save();
      canvas.translate(0, textHeight);

      // Draw the cardinal points
      if (i % 6 == 0) {
        String dirString = "";
        switch (i) {
          case(0)  : {
                       dirString = northString;
//                       int arrowY = 2*textHeight;
//                       canvas.drawLine(px, arrowY, px-5, 3*textHeight,
//                                       markerPaint);
//                       canvas.drawLine(px, arrowY, px+5, 3*textHeight, 
//                                       markerPaint);
                       break;
                     }
          case(6)  : dirString = eastString; break;
          case(12) : dirString = southString; break;
          case(18) : dirString = westString; break;
        }
        canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
      } 

      else if (i % 2 == 0) {//3 == 0) {
        // Draw the text every alternate 45deg
        String angle = Integer.toString(i*15);
//        float angleTextWidth = textPaint.measureText(angle);

        int angleTextX = (int)(px);//-angleTextWidth/3);
        int angleTextY = py-radius+textHeight+20;
        canvas.drawText(angle, angleTextX, angleTextY, textPaintAngle);
      }
      canvas.restore();

      canvas.rotate(15, px, py);
    }
    canvas.restore();
    
    // glideslope ticks
    for(int i = 0; i < 5; i++) {
    	canvas.drawLine(0, tickStart + tickSpacing*i, tickLength, tickStart + tickSpacing*i, velocityBoxPaint);
    	canvas.drawLine(mMeasuredWidth-tickLength, tickStart + tickSpacing*i, mMeasuredWidth, tickStart + tickSpacing*i, velocityBoxPaint);
    }
    
    // glideslope
    
    
    System.out.println(glideHeight);
    canvas.drawLine(0,glideHeight-glideThick,glideLength,glideHeight,glideslopePaint);
    canvas.drawLine(0,glideHeight+glideThick,glideLength,glideHeight,glideslopePaint);
    canvas.drawLine(mMeasuredWidth,glideHeight-glideThick,mMeasuredWidth-glideLength,glideHeight,glideslopePaint);
    canvas.drawLine(mMeasuredWidth,glideHeight+glideThick,mMeasuredWidth-glideLength,glideHeight,glideslopePaint);
	

    
//    course select pointer
    canvas.drawLine(course1Startx,course1Starty,course1Endx,course1Endy,deviationPaint1);
    canvas.drawLine(course2Startx,course2Starty,course2Endx,course2Endy,deviationPaint1);
    
//    course deviation bar
    canvas.drawLine(courseDevStartx,courseDevStarty,courseDevEndx,courseDevEndy,deviationPaint2);
    
//  lubber line
  canvas.drawLine(px, 0, px, (int) (radius*0.9), lubberPaint);
    
//    velocity excess arrow
  canvas.drawLine(lx,vOriginY,ex,ey,velocityPaint);
  canvas.drawLine(rx,vOriginY,ex,ey,velocityPaint);
  canvas.drawLine(vOriginX-50, vOriginY, vOriginX + 50, vOriginY, velocityBoxPaint);
  canvas.drawLine(vOriginX, vOriginY+50, vOriginX, vOriginY-50, velocityBoxPaint);
    
//    draw velocity box
  canvas.drawCircle(vOriginX, vOriginY, vSpace/2, velocityBoxPaint);

  // write time
//  canvas.drawText(Double.toString(time), px, vOriginY + 80, textPaint);
  canvas.drawText(Integer.toString((int)time), px, vOriginY + 80, textPaint);
  

  }
}