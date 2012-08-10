package com.srenner.billminder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BillPayment {
	private String mURL;
	
	private Date mPaymentDate;
	private String mAmount;
	private String mReminderDays;
	private String mConfirmationNumber;
	private String mNotes;
	
	
	public BillPayment(String url, Date paymentDate, String amount, String reminderDays, String confirmationNumber, String notes) {
		mURL = url;
		mPaymentDate = paymentDate;
		mAmount = amount;
		mReminderDays = reminderDays;
		mConfirmationNumber = confirmationNumber;
		mNotes = notes;
	}
	
	public String getURL() {
		return mURL;
	}
	
	public String getFormattedPaymentDate() {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		return format.format(mPaymentDate);
	}
	
	public String getAmount() {
		return mAmount;
	}
	
	public String getReminderDays() {
		return mReminderDays;
	}
	
	public String getConfirmationNumber() {
		return mConfirmationNumber;
	}
	
	public String getNotes() {
		return mNotes;
	}
}
