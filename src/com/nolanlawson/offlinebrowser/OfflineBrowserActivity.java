package com.nolanlawson.offlinebrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class OfflineBrowserActivity extends Activity implements OnKeyListener {
    
	private static final String TAG = "OfflineBrowserActivity";
	
	private static FileNameMap fileNameMap = URLConnection.getFileNameMap();
	
	private EditText inputEditText;
    private WebView webView;
    private Stack<String> history;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        history = new Stack<String>();
        
        setUpWidgets();
        
        webView.requestFocus(); // prevents the soft keyboard from coming up
        
        
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_open:
	    	startOpenFileActivity();
	    	break;
	    case R.id.menu_history:
	    	openHistory();
	    	break;
	    case R.id.menu_about:
	    	startAboutActivity();
	    	break;
	    }
	    return false;
	}



	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		inputEditText.setVisibility(View.VISIBLE);
		
		// disable history if there is none

		menu.findItem(R.id.menu_history).setEnabled(history != null && history.size() > 1);

		
		return super.onPrepareOptionsMenu(menu);		
	}

	
	

    
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		
		
		// switch out the edit text, if there's a web page displayed
		if (!TextUtils.isEmpty(inputEditText.getText())) {
			inputEditText.setVisibility(inputEditText.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
		
			String filename = data.getStringExtra("filename");	
			handleUrl(filename);
		
		}
	}
	private void openHistory() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		List<String> historyList = new ArrayList<String>(history);
		
		// shave off the last one in the history
		historyList.remove(historyList.size() - 1);

		
		builder.setTitle(R.string.history);
		builder.setItems(historyList.toArray(new CharSequence[historyList.size()]), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        while (history.size() > item + 1) {
		        	history.pop();
		        	webView.goBack();
		        }
		    }
		});
		
		AlertDialog alert = builder.create();
		alert.show();

	}
	
	private void startOpenFileActivity() {
		Intent intent = new Intent(this, OpenFileActivity.class);
		startActivityForResult(intent, 0);
		
	}

	private void startAboutActivity() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
		
	}
	private void setUpWidgets() {
		inputEditText = (EditText) findViewById(R.id.inputEditText);
		webView = (WebView) findViewById(R.id.mainWebView);
		
		inputEditText.setOnKeyListener(this);
		webView.setWebViewClient(new CustomWebViewClient());
		
	}


	private void goToPage(String filename, String mimeType) {
		inputEditText.setText(filename);
		goToPage(mimeType);
	}
	private void goToPage(String mimeType) {
		String filename = inputEditText.getText().toString();
		
		Log.d(TAG, "going to page: " + filename);
		
		try {
			String htmlData = loadTextFile(filename);
			
			webView.loadDataWithBaseURL("file://"+filename,htmlData.toString(), mimeType, "utf-8","file://"+filename);
			history.add(filename);
			inputEditText.setVisibility(View.GONE);
		} catch (FileNotFoundException e) {
			ToastUtil.showErrorToast(getApplicationContext(),R.string.fileNotFound, filename);
			Log.e(TAG, "unhandled exception",e);
		}
		
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	if (webView.canGoBack()) {
	    		webView.goBack();
	    		history.pop();
	    		inputEditText.setText(history.peek());
	    		return true;
	    	}
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		
		// user pressed enter
		if (keyCode == KeyEvent.KEYCODE_ENTER 
				&& event.getAction() == KeyEvent.ACTION_UP) {
			handleUrl(inputEditText.getText().toString());
			dismissSoftKeyboard();
			return true;
		}
		return false;
	}
	

	private String loadTextFile(String filename) throws FileNotFoundException {
		
		File file = new File(filename);
		
		InputStream is = new FileInputStream(file);
		
		BufferedReader buff = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		
		try {
			while (buff.ready()) {
				sb.append(buff.readLine()).append("\n");
			}
		} catch (IOException e) {
			Log.e("AboutActivity","This should not happen",e);
		}
		
		return sb.toString();
		
	}
	private void dismissSoftKeyboard() {
	       InputMethodManager imm =
	           (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	       imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
	       
	}

	private void loadLocalFile(String url) {
		
		String mimeType = fileNameMap.getContentTypeFor(url);
		
		if (mimeType != null && Arrays.asList("text/html","text/plain").contains(mimeType)) {
			goToPage(url, mimeType);
		} else {
			loadLocalNonHtmlFile(url);
		}
		
	}
	private void loadLocalNonHtmlFile(String url) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW); 
		

		String mimeType = fileNameMap.getContentTypeFor(url);
		
		if (mimeType != null) {
			intent.setDataAndType(Uri.parse("file://" + url), mimeType);
		} else if (url.endsWith(".pdf")) {
			// fileNameMap can't figure out pdf mimetype
			intent.setDataAndType(Uri.parse("file://" + url), "application/pdf");
		} else {
			intent.setData(Uri.parse(url));
		}
		
		try {
			startActivity(intent);
		} catch (Exception e) {
			ToastUtil.showErrorToast(getApplicationContext(),R.string.couldntLoadFile, url);
			Log.e(TAG, "unhandled exception",e);
		}
		
	}
	
	private void loadRemoteFile(String url) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW); 

		intent.setData(Uri.parse(url));
		
		try {
		
			startActivity(intent);
		} catch (Exception e) {
			ToastUtil.showErrorToast(getApplicationContext(),R.string.couldntLoadFile, url);
			Log.e(TAG, "unhandled exception",e);
		}
	}
	
	private void handleUrl(String url) {
		
		if (url.startsWith("file://") && url.length() > 7) {
			url = url.substring(7); // trim it off
		}
		url = Uri.decode(url);

		File file = new File(url);
		
		
		if (file.exists()) {
			
			if (loadLocalHtmlFileOrDir(file)) {
				return;
			}
			
		} else {
			if (!history.isEmpty()) {
				String parent = new File(history.peek()).getParent();
				file = new File(parent, url);
				if (file.exists()) {
					
					if (loadLocalHtmlFileOrDir(file)) {
						return;
					}
				}
			}
		}
			
		// assume it's an external page
			
		loadRemoteFile(url);
	
	}
	/**
	 * try to load a local html file, or index.html if the file is a directory.  Returns false if it fails.
	 * @param file
	 * @return
	 */
	private boolean loadLocalHtmlFileOrDir(File file) {
		
		if (file.isDirectory()) {
			
			file = new File(file,"index.html");
			if (file.exists()) {
				loadLocalFile(file.getAbsolutePath());
				return true;
			}
		} else {
			loadLocalFile(file.getAbsolutePath());
			return true;
		}
		return false;
	}

	
	private class CustomWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			handleUrl(url);			

			return true;
			
		}
		
	}

}
