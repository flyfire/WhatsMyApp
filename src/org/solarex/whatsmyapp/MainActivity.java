
package org.solarex.whatsmyapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import static org.solarex.whatsmyapp.Utils.log;

public class MainActivity extends Activity {
    private LinearLayout loadingProgress = null;
    private ListView allApps = null;
    private TextView noApps = null;
    private AppAdapter adapter = null;
    private String PREF_NAME = "solarex";
    private String SHOW_SYS = "ShowSys";
    private int index,top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("MainActivity: onCreate enter");
        setContentView(R.layout.activity_main);
        loadingProgress = (LinearLayout) this.findViewById(R.id.loading_progress);
        allApps = (ListView) this.findViewById(R.id.allapps);
        noApps = (TextView) this.findViewById(R.id.no_apps);
        getSharedPreferences(PREF_NAME, 0).edit().putBoolean(SHOW_SYS, false).commit();
        allApps.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != Api.applications) {
                    final SolarexApp app = Api.applications[position];
                    log("OnItemClick app = " + app);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    Resources res = getResources();
                    builder.setTitle(res.getString(R.string.show_info));
                    builder.setIcon(app.cachedIcon);
                    StringBuilder sbBuilder = new StringBuilder();
                    sbBuilder.append(res.getString(R.string.appinfo_name)+app.toString()+"\n");
                    sbBuilder.append(res.getString(R.string.package_name)+app.pkgInfo.packageName+"\n");
                    sbBuilder.append(res.getString(R.string.version_name)+app.pkgInfo.versionName);
                    builder.setMessage(sbBuilder.toString());
                    
                    builder.setPositiveButton(res.getString(R.string.start_app), new DialogInterface.OnClickListener(   ) {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*
                            ActivityInfo activityInfo = app.pkgInfo.activities[0];
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName(app.pkgInfo.packageName, activityInfo.name));
                            */
                            Intent intent = getPackageManager().getLaunchIntentForPackage(app.pkgInfo.packageName);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                MainActivity.this.startActivity(intent);
                            } else {
                                log("intenet = " + intent + " cant be resolved");
                            }
                        }
                    }); 
                    builder.setNegativeButton(res.getString(R.string.uninstall), new DialogInterface.OnClickListener() {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri packageUri = Uri.parse("package:" + app.pkgInfo.packageName);
                            Intent unIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                            MainActivity.this.startActivity(unIntent);
                        }
                    });
                    
                    AlertDialog dialog = builder.create();
                    ActivityInfo activityInfo[] = app.pkgInfo.activities;
                    
                    dialog.show();
                    Intent intent = getPackageManager().getLaunchIntentForPackage(app.pkgInfo.packageName);
                    if (null == intent) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                    if (app.isSys) {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                    }
                    
                    
                }
            }

        });
        log("MainActivity: onCreate exit");
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("MainActivity: onResume enter");
        final SharedPreferences pref = this.getSharedPreferences(PREF_NAME, 0);
        final Boolean showSys = pref.getBoolean(SHOW_SYS, false);
        showOrLoadApplications(showSys);
        log("MainActivity: onResume exit");
    }

    @Override
    protected void onPause() {
        super.onPause();
        index = allApps.getFirstVisiblePosition();
        View v = allApps.getChildAt(0);
        top = (v==null)?0:v.getTop();
        log("MainActivity: onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Api.applications = null;
        log("MainActivity: onDestory");
    }

    private void showOrLoadApplications(boolean showSys) {
        log("MainActivity: showOrLoadApplications enter");
        if (Api.applications == null) {
            new GetAppTask().execute(showSys);
        } else {
            showApplications();
        }
        log("MainActivity: showOrLoadApplications exit");
    }

    private void showApplications() {
        log("MainActivity: showApplications, enter");
        final SolarexApp[] apps = Api.applications;

        if (adapter == null) {
            adapter = new AppAdapter(this, apps);
            allApps.setAdapter(adapter);
        } else {
            adapter.bindApps(apps);
            allApps.setAdapter(adapter);
        }
        allApps.setSelectionFromTop(index, top);
        log("MainActivity: showApplications, exit");
    }

    final class AppAdapter extends BaseAdapter {
        private SolarexApp[] apps = null;
        private LayoutInflater inflater;

        public AppAdapter(Context ctx, SolarexApp[] apps) {
            this.apps = apps;
            this.inflater = getLayoutInflater();
        }

        public void bindApps(SolarexApp[] apps) {
            this.apps = apps;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return apps.length;
        }

        @Override
        public Object getItem(int position) {
            return apps[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // log("MainActivity: AppAdapter getView enter");
            ListEntry entry;
            if (null == convertView) {
                // convertView = inflater.inflate(R.layout.list_item, parent);
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                entry = new ListEntry();
                entry.icon = (ImageView) convertView.findViewById(R.id.icon);
                entry.text = (TextView) convertView.findViewById(R.id.label);
                convertView.setTag(entry);
            } else {
                entry = (ListEntry) convertView.getTag();
            }
            final SolarexApp app = apps[position];
            entry.app = app;
            entry.text.setText(app.toString());
            entry.icon.setImageDrawable(app.cachedIcon);

            if (!app.iconLoaded && app.appInfo != null) {
                new LoadIconTask().execute(app, getPackageManager(), convertView);
            }
            // log("MainActivity: AppAdapter getView exit");
            return convertView;
        }
    }

    private class LoadIconTask extends AsyncTask<Object, Void, View> {

        @Override
        protected View doInBackground(Object... params) {
            log("MainActivity LoadIconTask doInbackground enter");
            final SolarexApp app = (SolarexApp) params[0];
            final PackageManager pm = (PackageManager) params[1];
            final View viewToUpdate = (View) params[2];

            if (!app.iconLoaded) {
                app.cachedIcon = pm.getApplicationIcon(app.appInfo);
                app.iconLoaded = true;
            }
            log("MainActivity LoadIconTask doInBackground exit app = " + app.toString());
            return viewToUpdate;
        }

        @Override
        protected void onPostExecute(View result) {
            super.onPostExecute(result);
            final ListEntry entry = (ListEntry) result.getTag();
            entry.icon.setImageDrawable(entry.app.cachedIcon);
        }

    }

    private class ListEntry {
        private TextView text;
        private ImageView icon;
        SolarexApp app;
    }

    private class GetAppTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Boolean... params) {
            Api.getApps(MainActivity.this, params[0].booleanValue());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            loadingProgress.setVisibility(View.GONE);
            if (Api.applications == null || Api.applications.length == 0) {
                noApps.setVisibility(View.VISIBLE);
            } else {
                showApplications();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            boolean isShowSys = this.getSharedPreferences(PREF_NAME, 0).getBoolean(SHOW_SYS, false);
            log("optionsItem isShowSys = " + isShowSys);
            isShowSys = !isShowSys;
            Api.applications = null;
            showOrLoadApplications(isShowSys);
            this.getSharedPreferences(PREF_NAME, 0).edit().putBoolean(SHOW_SYS, isShowSys).commit();
            if (isShowSys) {
                item.setTitle(getResources().getString(R.string.show_sys_no));
            } else {
                item.setTitle(getResources().getString(R.string.show_sys_yes));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
