package com.king.whichone;

import java.io.File;
import java.net.URI;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.king.whichone.bean.DishBean;
import com.king.whichone.database.DishDao;
import com.king.whichone.util.ImageUtil;
import com.king.whichone.util.StringUtil;

public class AddNewDishActivity extends BaseActivity implements OnClickListener {
	public static final String TAG = "AddNewDishActivity";
	private static final int PHOTO_PICKED_WITH_NAME = 0;

	ImageButton mImage;
	TextView mImageTip;
	TextView mNameText;
	TextView mDesText;
	Button mSubmitBtn;

	String mFilePath = null;// 相册选择的路径
	Bitmap mImageBitmap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_new_dish);
		findView();
		initView();
	}

	public void findView() {
		mImage = (ImageButton) this.findViewById(R.id.add_image_view);
		mImageTip = (TextView) this.findViewById(R.id.add_image_tip);
		mNameText = (TextView) this.findViewById(R.id.add_dish_name_edit);
		mDesText = (TextView) this.findViewById(R.id.add_dish_des_edit);
		mSubmitBtn = (Button) this.findViewById(R.id.add_dish_submit);
	}

	public void initView() {
		mImage.setOnClickListener(this);
		mSubmitBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_dish_submit:
			Log.e(TAG, "add_dish_submit");
			doInsert();
			setResult(RESULT_OK);
			this.finish();
			break;

		case R.id.add_image_view:
			Log.e(TAG, "add_image_layout");
			String[] choices = new String[2];
			choices = new String[2];
			choices[0] = getString(R.string.choose_from_camera);
			choices[1] = getString(R.string.choose_from_gallery);
			Dialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.choose_from)
					.setSingleChoiceItems(choices, -1,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										Log.e(TAG, "0");
										pickPhotoFromCamera();
										break;

									case 1:
										Log.e(TAG, "1");
										pickPhotoFromGallery();
										break;
									}
									dialog.dismiss();
								}
							}).create();
			dialog.show();
			break;
		}

	}

	// 插入一条数据到数据库
	public void doInsert() {
		if (DishApp.mIsAddDishBatch) {
			DishDao dis = new DishDao(getApplication());
			String[] dishs = getResources().getStringArray(R.array.dish_list);
			for (int i = 0; i < dishs.length; i++) {
				dis.insert(new DishBean(dishs[i], mFilePath, dishs[i]));
			}
		} else {
			if (checkData()) {
				String name = mNameText.getText().toString();
				String des = mDesText.getText().toString();
				DishDao dis = new DishDao(getApplication());
				dis.insert(new DishBean(name, mFilePath, des));
			}
		}

	}

	// 检查数据完整性
	public boolean checkData() {
		boolean isOk = false;
		String name = mNameText.getText().toString();
		String des = mDesText.getText().toString();
		if (mFilePath != null && mImageBitmap != null
				&& !StringUtil.isEmpty(name) && !StringUtil.isEmpty(des)) {
			isOk = true;
		}
		return isOk;
	}

	public void pickPhotoFromCamera() {

	}

	public void pickPhotoFromGallery() {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");

			startActivityForResult(intent, PHOTO_PICKED_WITH_NAME);
		} catch (Exception e) {
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		final boolean isKitKat = Build.VERSION.SDK_INT >= 19;
		if (requestCode == PHOTO_PICKED_WITH_NAME) {
			if (data != null) {
				Uri uri = data.getData();
				if (uri != null) {
					mFilePath = null;
					if (isKitKat) {
						// 4.4版本格式改变为
						// content://com.android.providers.media.documents/document/image:62
						String id = uri.getLastPathSegment().split(":")[1];
						final String[] imageColumns = { MediaStore.Images.Media.DATA };
						Cursor cursor = managedQuery(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								imageColumns, MediaStore.Images.Media._ID + "="
										+ id, null, null);

						if (cursor.moveToFirst()) {
							mFilePath = cursor
									.getString(cursor
											.getColumnIndex(MediaStore.Images.Media.DATA));
						}
						cursor = null;
					} else {
						Cursor cursor = managedQuery(uri, null, null, null,
								null);
						try {
							if (cursor != null) {
								cursor.moveToFirst();
								mFilePath = cursor.getString(1);
							} else {
								String path = uri.toString();
								if (path.toLowerCase(Locale.getDefault())
										.startsWith("file://"))
									mFilePath = (new File(URI.create(path)))
											.getAbsolutePath();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (cursor != null) {
								// 4.0以上的版本会自动关闭 (4.0--14;; 4.0.3--15)
								if (Build.VERSION.SDK_INT < 14) {
									cursor.close();
								}
								cursor = null;
							}
						}
					}
				}
			}
			if (mFilePath != null) {
				mImageBitmap = ImageUtil.createBitmap(mFilePath, 400);
				mImage.setImageBitmap(mImageBitmap);
				mImageTip.setVisibility(View.GONE);
			}
		}
	}
}
