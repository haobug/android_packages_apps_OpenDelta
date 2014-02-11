package com.hikemobile.opendelta;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;

import eu.chainfire.opendelta.Logger;
import eu.chainfire.opendelta.MainActivity;

public class RootCmd {
	private static int seq_num = 0;
	private static String cmd_text = "";
	protected static String arg0 = "";
	private static long lines;
	public static int run(String cmds){
		int ret = 0;
		RootTools.debugMode = true;
		cmd_text  = cmds;
		String[] args = cmd_text.split(" ");
		if(args != null){
			arg0 = args[0];
		}
		try {
			Logger.d("cmds:%s", cmds);
			synchronized(cmds){
				seq_num += 1;
			}
			Command command = new Command(seq_num, cmds) {
				@Override
				public void output(int id, String line) {
					Logger.d("output");
					Logger.d("%s", line);
					lines += 1;
					if(arg0.endsWith(ApplyPatch.updater_path)){
						Intent i = new Intent(ApplyPatch.APPLY_PATCH_PROGRESS);
						i.putExtra("percent", lines*100/5000);
						ApplyPatch.sendBroadcast(i);
					}
				}

				@Override
				public void commandOutput(int id, String line) {
					Logger.d("commandOutput");
					Logger.d("%s", line);
				}

				@Override
				public void commandTerminated(int id, String reason) {
					Logger.d("commandTerminated");
					Logger.d("%s", reason);
					
					if(arg0.endsWith(ApplyPatch.updater_path)){
						Intent i = new Intent(ApplyPatch.APPLY_TERMINATED);
						i.putExtra("reason", reason);
						ApplyPatch.sendBroadcast(i);
					}
					arg0 = "";
				}

				@Override
				public void commandCompleted(int id, int exitCode) {
					Logger.d("commandCompleted");	
					Logger.d("exit status %d", exitCode);
					
					if(arg0.endsWith(ApplyPatch.updater_path)){
						Intent i = new Intent(ApplyPatch.APPLY_COMPLETED);
						i.putExtra("exitCode", exitCode);
						ApplyPatch.sendBroadcast(i);
					}
					arg0 = "";
				}
			};
			try {
				RootTools.getShell(true).add(command);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			ret = 5;
			e.printStackTrace();
		}
		return ret;
	}

	public static int run(String format, Object ... args) {
		return run(String.format(format, args));
	}

}
