package net.example.iot.client;

import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.Sensor;

/**
 * @author Masayuki Higashino
 */
public class SensorUtils {

	public static JSONObject toJSON(Sensor s) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("FifoMaxEventCount", s.getFifoMaxEventCount());
		json.put("FifoReservedEventCount", s.getFifoReservedEventCount());
		json.put("MinDelay", s.getMinDelay());
		json.put("Type", s.getType());
		json.put("Version", s.getVersion());
		json.put("MaximumRange", s.getMaximumRange());
		json.put("Name", s.getName());
		json.put("Power", s.getPower());
		json.put("Resolution", s.getResolution());
		json.put("Vendor", s.getVendor());
		return json;
	}

}
