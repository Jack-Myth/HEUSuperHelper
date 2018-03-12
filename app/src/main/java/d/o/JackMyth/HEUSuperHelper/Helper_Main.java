package d.o.JackMyth.HEUSuperHelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JackMyth on 3/7/2018.
 */

public class Helper_Main extends AppCompatActivity
{
    WebView MainWebView;
    Handler DelayPoster;
    long LastBackTime=0;
    ExpandableListView ELV;
    WebListELVAdapter ELVAdapter;
    DrawerLayout DrawerL;
    Map<String,?> WebSites;
    boolean AutoLogining=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.helper_main);
            final Helper_Main Me=this;
            GlobalApplication.App.CheckNewVersion(Me);
            MainWebView=(WebView)findViewById(R.id.Main_MainWebView);
            ELV=findViewById(R.id.WebList);
            DrawerL=findViewById(R.id.DrawerL);
            WebSites=getSharedPreferences("Websites",Context.MODE_PRIVATE).getAll();
            ELVAdapter=new WebListELVAdapter();
            ELVAdapter.SetDataRef(this);
            ELV.setAdapter(ELVAdapter);
            MainWebView.getSettings().setJavaScriptEnabled(true);
            MainWebView.getSettings().setUseWideViewPort(true);
            MainWebView.getSettings().setLoadWithOverviewMode(true);
            MainWebView.getSettings().setSupportZoom(true);
            MainWebView.getSettings().setBuiltInZoomControls(true);
            MainWebView.getSettings().setDisplayZoomControls(false);
            MainWebView.getSettings().setAllowContentAccess(true);
            MainWebView.setWebChromeClient(new WebChromeClient());
            MainWebView.addJavascriptInterface(new Object()
            {
                @JavascriptInterface
                public void AutoLoginForCAS(String LT,String execution)
                {
                    //自动登陆CAS
                    SharedPreferences SP= getSharedPreferences("AutoLoginForCAS",Context.MODE_PRIVATE);
                    final String PostString="username="+SP.getString("Account","")+
                            "&password="+SP.getString("Password","")+
                            "&captcha=&lt="+LT+"&execution="+execution+"&_eventId=submit";
                    //MainWebView.postUrl(url, Base64.encode(PostString.getBytes(),Base64.DEFAULT));
                    AutoLogining=true;
                    Me.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String tmpURL=MainWebView.getUrl();
                            MainWebView.postUrl(tmpURL, PostString.getBytes());
                        }
                    });
                }
            },"HEUSuperHelper");
            MainWebView.setWebViewClient(new WebViewClient()
            {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
                {
                    //强制访问证书出错的网站
                    handler.proceed();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView webview, String url)
                {
                    //首先处理自动登陆事件
                    SharedPreferences SP= getSharedPreferences("AutoLoginForCAS",Context.MODE_PRIVATE);
                    if (SP.getBoolean("Enabled",false)&&url.contains("cas.hrbeu.edu.cn/cas/login")&&!AutoLogining)
                    {
                        MainWebView.getSettings().setBlockNetworkImage(true);
                        MainWebView.getSettings().setLoadsImagesAutomatically(false);
                        Toast.makeText(Me,"正在自动登陆",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        MainWebView.getSettings().setBlockNetworkImage(false);
                        MainWebView.getSettings().setLoadsImagesAutomatically(true);
                        AutoLogining=false;
                    }
                    webview.loadUrl(url);
                    return true;

                }

                @Override
                public void onPageFinished(WebView view, String url)
                {
                    super.onPageFinished(view, url);
                    if (!AutoLogining&&url.contains("cas.hrbeu.edu.cn/cas/login"))
                    {
                        SharedPreferences SP= getSharedPreferences("AutoLoginForCAS",Context.MODE_PRIVATE);
                        if (SP.getBoolean("Enabled",false))
                        {
                            view.loadUrl("javascript:window.HEUSuperHelper.AutoLoginForCAS(document.getElementsByName('lt')[0].value,document.getElementsByName('execution')[0].value);");
                        }
                    }
                }
            });
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().removeSessionCookie();
        String[] Cookies=getIntent().getStringExtra("Cookies").split(";");
        for (int i=0;i<Cookies.length;i++)
            CookieManager.getInstance().setCookie("ssl.hrbeu.edu.cn",Cookies[i]);
        CookieSyncManager.getInstance().sync();
        MainWebView.loadUrl("https://ssl.hrbeu.edu.cn/web/1/http/0/edusys.hrbeu.edu.cn/jsxsd/");
        DelayPoster=new Handler();
        //添加按钮Listener
        findViewById(R.id.MenuLayout_Add).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final EditText InputWebsiteName=new EditText(Me);
                final String URL= MainWebView.getUrl();
                InputWebsiteName.setText(MainWebView.getTitle());
                new AlertDialog.Builder(Me)
                        .setView(InputWebsiteName)
                        .setTitle("输入网站名称")
                        //.setMessage("网址:"+URL)
                        .setCancelable(true)
                        .setPositiveButton("添加", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(!getSharedPreferences("Websites",Context.MODE_PRIVATE).getString(InputWebsiteName.getText().toString(),"").equals(""))
                                {
                                    new AlertDialog.Builder(Me)
                                            .setTitle("此名称已存在")
                                            .show();
                                    return;
                                }
                                getSharedPreferences("Websites",Context.MODE_PRIVATE).edit().putString(InputWebsiteName.getText().toString(),URL).commit();
                                Me.RefreshWebsiteList();
                                Toast.makeText(Me,"已添加",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消",null)
                        .show();
            }
        });
        findViewById(R.id.MenuLayout_Input).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final EditText InputWebsite=new EditText(Me);
                InputWebsite.setHint("输入网址");
                new AlertDialog.Builder(Me)
                        .setTitle("打开自定义网址")
                        .setView(InputWebsite)
                        .setCancelable(true)
                        .setPositiveButton("转到", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                final String tmpA[]=new String[2];
                                tmpA[0]="";
                                final String TargetURL= Me.MakeURL(InputWebsite.getText().toString(),tmpA);
                                if (!tmpA[0].equals(""))
                                {
                                    new AlertDialog.Builder(Me)
                                            .setCancelable(true)
                                            .setTitle("非常规协议")
                                            .setMessage("确实要使用\""+tmpA[0]+"\"协议吗?")
                                            .setPositiveButton("是", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i)
                                                {
                                                    Me.MainWebView.loadUrl(TargetURL);
                                                }
                                            })
                                            .setNegativeButton("不,使用HTTP打开", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i)
                                                {
                                                    Me.MainWebView.loadUrl("https://ssl.hrbeu.edu.cn/web/1/http/0/"+tmpA[1]);
                                                }
                                            })
                                            .show();
                                }
                                else
                                    Me.MainWebView.loadUrl(TargetURL);
                            }
                        })
                        .show();
            }
        });
        findViewById(R.id.MenuLayout_Help).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new AlertDialog.Builder(Me)
                        .setTitle("帮助")
                        .setMessage("超级内网助手可以把想添加的网址加入“收藏列表”，以后想要访问直接点击列表就能进入。\n" +
                                "只需要点击列表下方的加号按钮，就可以把网址加入列表。如果想要删除，长按网址就能够删除了。")
                        .setCancelable(true)
                        .setPositiveButton("好",null)
                        .show();
            }
        });
        findViewById(R.id.MenuLayout_Setting).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                View tmpView= Me.getLayoutInflater().inflate(R.layout.setting,null,false);
                tmpView.findViewById(R.id.Setting_Newest).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("http://jackmyth.cn/Application/DownloadHEUSuperHelper.php");
                        intent.setData(content_url);
                        startActivity(intent);
                    }
                });
                tmpView.findViewById(R.id.Setting_AutoLogin_CAS).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        MakeAutoLoginDialogForCAS(false);
                    }
                });
                try
                {
                    String version= getPackageManager().getPackageInfo(getPackageName(),0).versionName;
                    ((TextView)tmpView.findViewById(R.id.Setting_Version)).setText(version);
                } catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                    new AlertDialog.Builder(Me)
                            .setMessage("获取版本号失败")
                            .setCancelable(true)
                            .show();
                }
                new AlertDialog.Builder(Me)
                        .setView(tmpView)
                        .setPositiveButton("取消内网自动登陆", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                getSharedPreferences("LoginSettings", Context.MODE_PRIVATE).edit().remove("AutoLogin").commit();
                                getSharedPreferences("LoginSettings", Context.MODE_PRIVATE).edit().remove("AutoUsePublic").commit();
                                Toast.makeText(Me,"已取消自动登陆",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNeutralButton("给开发者发邮件", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                Uri uri = Uri.parse("mailto:wwwbkkk@126.com");
                                String[] email = {"wwwbkkk@126.com"};
                                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                                //intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
                                intent.putExtra(Intent.EXTRA_SUBJECT, "关于HEU超级内网助手"); // 主题
                                startActivity(Intent.createChooser(intent, "选择应用"));
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });
        DrawerL.openDrawer(Gravity.START);
        ELV.expandGroup(0);
        ELV.expandGroup(1);
        final SharedPreferences SP=getSharedPreferences("AutoLoginForCAS",Context.MODE_PRIVATE);
        if(SP.getBoolean("FirstAsk",true))
        {
            new AlertDialog.Builder(Me)
                    .setTitle("自动登陆")
                    .setMessage("超级内网助手现已支持直接登陆统一身份认证，只需要输入一次账号密码，在统一身份认证时即可自动登陆。\n是否开启？")
                    .setCancelable(false)
                    .setPositiveButton("现在开启!", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            MakeAutoLoginDialogForCAS(true);
                        }
                    })
                    .setNeutralButton("以后再说", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            SP.edit().putBoolean("FirstAsk",false).commit();
                            new AlertDialog.Builder(Me)
                                    .setMessage("好的，你以后可以随时在左边抽屉中的设置里开启自动登陆")
                                    .setCancelable(true)
                                    .show();
                        }
                    })
                    .show();
        }
    }

    String MakeURL(String TargetURL,String Should[])
    {
        TargetURL= TargetURL.trim();
        TargetURL= TargetURL.replace('\\','/');
        if (TargetURL.contains("ssl.hrbeu.edu.cn/web"))
        {
            return  TargetURL;
        }
        else
        {
            String mm[] =TargetURL.split("://");
            if (mm.length>1)
            {
                if (!"httpsftp".contains(mm[0]))
                {
                    if (Should.length > 1)
                    {
                        Should[0] = mm[0];
                        Should[1] = mm[1];
                        return "https://ssl.hrbeu.edu.cn/web/1/" + Should[0] + "/0/" + Should[1];
                    }
                } else
                {
                    return "https://ssl.hrbeu.edu.cn/web/1/" + mm[0] + "/0/" + mm[1];
                }
            }
            return "https://ssl.hrbeu.edu.cn/web/1/http/0/" + TargetURL;
        }
    }

    void MakeAutoLoginDialogForCAS(final boolean RemindSettingPosition)
    {
        final Helper_Main Me=this;
        final View tmpView=getLayoutInflater().inflate(R.layout.autologin_cas,null,false);
        final SharedPreferences SP=getSharedPreferences("AutoLoginForCAS",Context.MODE_PRIVATE);
        ((EditText)tmpView.findViewById(R.id.AutoLogin_CAS_Account)).setText(SP.getString("Account",""));
        ((EditText)tmpView.findViewById(R.id.AutoLogin_CAS_Password)).setText(SP.getString("Password",""));
        new AlertDialog.Builder(this)
                .setTitle("统一身份认证自动登陆")
                .setView(tmpView)
                .setCancelable(true)
                .setPositiveButton("开启自动登陆", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        SharedPreferences.Editor editor=SP.edit();
                        editor.putBoolean("FirstAsk",false);
                        editor.putBoolean("Enabled",true);
                        editor.putString("Account",((EditText)tmpView.findViewById(R.id.AutoLogin_CAS_Account)).getText().toString());
                        editor.putString("Password",((EditText)tmpView.findViewById(R.id.AutoLogin_CAS_Password)).getText().toString());
                        editor.commit();
                        if (RemindSettingPosition)
                        {
                            new AlertDialog.Builder(Me)
                                    .setMessage("你以后可以随时在左边抽屉中的设置里开启自动登陆")
                                    .setCancelable(true)
                                    .show();
                        }
                        if (MainWebView.getUrl().contains("cas.hrbeu.edu.cn/cas/login"))
                            MainWebView.loadUrl(MainWebView.getUrl());
                    }
                })
                .setNeutralButton("关闭自动登陆", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        SharedPreferences.Editor editor=SP.edit();
                        editor.putBoolean("FirstAsk",false);
                        editor.putBoolean("Enabled",false);
                        editor.commit();
                        if (RemindSettingPosition)
                        {
                            new AlertDialog.Builder(Me)
                                    .setMessage("你以后可以随时在左边抽屉中的设置里开启自动登陆")
                                    .setCancelable(true)
                                    .show();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialogInterface)
                    {
                        SharedPreferences.Editor editor=SP.edit();
                        editor.putBoolean("FirstAsk",false);
                        editor.commit();
                        if (RemindSettingPosition)
                        {
                            new AlertDialog.Builder(Me)
                                    .setMessage("你以后可以随时在左边抽屉中的设置里开启自动登陆")
                                    .setCancelable(true)
                                    .show();
                        }
                    }
                })
                .show();
    }

    void RefreshWebsiteList()
    {
        WebSites=getSharedPreferences("Websites",Context.MODE_PRIVATE).getAll();
        ELV.deferNotifyDataSetChanged();
        ELV.collapseGroup(1);
        ELV.expandGroup(1);
    }

    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
        if (DrawerL.isDrawerOpen(Gravity.START))
        {
            DrawerL.closeDrawers();
            return;
        }
        if(MainWebView.canGoBack())
            MainWebView.goBack();
        else
        {
            if (System.currentTimeMillis()-LastBackTime<2000)
            {
                super.onBackPressed();
            }
            else
            {
                LastBackTime=System.currentTimeMillis();
                Toast.makeText(this,"再按一次退出", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy()
    {

        CookieManager.getInstance().removeSessionCookie();
        CookieManager.getInstance().removeAllCookie();
        CookieSyncManager.getInstance().sync();
        super.onDestroy();
    }
}

class WebListELVAdapter implements ExpandableListAdapter
{
    Helper_Main Activ;
    Map<String,String> BuiltinWebSiteList;
    public void SetDataRef(Helper_Main Activ)
    {
        this.Activ=Activ;
        BuiltinWebSiteList=new HashMap<String, String>();
        BuiltinWebSiteList.put("教学一体化服务平台","https://ssl.hrbeu.edu.cn/web/1/http/0/edusys.hrbeu.edu.cn/jsxsd/");
        BuiltinWebSiteList.put("实验室综合管理系统","https://ssl.hrbeu.edu.cn/web/1/http/0/lims.hrbeu.edu.cn/");
        BuiltinWebSiteList.put("哈尔滨工程大学图书馆","https://ssl.hrbeu.edu.cn/web/1/http/0/lib.hrbeu.edu.cn/");
        BuiltinWebSiteList.put("校园卡网络服务平台","https://ssl.hrbeu.edu.cn/web/1/http/0/ecard.hrbeu.edu.cn/");
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver)
    {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver)
    {
    }

    @Override
    public int getGroupCount()
    {
        return 2;
    }

    @Override
    public int getChildrenCount(int i)
    {
        return i==0?BuiltinWebSiteList.size():Activ.WebSites.size();
    }

    @Override
    public Object getGroup(int i)
    {
        return null;
    }

    @Override
    public Object getChild(int i, int i1)
    {
        return null;
    }

    @Override
    public long getGroupId(int i)
    {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1)
    {
        return 0;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup)
    {
        View tmpView=Activ.getLayoutInflater().inflate(R.layout.weblist_header,viewGroup,false);
        ((TextView)tmpView.findViewById(R.id.WebList_HeaderText)).setText(i==0?"内置":"自定义");
        return tmpView;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup)
    {
        final Map<String,?> tmpMap;
        if (i==0)
            tmpMap=BuiltinWebSiteList;
        else
            tmpMap=Activ.WebSites;
        final String tm= (String)tmpMap.keySet().toArray()[i1];
        View tmpView = Activ.getLayoutInflater().inflate(R.layout.weblist_item, viewGroup,false);
        ((TextView) tmpView.findViewById(R.id.WebList_ItemText)).setText(tm);
        tmpView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Activ.MainWebView.loadUrl(tmpMap.get(tm).toString());
                Activ.DrawerL.closeDrawers();
            }
        });
        if (i==0)
        {
            tmpView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    new AlertDialog.Builder(Activ)
                            .setMessage("内置网址不能删除")
                            .setPositiveButton("我知道了",null)
                            .show();
                    return true;
                }
            });
        }
        else
        {
            tmpView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    new AlertDialog.Builder(Activ)
                            .setMessage("要删除\""+tm+"\"吗?")
                            .setPositiveButton("是", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    Activ.getSharedPreferences("Websites",Context.MODE_PRIVATE).edit().remove(tm).commit();
                                    Activ.RefreshWebsiteList();
                                }
                            })
                            .setNegativeButton("否",null)
                            .setCancelable(true)
                            .show();
                    return true;
                }
            });
        }
        return tmpView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1)
    {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public void onGroupExpanded(int i)
    {

    }

    @Override
    public void onGroupCollapsed(int i)
    {

    }

    @Override
    public long getCombinedChildId(long l, long l1)
    {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long l)
    {
        return 0;
    }
}
