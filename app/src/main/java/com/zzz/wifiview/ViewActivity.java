package com.zzz.wifiview;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.AbsListView.*;
import android.widget.SearchView.*;
import java.io.*;
import java.util.*;

import android.content.ClipboardManager;
import java.lang.Process;

public class ViewActivity extends Activity {
	ArrayList<Map<String, String>> mainList;
	PopupMenu popup;
	Context context = this;
	String backupPath;
	String sPath;
	SearchView mSearchView = null;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list);
		backupPath = context.getExternalFilesDir("Backup").getPath() + "/LastBackup";
		
		Intent intent = getIntent();
		
		if(Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			sPath = intent.getDataString().substring(8);
			if(sPath.contains("%20")) {
				Toast.makeText(this, "路径不能包含空格", Toast.LENGTH_SHORT).show();
				finish();
			} else {
				mainList = get(sPath);
			}
		} else {
			Bundle bundle = intent.getExtras();
			sPath = bundle.getString("path");
			mainList = get(sPath);
		}
		
		onWork();
		
	}
	
	private void onWork() {
		if (mainList == null) {
			Toast.makeText(ViewActivity.this, "获取列表失败", Toast.LENGTH_LONG).show();
		} else {
			if (mainList.size() == 0) {
				Toast.makeText(ViewActivity.this, "列表为空", Toast.LENGTH_LONG).show();
			} else {
				doWork();
			}
		}
	}
	
	private void doWork() {
		final ListView lv = (ListView) findViewById(R.id.lv);
		lv.setAdapter(new WiFiAdapter(this, mainList));
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
					popup = new PopupMenu(ViewActivity.this, view);
					getMenuInflater().inflate(R.menu.copy,popup.getMenu());
					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								final ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
								switch(item.getItemId()) {
									case R.id.menu_ssid:
										cm.setPrimaryClip(ClipData.newPlainText(null, mainList.get(position).get("ssid")));
										Toast.makeText(ViewActivity.this, "SSID已复制", Toast.LENGTH_SHORT).show();
										break;
									case R.id.menu_password:
										cm.setPrimaryClip(ClipData.newPlainText(null, mainList.get(position).get("psk")));
										Toast.makeText(ViewActivity.this, "密码已复制", Toast.LENGTH_SHORT).show();
										break;
									case R.id.menu_all:
										Map<String, String> s = mainList.get(position);
										cm.setPrimaryClip(ClipData.newPlainText(null, "SSID: " + s.get("ssid") + "\n" + "密码: " + s.get("psk")));
										Toast.makeText(ViewActivity.this, "SSID和密码都已复制", Toast.LENGTH_SHORT).show();
										break;
									case R.id.menu_view:
										AlertDialog.Builder TextDialog = new AlertDialog.Builder(ViewActivity.this);
										TextDialog.setTitle("源信息浏览").setMessage(mainList.get(position).get("pos") + "\n" + mainList.get(position).get("view")).setPositiveButton("关闭",null).setNeutralButton("复制", new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface dialog, int which) {
													cm.setPrimaryClip(ClipData.newPlainText(null, mainList.get(position).get("view")));
												}}).show();
										break;
									case R.id.menu_delete:
										//run("cp -f " + wifiPath + " " + backupPath);
										
										try {
											delete(mainList.get(position).get("view"));
										} catch (IOException e) {
											Toast.makeText(ViewActivity.this, "删除WiFi:" + e, Toast.LENGTH_LONG).show();
										}
										
										if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
											run("cp -f " + backupPath + " " + sPath);
											Toast.makeText(ViewActivity.this, "已删除", Toast.LENGTH_SHORT).show();
											mainList.clear();
											mainList = get(sPath);
											onWork();
											/*Intent intent = getIntent();
											finish();
											startActivity(intent);*/
										} else {
											run("cp -f " + backupPath + " " + sPath);
											run("chmod 660 " + sPath);
											Toast.makeText(ViewActivity.this, "已删除", Toast.LENGTH_SHORT).show();
											mainList.clear();
											mainList = get(sPath);
											onWork();
											/*Intent intent = getIntent();
											finish();
											startActivity(intent);*/
										}
										
										break;
									default:
										return false;
								}
								return true;
							}
						});
					popup.show();
				}
			});
			
		lv.setTextFilterEnabled(true);
		
		lv.setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm != null) {
						imm.hideSoftInputFromWindow(lv.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法
					}
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				}
			});

		mSearchView = (SearchView) findViewById(R.id.menu_search);
		mSearchView.setIconifiedByDefault(true);
		mSearchView.setSubmitButtonEnabled(true);
		mSearchView.onActionViewExpanded();
		//mSearchView.setBackgroundColor(0xff000000);
		mSearchView.setIconifiedByDefault(true);
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
				@Override
				public boolean onQueryTextChange(String queryText) {
					if (TextUtils.isEmpty(queryText)) {
						lv.clearTextFilter();
					}else {
						lv.setFilterText(queryText);
					}
					return true;
				}

				@Override
				public boolean onQueryTextSubmit(String queryText) {
					if (mSearchView != null) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (imm != null) {
							imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法
						}
						mSearchView.clearFocus();
					}
					return true;
				}
			});
			
	}

	private void delete(String ss) throws IOException {
        String s = "";
        DataOutputStream os = null;
		BufferedReader br = null;
		OutputStreamWriter pw = null;
        try {
            Process p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("cat " + sPath + "\n");
            os.writeBytes("exit\n");
            os.flush();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                s += line.trim() + "\n";
            }
			pw = new OutputStreamWriter(new FileOutputStream(backupPath),"UTF-8");
			s = s.replace(ss,"");
			pw.write(s);
			pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                os.close();
            }
            if (br != null) {
                br.close();
            }
        }
    }


	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}
	}

	private void doMySearch(String query) {
		// TODO 自动生成的方法存根
		Toast.makeText(this, "do search " + query, 0).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view, menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
		searchView.setSearchableInfo(info);
		searchView.setIconifiedByDefault(false);
		
		/*super.onCreateOptionsMenu(menu);
		menu.add(0,0,0,"刷新");
		menu.add(0,1,0,"打开WiFi设置");
		menu.add(0,2,0,"备份与恢复");
		menu.add(0,3,0,"获取列表出错");
		menu.getItem(3).setEnabled(false);
		if(mainList != null) menu.getItem(3).setTitle("共 " + mainList.size() + " 条WiFi");
		*/
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				mainList.clear();
				mainList = get(sPath);
				onWork();
				return true;
			case 1:
				startActivity(new Intent().setClassName("com.android.settings","com.android.settings.wifi.WifiSettings"));
				return true;
			case 2:
				startActivity(new Intent().setClassName("com.zzz.wifiview","com.zzz.wifiview.FileActivity"));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public ArrayList<Map<String, String>> get(String path) {
        try {
			ReadFile file = new ReadFile(path);
            return file.getList(this.context);
        } catch (Exception e) {
			Toast.makeText(this, "ReadFile:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
	
}
