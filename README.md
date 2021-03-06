# PowerManager 

Note: Regarding JAR file creation and usage, please refer to [the Guideline](https://github.com/picoxr/support/blob/master/How_to_use_JAR_file_in_Unity_project_on_Pico_device.docx)

## Introduction
This JAR file is used to modify power settings

## Modify AndroidManifest

 Modify the androidminifests. XML file and Add: android: sharedUserId = "android. Uid. System"

   Add Permission:

   ```
   <uses-permission android:name="android.permission.WAKE_LOCK" />
   <uses-permission android:name="android.permission.DEVICE_POWER" />
   <uses-permission android:name="android.permission.SHUTDOWN" />
   <uses-permission android:name="android.permission.REBOOT" />
   <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
   ```

## Class Name

   ```
   android:name="com.example.picovrpowermanager.PicoVRPowerManger"
   ```

## Interface List

| Interface           | Instructions                    | Remark                                 |
| ------------------- | ------------------------------- | -------------------------------------- |
| androidShutDown     | Shutdown                        | System signature required              |
| androidReBoot       | Root                            | System signature required              |
| androidLockScreen   | LockScreen                      |                                        |
| androidUnlockScreen | UnLockScreen                    |                                        |
| acquireWakeLock     | AcquireWakeLock                 | Must be paired with releaseWakeLock    |
| releaseWakeLock     | ReleaseWakeLock                 | Must be paired with acquireWakeLock    |
| goToApp             | Jump application                | Need to add the package name parameter |
| setpropSleep        | Sets the system sleep timeout   | Need to increase the time parameter    |
| setPropLockScreen   | Sets the screen closing timeout | Need to increase the time parameter    |
| silentInstall       | SilentInstall                   | System signature required              |
| silentUninstall     | SilentUninstall                 | System signature required              |

The second parameter in the silent installation must be the package name of the current application, not the package name of the installed application.

## Note
This demo requires system signature. About how to sign a apk, refer to this [Customize Launcher on Pico Device](https://github.com/picoxr/support/blob/master/Customize%20Launcher%20on%20Pico%20Device.docx?raw=true).


