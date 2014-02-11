package com.hikemobile.opendelta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.chainfire.opendelta.Logger;
import eu.chainfire.opendelta.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;

public class ApplyPatch {
	private static Context context;

	public static native String sayHello();

	public static native int applypatchNative(int argc, String[] argv);

	public static final int CHECK = 0x1;
	public static final int PATCH = 0x2;
	public static final int LICNESE = 0x4;
	private static final String OUT_TMP = "/data/local/tmp";
	public static final String SDCARD_PREFIX = "/sdcard/HIKeDelta";
	public static final String SYSTEM = "/system";
	public static final String SCRIPT_NAME = "META-INF/com/google/android/updater-script";
	public static final String APPLY_COMPLETED = "com.hikemobile.opendelta.APPLY_PATCH_COMPLETED";
	public static final String APPLY_TERMINATED = "com.hikemobile.opendelta.APPLY_PATCH_TERMINATED";
	public static final String PACK_ZIP_PROGRESS = "com.hikemobile.opendelta.PACK_ZIP_PROGRESS";
	public static final String APPLY_PATCH_PROGRESS = "com.hikemobile.opendelta.APPLY_PATCH__PROGRESS";
	public static final String updater_path = OUT_TMP + "/" + "updater";
	public static final String tmp_Final_zip = "/sdcard/updateFinal.zip";
	;
	static {
		try {
			Logger.d("loading jni library");
			System.loadLibrary("applypatch");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error on loading applypatch JNI library");
		}

	}

	public static void setContext(Context _context) {
		if (context == null) {
			context = _context;
		}
		Logger.setDebugLogging(context.getResources().getBoolean(
				R.bool.debug_output));
	}

	private static int runApplypatchBin(int length, String[] argv) {
		StringBuffer sb = new StringBuffer();
		int i = 1;
		for (; i < length; i += 1) {
			sb.append(" ");
			sb.append(argv[i]);
		}
		String cmd_str = "/system/bin/applypatch" + sb.toString() + "";
		Logger.d(cmd_str);
		return RootCmd.run(cmd_str);
	}

	static int applypatchMain(int cmd_type, String source_filename,
			String dest_filename, String new_sha1, long new_size,
			String[] sha1s_patches) {

		List<String> args_list = new ArrayList<String>();
		int argc = 3;
		args_list.add("applypatch");
		Logger.d("source_filename=%s", source_filename);

		switch (cmd_type) {
		case CHECK:
			args_list.add("-c");
			args_list.add(source_filename);
			break;
		case PATCH:
		default:
			args_list.add(source_filename);
			args_list.add(dest_filename);
			args_list.add(new_sha1);
			args_list.add(Long.toString(new_size));
			Logger.d("dest_filename=%s", dest_filename);
			break;
		}
		for (int i = 0; i < sha1s_patches.length; i += 1) {
			if (sha1s_patches[i] != null && sha1s_patches[i].length() > 0) {
				args_list.add(sha1s_patches[i]);
				argc += 1;
			}
			Logger.d("p[%d]=%s", i, sha1s_patches[i]);
		}

		String[] argv = new String[argc];
		argv = args_list.toArray(argv);

		return applypatchNative(argv.length, argv);
		// return runApplypatchBin(argv.length, argv);
	}

	public static int applypatchCheck(String source_filename,
			String... sha1s_patches) {
		return applypatchMain(CHECK, source_filename, "", "", 0, sha1s_patches);
	}

	public static int applypatchPatch(String source_filename,
			String dest_filename, String new_sha1, long new_size,
			String... sha1s_patches) {
		return applypatchMain(PATCH, source_filename, dest_filename, new_sha1,
				new_size, sha1s_patches);
	}

