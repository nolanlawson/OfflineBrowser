package com.nolanlawson.offlinebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class OpenFileActivity extends ListActivity implements OnClickListener {
	
	private EditText editText;
	private Button okButton, cancelButton;
	private FileListAdapter adapter;
	private File currentDirectory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.open_file);
		
		setUpWidgets();
		
		// no sd card
		if (! isExternalStorageAvailable()) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.noSdCard), Toast.LENGTH_LONG).show();
			finish();
		} else {
			adapter = new FileListAdapter(getApplicationContext(), R.layout.file_item, new ArrayList<File>());
			
			setListAdapter(adapter);
			
			showFilesInDir(Environment.getExternalStorageDirectory());
		}
		
	}
	
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if (! isExternalStorageAvailable()) {	
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.noSdCard), Toast.LENGTH_LONG).show();
			finish();
		} else {
			File file = adapter.getItem(position);
			
			if (file instanceof DummyFile) {
				// go up one dir; this is the ".." folder
				showFilesInDir(currentDirectory.getParentFile());
			} else if (file.isDirectory()) {
				showFilesInDir(file);
			} else {
				editText.setText(file.getAbsolutePath());
			}
		}
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
		// go up a level
		if (!currentDirectory.equals(Environment.getExternalStorageDirectory())) {
			showFilesInDir(currentDirectory.getParentFile());
			return true;
		}
	    }

	    return super.onKeyDown(keyCode, event);
	}

	private boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();

		return state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
	}

	private void showFilesInDir(File dir) {
		
		File[] fileArray = dir.listFiles();
		
		List<File> fileList = fileArray != null 
				? new ArrayList<File>(Arrays.asList(fileArray)) : Collections.<File>emptyList();
		
		Collections.sort(fileList, sortFilesByFolderThenName);
		
		adapter.clear();

		if (!dir.equals(Environment.getExternalStorageDirectory())) { // highest I'm going to allow the user to go
			adapter.add(new DummyFile());
		}
		
		for (File file : fileList) {
			adapter.add(file);
		}
		
		currentDirectory = dir;
		editText.setText(dir.getAbsolutePath());
		
		
	}

	private void setUpWidgets() {
		
		okButton = (Button) findViewById(R.id.okButton);
		cancelButton = (Button) findViewById(R.id.cancelButton);
		editText = (EditText) findViewById(R.id.openFileEditText);
		
		okButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancelButton:
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.okButton:
			
			String filename = editText.getText().toString();
			
			if (new File(filename).exists()) {

				Intent intent = new Intent();
				intent.putExtra("filename", filename);
				
				setResult(RESULT_OK, intent);
				finish();
			} else {
				ToastUtil.showErrorToast(getApplicationContext(), R.string.fileNotFound, filename);
			}
			
			break;
		}
		
	}	

	private static Comparator<File> sortFilesByFolderThenName = new Comparator<File>(){

		@Override
		public int compare(File arg0, File arg1) {
			
			if (arg0 instanceof DummyFile) {
				return -1; // list ".." first
			} else if (arg1 instanceof DummyFile) {
				return 1; // list ".." first
			}
			
			else if ((!arg0.isDirectory() && arg1.isDirectory()) 
					|| (arg0.isDirectory() && !arg1.isDirectory())) {
				return arg0.isDirectory() ? -1 : 1;
			}
			
			
			return arg0.getName().compareTo(arg1.getName());
		}};


	
	
}
