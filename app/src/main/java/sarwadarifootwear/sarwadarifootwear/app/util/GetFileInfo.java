package sarwadarifootwear.sarwadarifootwear.app.util;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;


public class GetFileInfo extends AsyncTask<String, Integer, String> {
    protected String doInBackground(String... urls) {
        URL url;
        String filename = null;
        try {
            url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            conn.setInstanceFollowRedirects(false);
//https://www.fiverr.com/zuhaib88
            String depo = conn.getHeaderField("Content-Disposition");
            if (depo == null)
                return null;
            String depoSplit[] = depo.split("filename=");
            filename = depoSplit[1].replace("filename=", "").replace("\"", "").trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filename;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // use result as file name
    }
}