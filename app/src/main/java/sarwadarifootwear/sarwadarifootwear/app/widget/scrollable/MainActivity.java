package sarwadarifootwear.sarwadarifootwear.app.widget.scrollable;

import sarwadarifootwear.sarwadarifootwear.app.App;
import sarwadarifootwear.sarwadarifootwear.app.Url;
import sarwadarifootwear.sarwadarifootwear.app.fragment.information;
import sarwadarifootwear.sarwadarifootwear.app.R;
import sarwadarifootwear.sarwadarifootwear.app.drawer.menu.Action;
import sarwadarifootwear.sarwadarifootwear.app.drawer.menu.MenuItemCallback;
import sarwadarifootwear.sarwadarifootwear.app.drawer.menu.SimpleMenu;
import sarwadarifootwear.sarwadarifootwear.app.util.ThemeUtils;
import sarwadarifootwear.sarwadarifootwear.app.widget.SwipeableViewPager;
import sarwadarifootwear.sarwadarifootwear.app.widget.webview.WebToAppWebClient;

import com.onesignal.OneSignal;
import com.tjeannin.apprate.AppRate;
import sarwadarifootwear.sarwadarifootwear.app.adapter.NavigationAdapter;
import sarwadarifootwear.sarwadarifootwear.app.fragment.WebFragment;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.MenuInflater;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MenuItemCallback {

    //Views
    public Toolbar mToolbar;
    public View mHeaderView;
    public TabLayout mSlidingTabLayout;
    public SwipeableViewPager mViewPager;
    private static final String ONESIGNAL_APP_ID = "789b6743-9e68-44e2-adc5-2bde3bdc1111";

    //App Navigation Structure
    private NavigationAdapter mAdapter;
    private NavigationView navigationView;
    private SimpleMenu menu;

    private WebFragment CurrentAnimatingFragment = null;
    private int CurrentAnimation = 0;

    private static int NO = 0;
    private static int HIDING = 1;
    private static int SHOWING = 2;
    private int interstitialCount = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.setTheme(this);

        setContentView(R.layout.activity_main);

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mHeaderView = (View) findViewById(R.id.header_container);
        mSlidingTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (SwipeableViewPager) findViewById(R.id.pager);
        //https://www.fiverr.com/zuhaib88        setSupportActionBar(mToolbar);



        mAdapter = new NavigationAdapter(getSupportFragmentManager(), this);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            String data = intent.getDataString();
            ((App) getApplication()).setPushUrl(data);
        }


        //Hiding ActionBar/Toolbar
        if (information.HIDE_ACTIONBAR)
          //  getSupportActionBar().hide();

        if (getHideTabs())
            mSlidingTabLayout.setVisibility(View.GONE);

        hasPermissionToDo(this, information.PERMISSIONS_REQUIRED);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
        if ((information.HIDE_ACTIONBAR && getHideTabs()) || ((information.HIDE_ACTIONBAR || getHideTabs()) && getCollapsingActionBar())){
            lp.topMargin = 0;
        } else if ((information.HIDE_ACTIONBAR || getHideTabs()) || (!information.HIDE_ACTIONBAR && !getHideTabs() && getCollapsingActionBar())){
            lp.topMargin = getActionBarHeight();
        } else if (!information.HIDE_ACTIONBAR && !getHideTabs()){
            lp.topMargin = getActionBarHeight() * 2;
        }

        mViewPager.setLayoutParams(lp);

        //Tabs
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(mViewPager.getAdapter().getCount() - 1);

        mSlidingTabLayout.setupWithViewPager(mViewPager);
        mSlidingTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (getCollapsingActionBar()) {
                    showToolbar(getFragment());
                }
                mViewPager.setCurrentItem(tab.getPosition());
                showInterstitial();
            }
        //https://www.fiverr.com/zuhaib88
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        for (int i = 0; i < mSlidingTabLayout.getTabCount(); i++) {
            if (information.ICONS.length > i  && information.ICONS[i] != 0) {
                mSlidingTabLayout.getTabAt(i).setIcon(information.ICONS[i]);
            }
        }

        //Drawer
        if (information.USE_DRAWER) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            DrawerLayout drawer =  ((DrawerLayout) findViewById(R.id.drawer_layout));
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, mToolbar, 0, 0);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            //Menu items
            navigationView = (NavigationView) findViewById(R.id.nav_view);
            menu = new SimpleMenu(navigationView.getMenu(), this);
            configureMenu(menu);

            if (information.HIDE_DRAWER_HEADER) {
                navigationView.getHeaderView(0).setVisibility(View.GONE);
                navigationView.setFitsSystemWindows(false);
            } else {
                if (information.DRAWER_ICON != R.drawable.icon)
                    ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_icon)).setImageResource(information.DRAWER_ICON);
                else {
                    ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.launcher_icon)).setVisibility(View.VISIBLE);
                    ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_icon)).setVisibility(View.INVISIBLE);
                }
            }
        } else {
            ((DrawerLayout) findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }



        //Application rating
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.rate_title))
                .setMessage(String.format(getString(R.string.rate_message), getString(R.string.app_name)))
                .setPositiveButton(getString(R.string.rate_yes), null)
                .setNegativeButton(getString(R.string.rate_never), null)
                .setNeutralButton(getString(R.string.rate_later), null);

        new AppRate(this)
                .setShowIfAppHasCrashed(false)
                .setMinDaysUntilPrompt(1)
                .setMinLaunchesUntilPrompt(2)
                .setCustomDialog(builder)
                .init();

        //Showing the splash screen
        if (information.SPLASH) {
            findViewById(R.id.imageLoading1).setVisibility(View.VISIBLE);
            //getFragment().browser.setVisibility(View.GONE);
        }

      //  OneSignal.promptForPushNotifications();
    }

    // using the back button of the device
    @Override
    public void onBackPressed() {
        View customView = null;
        WebChromeClient.CustomViewCallback customViewCallback = null;
        if (getFragment().chromeClient != null) {
            customView = getFragment().chromeClient.getCustomView();
            customViewCallback = getFragment().chromeClient.getCustomViewCallback();
        }

        if ((customView == null)
                && getFragment().browser.canGoBack()) {
            getFragment().browser.goBack();
        } else if (customView != null
                && customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        //Adjust menu item visibility/availability based on settings
        if (information.HIDE_MENU_SHARE) {
            menu.findItem(R.id.share).setVisible(false);
        }
        if (information.HIDE_MENU_HOME) {
            menu.findItem(R.id.home).setVisible(false);
        }
        if (information.HIDE_MENU_NAVIGATION){
            menu.findItem(R.id.previous).setVisible(false);
            menu.findItem(R.id.next).setVisible(false);
        }
        if (!information.SHOW_NOTIFICATION_SETTINGS || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            menu.findItem(R.id.notification_settings).setVisible(false);
        }

        ThemeUtils.tintAllIcons(menu, this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        WebView browser = getFragment().browser;
        if (item.getItemId() == (R.id.next)) {
            browser.goForward();
            return true;
        } else if (item.getItemId() == R.id.previous) {
            browser.goBack();
            return true;
        } else if (item.getItemId() == R.id.share) {
            getFragment().shareURL();
            return true;
        } else if (item.getItemId() == R.id.about) {
            AboutDialog();
            return true;
        } else if (item.getItemId() == R.id.home) {
            browser.loadUrl(getFragment().mainUrl);
            return true;
        } else if (item.getItemId() == R.id.close) {
            finish();
            Toast.makeText(getApplicationContext(),
                    getText(R.string.exit_message), Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.notification_settings){
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Showing the About Dialog
     */
    private void AboutDialog() {
        // setting the dialogs text, and making the links clickable
        final TextView message = new TextView(this);
        // i.e.: R.string.dialog_message =>
        final SpannableString s = new SpannableString(
                this.getText(R.string.dialog_about));
        Linkify.addLinks(s, Linkify.WEB_URLS);
        message.setTextSize(15f);
        int padding  = Math.round(20 * getResources().getDisplayMetrics().density);
        message.setPadding(padding, 15, padding, 15);
        message.setText(Html.fromHtml(getString(R.string.dialog_about)));
        message.setMovementMethod(LinkMovementMethod.getInstance());

        // creating the actual dialog

        AlertDialog.Builder AlertDialog = new AlertDialog.Builder(this);
        AlertDialog.setTitle(Html.fromHtml(getString(R.string.about)))
                // .setTitle(R.string.about)
                .setCancelable(true)
                // .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("ok", null).setView(message).create().show();
    }

    /**
     * Set the ActionBar Title
     * @param title title
     */
//    public void setTitle(String title) {
//        if (mAdapter != null && mAdapter.getCount() == 1 && !information.USE_DRAWER && !information.STATIC_TOOLBAR_TITLE)
////            getSupportActionBar().setTitle(title);
//    }

    /**
     * @return the Current WebFragment
     */
    public WebFragment getFragment(){
        return (WebFragment) mAdapter.getCurrentFragment();
    }

    /**
     * Hide the Splash Screen
     */
    public void hideSplash() {
        if (information.SPLASH) {
            if (findViewById(R.id.imageLoading1).getVisibility() == View.VISIBLE) {
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        // hide splash image
                        findViewById(R.id.imageLoading1).setVisibility(
                                    View.GONE);
                    }
                    // set a delay before splashscreen is hidden
                }, information.SPLASH_SCREEN_DELAY);
            }
        }
    }

    /**
     * Hide the toolbar
     */
    public void hideToolbar() {
        if (CurrentAnimation != HIDING) {
            CurrentAnimation = HIDING;
            AnimatorSet animSetXY = new AnimatorSet();

            ObjectAnimator animY = ObjectAnimator.ofFloat(getFragment().rl, "y", 0);
            ObjectAnimator animY1 = ObjectAnimator.ofFloat(mHeaderView, "y", -getActionBarHeight());
            animSetXY.playTogether(animY, animY1);

            animSetXY.start();
            animSetXY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    CurrentAnimation = NO;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

        }
    }

    /**
     * Show the toolbar
     * @param fragment for which to show the toolbar
     */
    public void showToolbar(WebFragment fragment) {
        if (CurrentAnimation != SHOWING || fragment != CurrentAnimatingFragment) {
            CurrentAnimation = SHOWING;
            CurrentAnimatingFragment = fragment;
            AnimatorSet animSetXY = new AnimatorSet();
            ObjectAnimator animY = ObjectAnimator.ofFloat(fragment.rl, "y", getActionBarHeight());
            ObjectAnimator animY1 = ObjectAnimator.ofFloat(mHeaderView, "y", 0);
            animSetXY.playTogether(animY, animY1);

            animSetXY.start();
            animSetXY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    CurrentAnimation = NO;
                    CurrentAnimatingFragment = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

        }
    }

    public int getActionBarHeight() {
        int mHeight = mToolbar.getHeight();

        //Just in case we get a unreliable result, get it from metrics
        if (mHeight == 0){
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            {
                mHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
        }

        return mHeight;
    }

    boolean getHideTabs(){
        if (mAdapter.getCount() == 1 || information.USE_DRAWER){
            return true;
        } else {
            return information.HIDE_TABS;
        }
    }

    public static boolean getCollapsingActionBar(){
        if (information.COLLAPSING_ACTIONBAR && !information.HIDE_ACTIONBAR){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check permissions on app start
     * @param context
     * @param permissions Permissions to check
     * @return if the permissions are available
     */
    private static boolean hasPermissionToDo(final Activity context, final String[] permissions) {
        boolean oneDenied = false;
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ContextCompat.checkSelfPermission(context, permission)
                            != PackageManager.PERMISSION_GRANTED)
                oneDenied = true;
        }

        if (!oneDenied) return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.common_permission_explaination);
        builder.setPositiveButton(R.string.common_permission_grant, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    context.requestPermissions(permissions,1);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }

    /**
     * Show an interstitial ad
     */
    public void showInterstitial(){
        if (interstitialCount == (information.INTERSTITIAL_INTERVAL - 1)) {
            interstitialCount = 0;
        } else {
            interstitialCount++;
        }
    }

    /**
     * Configure the navigationView
     * @param menu to modify
     */
    public void configureMenu(SimpleMenu menu){
        for (int i = 0; i < information.TITLES.length; i++) {
            //The title
            String title = null;
            Object titleObj = information.TITLES[i];
            if (titleObj instanceof Integer && !titleObj.equals(0)) {
                title = getResources().getString((int) titleObj);
            } else {
                title = (String) titleObj;
            }

            //The icon
            int icon = 0;
            if (information.ICONS.length > i)
                icon = information.ICONS[i];

            menu.add((String) information.TITLES[i], icon, new Action(title, Url.URLS[i]));
        }

        menuItemClicked(menu.getFirstMenuItem().getValue(), menu.getFirstMenuItem().getKey());
    }

    @Override
    public void menuItemClicked(Action action, MenuItem item) {
        if (WebToAppWebClient.urlShouldOpenExternally(action.url)){
            //Load url outside WebView
            try {
                startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(action.url)));
            } catch(ActivityNotFoundException e) {
                if (action.url.startsWith("intent://")) {
                    startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(action.url.replace("intent://", "http://"))));
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_app_message), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            //Uncheck all other items, check the current item
            for (MenuItem menuItem : menu.getMenuItems())
                menuItem.setChecked(false);
            item.setChecked(true);

            //Close the drawer
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

            //Load the url
            if (getFragment() == null) return;
            getFragment().browser.loadUrl("about:blank");
            getFragment().setBaseUrl(action.url);

            //Show intersitial if applicable
            showInterstitial();
            Log.v("INFO", "Drawer Item Selected");
        }
    }

    public void btnMovies(View view){
        WebView webview = getFragment().browser;
        webview.goBack();


    }
    public void btnMovies1(View view){



        WebView webview = getFragment().browser;
        webview.goForward();


    }
    public void btnTopVideos1(View view){
        WebView webview = getFragment().browser;
        webview.loadUrl("https://sarwadarifootwear.in");

//        txtHome.setTextColor(getResources().getColor(R.color.gray));
//        txtAccount.setTextColor(getResources().getColor(R.color.gray));
//        txtCategory.setTextColor(getResources().getColor(R.color.gray));
//        txtCart.setTextColor(getResources().getColor(R.color.red));
//        txtSupport.setTextColor(getResources().getColor(R.color.gray));
//
//        home.setColorFilter(getResources().getColor(R.color.gray));
//        account.setColorFilter(getResources().getColor(R.color.gray));
//        category.setColorFilter(getResources().getColor(R.color.gray));
//        cart.setColorFilter(getResources().getColor(R.color.red));
//        support.setColorFilter(getResources().getColor(R.color.gray));


    }



