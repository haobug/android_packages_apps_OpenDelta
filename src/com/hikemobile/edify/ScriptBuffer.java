package com.hikemobile.edify;

public class ScriptBuffer {

	private StringBuffer internal_buffer = null;

	public ScriptBuffer() {
		internal_buffer = new StringBuffer();
	}

	public void append(String format, Object... args) {
		internal_buffer.append(String.format(format, args));
	}
	
	public String toString(){
		return this.internal_buffer.toString();
	}
}
