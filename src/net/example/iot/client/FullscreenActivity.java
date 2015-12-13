package net.example.iot.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.example.iot.R;
import net.example.iot.client.util.SystemUiHider;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the
 * system UI (i.e. status bar and navigation/system bar)
 * with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements SensorEventListener {

	private static final String TAG = FullscreenActivity.class.getSimpleName();
	/**
	 * Whether or not the system UI should be auto-hidden
	 * after {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of
	 * milliseconds to wait after user interaction before
	 * hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon
	 * interaction. Otherwise, will show the system UI
	 * visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to
	 * {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this
	 * activity.
	 */
	private SystemUiHider mSystemUiHider;
	private SensorManager manager;
	private WebSocketClient mClient;

	//	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
			// Cached values.
			int mControlsHeight;
			int mShortAnimTime;

			@Override
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
			public void onVisibilityChange(boolean visible) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
					// If the ViewPropertyAnimator API is available
					// (Honeycomb MR2 and later), use it to animate the
					// in-layout UI controls at the bottom of the
					// screen.
					if (mControlsHeight == 0) {
						mControlsHeight = controlsView.getHeight();
					}
					if (mShortAnimTime == 0) {
						mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					}
					controlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
				} else {
					// If the ViewPropertyAnimator APIs aren't
					// available, simply show or hide the in-layout UI
					// controls.
					controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
				}

				if (visible && AUTO_HIDE) {
					// Schedule a hide().
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
			}
		});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
		manager = (SensorManager) getSystemService(SENSOR_SERVICE);
		try {
			Log.d(TAG, "ws");
			URI uri = new URI("ws://192.168.1.6:8080");
			mClient = new WebSocketClient(uri) {
				@Override
				public void onOpen(ServerHandshake handshake) {
					Log.d(TAG, "onOpen");
				}

				//
				//
				@Override
				public void onMessage(final String message) {

					//					Log.d(TAG, "onMessage");
					//					Log.d(TAG, "Message:" + message);
					//					mHandler.post(new Runnable() { // ----2
					//						@Override
					//						public void run() {
					//							Toast.makeText(FullscreenActivity.this, message, Toast.LENGTH_SHORT).show();
					//						}
					//					});
				}

				@Override
				public void onError(Exception ex) {
					Log.d(TAG, "onError");
					Log.d(TAG, ex.getMessage());
					ex.printStackTrace();
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					Log.d(TAG, "onClose" + reason + remote);
					System.err.println();
				}
			};

			try {
				Log.d(TAG, "url3");
				mClient.connect();
				Log.d(TAG, "url4");
			} catch (Exception e) {
				Log.d(TAG, e.getMessage(), e);

			}

		} catch (URISyntaxException e) {
			Log.d(TAG, e.getMessage(), e);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to
	 * delay hiding the system UI. This is to prevent the
	 * jarring behavior of controls going away while
	 * interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds,
	 * canceling any previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	protected void onStop() {
		super.onStop();
		manager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		//
		List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			Sensor s = sensors.get(0);
			manager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.v("onAccuracyChanged", sensor.toString() + Integer.valueOf(accuracy));

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		try {
			JSONObject json = new JSONObject();
			json.put("accuracy", event.accuracy);
			json.put("timestamp", event.timestamp);
			json.put("values", new JSONArray(event.values));
			json.put("sensor", SensorUtils.toJSON(event.sensor));
			//			Log.v("json", json.toString());
			try {
				//

				//
				mClient.send(json.toString());
			} catch (Exception e) {
				//				Log.v(TAG, e.getMessage(), e);
				//				e.printStackTrace();
			}
			//			if (mClient.getConnection().isOpen()) {
			//			}
		} catch (JSONException e) {
			Log.e("onException", e.getMessage(), e);
			e.printStackTrace();
		}
	}
}
