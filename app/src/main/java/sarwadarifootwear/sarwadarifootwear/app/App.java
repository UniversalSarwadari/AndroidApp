package sarwadarifootwear.sarwadarifootwear.app;

import android.app.Application;

public class App extends Application { 
	

      private String push_url = null;

    @Override public void onCreate() {
        super.onCreate();

    }



    public synchronized String getPushUrl(){
        String url = push_url;
        push_url = null;
        return url;
    }

    public synchronized void setPushUrl(String url){
        this.push_url = url;
    }
} 