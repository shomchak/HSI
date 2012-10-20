package com.cosmogia.situation;

public class ErrorVector {
	
	public final double magnitude, bearing, XTE, ATE, magVert, magHorz;
	
	public ErrorVector(double magnitude, double bearing, double XTE, double ATE, double altE, double magHorz) {
		super();
		this.magnitude = magnitude;
		this.bearing = bearing;
		this.XTE = XTE;
		this.ATE = ATE;
		this.magVert = altE;
		this.magHorz = magHorz;
	}
	
	public static ErrorVector errorVector(Waypoint actual, Waypoint desired) {
		// meters
		System.out.println("errorVector: lat, lon: " + actual.lat +"," + actual.lon + "," + desired.lat + "," + desired.lon);
		double R = 6371000;
		double dLat = Math.toRadians(desired.lat - actual.lat);
		double dLon = Math.toRadians(desired.lon - actual.lon);
		double lat1 = Math.toRadians(actual.lat);
		double lat2 = Math.toRadians(desired.lat);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		double magHorz = R * c;
		double magVert = desired.alt - actual.alt; // positive magVert means you are too low
		double magnitude = Math.hypot(magHorz, magVert);
		
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
		
		double bearingDegrees = Math.toDegrees(Math.atan2(y,x)); // degrees
		double bearing = (bearingDegrees + 360) % 360; // shifted
		
		double XTE,ATE;
		if(desired.previous != null) {			
			double theta = courseBearing(desired.previous, desired);
			double phi = courseBearing(actual, desired.previous);
			double beta = phi - theta;
			
			// Positive XTE means you are to the "left" of the course
			XTE = magHorz(actual, desired.previous) * Math.sin(Math.toRadians(beta));
			ATE = Math.sqrt(magHorz * magHorz - XTE * XTE);
			beta = bearing - theta;
			
			if(Math.signum(beta-90) == 1); {
				ATE = -1 * ATE;
			}  
		}
		else {
			XTE = 0;
			ATE = magHorz;
		}
		
		ErrorVector result = new ErrorVector(magnitude,bearing,XTE,ATE,magVert,magHorz);
		
		System.out.println("errorVector: XTE= " + XTE + ", ATE = " + ATE);
		return result;
	}
	
	public static double[] velocityRequired(Waypoint actual, Waypoint desired) {
		double i = 0;
		Waypoint last = desired;
		while(last.next != null && i <= 10.0) {
			i += 1;
			last = last.next;
		}
		ErrorVector error = errorVector(actual,last);
		double speed = error.magHorz/i; // meters/second
		double bearing = error.bearing; // degrees
		double[] result = {speed, bearing};
		return result;
	}
	
	public static double courseBearing(Waypoint dest) {
		if(dest.previous != null) {
			Waypoint source = dest.previous;
			double dLon = Math.toRadians(dest.lon - source.lon);
			double lat1 = Math.toRadians(source.lat);
			double lat2 = Math.toRadians(dest.lat);
			
			double y = Math.sin(dLon) * Math.cos(lat2);
			double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
			
			double bearingDegrees = Math.toDegrees(Math.atan2(y,x)); // degrees
			double bearing = (bearingDegrees + 360) % 360; // shifted
			
			return bearing;
		}
		else {
			return courseBearing(dest, dest.next);
		}
	}
	
	public static double courseBearing(Waypoint source, Waypoint dest) {
		double dLon = Math.toRadians(dest.lon - source.lon);
		double lat1 = Math.toRadians(source.lat);
		double lat2 = Math.toRadians(dest.lat);
			
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
		
		double bearingDegrees = Math.toDegrees(Math.atan2(y,x)); // degrees
		double bearing = (bearingDegrees + 360) % 360; // shifted
			
		return bearing;
	}
	
	public static double magHorz(Waypoint source, Waypoint dest) {
		double R = 6371;
		double dLat = Math.toRadians(source.lat - dest.lat);
		double dLon = Math.toRadians(source.lon - dest.lon);
		double lat1 = Math.toRadians(dest.lat);
		double lat2 = Math.toRadians(source.lat);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		double magHorz = R * c;
		
		return magHorz;
	}
}