package com.antoniocachuan.blogreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainListActivity extends ListActivity {
	
	protected String[] mBlogPostTitles;
	public static final String TAG = MainListActivity.class.getSimpleName();
	public static final int NUMBER_OF_POST = 20;
	protected JSONObject mBlogData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		
		
		if(isNetworkAvailable()){
			GetBlogPostTask getBlogPostTask = new GetBlogPostTask();
			getBlogPostTask.execute();		
		}
		else{
			Toast.makeText(this, "Network is unavialable!", Toast.LENGTH_LONG).show();
			
		}
		//Resources resources = getResources();//Obtengo los recursos para mostrar los items del array que estan en el 
		//mAndroidNames = resources.getStringArray(R.array.android_names);//string.xml
		//ArrayAdapter<String> adapter 	= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mAndroidNames);//muestra en una lista los elementos
		//setListAdapter(adapter);//que tengo en ese array
		
		//Toast.makeText(this, getString(R.string.no_items), Toast.LENGTH_LONG).show();
	}
	
	private boolean isNetworkAvailable() {// Ver si la aplicación tiene acceso a internet
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		boolean isAvailable = false;
		if(networkInfo != null && networkInfo.isConnected()){
			isAvailable = true;
		}
			
		return isAvailable;
	}

	private void updateList() {
		if(mBlogData == null){ // crear un dialogo
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.error_title));//las variables error_title y error_message creadas en string.xml
			builder.setMessage(getString(R.string.error_message));
			builder.setPositiveButton(android.R.string.ok, null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		else{
			try {
				JSONArray jsonPosts = mBlogData.getJSONArray("posts");
				mBlogPostTitles = new String[jsonPosts.length()];
				for(int i = 0; i < jsonPosts.length(); i++){
					JSONObject post = jsonPosts.getJSONObject(i);
					String title= post.getString("title");
					title=Html.fromHtml(title).toString();//para convertir caracteres especiales como &htrm etc
					mBlogPostTitles[i]=title;					
				}
				
				ArrayAdapter <String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mBlogPostTitles);
				setListAdapter(adapter);
			} catch (JSONException e) {
				Log.d(TAG, "Exception caught oso!");
			}
		}
	}
	
	private class GetBlogPostTask extends AsyncTask<Object, Void, JSONObject>{
		int responseCode = -1;
		JSONObject jsonResponse = null;
		@Override
		protected JSONObject doInBackground(Object... arg0) {
			try{
				URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count="+NUMBER_OF_POST);
				HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
				connection.connect();
				
				responseCode = connection.getResponseCode();
				if(responseCode == HttpURLConnection.HTTP_OK){
					InputStream inputStream = connection.getInputStream();
					Reader reader = new InputStreamReader(inputStream);
					int contentLength = connection.getContentLength();
					char[] charArray = new char[contentLength];
					reader.read(charArray);
					String responseData = new String(charArray);
					
					jsonResponse = new JSONObject(responseData);	
				}else{
					Log.i(TAG, "Bad Http Response Code: "+ responseCode);
					
				}
				
			}
			catch(MalformedURLException e){
				Log.e(TAG, "Exception caught: " + e);
			}
			catch(IOException e){
				Log.e(TAG, "Exception caught: "+ e);
			}
			catch(Exception e){
				
			}
			return jsonResponse;
		}
		
		@Override
		protected void onPostExecute(JSONObject result){
			mBlogData = result;// para tenerlo en nuestra actividad traerlo del background
			updateList();
		}
	}


	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_list, menu);
		return true;
	}

}
