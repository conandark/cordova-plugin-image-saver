package com.bigtickets.ImageSaver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class ImageSaver extends CordovaPlugin {

	private static final String ACTION = "saveImageToLibrary";

	private static final String WRITE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
	private static final int REQUEST_WRITE_PERMISSION = 97483;

	private String base64;
	private CallbackContext callback;

	@Override
	public boolean execute(String action, JSONArray data,
		CallbackContext callbackContext) throws JSONException {

		if (action.equals(ACTION)) {

			this.base64 = data.optString(0);
			this.callback = callbackContext;

			// Check permission
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || cordova.hasPermission(WRITE)) {
				savePhoto();
			} else {
				cordova.requestPermission(this, REQUEST_WRITE_PERMISSION, WRITE);
			}

			return true;
		} else {
			return false;
		}
	}

	private void savePhoto() throws JSONException {
		if (base64 == null || callback == null) {
			return;
		}

		if (base64.equals("")) {
			callback.error("Missing base64 string");
		} else {
			// Create the bitmap from the base64 string
			byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
			Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			if (bmp == null) {
				callback.error("The image could not be decoded");
			} else {
				// Save the image
				File imageFile = writeBitmap(bmp);
				if (imageFile == null) {
					callback.error("Error while saving image");
				} else {
					// Update image gallery
					scanPhoto(imageFile);
					callback.success(imageFile.toString());
				}
			}
		}

		base64 = null;
		callback = null;
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions,
		int[] grantResults) throws JSONException {
		if (callback == null) {
			return;
		}

		if (permissions != null && permissions.length > 0) {
			if (cordova.hasPermission(WRITE)) {
				savePhoto();
				return;
			}
		}

		callback.error("The image could not be saved");
	}

	private File writeBitmap(Bitmap bmp) {
		File retVal = null;

		try {
			Calendar c = Calendar.getInstance();
			String date = "" + c.get(Calendar.DAY_OF_MONTH) +
				c.get(Calendar.MONTH) +
				c.get(Calendar.YEAR) +
				c.get(Calendar.HOUR_OF_DAY) +
				c.get(Calendar.MINUTE) +
				c.get(Calendar.SECOND);

			String deviceVersion = Build.VERSION.RELEASE;
			Log.i("ImageSaver", "Android version " + deviceVersion);
			int check = deviceVersion.compareTo("2.3.3");

			File folder;
			/*
			 * File path = Environment.getExternalStoragePublicDirectory(
			 * Environment.DIRECTORY_PICTURES ); //this throws error in Android
			 * 2.2
			 */
			if (check >= 1) {
				folder = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

				if (!folder.exists()) {
					folder.mkdirs();
				}
			} else {
				folder = Environment.getExternalStorageDirectory();
			}

			File imageFile = new File(folder, "c2i_" + date.toString() + ".png");

			FileOutputStream out = new FileOutputStream(imageFile);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();

			retVal = imageFile;
		} catch (Exception e) {
			Log.e("ImageSaver", "An exception occured while saving image: " +
				e.toString());
		}
		return retVal;
	}

	/* Invoke the system's media scanner to add your photo to the Media Provider's database, 
	 * making it available in the Android Gallery application and to other apps. */
	private void scanPhoto(File imageFile) {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri contentUri = Uri.fromFile(imageFile);
		mediaScanIntent.setData(contentUri);
		cordova.getActivity().sendBroadcast(mediaScanIntent);
	}
}