package com.dozuki.ifixit.view.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.ui.MediaAdapter.ViewHolder;

public class MediaFragment extends SherlockFragment implements
		OnItemClickListener, OnClickListener {
	protected static final String IMAGE_URL = "IMAGE_URL";
	protected static final String LOCAL_URL = "LOCAL_URL";
	private static MediaFragment thisInstance;
	private Context mContext;
	static final int SELECT_PICTURE = 1;
	static final int CAMERA_PIC_REQUEST = 2;
	private String selectedImagePath;
	private String filemanagerstring;
	GridView mGridView;
	MediaAdapter galleryAdapter;

	private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			APIService.Result result = (APIService.Result) intent.getExtras()
					.getSerializable(APIService.RESULT);

			if (!result.hasError()) {

				// Log.e("Login Activity", "Welcome " + user.getUsername());
			}
		}
	};

	public static MediaFragment newInstance() {
		MediaFragment mFrgment = new MediaFragment();
		return mFrgment;
	}

	/**
	 * Required for restoring fragments
	 */
	public MediaFragment() {
	}

	public static MediaFragment getInstance() {
		if (thisInstance == null)
			thisInstance = new MediaFragment();

		return thisInstance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.media, container, false);

		mGridView = (GridView) view.findViewById(R.id.gridview);
		galleryAdapter = new MediaAdapter(mContext, mGridView, this);
		mGridView.setAdapter(galleryAdapter);
		mGridView.setOnItemClickListener(this);

		if (savedInstanceState != null) {
			Log.i("MediaFrag", "rebuilding view");
			String arr[] = savedInstanceState.getStringArray("URIs");
			boolean c_arr[] = savedInstanceState.getBooleanArray("checked");
			ArrayList<Uri> uriArr = new ArrayList<Uri>();
			ArrayList<Boolean> cList = new ArrayList<Boolean>();
			for (int i = 0; i < arr.length; i++) {
				uriArr.add(Uri.parse(arr[i]));
				cList.add(c_arr[i]);
			}
			galleryAdapter.setMediaList(uriArr);	
			galleryAdapter.setCheckedList(cList);
		}

		((Button) view.findViewById(R.id.gallery_button))
				.setOnClickListener(this);

		((Button) view.findViewById(R.id.camera_button))
				.setOnClickListener(this);
		
		((Button) view.findViewById(R.id.delete_button))
		.setOnClickListener(this);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		ArrayList<Uri> mArr = galleryAdapter.getMediaList();
		String arr[] = new String[mArr.size()];
		boolean checked_arr[] = new boolean[galleryAdapter.getCheckedList().size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = mArr.get(i).getEncodedPath();
			checked_arr[i] = galleryAdapter.getCheckedList().get(i);
		}
		savedInstanceState.putStringArray("URIs", arr);
		savedInstanceState.putBooleanArray("checked", checked_arr);
		Log.i("MediaFragment", "on save instance state");
	}

	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		ViewHolder cell = (ViewHolder) view.getTag();
		
		Uri uri = galleryAdapter.getImageAt(position);
		String url = getPath(uri);
		Intent intent = new Intent(mContext, FullImageViewActivity.class);
		intent.putExtra(IMAGE_URL, url);
		intent.putExtra(LOCAL_URL, true);
		startActivity(intent);
	}
	
	public void expandImage(int position)
	{
		Log.i("MediaFragment", "On item Click num: " + position);
		Uri uri = galleryAdapter.getImageAt(position);
		String url = getPath(uri);
		Intent intent = new Intent(mContext, FullImageViewActivity.class);
		intent.putExtra(IMAGE_URL, url);
		intent.putExtra(LOCAL_URL, true);
		startActivity(intent);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			// topicSelectedListener = (TopicSelectedListener)activity;
			mContext = (Context) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement TopicSelectedListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		IntentFilter filter = new IntentFilter();
		filter.addAction(APIService.ACTION_LOGIN);
		mContext.registerReceiver(mApiReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();

		try {
			mContext.unregisterReceiver(mApiReceiver);
		} catch (IllegalArgumentException e) {
			// Do nothing. This happens in the unlikely event that
			// unregisterReceiver has been called already.
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.gallery_button:
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					SELECT_PICTURE);
			break;

		case R.id.camera_button:
			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			//TODO: This is fucked
			try {
				File outFile = mContext.getFileStreamPath("test");
				outFile.createNewFile();
				outFile.setWritable(true, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
			break;
		case R.id.delete_button:
			ArrayList<Uri> deleteList = new ArrayList<Uri>();
			for(int i = 0; i < galleryAdapter.getCount() ; i++)
			{
				if(galleryAdapter.isChecked(i))
				{
					deleteList.add(galleryAdapter.getImageAt(i));
				}
			}
			for(Uri uri : deleteList)
			{
				galleryAdapter.getMediaList().remove(uri);
			}

			galleryAdapter.checkedList = new ArrayList<Boolean>(galleryAdapter.getMediaList().size());
			for(int i = 0 ; i < galleryAdapter.getCount() ; i++)
			{
				galleryAdapter.checkedList.add(false);
			}
			galleryAdapter.invalidatedView();
			
			break;
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = ((Activity) mContext).managedQuery(uri, projection,
				null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == MediaFragment.SELECT_PICTURE) {
				Log.i("", "in media frag");
				Uri selectedImageUri = data.getData();

				galleryAdapter.addUri(selectedImageUri);
				/*
				 * // OI FILE Manager filemanagerstring =
				 * selectedImageUri.getPath();
				 * 
				 * // MEDIA GALLERY selectedImagePath =
				 * getPath(selectedImageUri);
				 * 
				 * // DEBUG PURPOSE - you can delete this if you want if
				 * (selectedImagePath != null)
				 * System.out.println(selectedImagePath); else
				 * System.out.println("selectedImagePath is null"); if
				 * (filemanagerstring != null)
				 * System.out.println(filemanagerstring); else
				 * System.out.println("filemanagerstring is null");
				 * 
				 * // NOW WE HAVE OUR WANTED STRING if (selectedImagePath !=
				 * null) System.out
				 * .println("selectedImagePath is the right one for you!"); else
				 * System.out
				 * .println("filemanagerstring is the right one for you!");
				 */
			} else if (requestCode == MediaFragment.CAMERA_PIC_REQUEST) {
				Log.i("mediact", "ret from camera image: ");
				Uri selectedImageUri = data.getData();
				
				galleryAdapter.addUri(selectedImageUri);

				// Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
			}
		}
	}

}
