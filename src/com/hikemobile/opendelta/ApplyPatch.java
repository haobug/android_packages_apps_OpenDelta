package com.hikemobile.opendelta;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.opendelta.Logger;
import eu.chainfire.opendelta.R;

import android.content.Context;
import android.util.Log;

public class ApplyPatch {
	private static Context context;

	public static native String sayHello();
	public static native int applypatchNative(int argc, String[] argv);

	public static final int CHECK = 0x1;
	public static final int PATCH = 0x2;
	public static final int LICNESE = 0x4;



	static {
        try {
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
	    Logger.setDebugLogging(context.getResources().getBoolean(R.bool.debug_output));
	}

	
	static int applypatchMain(int cmd_type, String source_filename, 
									String dest_filename,
									String new_sha1, long new_size, 
									String[] sha1s_patches) {
		
		List<String> args_list = new ArrayList<String>();
		int argc = 3;
		args_list.add("applypatch");
		switch(cmd_type){
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
			break;
		}
		
		for (int i =0; i< sha1s_patches.length; i+=1){
			if(sha1s_patches[i] != null && sha1s_patches[i].length() > 0){
				args_list.add(sha1s_patches[i]);
				argc += 1;
			}
			Logger.d("p[%d]=%s", i , sha1s_patches[i]);
		}
		
		String[] argv = new String[argc]; 
		argv = args_list.toArray(argv);
		
		return applypatchNative(argv.length, argv);
	}
	public static int applypatchCheck(String source_filename,  
									String ... sha1s_patches){
		return applypatchMain(CHECK, source_filename, "", "", 0, sha1s_patches);
	}
	
	public static int applypatchPatch(String source_filename, String dest_filename,
									String new_sha1, long new_size, 
									String ... sha1s_patches){
		return applypatchMain(PATCH, source_filename, dest_filename, new_sha1, new_size, sha1s_patches);
	}
}
