package sarwadarifootwear.sarwadarifootwear.app.drawer.menu;

import android.view.Menu;
import android.view.MenuItem;


public class SimpleMenu extends SimpleAbstractMenu {
//https://www.fiverr.com/zuhaib88
    public SimpleMenu(Menu menu, MenuItemCallback callback){
        super();
        this.menu = menu;
        this.callback = callback;
    }

    public MenuItem add(String title, int drawable, Action action) {
        return add(menu, title, drawable, action);
    }

}
