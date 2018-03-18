/** 佛祖保佑，永无bug🙏🙏🙏 */
package com.zzz.wifiview;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.io.*;

import java.lang.Process;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent();  
		intent.setClass(MainActivity.this, ViewActivity.class);
		
		if (check_root()) {
			run("mkdir " + MainActivity.this.getExternalFilesDir("Backup").getPath());
			if (android.os.Build.VERSION.SDK_INT >= 26) {
				intent.putExtra("path", "/data/misc/wifi/WifiConfigStore.xml");
				startActivity(intent);
				finish();
			} else {
				intent.putExtra("path", "/data/misc/wifi/wpa_supplicant.conf");
				startActivity(intent);
				finish();
			}
		} else {
			Toast.makeText(MainActivity.this, "无法获取ROOT", Toast.LENGTH_LONG).show();
		}
		
	}
	
	public boolean run(String command) {
        Process process = null;
        DataOutputStream os = null;
        int r = 1;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            r = process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return r == 0;
    }

    public boolean check_root() {
        try {
            try {
                return run("system/bin/mount -o rw,remount -t rootfs /data");
            } catch (Exception e) {
                Toast.makeText(this, "Root:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
	
}
