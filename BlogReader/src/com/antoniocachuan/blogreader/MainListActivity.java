package com.antoniocachuan.blogreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainListActivity extends ListActivity {
	
	
	public static final String TAG = MainListActivity.class.getSimpleName();
	public static final int NUMBER_OF_POST = 20;
	protected JSONObject mBlogData;
	protected ProgressBar mProgressBar;
	
	private final String KEY_TITLE= "title";
	private final String KEY_AUTHOR= "author";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);//para agregar el progressBar
		
		if(isNetworkAvailable()){
			mProgressBar.setVisibility(View.VISIBLE);
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
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		try {
			JSONArray jsonPosts = mBlogData.getJSONArray("posts");
			JSONObject jsonPost;
			jsonPost = jsonPosts.getJSONObject(position);
			String blogUrl = 	jsonPost.getString("url");
			
			//Intent intent = new Intent(Intent.ACTION_VIEW);//PARA MOSTRAR LA PÁGINA EN EL BROWSER IMPLICIT INTENT
			Intent intent = new Intent(this, BlogWebViewActivity.class);//Explicit intent
			intent.setData(Uri.parse(blogUrl));
			startActivity(intent);
			
		} catch (Exception e) {
			
			logException(e);
		}
		
	}

	private void logException(Exception e) {
		Log.e("TAG", "Exception Caught", e);
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

	private void handleBlogResponse() {
		mProgressBar.setVisibility(View.INVISIBLE);
		if(mBlogData == null){ // crear un dialogo
			updateDisplayForErrors();
		}
		else{
			try {
				JSONArray jsonPosts = mBlogData.getJSONArray("posts");
				ArrayList<HashMap<String, String>> blogPosts=new ArrayList<HashMap<String,String>>();
				for(int i = 0; i < jsonPosts.length(); i++){
					JSONObject post = jsonPosts.getJSONObject(i);
					String title= post.getString(KEY_TITLE);
					title=Html.fromHtml(title).toString();//para convertir caracteres especiales como &htrm etc
					String author= post.getString(KEY_AUTHOR);
					author=Html.fromHtml(author).toString();
					
					HashMap<String, String> blogPost=new HashMap<String, String>();
					blogPost.put(KEY_TITLE, title);
					blogPost.put(KEY_AUTHOR, author);
					
					blogPosts.add(blogPost);
				}
				
				String[] keys = {KEY_TITLE, KEY_AUTHOR};
				int[] ids = { android.R.id.text1, android.R.id.text2};
				SimpleAdapter adapter = new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2, keys, ids); 	
				setListAdapter(adapter);
			} catch (JSONException e) {
				Log.d(TAG, "Exception caught oso!");
			}
		}
	}

	private void updateDisplayForErrors() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.error_title));//las variables error_title y error_message creadas en string.xml
		builder.setMessage(getString(R.string.error_message));
		builder.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
		
		TextView emptyTextView = (TextView) getListView().getEmptyView();
		emptyTextView.setText(getString(R.string.no_items));
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
				logException(e);
			}
			catch(IOException e){
				logException(e);
			}
			catch(Exception e){
				logException(e);
			}
			return jsonResponse;
		}
		
		@Override
		protected void onPostExecute(JSONObject result){
			mBlogData = result;// para tenerlo en nuestra actividad traerlo del background
			handleBlogResponse();
		}
	}


	



}
