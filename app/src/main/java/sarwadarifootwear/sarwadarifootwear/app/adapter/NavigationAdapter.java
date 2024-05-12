package sarwadarifootwear.sarwadarifootwear.app.adapter;

import sarwadarifootwear.sarwadarifootwear.app.Url;
import sarwadarifootwear.sarwadarifootwear.app.fragment.information;
import sarwadarifootwear.sarwadarifootwear.app.fragment.WebFragment;

import android.app.Activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.ViewGroup;

public class NavigationAdapter extends CacheFragmentStatePagerAdapter {

    private Fragment mCurrentFragment;
    private Activity mContext;
//https://www.fiverr.com/zuhaib88
    public NavigationAdapter(FragmentManager fm, Activity activity) {
        super(fm);
        mContext = activity;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            mCurrentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    @Override
    protected Fragment createItem(int position) {
        // Initialize fragments.
        // Please be sure to pass scroll position to each fragments using setArguments.
        Fragment f;
        //final int pattern = position % 3;
        f = WebFragment.newInstance(Url.URLS[position]);
        return f;
    }

    @Override
    public int getCount() {
        if (information.USE_DRAWER)
            return 1;
        else
            return information.TITLES.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //If there is a localized title available, use it
        Object title = information.TITLES[position];
        if (title instanceof Integer && !title.equals(0)){
            return mContext.getResources().getString((int) title);
        } else {
            return (String) title;
        }
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }
}
