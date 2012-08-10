package com.srenner.billminder;

import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.Days;

public class Bill {

	private int mID;
	
	// field names and naming convention needs to match the python objects
	
	@JsonProperty("default_amount")
	private double mDefaultAmount;
	@JsonProperty("default_reminder_days")
	private int mDefaultReminderDays;
	@JsonProperty("name")
	private String mName;
	@JsonProperty("last_payment_date")
	private String mLastPaymentDate;
	@JsonProperty("demo_access")
	private boolean mDemoAccess;
	
	@JsonProperty("default_amount")
	public double getDefaultAmount() {
		return mDefaultAmount;
	}
	
	@JsonProperty("default_reminder_days")
	public int getDefaultReminderDays() {
		return mDefaultReminderDays;
	}
	
	@JsonProperty("name")
	public String getName() {
		return mName;
	}
	
	@JsonProperty("last_payment_date")
	public String getLastPaymentDate() {
		return mLastPaymentDate;
	}
	
	@JsonProperty("demo_access")
	public boolean getDemoAccess() {
		return mDemoAccess;
	}
	
	public int getID() {
		return mID;
	}
	
	public void setID(int id) {
		mID = id;
	}
	
	public String getDaysAgoString() {
		String daysAgo = "";
		String dayText = "day";
		if(mLastPaymentDate != null) {
			DateTime now = new DateTime();
			DateTime lastPayment = new DateTime(mLastPaymentDate.substring(0, mLastPaymentDate.lastIndexOf(" ")));
			int daysBetween = Days.daysBetween(lastPayment, now).getDays();
			if(daysBetween < 1 || daysBetween > 1) {
				dayText += "s";
			}
			daysAgo = String.valueOf(daysBetween);
		}
		if(daysAgo.length() == 0) {
			return "never paid";
		}
		else {
			return "paid " + daysAgo + " " + dayText + " ago";
		}		
	}
	
	public String toString() {
		String daysAgo = "";
		String dayText = "day";
		if(mLastPaymentDate != null) {
			DateTime now = new DateTime();
			DateTime lastPayment = new DateTime(mLastPaymentDate.substring(0, mLastPaymentDate.lastIndexOf(" ")));
			int daysBetween = Days.daysBetween(lastPayment, now).getDays();
			if(daysBetween < 1 || daysBetween > 1) {
				dayText += "s";
			}
			daysAgo = String.valueOf(daysBetween);
		}
		if(daysAgo.length() == 0) {
			return mName;
		}
		else {
			return mName + " - " + daysAgo + " " + dayText + " ago";
		}
	}
}
