package com.alibaba.cobar;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Test {

    public static long getSHTime() {
        Gson gson = new Gson();
        String appccServer = "energy.powerone.pispower.com:8100";
        URL url = null;
        try {
            url = new URL("http://" + appccServer + "/business/sys/gettime");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(2000);
            urlConnection.connect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String in = null;
            while ((in = bufferedReader.readLine()) != null) {
                sb.append(in);
            }
            AppccTime at = gson.fromJson(sb.toString(), AppccTime.class);
            if (at.getCode() != 0) {
                throw new RuntimeException("get time has error because code is not 0");
            }
            System.out.println(at.getTime());
            return at.getTime();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        getSHTime();
    }

    public class AppccTime {
        private int code;
        private long time;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}
