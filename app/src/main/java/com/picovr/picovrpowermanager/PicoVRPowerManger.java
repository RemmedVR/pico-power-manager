package com.picovr.picovrpowermanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.unity3d.player.UnityPlayerNativeActivityPico;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;

public class PicoVRPowerManger
{
	private static final String TAG = "PicoVRPowerManger";
	private WakeLock wakeLock;
	private PowerManager pm;
	private static ExecutorService mInstaller = Executors.newFixedThreadPool(1);

	private Context unityContext = null;

	private static DevicePolicyManager policyManager;
	private static ComponentName componentName;
	private static final int MY_REQUEST_CODE = 9999;

	public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
	public static final String EXTRA_KEY_CONFIRM = "android.intent.extra.KEY_CONFIRM";
	public static final String SLEEP_TIME = "setprop persist.psensor.sleep.delay ";
	public static final String LOCK_SCREEN = "setprop persist.psensor.screenoff.delay ";

	public PicoVRPowerManger(Activity unityActivity)
	{
		unityContext = unityActivity.getApplicationContext();
		Log.i(TAG, "CONSTRUCTOR PW!");
		pm = (PowerManager) unityContext.getSystemService(Context.POWER_SERVICE);
		policyManager = (DevicePolicyManager) unityContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
		componentName = new ComponentName("com.picovr.picovrpowermanager", "AdminReceiver.class");

	}

	public void androidShutDown() {

		Log.i(TAG, "androidShutDown");
		
		if(Build.VERSION.SDK_INT < 24){
			try {
				Method method = pm.getClass().getDeclaredMethod("shutdown", boolean.class, boolean.class);
				method.invoke(pm, false,true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			try {
				Method method = pm.getClass().getDeclaredMethod("shutdown", boolean.class, String.class, boolean.class);
				method.invoke(pm, false,null,true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void androidReBoot() {

		Log.i(TAG, "androidReBoot");
		// PowerManager pManager = (PowerManager)
		// getSystemService(Context.POWER_SERVICE); 
		pm.reboot("");
	}

	public void TestDebug()
	{
		Log.i(TAG, "TEST DEBUG LOG");
	}

	public void androidLockScreen() {
		Log.i(TAG, "androidLockScreen");
	
		if (policyManager.isAdminActive(componentName)) {
			Log.i(TAG, "lockNow");
			policyManager.lockNow();
		} else {

			Log.i(TAG, "activeManage");
		}
	}

	public void androidUnlockScreen() {

//		Log.e(TAG, "androidUnlockScreen");
//		wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
//		wakeLock.acquire();
//		wakeLock.release();
	}

	public void acquireWakeLock() {

		Log.i(TAG, "Wake_Lock!");

		if (wakeLock == null) {
			wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, this.getClass().getCanonicalName());
			wakeLock.setReferenceCounted(false);
			wakeLock.acquire();
			Log.i(TAG, "acquireWakeLock");
		}
	}

	public void acquireWakeLock(long timeout) {

		
		if (wakeLock == null) {
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
			wakeLock.setReferenceCounted(false);
			wakeLock.acquire(timeout);
			Log.i(TAG, "acquireWakeLock(long timeout)");
		}
	}

	public void releaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
			Log.i(TAG, "releaseWakeLock");
		}
	}

	


	public void goToApp(String packagename) {

		Intent intent = new Intent();
		PackageManager packageManager = unityContext.getPackageManager();
		intent = packageManager.getLaunchIntentForPackage(packagename);
		unityContext.startActivity(intent);

	}

	public void goToActivity(String packagename, String activityname) {

		Intent intent;
		PackageManager packageManager = unityContext.getPackageManager();
		intent = packageManager.getLaunchIntentForPackage(packagename);
		ComponentName comp = new ComponentName(packagename, activityname);
		intent.setComponent(comp);
		unityContext.startActivity(intent);

	}

	
	public void setpropSleep(String time) {
		Log.e(TAG, "setpropSleep:" + time);
		try {
			Log.e(TAG, "setpropSleep:" + SLEEP_TIME + time);
			execCommand(SLEEP_TIME + time);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setPropLockScreen(String time) {
		Log.e(TAG, "setPropLockScreen:" + time);
		try {
			Log.e(TAG, "setPropLockScreen:" + LOCK_SCREEN + time);
			execCommand(LOCK_SCREEN + time);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void execCommand(String command) throws IOException {

		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec(command);
		InputStream inputstream = proc.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
		String line = "";
		StringBuilder sb = new StringBuilder(line);
		while ((line = bufferedreader.readLine()) != null) {
			sb.append(line);
			sb.append("/n");
		}
		Log.e(TAG, sb.toString());
		
		try {
			if (proc.waitFor() != 0) {
				System.err.println("exit value = " + proc.exitValue());
			}
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}
	
	public void silentInstall(String apkPath, String installerPkgName){
		SilentInstaller.install(apkPath, installerPkgName, new ShellCmd.ICmdResultCallback() {
			
			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub
				Log.e(TAG, "Enable silent installation，onException");
			}
			
			@Override
			public void onError(String arg0) {
				// TODO Auto-generated method stub
				Log.e(TAG, "Enable silent installation， onError");
			}
			
			@Override
			public void onComplete(String arg0) {
				// TODO Auto-generated method stub
				Log.e(TAG, "Enable silent installation，onComplete");
			}
		});
	}
	

	/*
	public void silentInstall(final String apkPath) {
		Log.i(TAG, "Enable silent installation，apkPath = " + apkPath);

		mInstaller.execute(new Runnable() {
			@Override
			public synchronized void run() {
				String[] args = { "pm", "install", "-r", apkPath };
				String result = "";
				ProcessBuilder processBuilder = new ProcessBuilder(args);
				Process process = null;
				InputStream errIs = null;
				InputStream inIs = null;
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int read = -1;
					process = processBuilder.start();
					errIs = process.getErrorStream();
					while ((read = errIs.read()) != -1) {
						baos.write(read);
					}
					baos.write('\n');
					inIs = process.getInputStream();
					while ((read = inIs.read()) != -1) {
						baos.write(read);
					}
					byte[] data = baos.toByteArray();
					result = new String(data);
					Log.i(TAG, "silentInstall result is : " + result);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (errIs != null) {
							errIs.close();
						}
						if (inIs != null) {
							inIs.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (process != null) {
						process.destroy();
					}
				}
			}
		});
	}
*/
	
	public void silentUninstall(String pkgName) {
		PackageManager pm = unityContext.getPackageManager();
		Class<?>[] uninstalltypes = new Class[] {String.class, IPackageDeleteObserver.class, int.class};
		Method uninstallmethod = null;
		try {
			uninstallmethod = pm.getClass().getMethod("deletePackage", uninstalltypes);
			uninstallmethod.invoke(pm, new Object[] {pkgName, new MyPackageDeleteObserver(), 0});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	class MyPackageDeleteObserver extends IPackageDeleteObserver.Stub {

		@Override
		public void packageDeleted(String packageName, int returnCode) throws RemoteException {

			if (returnCode == 1) {
				Log.i(TAG, "Succeed " + returnCode);
			} else {
				Log.i(TAG, "Failed " + returnCode);
			}
		}
	}
}
