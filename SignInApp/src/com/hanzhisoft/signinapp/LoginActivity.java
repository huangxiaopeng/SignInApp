package com.hanzhisoft.signinapp;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hanzhisoft.signinapp.R;
import com.hanzhisoft.signinapp.user.UserDataSource;
import com.hanzhisoft.signinapp.webservice.WsClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @title: LoginActivity.java
 * @description: 登录界面
 * @copyright: Copyright (c) 2014
 * @company: HanZhiSoft
 * @author HuangXiaoPeng
 * @date 2014-12-7
 * @version 1.0
 */
@SuppressLint("NewApi")
public class LoginActivity extends Activity {
	private final String LOGIN_METHOD_NAME = "doLogin";
	private final String URL = "http://ip/axis2/services/SignInDao?wsdl";
	private final String NAMESPACE = "http://dao.hzsignin.com";
	public UserDataSource userData;
	// private static final String SOAP_ACTION = "SignInDao";
	Map<String, String> maps;
	Button resetUserBtn;
	Button resetPwdBtn;
	Button loginBtn;
	EditText userText = null;
	EditText pwdText = null;
	WsClient wsClient;
	String fahren = null;
	// 感应管理器
	private SensorManager mSensorManager;

	// 震动器
	private Vibrator vibrator;

	// 活动管理器
	ActivityManager activityManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_layout);

		resetUserBtn = (Button) findViewById(R.id.resetUser);
		resetPwdBtn = (Button) findViewById(R.id.resetPwd);
		loginBtn = (Button) findViewById(R.id.loginBtn);
		userText = (EditText) findViewById(R.id.userText);
		pwdText = (EditText) findViewById(R.id.pwdText);
	

		//userText.setText("用户名");
		//pwdText.setText("密码");
		// 初始化按钮事件
		initLoginUI();
		// 检测联网状态
		checkNetWork();
		userData = new UserDataSource(LoginActivity.this);

		if (userData.checkLoginState()) {
			Intent intent = new Intent(LoginActivity.this,
					DesktopActivity.class);
			startActivity(intent);
			finish();
			return;
		}

	}

	// 请求登录接口
	private String requestWs(String wsdl_url, String name_space,
			String method_name, String... params) {

		wsClient = new WsClient(wsdl_url, name_space);

		return wsClient.getSoapObject(method_name, params);

	}

	// 检查网络
	private boolean checkNetWork() {
		if (!isOnline()) {
			Toast.makeText(LoginActivity.this, "当前网络断开", Toast.LENGTH_LONG)
					.show();
			return false;
		}
		return true;
	}

	// 初始化界面
	private void initLoginUI() {
		userText.addTextChangedListener(new LoginEditTextListener(resetUserBtn));
		pwdText.addTextChangedListener(new LoginEditTextListener(resetPwdBtn));
		resetUserBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userText.setHint("请输入用户名");
				userText.setText("");
			}
		});
		resetPwdBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pwdText.setText("");

			}
		});

		loginBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				loginAction();
			}

			private void loginAction() {
				if (checkNetWork()) {
					loginBtn.setEnabled(false);
					LoginAsyncTask task = new LoginAsyncTask();
					task.execute(userText.getText().toString(), pwdText
							.getText().toString());
					// Intent intent = new Intent(LoginActivity.this,
					// DesktopActivity.class);
					// startActivity(intent);
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.exit(0);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			System.exit(0);
		}
		return super.onOptionsItemSelected(item);
	}

	protected boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}
	}

	class LoginEditTextListener implements TextWatcher {
		Button btn;

		public LoginEditTextListener(Button btn) {
			this.btn = btn;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {

			if (!s.toString().equals("")) {
				btn.setVisibility(View.VISIBLE);

			} else {

				btn.setVisibility(View.INVISIBLE);
			}

		}

	}

	// 异步调用WebService
	private class LoginAsyncTask extends AsyncTask<String, String, String> {
		// 开始请求数据
		@Override
		protected String doInBackground(String... args) {

			boolean isEmptyParams = args[0].trim().equals("")
					|| args[1].trim().equals("");
			Map<String, String> params = new HashMap<>();

			return isEmptyParams ? null : requestWs(URL, NAMESPACE,
					LOGIN_METHOD_NAME, "name", args[0], "password", args[1]);
		}

		// 执行完毕后
		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				showLoginErrorTips("用户名密码不能为空");

				return;
			}
			try {
				// 解析返回结果

				if (!"anyType{}".equals(result)) {
					Document doc;

					doc = DocumentHelper.parseText(result);

					Element rootElt = doc.getRootElement();
					String uName = rootElt.element("userName").getText()
							.toString();
					String uPwd = rootElt.element("userPwd").getText()
							.toString();
					if (uName.trim().equals("") || uPwd.trim().equals("")) {
						showLoginErrorTips("用户名密码有误");
						return;
					}
					userData.login(uName, uPwd);
					Intent intent = new Intent(LoginActivity.this,
							DesktopActivity.class);

					startActivity(intent);
					finish();
				} else {
					showLoginErrorTips("用户名密码有误");
				}
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// 提示登录错误
		private void showLoginErrorTips(String msg) {
			loginBtn.setEnabled(true);
			userText.setText("");
			pwdText.setText("");
			userText.setHint(msg);

		}

	}

}
