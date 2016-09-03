package com.example.jaeseok.airshare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileList extends ListView {

	private Context _Context = null;
	private static LayoutInflater mInflator;
	private ArrayList<String> _List = new ArrayList<String>();
	private ArrayList<String> _FolderList = new ArrayList<String>();
	private ArrayList<String> _FileList = new ArrayList<String>();
	private ListViewAdapter _Adapter = null;

	private String _Path = "";

	static File[] files;
	static File file;

	private OnPathChangedListener _OnPathChangedListener = null;
	private OnFileSelectedListener _OnFileSelectedListener = null;

	public FileList(Context context, LayoutInflater mInflator) {
		super(context);
		init(context, mInflator);
	}
	
	private void init(Context context, LayoutInflater mInflator) {
		_Context = context;
		this.mInflator = mInflator;
        setOnItemClickListener(_OnItemClick);
	}
	
	private boolean openPath(String path) {
		_FolderList.clear();
		_FileList.clear();
		
        file = new File(path);
		if (!file.canRead())
			return false;

        files = file.listFiles();

        for (int i=0; i<files.length; i++) {
        	if (files[i].isDirectory()) {
        		_FolderList.add("<" + files[i].getName() + ">");
        	} else {
        		_FileList.add(files[i].getName());
        	}
        }
        
        Collections.sort(_FolderList);
        Collections.sort(_FileList);
        
		if(!path.equals("/")) {
			_FolderList.add(0, "<..>");
			_FolderList.add(0, "<.>");
		}
		
        return true;
	}
	
	private void updateAdapter() {
		_List.clear();
        _List.addAll(_FolderList);
        _List.addAll(_FileList);
        
		_Adapter = new ListViewAdapter();
        setAdapter(_Adapter);
	}

	private class ViewHolder {
		public ImageView mIcon;
		public TextView mFileName;
	}

	private class ListViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return _List.size();
		}

		@Override
		public Object getItem(int position) {
			return _List.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ViewHolder holder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.file_item, null);
				holder = new ViewHolder();
				holder.mIcon = (ImageView) view.findViewById(R.id.default_file);
				holder.mFileName = (TextView) view.findViewById(R.id.file_name);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			String mData = _List.get(position);

			if (mData.matches("<.*>")) {
				holder.mIcon.setImageResource(R.drawable.ic_list_folder);
				holder.mFileName.setText(mData.substring(1, mData.length() - 1));
			} else {
				holder.mIcon.setImageResource(R.drawable.ic_list_file);
				holder.mFileName.setText(mData);
			}

			return view;
		}
	}

	public void setPath(String value) {
		
		if (value.length() == 0) {
			value = "/";
		} else {
			String lastChar = value.substring(value.length()-1, value.length());
			if (lastChar.matches("/") == false) value = value + "/"; 
		}
		
		if (openPath(value)) {
			_Path = value;
			updateAdapter();	        
			if (_OnPathChangedListener != null)
				_OnPathChangedListener.onChanged(true);
		} else {
			if (_OnPathChangedListener != null)
				_OnPathChangedListener.onChanged(false);
		}
	}

	public String getPath() {
		return _Path;
	}
	
	public void setOnPathChangedListener(OnPathChangedListener value) {
		_OnPathChangedListener = value;
	}

	public void setOnFileSelected(OnFileSelectedListener value) {
		_OnFileSelectedListener = value;
	}


	
	private String deleteLastFolder(String value) {
		String list[] = value.split("/");

		String result = "";
		
		for (int i=0; i<list.length-1; i++) {
			result = result + list[i] + "/"; 
		}
		
		return result;
	}

	private String getRealPathName(String newPath, int position) {
		String path = newPath.substring(1, newPath.length() - 1);
		if (path.equals(".") && position == 0) {
			return _Path;
		} else if(path.equals("..") && position == 1) {
			return deleteLastFolder(_Path);
		} else {
			return _Path + path + "/";
		}
	}

	private OnItemClickListener _OnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			String fileName = getItemAtPosition(position).toString();
			if (fileName.matches("<.*>")) {
				setPath(getRealPathName(fileName, position));
			} else {
				if (_OnFileSelectedListener != null) _OnFileSelectedListener.onSelected(_Path, fileName);
			}
		}
	};
}
