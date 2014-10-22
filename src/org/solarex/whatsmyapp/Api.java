
package org.solarex.whatsmyapp;

import android.R.integer;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import static org.solarex.whatsmyapp.Utils.log;

import java.util.HashMap;
import java.util.List;

public class Api {
    public static SolarexApp[] applications = null;

    public static SolarexApp[] getApps(Context ctx, boolean isShowSys)
    {
        log("Api getApps isShowSys = " + isShowSys);
        if (applications != null) {
            return applications;
        }
        final PackageManager pm = ctx.getPackageManager();
        final List<ApplicationInfo> installed = pm.getInstalledApplications(0);
        log("Api getApps installed = " + installed);
        final HashMap<Integer, SolarexApp> map = new HashMap<Integer, SolarexApp>();
        String name = null;
        SolarexApp app = null;
        for (ApplicationInfo applicationInfo : installed) {
            app = map.get(applicationInfo.uid);
            int isSysApp = applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM;
            log("app = " + app + " isShowSys = " + isShowSys + " isSysApp = " + isSysApp);
//            if ((app == null) && isShowSys && (isSysApp != 0)) {
//                log("into continue");
//                continue;
//            }
            if (!isShowSys) {
                if (app == null && (isSysApp != 0)) {
                    continue;
                }
            }
            // name = pm.getApplicationLabel(applicationInfo).toString();
            CharSequence charSequence = pm.getApplicationLabel(applicationInfo);
            if (null != charSequence) {
                name = charSequence.toString();
            } else {
                name = "Android OS";
            }
            log("uid: " + applicationInfo.uid + " name: " + name + " isSys: " + isSysApp);
            if (app == null) {
                app = new SolarexApp();
                app.uid = applicationInfo.uid;
                app.names = new String[] {
                        name
                };
                app.appInfo = applicationInfo;
                map.put(applicationInfo.uid, app);
            } else {
                final String newNames[] = new String[app.names.length + 1];
                System.arraycopy(app.names, 0, newNames, 0, app.names.length);
                newNames[app.names.length] = name;
            }
        }
        applications = map.values().toArray(new SolarexApp[map.size()]);
        for (SolarexApp app2 : applications) {
            log("getApps for uid = " + app2.uid + " begin");
            for (String name2 : app2.names) {
                log(name2);
            }
            log("getApps for uid = " + app2.uid + " end");
        }
        return applications;
    }
}