//    public void btnTrending(View view){
//        WebView webview = getFragment().browser;
//
//        webview.loadUrl("https://dallali.com/ar/account/edit/");
//        txtHome.setTextColor(getResources().getColor(R.color.red));
//        txtAccount.setTextColor(getResources().getColor(R.color.gray));
//        txtCategory.setTextColor(getResources().getColor(R.color.gray));
//        txtCart.setTextColor(getResources().getColor(R.color.gray));
//        txtSupport.setTextColor(getResources().getColor(R.color.gray));
//
//        home.setColorFilter(getResources().getColor(R.color.red));
//        account.setColorFilter(getResources().getColor(R.color.gray));
//        category.setColorFilter(getResources().getColor(R.color.gray));
//        cart.setColorFilter(getResources().getColor(R.color.gray));
//        support.setColorFilter(getResources().getColor(R.color.gray));
//
//    }
//    public void btnMovies(View view){
//        WebView webview = getFragment().browser;
//        webview.loadUrl("https://dallali.com/ar/logs/");
//
//        txtHome.setTextColor(getResources().getColor(R.color.gray));
//        txtAccount.setTextColor(getResources().getColor(R.color.red));
//        txtCategory.setTextColor(getResources().getColor(R.color.gray));
//        txtCart.setTextColor(getResources().getColor(R.color.gray));
//        txtSupport.setTextColor(getResources().getColor(R.color.gray));
//
//        home.setColorFilter(getResources().getColor(R.color.gray));
//        account.setColorFilter(getResources().getColor(R.color.red));
//        category.setColorFilter(getResources().getColor(R.color.gray));
//        cart.setColorFilter(getResources().getColor(R.color.gray));
//        support.setColorFilter(getResources().getColor(R.color.gray));
//
//    }
//    public void btnTopVideos(View view){
//        WebView webview = getFragment().browser;
//        webview.loadUrl("https://dallali.com/ar/application-requests/");
//
//        txtHome.setTextColor(getResources().getColor(R.color.gray));
//        txtAccount.setTextColor(getResources().getColor(R.color.gray));
//        txtCategory.setTextColor(getResources().getColor(R.color.red));
//        txtCart.setTextColor(getResources().getColor(R.color.gray));
//        txtSupport.setTextColor(getResources().getColor(R.color.gray));
//
//        home.setColorFilter(getResources().getColor(R.color.gray));
//        account.setColorFilter(getResources().getColor(R.color.gray));
//        category.setColorFilter(getResources().getColor(R.color.red));
//        cart.setColorFilter(getResources().getColor(R.color.gray));
//        support.setColorFilter(getResources().getColor(R.color.gray));
//
//
//    }
//    public void btnMyCart(View view){
//        WebView webview = getFragment().browser;
//        webview.loadUrl("https://dallali.com/ar/visit-request/");
//
//        txtHome.setTextColor(getResources().getColor(R.color.gray));
//        txtAccount.setTextColor(getResources().getColor(R.color.gray));
//        txtCategory.setTextColor(getResources().getColor(R.color.gray));
//        txtCart.setTextColor(getResources().getColor(R.color.red));
//        txtSupport.setTextColor(getResources().getColor(R.color.gray));
//
//        home.setColorFilter(getResources().getColor(R.color.gray));
//        account.setColorFilter(getResources().getColor(R.color.gray));
//        category.setColorFilter(getResources().getColor(R.color.gray));
//        cart.setColorFilter(getResources().getColor(R.color.red));
//        support.setColorFilter(getResources().getColor(R.color.gray));
//
//
//    }
//    public void btnSubscriptions(View view){
//
//        WebView webview = getFragment().browser;
//
//        webview.loadUrl("https://dallali.com/ar/dashboard/");
//        txtHome.setTextColor(getResources().getColor(R.color.gray));
//        txtAccount.setTextColor(getResources().getColor(R.color.gray));
//        txtCategory.setTextColor(getResources().getColor(R.color.gray));
//        txtCart.setTextColor(getResources().getColor(R.color.gray));
//        txtSupport.setTextColor(getResources().getColor(R.color.red));
//
//        home.setColorFilter(getResources().getColor(R.color.gray));
//        account.setColorFilter(getResources().getColor(R.color.gray));
//        category.setColorFilter(getResources().getColor(R.color.gray));
//        cart.setColorFilter(getResources().getColor(R.color.gray));
//        support.setColorFilter(getResources().getColor(R.color.red));
//
//    }
}
