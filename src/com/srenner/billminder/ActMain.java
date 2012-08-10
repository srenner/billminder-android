package com.srenner.billminder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActMain extends ActionBarActivity {
	
	private ProgressDialog mProgress;
	private PostClient mPostClient;
	private SharedPreferences mPreferences;
	private static int REQUEST_PREFS = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        load();
    }
    
    private void load() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
		String loginURL = mPreferences.getString("prefLoginURL", "");
		String indexURL = mPreferences.getString("prefIndexURL", "");
        String username = mPreferences.getString("prefUsername", "");
        String password = mPreferences.getString("prefPassword", "");
        
        if(loginURL.length() == 0 || indexURL.length() == 0 || username.length() == 0 || password.length() == 0) {
        	Intent i = new Intent(getApplicationContext(), ActPreferences.class);
        	startActivityForResult(i, REQUEST_PREFS);
        }
        else {
	        final ListView lvBills = (ListView)findViewById(R.id.lvBills);
	        lvBills.setOnItemClickListener(new OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
					final Bill b = (Bill)lvBills.getItemAtPosition(position);
					LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
					final View layout = inflater.inflate(R.layout.payment_form, (ViewGroup)findViewById(R.id.payment_form_root));
					final EditText etPaymentAmount = (EditText)layout.findViewById(R.id.etPaymentAmount);
					etPaymentAmount.setText(String.valueOf(b.getDefaultAmount()));
					final EditText etDays = (EditText)layout.findViewById(R.id.etDays);
					etDays.setText(String.valueOf(b.getDefaultReminderDays()));
					
					AlertDialog.Builder builder = new AlertDialog.Builder(ActMain.this);
					builder.setView(layout);
					builder.setTitle("Pay " + b.getName());
					builder.setPositiveButton("Pay", new DialogInterface.OnClickListener() {
	
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							
							
							//String urlTemplate = getString(R.string.payment_url);
							//String paymentURL = urlTemplate.replace("$ID$", String.valueOf(b.getID()));
							
							String paymentURL = mPreferences.getString("prefIndexURL", "");
							if(!paymentURL.endsWith("/")) {
								paymentURL += "/";
							}
							paymentURL += String.valueOf(b.getID()) + "/pay/";
							
							BillPayment bp = new BillPayment(paymentURL, new Date(), etPaymentAmount.getText().toString(), 
									etDays.getText().toString(), ((EditText)layout.findViewById(R.id.etConfirmationNumber)).getText().toString(), 
									((EditText)layout.findViewById(R.id.etNotes)).getText().toString());
							
							if(mProgress == null) {
						        mProgress = new ProgressDialog(ActMain.this);
						        mProgress.setTitle("Please Wait");
						        mProgress.setMessage("Contacting Server");
							}
							mProgress.show();
							getActionBarHelper().setRefreshActionItemState(true);
							new PayBillTask().execute(bp);
						} 
					});
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			});
	        
			if(mProgress == null) {
		        mProgress = new ProgressDialog(ActMain.this);
		        mProgress.setTitle("Please Wait");
		        mProgress.setMessage("Contacting Server");
			}
			getActionBarHelper().setRefreshActionItemState(true);
			mProgress.show();
	        new GetBillsTask().execute();
        }    	
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PREFS) {
        	try {
	        	if(data.getBooleanExtra("prefsReload", false)) {
	            	load();
	        	}
        	}
        	catch(Exception e) {
        		//some devices have a back button that won't set prefsReload upon leaving
        		//the preferences screen
        		load();
        	}
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater menuInflater = getMenuInflater();
    	menuInflater.inflate(R.menu.main, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_refresh:
                //Toast.makeText(this, "Refreshing data", Toast.LENGTH_SHORT).show();
				if(mProgress == null) {
			        mProgress = new ProgressDialog(ActMain.this);
			        mProgress.setTitle("Please Wait");
			        mProgress.setMessage("Contacting Server");
				}
				mProgress.show();
                getActionBarHelper().setRefreshActionItemState(true);
                new GetBillsTask().execute();
                break;

            case R.id.menu_settings:
            	Intent i = new Intent(getApplicationContext(), ActPreferences.class);
            	//startActivity(i);
            	startActivityForResult(i, REQUEST_PREFS);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    
    private boolean makePayment(PostClient postClient, BillPayment bp) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
        HttpConnectionParams.setSoTimeout(httpParams, 20000);
        HttpPost httpPost = new HttpPost(bp.getURL());
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("payment_date", bp.getFormattedPaymentDate()));
        pairs.add(new BasicNameValuePair("amount", bp.getAmount()));
        pairs.add(new BasicNameValuePair("reminder_days", bp.getReminderDays()));
        pairs.add(new BasicNameValuePair("confirmation_number", bp.getConfirmationNumber()));
        pairs.add(new BasicNameValuePair("notes", bp.getNotes()));
        pairs.add(new BasicNameValuePair("csrfmiddlewaretoken", postClient.getCSRFTokenValue()));
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
			HttpResponse response = postClient.getClient().execute(httpPost);
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			response.getEntity().writeTo(ostream);
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				entity.consumeContent();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
        return true;
    }
    
    private List<Bill> makeBillList(String strJson) {
        List<Bill> bills = new ArrayList<Bill>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);        	
        	JSONArray array = new JSONArray(strJson);
        	for(int i = 0; i < array.length(); i++) {
        		JSONObject inner = (JSONObject)array.get(i);
        		String strBill = inner.getString("fields");
        		Bill b = mapper.readValue(strBill, Bill.class);
        		b.setID(Integer.valueOf(inner.getString("pk")));
        		//String strJsonExtras = inner.getString("extras");
        		JSONObject extras = inner.getJSONObject("extras");
        		//extras.getString("health") "health_color "days_since_payment"
        		
        		bills.add(b);
        	}
    	}
		 catch (JSONException e) {
			 e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return bills;
    }
    
    private void drawList(List<Bill> bills) {
        if(bills.size() > 0) {
        	ListView lvBills = (ListView)findViewById(R.id.lvBills);
        	BillAdapter adapter = new BillAdapter(getApplicationContext(), R.layout.bill_listview, bills);
        	lvBills.setAdapter(adapter);
        	//lvBills.setAdapter(new ArrayAdapter<Bill>(getApplicationContext(), android.R.layout.simple_list_item_1, bills));
        }
    }
    
    private String viewMainPage(PostClient postClient, String url) {
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(url);
		try {
	        HttpResponse response = postClient.getClient().execute(httpGet, localContext);
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			response.getEntity().writeTo(ostream);
			return ostream.toString();
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
    }
    
    private PostClient login(PostClient postClient, String url) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
        HttpConnectionParams.setSoTimeout(httpParams, 20000);
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("username", mPreferences.getString("prefUsername", "")));
        pairs.add(new BasicNameValuePair("password", mPreferences.getString("prefPassword", "")));
        pairs.add(new BasicNameValuePair("csrfmiddlewaretoken", postClient.getCSRFTokenValue()));
        
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
			HttpResponse response = postClient.getClient().execute(httpPost);
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			response.getEntity().writeTo(ostream);
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				entity.consumeContent();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return postClient;
    }
    
    private PostClient getCSRFToken(String url) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
        HttpConnectionParams.setSoTimeout(httpParams, 20000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(url);
		try {
	        httpClient.execute(httpGet, localContext);
	        List<Cookie> cookies = httpClient.getCookieStore().getCookies();
	        for(int i = 0; i < cookies.size(); i++) {
	        	Cookie c = cookies.get(i);
	        	if(c.getName().equalsIgnoreCase("csrftoken")) {
	        		return new PostClient(httpClient, c.getValue());
	        	}
	        }
		}
		catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
		return null;
    }

    private class PayBillTask extends AsyncTask<BillPayment, Void, Boolean> {

		@Override
		protected Boolean doInBackground(BillPayment... params) {
			return makePayment(mPostClient, params[0]);
		}
		
		protected void onPostExecute(Boolean success) {
			//mProgress.dismiss();
			if(!success) {
				String message = "Error submitting payment";
				AlertDialog.Builder alert = new AlertDialog.Builder(ActMain.this);
	        	alert.setTitle("Results");
	        	alert.setMessage(message);
	        	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
	    		});
	        	alert.show();
			}
			else {
				Toast.makeText(getApplicationContext(), "Payment posted, refreshing data", Toast.LENGTH_SHORT).show();
                getActionBarHelper().setRefreshActionItemState(true);
                new GetBillsTask().execute();
			}
		}
    }
    
    private class GetBillsTask extends AsyncTask<Void, Void, List<Bill>> {

		@Override
		protected List<Bill> doInBackground(Void... arg0) {
			String loginURL = mPreferences.getString("prefLoginURL", "");
			String indexURL = mPreferences.getString("prefIndexURL", "");
			if(!loginURL.endsWith("/")) {
				loginURL += "/";
			}
			if(!indexURL.endsWith("/")) {
				indexURL += "/";
			}
			indexURL += "?format=json";
	        PostClient postClient = getCSRFToken(loginURL);
	        postClient = login(postClient, loginURL);
	        mPostClient = postClient;
	        String strJson = viewMainPage(postClient, indexURL);
	        List<Bill> bills = makeBillList(strJson);
			return bills;
		}
		
		protected void onPostExecute(List<Bill> bills) {
			drawList(bills);
			mProgress.dismiss();
			getActionBarHelper().setRefreshActionItemState(false);
		}
    }

    private class BillAdapter extends ArrayAdapter<Bill> {
    	private ArrayList<Bill> mItems;
    	
    	public BillAdapter(Context context, int textViewResourceId, List<Bill> objects) {
			super(context, textViewResourceId, objects);
			mItems = (ArrayList<Bill>)objects;
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View v = convertView;
    		if(v == null) {
    			LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
    			v = li.inflate(R.layout.bill_listview, null);
    		}
    		Bill b = mItems.get(position);
    		if(b != null) {
    			TextView tvLV_BillName = (TextView)v.findViewById(R.id.tvLV_BillName);
    			TextView tvLV_DaysAgo = (TextView)v.findViewById(R.id.tvLV_DaysAgo);
    			
    			tvLV_BillName.setText(b.getName());
    			if(b.getDaysAgoString().length() == 0) {
    				tvLV_DaysAgo.setVisibility(View.GONE);
    			}
    			else {
    				tvLV_DaysAgo.setVisibility(View.VISIBLE);
    				tvLV_DaysAgo.setText(b.getDaysAgoString());
    			}
    		}
    		return v;
    	}
    }
}