package org.solarex.whatsmyapp;

import android.R.integer;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class SolarexApp {
    int uid;
    String names[];
    ApplicationInfo appInfo;
    PackageInfo pkgInfo;
    Drawable cachedIcon;
    String toStr;
    boolean iconLoaded;
    boolean isSys;
    
    public SolarexApp(){
    }
    
    public SolarexApp(int uid, String name){
        this.uid = uid;
        this.names = new String[]{name};
    }

    @Override
    public String toString() {
        if (null == this.toStr ) {
            final StringBuilder sb = new StringBuilder();
            /*
            if (uid > 0) {
                sb.append(uid + ": ");
            }
            */
            for (int i = 0; i < names.length; i++) {
                if (i!=0) {
                    sb.append(",");
                }
                sb.append(names[i]);
            }
            toStr = sb.toString();
        }
        return toStr;
    }
    
    
}
