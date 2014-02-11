package com.hikemobile.edify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import eu.chainfire.opendelta.Logger;

public class EdifyGenerator {
	private ScriptBuffer script;
	private Object info;
	private Partition[] fstab;

	public EdifyGenerator() {
		this.script = new ScriptBuffer();
	}

	public String toString() {
		return this.script.toString();
	}
	public void WriteRawImage(String mount_point, String fn){
		Logger.d("write %s to %s", fn, mount_point);
		Logger.d("WriteRawImage not implemented"); 
	}
		
	public void UnpackPackageFile(String src, String dst) {
		Logger.d("UnpackPackageFile not implemented");
	}
	public void UnpackPackageDir(String src, String dst) {
		// """Unpack a given directory from the OTA package into the given
		// destination directory."""
		this.script.append("package_extract_dir(\"%s\", \"%s\");", src, dst);
	}

	public void SetPermissions(String filename, int uid, int gid, int file_mode) {
		// """Set file ownership and permissions."""
		this.script
				.append("set_perm(%d, %d, 0%o, \"%s\");", uid, gid, file_mode, filename);
	}

	public void SetPermissionsRecursive(String filename, int uid, int gid, int dir_mode,
			int file_mode) {
		// """Recursively set path ownership and permissions."""
		this.script.append("set_perm_recursive(%d, %d, 0%o, 0%o, \"%s\");",
				uid, gid, dir_mode, file_mode, filename);
	}

	public void Mount(String mount_point) {
		 /* def Mount(self, mount_point):
			    """Mount the partition with the given mount_point."""
			    fstab = self.info.get("fstab", None)
			    if fstab:
			      p = fstab[mount_point]
			      self.script.append('mount("%s", "%s", "%s", "%s");' %
			                         (p.fs_type, common.PARTITION_TYPES[p.fs_type],
			                          p.device, p.mount_point))
			      self.mounts.add(p.mount_point)
*/
//		p = fstab[mount_point];
//		this.mounts.add(p.mount_point);
		//this.script.append(format, args)
	}
	public void writeFile(String filename){
		File f = new File(filename);
		File p = null;
		if(!(p = f.getParentFile()).exists()){
			p.mkdirs();
		}
		
		try {
			FileOutputStream fo = new FileOutputStream(f);
			fo.write(this.script.toString().getBytes());
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
