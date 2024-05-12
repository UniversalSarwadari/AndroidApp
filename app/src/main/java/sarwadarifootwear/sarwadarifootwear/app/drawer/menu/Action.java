package sarwadarifootwear.sarwadarifootwear.app.drawer.menu;

import java.io.Serializable;


public class Action implements Serializable{

    public String name;
    public String url;
//https://www.fiverr.com/zuhaib88
    public Action(String name, String url){
        this.name = name;
        this.url = url;
    }

}
