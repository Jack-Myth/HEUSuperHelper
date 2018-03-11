package d.o.JackMyth.HEUSuperHelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.SslError;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity
{
    ConstraintLayout LogoCL=null;
    ConstraintLayout LoginCL=null;
    boolean ShouldAutoLogin=false;
    boolean Login_succeed=false;
    WebView tmpWebView=null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        LogoCL=findViewById(R.id.LogoLayout);
        LoginCL=findViewById(R.id.LoginCL);
        TranslateAnimation TA=new TranslateAnimation(0,0,500,0);
        AlphaAnimation AA=new AlphaAnimation(0f,1f);
        TA.setStartOffset(500);
        TA.setDuration(1000);
        AA.setStartOffset(1500);
        AA.setDuration(500);
        LogoCL.setAnimation(TA);
        LoginCL.setAnimation(AA);
        tmpWebView=new WebView(this);
        //tmpWebView=findViewById(R.id.debugWebView);
        tmpWebView.getSettings().setJavaScriptEnabled(true);
        final Login L=this;
        tmpWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                if (errorCode>=400)
                {
                    Toast.makeText(L,"连接时出现错误，请检查网络",Toast.LENGTH_SHORT).show();
                    findViewById(R.id.Login_Login).setVisibility(View.VISIBLE);
                    findViewById(R.id.Login_Logining).setVisibility(View.INVISIBLE);
                    findViewById(R.id.Login_WhatIs).setVisibility(View.VISIBLE);
                    findViewById(R.id.Login_UsePubilcAccount).setVisibility(View.VISIBLE);
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
            {
                //强制访问证书出错的网站
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webview, String url)
            {
                webview.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                if (url.contains("portal.hrbeu.edu.cn")&&!Login_succeed)
                {
                    Login_succeed=true;
                    SharedPreferences.Editor SPEditor=getSharedPreferences("LoginSettings", Context.MODE_PRIVATE).edit();
                    String UserName=((EditText)findViewById(R.id.Login_UserName)).getText().toString();
                    String Password=((EditText)findViewById(R.id.Login_Password)).getText().toString();
                    SPEditor.putString("UserName",UserName);
                    SPEditor.putString("Password",Password);
                    SPEditor.putBoolean("RememberPassword",((CheckBox)findViewById(R.id.Login_Remember)).isChecked());
                    SPEditor.putBoolean("AutoLogin",((CheckBox)findViewById(R.id.Login_AutoLogin)).isChecked());
                    SPEditor.commit();
                    CookieManager CM=CookieManager.getInstance();
                    String CookieStr=CM.getCookie(url);
                    if (!getSharedPreferences("AutoLoginForCAS",Context.MODE_PRIVATE).getBoolean("Enabled",false))
                        Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_LONG).show();
                    Intent I=new Intent(L,Helper_Main.class);
                    I.putExtra("Cookies",CookieStr);
                    startActivity(I);
                    L.finish();
                }
            }
        });
        tmpWebView.setWebChromeClient(new WebChromeClient()
        {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result)
            {
                if (message.contains("错误")||message.contains("不"))
                {
                    //登录失败
                    Toast.makeText(L,message,Toast.LENGTH_SHORT).show();
                    findViewById(R.id.Login_Login).setVisibility(View.VISIBLE);
                    findViewById(R.id.Login_Logining).setVisibility(View.INVISIBLE);
                    findViewById(R.id.Login_WhatIs).setVisibility(View.VISIBLE);
                    findViewById(R.id.Login_UsePubilcAccount).setVisibility(View.VISIBLE);
                }
                result.confirm();
                return true;
                //return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result)
            {
                Toast.makeText(L,message,Toast.LENGTH_SHORT).show();
                if (message.contains("错误")||message.contains("不"))
                {
                    //登录失败
                    findViewById(R.id.Login_Login).setVisibility(View.VISIBLE);
                    findViewById(R.id.Login_Logining).setVisibility(View.INVISIBLE);
                    findViewById(R.id.Login_WhatIs).setVisibility(View.VISIBLE);
                    findViewById(R.id.Login_UsePubilcAccount).setVisibility(View.VISIBLE);
                }
                result.confirm();
                return true;
                //return super.onJsConfirm(view, url, message, result);
            }
        });
        SharedPreferences SP=getSharedPreferences("LoginSettings", Context.MODE_PRIVATE);
        ((EditText)findViewById(R.id.Login_UserName)).setText(SP.getString("UserName",""));
        if(SP.getBoolean("RememberPassword",false))
        {
            ((CheckBox)findViewById(R.id.Login_Remember)).setChecked(true);
            ((EditText)findViewById(R.id.Login_Password)).setText(SP.getString("Password", ""));
            ShouldAutoLogin= SP.getBoolean("AutoLogin",false);
            ((CheckBox)findViewById(R.id.Login_AutoLogin)).setChecked(ShouldAutoLogin);
        }
        TA.start();
        AA.start();
        TA.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                if (getSharedPreferences("LoginSettings",Context.MODE_PRIVATE).getBoolean("FirstOpen",true))
                {
                    new AlertDialog.Builder(L)
                            .setTitle("感谢使用HEU超级内网助手")
                            .setMessage("希望这款应用能给你带来方便\n如果有任何Bug或者建议，欢迎给我发送邮件\n发送邮件的按钮在登陆界面和网页界面都能找到\n同时也希望你能够在有空的时候检查更新，说不定会有新功能哦。")
                            .setCancelable(false)
                            .setPositiveButton("我知道了", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    getSharedPreferences("LoginSettings",Context.MODE_PRIVATE).edit().putBoolean("FirstOpen",false).commit();
                                }
                            })
                            .setNeutralButton("现在就骚扰一下~", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    getSharedPreferences("LoginSettings",Context.MODE_PRIVATE).edit().putBoolean("FirstOpen",false).commit();
                                    Uri uri = Uri.parse("mailto:wwwbkkk@126.com");
                                    String[] email = {"wwwbkkk@126.com"};
                                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                                    //intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "关于HEU超级内网助手"); // 主题
                                    startActivity(Intent.createChooser(intent, "选择应用"));
                                }
                            })
                            .show();
                }
                if (ShouldAutoLogin)
                {
                    BeginLogin();
                }
                else if(getSharedPreferences("LoginSettings",Context.MODE_PRIVATE).getBoolean("AutoUsePublic",false))
                {
                    LoginWithPublicAccount();
                }
                else
                {
                    findViewById(R.id.Login_WhatIs).setVisibility(View.VISIBLE);
                    findViewById(R.id.Login_UsePubilcAccount).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        ((Button)findViewById(R.id.Login_Login)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                BeginLogin();
            }
        });
        findViewById(R.id.Login_WhatIs).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new AlertDialog.Builder(L)
                        .setTitle("这是什么？")
                        .setMessage("此处需要登陆你\"校园信息门户\"的账号和密码，用以接入学校的服务." +
                                "如果你记不清你的账号和密码，请参照以下说明和你经常使用的密码来试试:\n" +
                                "1.登录使用的帐号是用户的工号（教师）或学号（学生）。\n" +
                                "2.初始密码为本人“15或18位”身份证号码的后8位（“X”为大写字母）。\n" +
                                "如果实在无法登陆，请点击下方按钮，在打开的页面中选择\"忘记密码\"来尝试重置.\n"+
                                "另外:如果遇到校园信息门户可以正常登陆，但是在软件内却无法登陆的情况，请给我发邮件来反馈.")
                        .setPositiveButton("我实在记不起来了", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                Intent intent = new Intent();
                                intent.setAction("android.intent.action.VIEW");
                                Uri content_url = Uri.parse("http://portal.hrbeu.edu.cn/indexNone/index_none.jsp");
                                intent.setData(content_url);
                                startActivity(intent);
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
                        .show();
            }
        });
        findViewById(R.id.Login_UsePubilcAccount).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new AlertDialog.Builder(L)
                        .setTitle("使用公共账户登陆")
                        .setMessage("如果你的账号因为各种问题而无法使用，或者根本没有帐户的情况下，此功能可以让你使用公共账户登陆。\n" +
                                "不过，如果你自己的帐户可以使用的话，还是建议使用你本人的账户。")
                        .setPositiveButton("仅登陆一次", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                LoginWithPublicAccount();
                            }
                        })
                        .setNeutralButton("自动登陆公共账户", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                getSharedPreferences("LoginSettings",Context.MODE_PRIVATE).edit().putBoolean("AutoUsePublic",true).commit();
                                LoginWithPublicAccount();
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });
    }

    void BeginLogin()
    {
        findViewById(R.id.Login_Login).setVisibility(View.INVISIBLE);
        findViewById(R.id.Login_Logining).setVisibility(View.VISIBLE);
        String UserName=((EditText)findViewById(R.id.Login_UserName)).getText().toString();
        String Password=((EditText)findViewById(R.id.Login_Password)).getText().toString();
        if(UserName.length()==0||Password.length()==0)
        {
            Toast.makeText(this,"用户名和密码均不能为空",Toast.LENGTH_LONG).show();
            findViewById(R.id.Login_Login).setVisibility(View.VISIBLE);
            findViewById(R.id.Login_Logining).setVisibility(View.INVISIBLE);
            return;
        }
        tmpWebView.loadUrl("https://ssl.hrbeu.edu.cn/por/login_psw.csp?svpn_name="+UserName+"&svpn_password="+Password);
    }

    void LoginWithPublicAccount()
    {
        findViewById(R.id.Login_Login).setVisibility(View.INVISIBLE);
        findViewById(R.id.Login_Logining).setVisibility(View.VISIBLE);
        tmpWebView.loadUrl("http://jackmyth.cn/HEUSuperHelper/LoginWithPublicAccount.php");
    }
}
