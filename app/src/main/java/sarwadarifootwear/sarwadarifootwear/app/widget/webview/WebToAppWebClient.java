package sarwadarifootwear.sarwadarifootwear.app.widget.webview;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import sarwadarifootwear.sarwadarifootwear.app.fragment.information;
import sarwadarifootwear.sarwadarifootwear.app.R;
import sarwadarifootwear.sarwadarifootwear.app.fragment.WebFragment;

public class WebToAppWebClient extends WebViewClient {

    WebFragment fragment;
    WebView browser;

    public WebToAppWebClient(WebFragment fragment, WebView browser)
    {
        super();
        this.fragment = fragment;
        this.browser = browser;
    }

    @TargetApi(android.os.Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoading(view, request.getUrl().toString());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (urlShouldOpenExternally(url)) {
            // Load new URL Don't override URL Link
            Toast.makeText(fragment.getActivity(), url, Toast.LENGTH_SHORT).show();
            if(url.equals("intent://user/148549141853150/?intent_trigger=mme&nav=discover&source_id=1441792#Intent;scheme=fb-messenger;package=com.facebook.orca;end")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb-messenger://user/148549141853150"));
                    fragment.getActivity().startActivity(intent);
                } catch (Exception e) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb-messenger://user/148549141853150"));
                    fragment.getActivity().startActivity(intent);
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/onbiponi"));
//                    fragment.getActivity().startActivity(intent);
                }
            }else{
            try {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch(ActivityNotFoundException e) {
                if (url.startsWith("intent://")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url.replace("intent://", "http://"))));
                } else {
                    Toast.makeText(fragment.getActivity(), fragment.getActivity().getResources().getString(R.string.no_app_message), Toast.LENGTH_LONG).show();
                }
            }
            }

            return true;
        } else if (url.endsWith(".mp4") || url.endsWith(".avi")
                || url.endsWith(".flv")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/mp4");
                view.getContext().startActivity(intent);
            } catch (Exception e) {
                // error
            }

            return true;
        } else if (url.endsWith(".mp3") || url.endsWith(".wav")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "audio/mp3");
                view.getContext().startActivity(intent);
            } catch (Exception e) {
                // error
            }

            return true;
        }

        // Return true to override url loading (In this case do
        // nothing).
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        //
    }

    @TargetApi(android.os.Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
        // Redirect to deprecated method, so you can use it in all SDK versions
        onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
    }

    // handeling errors
    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        if (hasConnectivity("", false)) {
            //If an error occurred while we had connectivity, the page must be borken
            fragment.showErrorScreen(fragment.getActivity().getString(R.string.error));
        } else {
            //If we don't have connectivity, and this isn't an online page, let hasConnectivity handle the error
            if (!failingUrl.startsWith("file:///android_asset")) {
                hasConnectivity("", true);
            }
        }
    }


    public boolean hasConnectivity(String loadUrl, boolean showDialog) {
        boolean enabled = true;

        if (loadUrl.startsWith("file:///android_asset")){
            return true;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) fragment.getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if ((info == null || !info.isConnected() || !info.isAvailable())) {

            enabled = false;

            if (showDialog){
                if (information.NO_CONNECTION_PAGE.length() > 0 && information.NO_CONNECTION_PAGE.startsWith("file:///android_asset")) {
                    browser.loadUrl(information.NO_CONNECTION_PAGE);
                } else {
                    fragment.showErrorScreen(fragment.getActivity().getString(R.string.no_connection));
                }
            }
        }
        return enabled;
    }


    public static boolean urlShouldOpenExternally(String url){


        if (information.OPEN_ALL_OUTSIDE_EXCEPT.length > 0) {
            for (String pattern : information.OPEN_ALL_OUTSIDE_EXCEPT) {
                if (!url.contains(pattern))
                    return true;
            }
        }

        for (String pattern : information.OPEN_OUTSIDE_WEBVIEW){
            if (url.contains(pattern))
                return true;
        }

        return false;
    }
}