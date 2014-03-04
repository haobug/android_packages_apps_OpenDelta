package eu.chainfire.opendelta;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;

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
					
					arg0 = "";
				}

				@Override
				public void commandCompleted(int id, int exitCode) {
					Logger.d("commandCompleted");	
					Logger.d("exit status %d", exitCode);
					
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
