/** 佛祖保佑，永无bug🙏🙏🙏 */
package com.zzz.wifiview;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

import java.lang.Process;

public class MainActivity extends Activity {
	
	ArrayList<Map<String, String>> mainList;
	PopupMenu popup;
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (check_root()) {
			setContentView(R.layout.main);
			mainList = get();
			if (mainList == null) {
				Toast.makeText(MainActivity.this, "获取列表失败", Toast.LENGTH_LONG).show();
			} else {
				if (mainList.size() == 0) {
					Toast.makeText(MainActivity.this, "列表为空", Toast.LENGTH_LONG).show();
				} else {
					doWork();
				}
			}
		} else {
			Toast.makeText(MainActivity.this, "无法获取ROOT", Toast.LENGTH_LONG).show();
		}
	}
	
	private void doWork() {
		ListView lv = (ListView) findViewById(R.id.lv);
		//adapter = new WiFiAdapter(this, mainList);
		lv.setAdapter(new WiFiAdapter(this, mainList));
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				popup = new PopupMenu(MainActivity.this, view);
				getMenuInflater().inflate(R.menu.copy,popup.getMenu());
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
						switch(item.getItemId()) {
							case R.id.menu_ssid:
								cm.setPrimaryClip(ClipData.newPlainText(null, mainList.get(position).get("ssid")));
								break;
							case R.id.menu_password:
								cm.setPrimaryClip(ClipData.newPlainText(null, mainList.get(position).get("psk")));
								break;
							case R.id.menu_all:
								Map<String, String> s = mainList.get(position);
								cm.setPrimaryClip(ClipData.newPlainText(null, "SSID: " + s.get("ssid") + "\n" + "密码: " + s.get("psk")));
								break;
							default:
								return false;
						}
						Toast.makeText(MainActivity.this, "已复制", Toast.LENGTH_SHORT).show();
						return true;
					}
				});
			popup.show();
			}
		});
	}
	
	public ArrayList<Map<String, String>> get() {
        try {
            ReadFile file = new ReadFile("/data/misc/wifi/wpa_supplicant.conf");
            return file.getList(this.context);
        } catch (Exception e) {
            Log.e("ReadFile: ", e.getMessage());
            return null;
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
                Toast.makeText(context, "Error (Root Permission) " + ":" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
	
}
