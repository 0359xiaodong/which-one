package com.king.whichone.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.king.whichone.R;
import com.king.whichone.bean.DishBean;
import com.king.whichone.database.DishDao;
import com.king.whichone.imagepool.ImageLoader;

public class DishListAdapter extends BaseAdapter {
	public static final String TAG = "DishListAdapter";
	ArrayList<DishBean> mDishList;
	Context mContext;
	LayoutInflater mInflater;
	DishDao mDishDao;
	ImageLoader mImageLoader;
	public DishListAdapter(Context context, ArrayList<DishBean> dishList) {
		mDishList = dishList;
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mDishDao = new DishDao(mContext);
		mImageLoader = new ImageLoader(mContext);
	}

	@Override
	public int getCount() {
		return mDishList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public final class ViewHolder {
		public ImageView photoImage;
		public TextView nameText;
		public TextView desText;
		public ImageView deleteImage;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.dish_item, null);
			holder.photoImage = (ImageView) convertView
					.findViewById(R.id.dish_item_image);
			holder.nameText = (TextView) convertView
					.findViewById(R.id.dish_item_name);
			holder.desText = (TextView) convertView
					.findViewById(R.id.dish_item_des);
			holder.deleteImage = (ImageView) convertView
					.findViewById(R.id.dish_item_delete);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		// Bitmap bitmap = ImageUtil.createThumbnail(mDishList.get(position)
		// .getImagePath(), ImageUtil.dip2px(mContext, 80), ImageUtil
		// .dip2px(mContext, 80));
		// holder.photoImage.setImageBitmap(bitmap);
		mImageLoader.DisplayImage(mDishList.get(position)
				.getImagePath(), holder.photoImage, R.drawable.default_dish,80);
		holder.nameText.setText(mDishList.get(position).getDishName());
		holder.desText.setText(mDishList.get(position).getDishDes());
		holder.deleteImage
				.setOnClickListener(new DeleteClickListener(position));
		return convertView;
	}

	public class DeleteClickListener implements OnClickListener {

		private int position;

		public DeleteClickListener(int positon) {
			this.position = positon;
		}

		@Override
		public void onClick(View v) {
			Log.e(TAG, "delete position " + position);
			if (mDishDao.delete(mDishList.get(position))) {
				mDishList.remove(position);
				notifyDataSetChanged();
			}
		}
	}
}
