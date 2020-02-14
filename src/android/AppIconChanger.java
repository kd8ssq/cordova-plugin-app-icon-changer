package org.apache.cordova.appiconchanger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AppIconChanger extends CordovaPlugin {
    List<String> disableNames = new ArrayList<>();
    String packageName = AppIconChanger.class.getPackage().getName();

    /**
     * Gets the application activity from cordova's main activity.
     *
     * @return the application activity
     */
    private Context getApplicationActivity() {
        return this.cordova.getActivity();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("isSupported".equals(action)) {
            callbackContext.success();
            return true;
        } else if ("changeIcon".equals(action)) {
            String activeActivityName;

            JSONObject argList = args.getJSONObject(0);
            String iconName = argList.getString("iconName");
            Boolean suppressUserNotification = argList.getBoolean("suppressUserNotification");

            // get the list of activities that need to be disabled
            this.getAppIconActivities(getApplicationActivity());

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.packageName);
            stringBuilder.append("." + getApplicationActivity().getClass().getSimpleName() + "__");
            stringBuilder.append(iconName);

            activeActivityName = stringBuilder.toString();

            this.setAppIcon(activeActivityName, disableNames);

            if (!suppressUserNotification) {
                this.iconChangeDialog(iconName);
            }

            callbackContext.success();
            return true;
        } else {
            callbackContext.error(action + " is not a supported action");
            return false;  // Returning false results in a "MethodNotFound" error.
        }
    }

    public void getAppIconActivities(Context context) {
        try {
            String activityName;
            String shortActivityName;

            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES);

            for (int i = 0; i < pi.activities.length; i++) {
                activityName = pi.activities[i].name;

                // only check the activities related to the AppIconChanger package
                if (activityName.contains(this.packageName)) {
                    shortActivityName = activityName.replace(this.packageName + ".", "");

                    if (!shortActivityName.equals(getApplicationActivity().getClass().getSimpleName())) {
                        disableNames.add(activityName);
                    }
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e("ERROR", "Could not get running activity list");
        }
    }

    public void setAppIcon(String activeActivityName, List<String> disableNames) {
        new AppIconNameChanger.Builder(cordova.getActivity())
                .activeActivityName(activeActivityName) // String
                .disableNames(disableNames) // List<String>
                .packageName(getApplicationActivity().getPackageName())
                .build()
                .setNow();
    }

    public void iconChangeDialog(String iconName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationActivity());

        Resources activityRes = getApplicationActivity().getResources();

        // get the resource IDs for all elements used from main cordova app (Ex. R.layout.appiconchanger) to get around importing the mainActivity class
        int appiconchangerResId = activityRes.getIdentifier("appiconchanger", "layout", getApplicationActivity().getPackageName());
        int appIconTextResId = activityRes.getIdentifier("appIconChangerDialog_appIconText", "id", getApplicationActivity().getPackageName());
        int appIconResId = activityRes.getIdentifier("appIconChangerDialog_appIcon", "id", getApplicationActivity().getPackageName());
        int appIconButtonResId = activityRes.getIdentifier("appIconChangerDialog_appIconButton", "id", getApplicationActivity().getPackageName());
        int ic_launcherResId = activityRes.getIdentifier("ic_launcher", "mipmap", getApplicationActivity().getPackageName());
        int app_nameResId = activityRes.getIdentifier("app_name", "string", getApplicationActivity().getPackageName());
        int default_icon_idResId = activityRes.getIdentifier("default_icon_id", "string", getApplicationActivity().getPackageName());

        String defaultIconID = activityRes.getString(default_icon_idResId);

        LayoutInflater factory = LayoutInflater.from(getApplicationActivity());
        View view = factory.inflate(appiconchangerResId, null);

        TextView appIconText = view.findViewById(appIconTextResId);
        ImageView appIcon = view.findViewById(appIconResId);
        Button appIconButton = view.findViewById(appIconButtonResId);

        // Set the Icon for the Dialog
        if (iconName.equals(defaultIconID)) {
            appIcon.setImageResource(ic_launcherResId);
        } else {
            // get the id of the icon
            int drawableID = getApplicationActivity().getResources().getIdentifier(iconName, "drawable", getApplicationActivity().getPackageName());
            appIcon.setImageResource(drawableID);
        }

        appIconText.setText("You have changed the icon for \"" + getApplicationActivity().getResources().getString(app_nameResId) + "\".");

        final AlertDialog alertDialog = builder.create();

        appIconButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(view);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}