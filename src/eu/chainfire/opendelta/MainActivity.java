/* 
 * Copyright (C) 2013 Jorrit "Chainfire" Jongma
 * Copyright (C) 2013 The OmniROM Project
 */
/* 
 * This file is part of OpenDelta.
 * 
 * OpenDelta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenDelta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenDelta. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.chainfire.opendelta;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.*;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.hikemobile.edify.EdifyGenerator;
import com.hikemobile.opendelta.ApplyPatch;
import com.hikemobile.opendelta.RootCmd;

public class MainActivity extends Activity {
	protected static final int UPDATE_PERCENT = 0;
	private TextView title = null;
	private TextView sub = null;
	private ProgressBar progress = null;
	private Button checkNow = null;
	private Button flashNow = null;
	ApplyPatchCompletedReceiver mReceiver = mReceiver = new ApplyPatchCompletedReceiver(); // 广播接收类初始化
	private Config config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			getActionBar().setIcon(
					getPackageManager().getApplicationIcon(
							"com.android.settings"));
		} catch (NameNotFoundException e) {
			// The standard Settings package is not present, so we can't snatch
			// its icon
			Logger.ex(e);
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);

		UpdateService.start(this);

		setContentView(R.layout.activity_main);

		title = (TextView) findViewById(R.id.text_title);
		sub = (TextView) findViewById(R.id.text_sub);
		progress = (ProgressBar) findViewById(R.id.progress);
		checkNow = (Button) findViewById(R.id.button_check_now);
		flashNow = (Button) findViewById(R.id.button_flash_now);

		config = Config.getInstance(this);

		ApplyPatch.setContext(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		if (!config.getSecureModeEnable()) {
			menu.findItem(R.id.action_secure_mode).setVisible(false);
		} else {
			menu.findItem(R.id.action_secure_mode).setChecked(
					config.getSecureModeCurrent());
		}

		return true;
	}

	private void showNetworks() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		int flags = prefs.getInt(UpdateService.PREF_AUTO_UPDATE_NETWORKS_NAME,
				UpdateService.PREF_AUTO_UPDATE_NETWORKS_DEFAULT);
		final boolean[] checkedItems = new boolean[] {
				(flags & NetworkState.ALLOW_2G) == NetworkState.ALLOW_2G,
				(flags & NetworkState.ALLOW_3G) == NetworkState.ALLOW_3G,
				(flags & NetworkState.ALLOW_4G) == NetworkState.ALLOW_4G,
				(flags & NetworkState.ALLOW_WIFI) == NetworkState.ALLOW_WIFI,
				(flags & NetworkState.ALLOW_ETHERNET) == NetworkState.ALLOW_ETHERNET,
				(flags & NetworkState.ALLOW_UNKNOWN) == NetworkState.ALLOW_UNKNOWN };

		(new AlertDialog.Builder(this))
				.setTitle(R.string.title_networks)
				.setMultiChoiceItems(
						new CharSequence[] { getString(R.string.network_2g),
								getString(R.string.network_3g),
								getString(R.string.network_4g),
								getString(R.string.network_wifi),
								getString(R.string.network_ethernet),
								getString(R.string.network_unknown), },
						checkedItems, new OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								checkedItems[which] = isChecked;
							}
						})
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int flags = 0;
						if (checkedItems[0])
							flags += NetworkState.ALLOW_2G;
						if (checkedItems[1])
							flags += NetworkState.ALLOW_3G;
						if (checkedItems[2])
							flags += NetworkState.ALLOW_4G;
						if (checkedItems[3])
							flags += NetworkState.ALLOW_WIFI;
						if (checkedItems[4])
							flags += NetworkState.ALLOW_ETHERNET;
						if (checkedItems[5])
							flags += NetworkState.ALLOW_UNKNOWN;
						prefs.edit()
								.putInt(UpdateService.PREF_AUTO_UPDATE_NETWORKS_NAME,
										flags).commit();
					}
				}).setNegativeButton(android.R.string.cancel, null)
				.setCancelable(true).show();
	}

	private void showAbout() {
		int thisYear = Calendar.getInstance().get(Calendar.YEAR);
		String opendelta = (thisYear == 2013) ? "2013" : "2013-"
				+ String.valueOf(thisYear);
		String xdelta = (thisYear == 1997) ? "1997" : "1997-"
				+ String.valueOf(thisYear);

		AlertDialog dialog = (new AlertDialog.Builder(this))
				.setTitle(R.string.app_name)
				.setMessage(
						Html.fromHtml(getString(R.string.about_content)
								.replace("_COPYRIGHT_OPENDELTA_", opendelta)
								.replace("_COPYRIGHT_XDELTA_", xdelta)))
				.setNeutralButton(android.R.string.ok, null)
				.setCancelable(true).show();
		TextView textView = (TextView) dialog
				.findViewById(android.R.id.message);
		if (textView != null)
			textView.setTypeface(title.getTypeface());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_networks:
			showNetworks();
			return true;
		case R.id.action_secure_mode:
			item.setChecked(config.setSecureModeCurrent(!item.isChecked()));

			(new AlertDialog.Builder(this))
					.setTitle(
							item.isChecked() ? R.string.secure_mode_enabled_title
									: R.string.secure_mode_disabled_title)
					.setMessage(
							Html.fromHtml(getString(item.isChecked() ? R.string.secure_mode_enabled_description
									: R.string.secure_mode_disabled_description)))
					.setCancelable(true)
					.setNeutralButton(android.R.string.ok, null).show();

			return true;
		case R.id.action_about:
			showAbout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private IntentFilter updateFilter = new IntentFilter(
			UpdateService.BROADCAST_INTENT);
	private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		private String formatLastChecked(String filename, long ms) {
			Date date = new Date(ms);
			if (filename == null) {
				if (ms == 0) {
					return getString(R.string.last_checked_never);
				} else {
					return getString(
							R.string.last_checked,
							DateFormat.getDateFormat(MainActivity.this).format(
									date),
							DateFormat.getTimeFormat(MainActivity.this).format(
									date));
				}
			} else {
				if (ms == 0) {
					return "";
				} else {
					return String.format(
							"%s\n%s",
							filename,
							getString(R.string.last_checked,
									DateFormat.getDateFormat(MainActivity.this)
											.format(date), DateFormat
											.getTimeFormat(MainActivity.this)
											.format(date)));
				}
			}
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String title = "";
			String sub = "";
			long current = 0L;
			long total = 1L;
			boolean enableCheck = false;
			boolean enableFlash = false;

			String state = intent.getStringExtra(UpdateService.EXTRA_STATE);
			// don't try this at home
			if (state != null) {
				try {
					title = getString(getResources().getIdentifier(
							"state_" + state, "string", getPackageName()));
				} catch (Exception e) {
					// String for this state could not be found (displays empty
					// string)
					Logger.ex(e);
				}
			}

			if (UpdateService.STATE_ERROR_DISK_SPACE.equals(state)) {
				current = intent.getLongExtra(UpdateService.EXTRA_CURRENT,
						current);
				total = intent.getLongExtra(UpdateService.EXTRA_TOTAL, total);

				current /= 1024L * 1024L;
				total /= 1024L * 1024L;

				sub = getString(R.string.error_disk_space_sub, current, total);
			} else if (UpdateService.STATE_ERROR_UNKNOWN.equals(state)) {
				enableCheck = true;
			} else if (UpdateService.STATE_ACTION_NONE.equals(state)) {
				enableCheck = true;
				sub = formatLastChecked(null,
						intent.getLongExtra(UpdateService.EXTRA_MS, 0));
			} else if (UpdateService.STATE_ACTION_READY.equals(state)) {
				enableCheck = true;
				enableFlash = true;
				sub = formatLastChecked(
						intent.getStringExtra(UpdateService.EXTRA_FILENAME),
						intent.getLongExtra(UpdateService.EXTRA_MS, 0));
			} else {
				current = intent.getLongExtra(UpdateService.EXTRA_CURRENT,
						current);
				total = intent.getLongExtra(UpdateService.EXTRA_TOTAL, total);

				// long --> int overflows FTL (progress.setXXX)
				boolean progressInK = false;
				if (total > 1024L * 1024L * 1024L) {
					progressInK = true;
					current /= 1024L;
					total /= 1024L;
				}

				String filename = intent
						.getStringExtra(UpdateService.EXTRA_FILENAME);
				if (filename != null) {
					long ms = intent.getLongExtra(UpdateService.EXTRA_MS, 0);

					if ((ms <= 500) || (current <= 0) || (total <= 0)) {
						sub = String.format(Locale.ENGLISH, "%s\n%.0f %%",
								filename, intent.getFloatExtra(
										UpdateService.EXTRA_PROGRESS, 0));
					} else {
						float kibps = ((float) current / 1024f)
								/ ((float) ms / 1000f);
						if (progressInK)
							kibps *= 1024f;
						int sec = (int) (((((float) total / (float) current) * (float) ms) - ms) / 1000f);

						if (kibps < 10000) {
							sub = String.format(Locale.ENGLISH,
									"%s\n%.0f %%, %.0f KiB/s, %02d:%02d",
									filename, intent.getFloatExtra(
											UpdateService.EXTRA_PROGRESS, 0),
									kibps, sec / 60, sec % 60);
						} else {
							sub = String.format(Locale.ENGLISH,
									"%s\n%.0f %%, %.0f MiB/s, %02d:%02d",
									filename, intent.getFloatExtra(
											UpdateService.EXTRA_PROGRESS, 0),
									kibps / 1024f, sec / 60, sec % 60);
						}
					}
				}
			}

			MainActivity.this.title.setText(title);
			MainActivity.this.sub.setText(sub);

			progress.setProgress((int) current);
			progress.setMax((int) total);

			checkNow.setVisibility(enableCheck ? View.VISIBLE : View.GONE);
			flashNow.setVisibility(enableFlash ? View.VISIBLE : View.GONE);
		}
	};
	private long startTime;
	public int percent;
	public String action;

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(updateReceiver, updateFilter);
	}

	@Override
	protected void onStop() {
		unregisterReceiver(updateReceiver);
		super.onStop();
	}

	public void onButtonCheckNowClick(View v) {
		UpdateService.startCheck(this);
	}

	public void onButtonFlashNowClick(View v) {
		flashRecoveryWarning.run();
	}

	public void onButtonTestMergeClick(View v) {
		// testPatchBootImg();
		testPatchZip();
		v.setVisibility(View.GONE);
	}

	private void testPatchZip() {
		File sd_out_path = new File(ApplyPatch.SDCARD_PREFIX);
		int ret = -1;

		if (!sd_out_path.exists())
			sd_out_path.mkdirs();

		// ret = ApplyPatch.applypatchZip("/sdcard/update.zip");
		startTime = System.currentTimeMillis();
		ApplyPatch.applypatchZip("/sdcard/update.zip");

		/*
		 * if(ret != 0){ Logger.e("applyPatch failed"); return; }
		 */

		// generator new updater_script

		// for (String fn : getFileList(ApplyPatch.SDCARD_PREFIX)) {
		// EdifyGenerator edify = new EdifyGenerator();
		// Logger.d("%s", fn);
		// String target_path = fn
		// .substring(ApplyPatch.SDCARD_PREFIX.length());
		// String pkg_path = target_path.substring(1);
		// if (pkg_path.equals("boot.img")) {
		// edify.WriteRawImage("boot", pkg_path);
		// } else {
		// edify.UnpackPackageFile(pkg_path, target_path);
		// }
		// }

		// EdifyGenerator edify = new EdifyGenerator();
		// edify.Mount("/system");
		// edify.UnpackPackageDir("system", "/system");
		// edify.Unount("/system");
		// Logger.d("%s", edify.toString());

		// pack_zip();
		IntentFilter iFilter = null; // 意图过滤对象

		iFilter = new IntentFilter(ApplyPatch.APPLY_COMPLETED); // 意图过滤初始化
		iFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY); // 设置优先级
		this.registerReceiver(mReceiver, iFilter); // 注册广播接

		iFilter = new IntentFilter(ApplyPatch.APPLY_TERMINATED); // 意图过滤初始化
		iFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY); // 设置优先级
		this.registerReceiver(mReceiver, iFilter); // 注册广播接

		iFilter = new IntentFilter(ApplyPatch.PACK_ZIP_PROGRESS); // 意图过滤初始化
		iFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY); // 设置优先级
		this.registerReceiver(mReceiver, iFilter); // 注册广播接

		iFilter = new IntentFilter(ApplyPatch.APPLY_PATCH_PROGRESS); // 意图过滤初始化
		iFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY); // 设置优先级
		this.registerReceiver(mReceiver, iFilter); // 注册广播接
	}
	Handler handler = new Handler();
	class ApplyPatchCompletedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase(ApplyPatch.APPLY_COMPLETED)) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Logger.d("package zip to %s", ApplyPatch.tmp_Final_zip);
						startTime = System.currentTimeMillis();
						ApplyPatch.pack_zip(ApplyPatch.tmp_Final_zip);
					}
				}).start();
			} else if (action.equalsIgnoreCase(ApplyPatch.APPLY_TERMINATED)) {
				String fail_reason = intent.getExtras().getString("reason");
				Logger.d("fail_reason %s" + fail_reason);
			} else if (action.equalsIgnoreCase(ApplyPatch.PACK_ZIP_PROGRESS)
					|| action.equalsIgnoreCase(ApplyPatch.APPLY_PATCH_PROGRESS)) {
				int percent = intent.getExtras().getInt("percent");
				Logger.d("percent %d", percent);
				synchronized (this) {
					MainActivity.this.percent = percent;
					MainActivity.this.action = action;
					handler.postDelayed(progressUpdater, 500);
				}
			}
		}
	}	
	
	Runnable progressUpdater = new Runnable(){
		@Override
		public void run() {
			updatePercent();
			handler.postDelayed(progressUpdater, 500);
		}
		
	};
	void updatePercent() {
		String percent_str = String.format("curr perenct %d", percent);
		if (action.equalsIgnoreCase(ApplyPatch.PACK_ZIP_PROGRESS))
			MainActivity.this.title.setText("Packaging zip....." + duration());
		else
			MainActivity.this.title.setText("Apply patch....." + duration());
		MainActivity.this.sub.setText(percent_str);
		progress.setProgress(percent);
		progress.setMax(100);
	}

	private void testPatchBootImg() {
		/*
		 * apply_patch(
		 * "EMMC:boot:4968448:157824e88a8797f0cfd70b33447fffe54502aa72:4968448:b4476c519f759888551c12677d9f4e4db9995037"
		 * , "-", b4476c519f759888551c12677d9f4e4db9995037, 4968448,
		 * 157824e88a8797f0cfd70b33447fffe54502aa72,
		 * package_extract_file("patch/boot.img.p"));
		 */

		/* 497dd554e600a6c43f35c6d8da1b6706ac7889e6 before patch */
		// String src_file =
		// "EMMC:boot:4968448:157824e88a8797f0cfd70b33447fffe54502aa72:4968448:b4476c519f759888551c12677d9f4e4db9995037";
		String src_file = "/sdcard/HIKeDelta/boot.img.org";
		String dst_sha1 = "b4476c519f759888551c12677d9f4e4db9995037";
		int dst_size = 4968448;
		String src_sha1 = "157824e88a8797f0cfd70b33447fffe54502aa72";
		String out_prefix = "/sdcard" + "/" + "HIKeDelta";
		String dst_file = out_prefix /* + "/" */+ src_file;
		String update_path = "/sdcard" + "/update";
		String patch_file = update_path + "/patch" + src_file + ".p";

		dst_file = "/sdcard/HIKeDelta/boot.img";
		patch_file = update_path + "/patch" + "/"
				+ new File(dst_file).getName() + ".p";

		if (0 == ApplyPatch.applypatchCheck(src_file, src_sha1)) {
			Logger.d("Can be patched");

			// File in_file= new File(src_file);
			// if(!in_file.exists()){
			// Logger.d("src file %s not exist", src_file);
			// return;
			// }

			Logger.d("dst_file is %s", dst_file);
			File out_path = new File(dst_file).getParentFile();
			Logger.d("out_path is %s", out_path);
			if (!out_path.exists()) {
				Logger.d("out_path does not exist try to create it.");
				out_path.mkdirs();
			}
			if (out_path.exists()) {
				Logger.d("out_path exists.");
			}

			int ret = ApplyPatch.applypatchPatch(src_file, dst_file, dst_sha1,
					dst_size, src_sha1 + ":" + patch_file);
			if (ret == 0)
				Logger.d("%s Patched successfully", src_file);
			else
				Logger.d("it returns %d", ret);

		} else if (0 == ApplyPatch.applypatchCheck(src_file, dst_sha1)) {
			Logger.d("Already patched");
		} else {
			Logger.d("Can not be patched");
		}
		return;
	}

	public String duration() {
		long now = System.currentTimeMillis();
		long dur = (now - startTime) / 1000;
		return String.format("%d:%d", dur / 60, dur % 60);
	}

	private Runnable flashRecoveryWarning = new Runnable() {
		@Override
		public void run() {
			// Show a warning message about recoveries we support, depending
			// on the state of secure mode and if we've shown the message before

			final Runnable next = flashWarningFlashAfterUpdateZIPs;

			CharSequence message = null;
			if (!config.getSecureModeCurrent()
					&& !config.getShownRecoveryWarningNotSecure()) {
				message = Html
						.fromHtml(getString(R.string.recovery_notice_description_not_secure));
				config.setShownRecoveryWarningNotSecure();
			} else if (config.getSecureModeCurrent()
					&& !config.getShownRecoveryWarningSecure()) {
				message = Html
						.fromHtml(getString(R.string.recovery_notice_description_secure));
				config.setShownRecoveryWarningSecure();
			}

			if (message != null) {
				(new AlertDialog.Builder(MainActivity.this))
						.setTitle(R.string.recovery_notice_title)
						.setMessage(message)
						.setCancelable(true)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										next.run();
									}
								}).show();
			} else {
				next.run();
			}
		}
	};

	private Runnable flashWarningFlashAfterUpdateZIPs = new Runnable() {
		@Override
		public void run() {
			// If we're in secure mode, but additional ZIPs to flash have been
			// detected, warn the user that these will not be flashed

			final Runnable next = flashStart;

			if (config.getSecureModeCurrent()
					&& (config.getFlashAfterUpdateZIPs().size() > 0)) {
				(new AlertDialog.Builder(MainActivity.this))
						.setTitle(R.string.flash_after_update_notice_title)
						.setMessage(
								Html.fromHtml(getString(R.string.flash_after_update_notice_description)))
						.setCancelable(true)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										next.run();
									}
								}).show();
			} else {
				next.run();
			}
		}
	};

	private Runnable flashStart = new Runnable() {
		@Override
		public void run() {
			checkNow.setEnabled(false);
			flashNow.setEnabled(false);
			UpdateService.startFlash(MainActivity.this);
		}
	};
}
