/** 佛祖保佑，永无bug🙏🙏🙏 */
package com.zzz.wifiview;

import android.content.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ReadFile {
	
    ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();

    public ReadFile(String path) throws IOException {
        String s = "";
        DataOutputStream os = null;
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("cat " + path + "\n");
            os.writeBytes("exit\n");
            os.flush();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                s += line.trim() + "\n";
            }
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
        pp(s);
    }

    private void pp(String s) {
        Pattern pattern = Pattern.compile("network=\\{\\n([\\s\\S]+?)\\n\\}");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            add(matcher.group());
        }
    }

    private void add(String content) {
        content = content.substring(9, content.length() - 2);
        HashMap<String, String> map = new HashMap<String, String>();
        String[] list = content.split("\\n");
        String x, y;
        for (String info : list) {
            int index = info.indexOf("=");
            if (index > -1) {
                x = info.substring(0, index);
                y = info.substring(index + 1);
            } else {
                continue;
            }
            if ("ssid".equals(x)) {
                if (y.charAt(0) == '"') {
                    y = y.substring(1, y.length() - 1);
                } else {
                    y = toUTF8(y);
                }
            } else if ("psk".equals(x)) {
                y = y.substring(1, y.length() - 1);
            }
            if (y == null) {
                continue;
            }
            map.put(x, y);
        }
        this.list.add(map);
    }

    public ArrayList<Map<String, String>> getList(Context context) {
        ArrayList<Map<String, String>> m = new ArrayList<Map<String, String>>();
        for (Map<String, String> map : this.list) {
            if (map.containsKey("ssid") && map.containsKey("psk")) {
                m.add(map);
            }
        }
		return m;
    }
	
    private static String toUTF8(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        try {
            s = s.toUpperCase();
            int total = s.length() / 2;
            int pos = 0;
            byte[] buffer = new byte[total];
            for (int i = 0; i < total; i++) {
                int start = i * 2;
                buffer[i] = (byte) Integer.parseInt(s.substring(start, start + 2), 16);
                pos++;
            }
            return new String(buffer, 0, pos, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