	public static int applypatchZip(String zip_filename) {		
		String[] files = {/* "su", "busybox", */ "updater" };
		int ret = -1;
		for (String fn : files) {
			String sd_file = SDCARD_PREFIX + "/" + fn;
			String tmp_file = OUT_TMP + "/" + fn;
			copyAssets(fn, sd_file);
			Logger.d("1. extract updater to /data/local/tmp");
			Logger.d("2. change permission");
			Logger.d("cp %s %s && chmod 0755 %s/%s && rm -f %s",sd_file,
					tmp_file, OUT_TMP, fn, sd_file);
			RootCmd.run("cp %s %s && chmod 0755 %s/%s && rm -f %s", sd_file,
					tmp_file, OUT_TMP, fn, sd_file);
		}
		// call updater output to sdcard
				
		if(!new File(zip_filename).exists())
			return -2;
			
		String updater_sh = OUT_TMP+ "/" + "run_updater.sh";
		Logger.d("%s 2 1 %s", updater_path, zip_filename);
		return RootCmd.run("%s 2 1 %s", updater_path, zip_filename);
	}

	private static void copyAssets(String str_file, String str_dst) {
		AssetManager assetManager = context.getAssets();
		;

		/* for(String filename : files) */{
			InputStream in = null;
			OutputStream out = null;
			Logger.d("%s", str_file);
			try {
				in = assetManager.open(str_file);

				// File outFile = new File(context.getExternalFilesDir(null),
				// filename);
				File outFile = new File(str_dst);
				File parent_dir = outFile.getParentFile();
				if(!parent_dir.exists())
					parent_dir.mkdirs();
				Logger.d("%s", outFile);
				out = new FileOutputStream(outFile);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (IOException e) {
				Logger.ex(new Exception("Failed to copy asset file: " + str_file, e));
			}
		}
	}

	private static void copyFile(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
	
	public static void pack_zip(String output_zip) {
		// package it up and the flash it.
		try {
			final File f = new File(output_zip);
			if(f.exists())
				f.delete();
			// loop through system
			List<String> filenames = getFileList(ApplyPatch.SDCARD_PREFIX);
			Logger.d("filelist is :%s", filenames);
			if(filenames == null)
				return;
			ZipOutputStream out;			
			out = new ZipOutputStream(new FileOutputStream(f));
			int file_num = filenames.size();
			for (int i =0; i < file_num; i += 1) {
				String fn = filenames.get(i);
				String target_path = fn
						 .substring(ApplyPatch.SYSTEM.length());
				String pkg_path = target_path.substring(1);
				Logger.d("%s to %s", pkg_path, target_path);
				;
				String sd_file = String.format("%s/%s",ApplyPatch.SDCARD_PREFIX,pkg_path);
				if(new File(sd_file).exists())
					fn = sd_file;
				else
					if(!new File(fn).canRead()) /* skip file  that can not be read */
						continue;
				
				ZipEntry e = new ZipEntry(pkg_path);
				out.setLevel(Deflater.NO_COMPRESSION);
				out.putNextEntry(e);
				FileInputStream fi = new FileInputStream(fn);
				byte[] buff = new byte[1024];
				int len = 0;
				while((len = fi.read(buff)) > 0){										
					out.write(buff, 0, len);
				}
				fi.close();				
				out.closeEntry();
				Intent intent = new Intent(ApplyPatch.PACK_ZIP_PROGRESS);
				intent.putExtra("percent", i*100/file_num);
				ApplyPatch.sendBroadcast(intent);
			}
			Logger.d("pack_zip finished %s", out.toString());
			out.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			Log.e("OpenDelta", e1.getMessage());
			
		}
	}

	private static List<String> getFileList(String path) {
		File path_file = new File(path);
		File[] child_files = path_file.listFiles();
		if(child_files == null){
			return null;
		}		
		List<String> files = new ArrayList<String>();
		for (File f : child_files) {
			if (f.isDirectory()) {
				List<String> sub_file = getFileList(f.getPath());
				if(sub_file != null)
					files.addAll(sub_file);
			} else {
				// Logger.d("%s", f.getName());
				files.add(f.getPath());
			}
		}
		return files;
	}

	public static void sendBroadcast(Intent i) {
		context.sendBroadcast(i);
	}
}
