package com.cosmogia.situation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.cosmogia.situation.R;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.System;

public class CompassActivity extends Activity {
	
	CompassView compassView;	
	Location loc;
	LocationManager locMan;
    LocationListener loclis;
    Criteria criteria;
    double speed,alt,bearing,oughtVelocity,velocityExcess;
    double lat,lon;
    long minTime = 0;
    Float minDistance = 0f;
    String bestProvider;
    String filename = "waypoints.txt";
    ArrayList<Waypoint> course;
    double startTime,timeBuffer,currentTime,offset;
    ArrayList<String> log;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle); 
		setContentView(R.layout.main);
		compassView = (CompassView)this.findViewById(R.id.compassView1);
		log = new ArrayList<String>();
		locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		course = Waypoint.getWaypoints(this, filename);
		timeBuffer = (course.get(0).time);			
		course.remove(0);	
		startTime = System.currentTimeMillis()/1000.0;
		offset = 0;
		
		criteria = new Criteria();
		criteria.setSpeedRequired(true);
		criteria.setBearingRequired(true);
		criteria.setAltitudeRequired(true);
		compassView.invalidate();
		// try to sleep this each iteration
		while((bestProvider = locMan.getBestProvider(criteria, false)) == null) {
			System.out.println("Searching for provider.");
			continue;
		}
		loclis = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				System.out.println("about to update position");
				updatePosition(location);
			}
			@Override
			public void onProviderDisabled(String provider) {}
			@Override
			public void onProviderEnabled(String provider) {}
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
        };
	        
        locMan.requestLocationUpdates(bestProvider, minTime, minDistance, loclis);	
	}

	private void updatePosition(Location location) {
		System.out.println("Updating position");
		currentTime = System.currentTimeMillis()/1000.0 - startTime + timeBuffer - offset;
		Waypoint desiredLocation= Waypoint.getNearestWaypoint(course, currentTime);
		System.out.println("nearest waypoint: " + desiredLocation.toString());
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		System.out.println("got lat and lon");
		double alt = location.getAltitude();
		Waypoint actual = new Waypoint(lat,lon,alt,currentTime,null,null);
		
		log.add(actual.toString());
		// write log to file each time, or get rid of log entirely
		System.out.println(actual.toString());
		ErrorVector currentError = ErrorVector.errorVector(actual, desiredLocation);
		updateDials(actual, desiredLocation, currentError, location);
}
	
	private void updateDials(Waypoint actual, Waypoint desired, ErrorVector error, Location location) {
		System.out.println("Updating Dials");
		compassView.setBearing(location.getBearing());
		compassView.setGlide(error.magVert);
		compassView.setCourseBearing(ErrorVector.courseBearing(desired));
		compassView.setCourseDeviation(error.XTE);
		compassView.setDesiredVelocity(ErrorVector.velocityRequired(actual, desired)[0]);
		compassView.setDVelocityAngle(ErrorVector.velocityRequired(actual, desired)[1]);
		compassView.setVelocityExcess(location.getSpeed());
		compassView.setVelocityExcessAngle(location.getBearing());
		compassView.setTime(actual.time);
		compassView.invalidate();
	}
		
   	@Override
   	protected void onResume() {
   	  super.onResume();
   		  locMan.requestLocationUpdates(bestProvider, minTime, minDistance, loclis);	
   	}

   	@Override
   	protected void onStop() {
   		locMan.removeUpdates(loclis);
   		BufferedWriter bufferedWriter;
   		try {
   			String root = Environment.getExternalStorageDirectory().toString();
   	        File myDir = new File(root + "/HSI");
   	        myDir.mkdirs();
   	        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.US);//DateFormat.getTimeInstance();
   	        String time = df.format(new Date());
   	        bufferedWriter = new BufferedWriter(new FileWriter(new File(myDir, "log"+time+".txt")));
   			for(int i = 0; i < log.size(); i++) {
   				bufferedWriter.write(log.get(i));
   				System.out.println("writing to log");
   			}
   			bufferedWriter.close();
   			System.out.println("log written");
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
   		super.onStop();
   		finish();
   	}
}   	
