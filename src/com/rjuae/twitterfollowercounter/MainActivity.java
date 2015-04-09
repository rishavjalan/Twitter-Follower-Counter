package com.rjuae.twitterfollowercounter;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rjuae.twitterfollowercounter.R;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private RadioGroup radioInputGroup;
	private RadioButton radioInputButton;
	private Button btnDisplay;
	Activity thisActivity;
	EditText url;
	TextView resultView,oldView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		thisActivity = this;
		addListenerOnButton();
		loadLastChecked();
	}

	public void addListenerOnButton() {

		radioInputGroup = (RadioGroup) findViewById(R.id.radioInput);
		btnDisplay = (Button) findViewById(R.id.button_check);
		url = (EditText) findViewById(R.id.editText1);
		resultView=(TextView) findViewById(R.id.textView_result);
		oldView=(TextView) findViewById(R.id.textView_old);
		btnDisplay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// get selected radio button from radioGroup
				int selectedId = radioInputGroup.getCheckedRadioButtonId();

				// find the radiobutton by returned id
				radioInputButton = (RadioButton) findViewById(selectedId);
				checkLikeCount(radioInputButton.getText().toString());
				// Toast.makeText(thisActivity, radioInputButton.getText(),
				// Toast.LENGTH_SHORT).show();

			}

		});
		showDialog();
	}

	String result = "";
	String page_url = "";
	String response = "";

	void checkLikeCount(String method) {
		page_url = url.getText().toString();

		if (page_url==null || page_url.equalsIgnoreCase("") ||page_url.length()<2) {
			Toast.makeText(thisActivity, "Invalid Input", Toast.LENGTH_SHORT)
					.show();

			return;
		}
		if (method.equalsIgnoreCase("URL")) {

			page_url=page_url.replaceAll("\\s+","");
			if (!(page_url.startsWith("http://") || (page_url
					.startsWith("https://")))) {
				page_url = "http://" + page_url;
				url.setText(page_url);
			}
			url.setText(page_url);


		} else {
			page_url=page_url.replaceAll("\\s+","");
			url.setText(page_url);

			page_url = "https://twitter.com/" + page_url;

		}
		String pathtofetch = URLEncoder.encode(page_url);
		AndroidHttpClient httpClient = new AndroidHttpClient(
				"http://www.facebook.com/plugins/like.php?href="+pathtofetch+"&width&layout=standard&action=like&show_faces=true&share=true&height=80");
		httpClient.setMaxRetries(5);
		ParameterMap params = httpClient.newParams()
				.add("href", "getChildItems")
				.add("width", "EA5466FB7FBA3E0B1543345D6")
				.add("screenId", "67F0644CF02DE0DCC13C5CD")
				.add("apiKey", "0D66AA7FF2E067231B4D72A")
				.add("apiSecret", "CF73E29E0990D156E0A41DD");
		httpClient.get("", null, new AsyncCallback() {
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				response = "";
				checkLike2();
			}

			@Override
			public void onComplete(HttpResponse httpResponse) {
				// finish
				
				
				response = httpResponse.getBodyAsString();
				System.out.println("Leng"+response.length());
				
				checkLike2();
			}
		});

	}

	void checkLike2() {
		try {
			loadLastChecked();
			if (response != "") {
				int script = response.lastIndexOf("</head>");
				response=response.substring(script);
				System.out.println("len - " + response.length());

				int start = response.indexOf("<span>");
				
				response=response.substring(start);

				int end = response.indexOf("</span>");
				System.out.println("Script - " + script);
				System.out.println("Start - " + start);
				System.out.println("End - " + end);

				result = response.substring(0, end);
				result = result.replace("<span>", "");
				result = result.trim();
				if(result.length()>200)
					result = "Please check the url.";
				else
					{
					saveLastChecked();
					 }
				//result += " people like this. ";
			} else
				result = "Please check the url. Try Again.";
			resultView.setVisibility(View.VISIBLE);
			resultView.setText(result);
			System.out.println(result);
		} catch (Exception ed) {
			ed.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	void saveLastChecked(){
		
		setPrefString("result",result);
		setPrefString("page_url",page_url);
	}
	
	void loadLastChecked(){
		
	String old_result=	getPrefString("result");
	String old_url=getPrefString("page_url");
	if(old_result.length()>2 && old_url.length()>2)
	{
		oldView.setVisibility(View.VISIBLE);
		oldView.setText("Last Checked\n"+old_url+"\n"+old_result);
	}
	}

	// gets a value of a preference saved on the device...
	public String getPrefString(String nameOfPreference) {
		// BT_debugger.showIt(objectName + ":getPrefString getting value of \""
		// + nameOfPreference +
		// "\" from the devices settings");
		String ret = "";
		try {
			if (nameOfPreference.length() > 1) {
				ret = "get the value here...";

				SharedPreferences BT_prefs = this.getSharedPreferences(
						"MyPref", 0); // 0 - for private mode
				ret = BT_prefs.getString(nameOfPreference, "");

				// BT_debugger.showIt(objectName + ":getPrefString value is: \""
				// + ret + "\"");

			}
		} catch (Exception e) {
			Log.e("TagError", "getPrefString EXCEPTION " + e.toString());
		}
		return ret;
	}

	// sets a value of a preference to the device...
	public void setPrefString(String nameOfPreference, String valueOfPreference) {
		// BT_debugger.showIt(objectName + ":setPrefString setting \"" +
		// nameOfPreference + "\" to \"" +
		// valueOfPreference + "\" in the devices settings");
		try {

			SharedPreferences BT_prefs = this.getSharedPreferences("MyPref", 0); // 0
																					// -
																					// for
																					// private
																					// mode
			SharedPreferences.Editor prefsEditor = BT_prefs.edit();
			prefsEditor.putString(nameOfPreference, valueOfPreference);
			prefsEditor.commit();

		} catch (Exception e) {
			Log.e("TagError", "setPrefString EXCEPTION " + e.toString());
		}

	}
	

	String dialogLink = "";
	static JSONArray dialogs = null;

	
	void showDialog() {
		int versionNumber = 0;
		// if (shouldCreateUser)
		// getUserInfo();
		AndroidHttpClient httpClient = new AndroidHttpClient(
				"http://api.admileage.com/app/movies/dialog_x.php");
		try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionNumber = pinfo.versionCode;
		} catch (Exception sd) {
		}
		httpClient.setMaxRetries(5);
		ParameterMap params = httpClient.newParams()
				.add("version", versionNumber + "").add("app", "facbook_like_x")
				.add("apiSecret", "CF73E29E0990D156E0A41DD");
		httpClient.get("", params, new AsyncCallback() {
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				// finish();
			}

			@Override
			public void onComplete(HttpResponse httpResponse) {
				JSONObject jsonObj;
				try {
					jsonObj = new JSONObject(httpResponse.getBodyAsString());
					dialogs = jsonObj.getJSONArray("childItems");
					JSONObject c = dialogs.getJSONObject(0);
					dialogLink = c.getString("link");
					if (c.getString("show").equalsIgnoreCase("yes"))
						new AlertDialog.Builder(thisActivity)
								.setTitle(c.getString("title"))
								.setMessage(c.getString("message"))
								.setPositiveButton(c.getString("button2"),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												// do nothing
											}
										})
								.setNegativeButton(c.getString("button1"),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												// continue with delete
												thisActivity
														.startActivity(new Intent(
																Intent.ACTION_VIEW,
																Uri.parse(dialogLink)));
											}
										}).setIcon(R.drawable.ic_launcher)
								.show();

					// System.out.println("this is my response"+id);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}
}
