package com.hanzhisoft.signinapp.signin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ksoap2.serialization.PropertyInfo;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.hanzhisoft.signinapp.R;
import com.hanzhisoft.signinapp.R.id;
import com.hanzhisoft.signinapp.R.layout;
import com.hanzhisoft.signinapp.R.raw;
import com.hanzhisoft.signinapp.data.SignInDataSource;
import com.hanzhisoft.signinapp.data.SignRecord;
import com.hanzhisoft.signinapp.user.UserDataSource;
import com.hanzhisoft.signinapp.util.SoundUtils;
import com.hanzhisoft.signinapp.webservice.WsClient;

import android.app.Activity;
import android.app.Service;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @title: SignInActivity.java
 * @description: 签到页面
 * @copyright: Copyright (c) 2014
 * @company: HanZhiSoft
 * @author HuangXiaoPeng
 * @date 2014-12-7
 * @version 1.0
 */
public class SignInActivity extends Activity implements SensorEventListener {
	private final String SIGN_IN_METHOD_NAME = "doSaveSigninInfo";
	private final String URL = "http://ip/axis2/services/SignInDao?wsdl";
	private final String NAMESPACE = "http://dao.hzsignin.com";
	public LocationClient locationClient;
	public TextView currentLocation;
	public ProgressBar pgb;
	public TextView locationState;
	public TextView locatingState;
	public Button locaBtn;
	public Button signBtn;
	public TextView succssImg;
	public SignInDataSource signInDataSource;
	public UserDataSource userData;
	public boolean mSoundLoaded;
	public SoundPool soundPool;
	public WsClient wsClient;
	public String address;
	// 签到参数
	public String location_x;
	public String location_y;
	public String addrStr;
	// 感应
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Vibrator vibrator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in_layout);
		initUI();
		initSensor();
		locationClient = new LocationClient(SignInActivity.this);
		locationClient.registerLocationListener(new HZSignLocationListener(
				locationClient));
		initLocationOptions(locationClient);
		succssImg = (TextView) findViewById(R.id.success_sign);
		userData = new UserDataSource(SignInActivity.this);
		wsClient = new WsClient(URL, NAMESPACE);
		signBtn.setEnabled(false);
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				mSoundLoaded = true;
			}
		});

		soundPool.load(this, R.raw.begin, 1);
		soundPool.load(this, R.raw.end, 1);
		soundPool.load(this, R.raw.signin2, 1);

	}

	private void initSensor() {
		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	private void initUI() {
		currentLocation = (TextView) findViewById(R.id.currentLocation);
		locationState = (TextView) findViewById(R.id.locationState);
		pgb = (ProgressBar) findViewById(R.id.locationProgress);
		locaBtn = (Button) findViewById(R.id.location_btn);
		locatingState = (TextView) findViewById(R.id.locatingState);
		signBtn = (Button) findViewById(R.id.signin_btn);
		signBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				signBtn.setEnabled(false);
				signBtn.setText("正在签到，请稍候……");
				SignInTask signTask = new SignInTask();
				signTask.execute();

			}
		});
		locaBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SoundUtils.play(soundPool, 1, mSoundLoaded);
				startLocation();
			}
		});
	}

	// start location
	private void startLocation() {
		succssImg.setVisibility(View.INVISIBLE);
		locatingState.setVisibility(View.VISIBLE);
		locationState.setVisibility(View.INVISIBLE);
		locaBtn.setEnabled(false);
		signBtn.setEnabled(false);

		locationClient.start();
		currentLocation.setVisibility(View.INVISIBLE);
		pgb.setVisibility(View.VISIBLE);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	public void initLocationOptions(LocationClient locationClient) {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setLocationMode(LocationMode.Device_Sensors);// 设置定位模式
		option.setCoorType("gcj02");// 返回的定位结果是百度经纬度，默认值gcj02
		option.setTimeOut(10000);
		option.setScanSpan(1000);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true); // 是否返回中文地址

		locationClient.setLocOption(option);

	}

	public class HZSignLocationListener implements BDLocationListener {
		LocationClient locationClient;

		public HZSignLocationListener(LocationClient locationClient) {
			this.locationClient = locationClient;
		}

		@Override
		public void onReceiveLocation(BDLocation location) {

			addrStr = location.getAddrStr();
			currentLocation.setText(addrStr);
			location_x = location.getLongitude() + "";
			location_y = location.getLatitude() + "";
			locationClient.stop();
			pgb.setVisibility(View.INVISIBLE);
			currentLocation.setVisibility(View.VISIBLE);
			locatingState.setVisibility(View.INVISIBLE);
			locationState.setVisibility(View.VISIBLE);
			locaBtn.setEnabled(true);
			signBtn.setEnabled(true);
			signBtn.setText("确认签到");
			SoundUtils.play(soundPool, 2, mSoundLoaded);
			mSensorManager.registerListener(SignInActivity.this,
					mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	class SignInTask extends AsyncTask<String, String, String> {

		private void saveLocation(String addr) {
			signInDataSource = new SignInDataSource(SignInActivity.this);
			SignRecord sr = new SignRecord();
			sr.setSignId(System.currentTimeMillis() + "");
			sr.setAddress(addr);
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy年MM月dd日 HH时mm分ss秒 E ");
			sr.setSignTime(dateFormat.format(new Date()));
			signInDataSource.addSignRecord(sr);
		}

		@Override
		protected String doInBackground(String... args) {

			String[] params = { "username", userData.getUser().getUserName(),
					"location_x", location_x, "location_y", location_y,
					"signaddress", addrStr };
			String result = wsClient.getSoapObject(SIGN_IN_METHOD_NAME, params);

			return result;
		}

		@Override
		protected void onPostExecute(String result) {

			if (!result.trim().equals("true")) {
				succssImg.setText(result);

			} else {
				signBtn.setText("签到成功");
				SoundUtils.play(soundPool, 3, mSoundLoaded);

				succssImg.setVisibility(View.VISIBLE);

				saveLocation(addrStr);

			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();

		// 读取摇一摇敏感值
		float shakeSenseValue = 18.5f;

		// values[0]:X轴，values[1]：Y轴，values[2]：Z轴
		float[] values = event.values;

		if (sensorType == Sensor.TYPE_ACCELEROMETER) {
			if ((Math.abs(values[0]) >= shakeSenseValue
					|| Math.abs(values[1]) >= shakeSenseValue || Math
					.abs(values[2]) >= shakeSenseValue)) {
				// 触发事件，执行打开应用行为
				startLocation();
				SoundUtils.play(soundPool, 1, mSoundLoaded);
				mSensorManager.unregisterListener(this);

				vibrator.vibrate(150); // 振动 millsecond 振动毫秒
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {

		super.onResume();

		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
