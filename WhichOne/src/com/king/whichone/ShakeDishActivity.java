package com.king.whichone;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.widget.ListView;
import android.widget.Toast;

import com.king.whichone.adapter.DishListAdapter;
import com.king.whichone.bean.DishBean;
import com.king.whichone.database.DishDao;

public class ShakeDishActivity extends BaseActivity {
	private SensorManager sensorManager;
	private Vibrator vibrator;
	private static final int SENSOR_SHAKE = 10;
	private MediaPlayer mediaPlayer;

	private ListView mChoosedListView;
	private DishListAdapter mChoosedAdapter;
	private ArrayList<DishBean> mExistDish;
	private ArrayList<DishBean> mChoosedDish = new ArrayList<DishBean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shake_dish);
		sensorManager = (SensorManager) getSystemService(android.content.Context.SENSOR_SERVICE);
		vibrator = (Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
		initMediaPlay();
		mChoosedAdapter = new DishListAdapter(this, mChoosedDish);
		mChoosedListView = (ListView) findViewById(R.id.choose_list_view);
		mExistDish = (ArrayList<DishBean>) new DishDao(this).getAll();
		mChoosedListView.setAdapter(mChoosedAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterListener();
	}

	public void registerListener() {
		if (sensorManager != null) {
			sensorManager.registerListener(sensorEventListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	public void unregisterListener() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(sensorEventListener);
		}
	}

	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] values = event.values;
			float x = values[0];
			float y = values[1];
			float z = values[2];
			int medumValue = 19;
			if (Math.abs(x) > medumValue || Math.abs(y) > medumValue
					|| Math.abs(z) > medumValue) {
				vibrator.vibrate(200);
				Message msg = new Message();
				msg.what = SENSOR_SHAKE;
				handler.sendMessage(msg);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SENSOR_SHAKE:
				playMusic(ShakeDishActivity.this, mediaPlayer);
				getRandomDish();
				break;
			}
		}

	};

	private void initMediaPlay() {
		try {
			AssetFileDescriptor afd = getResources().openRawResourceFd(
					R.raw.shake_sound_male);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength());
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mediaPlayer.setLooping(false);
			mediaPlayer.prepare();
			afd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void playMusic(Context context, MediaPlayer mediaPlayer) {

		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
			mediaPlayer.seekTo(0);
			mediaPlayer.start();
		}

	}

	public void getRandomDish() {
		Random random = new Random();
		if (mExistDish.size() > 0) {
			int index = random.nextInt(mExistDish.size());
			mChoosedDish.add(mExistDish.get(index));
			mExistDish.remove(index);
			mChoosedAdapter.notifyDataSetChanged();
		} else {
			Toast.makeText(this, getString(R.string.no_to_choose),
					Toast.LENGTH_SHORT).show();
		}
	}
}
