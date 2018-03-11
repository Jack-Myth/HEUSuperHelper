package d.o.JackMyth.HEUSuperHelper;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * Created by JackMyth on 3/11/2018.
 */

public class GlobalApplication extends Application
{
    public static GlobalApplication App;
    public static WebView BrowserToCheckNewVersion=null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        App =this;
    }

    public void CheckNewVersion(final Context c)
    {
        if (BrowserToCheckNewVersion==null)
        {
            BrowserToCheckNewVersion = new WebView(App);
            BrowserToCheckNewVersion.getSettings().setJavaScriptEnabled(true);
            BrowserToCheckNewVersion.addJavascriptInterface(new Object()
            {
                @JavascriptInterface
                public void ReportNewestVersion(final int Version, String VersionName, String VersionMessage)
                {
                    try
                    {
                        final int V= getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
                        if (V<Version&&getSharedPreferences("Settings",Context.MODE_PRIVATE).getInt("MinCheckVersion",0)<Version)
                        {
                            new AlertDialog.Builder(c)
                                    .setTitle("发现新版本:" + VersionName)
                                    .setMessage(VersionMessage)
                                    .setPositiveButton("更新!", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            Intent intent = new Intent();
                                            intent.setAction("android.intent.action.VIEW");
                                            Uri content_url = Uri.parse("http://jackmyth.cn/Application/DownloadHEUSuperHelper.php");
                                            intent.setData(content_url);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("暂时不",null)
                                    .setNeutralButton("此版本不再提醒", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            getSharedPreferences("Settings",Context.MODE_PRIVATE).edit().putInt("MinCheckVersion",Version).commit();
                                        }
                                    })
                                    .show();
                        }
                    } catch (PackageManager.NameNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    BrowserToCheckNewVersion=null;
                }
            },"JSInterface");
            BrowserToCheckNewVersion.loadUrl("http://jackmyth.cn/HEUSuperHelper/CheckUpdate.php");
        }
    }
}
