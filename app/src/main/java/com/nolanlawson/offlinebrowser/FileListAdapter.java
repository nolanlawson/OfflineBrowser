package com.nolanlawson.offlinebrowser;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<File> {
	
	private List<File> items;
	int resId;
	
	public FileListAdapter(Context context, int resId,
			List<File> items) {
		super(context, resId, items);
		
		this.items = items;
		this.resId = resId;
		
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		Context context = parent.getContext();
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(resId, parent, false);
		}
		
		TextView text = (TextView) view.findViewById(R.id.file_item_text);
		ImageView icon = (ImageView) view.findViewById(R.id.file_item_icon);
		
		File file = items.get(position);
		
		if (file.exists() || file instanceof DummyFile) {
		
			text.setText(file.getName());
			if (file.isDirectory()) {
				icon.setImageResource(R.drawable.folder_icon);
			} else {
				icon.setImageResource(R.drawable.file_icon);
			}
		
		} else {
			text.setText(context.getResources().getString(R.string.doesNotExist));
			icon.setImageResource(R.drawable.file_icon);
		}
		
		return view;
		
	}
	

}
