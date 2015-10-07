package com.conqueror.d.laucheronly;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conqueror.d.laucheronly.adapter.ViewPageAdapter;
import com.conqueror.d.laucheronly.util.DownloadPostTask;
import com.conqueror.d.laucheronly.util.DownloadTask;
import com.conqueror.d.laucheronly.util.NetUtil;
import com.conqueror.d.laucheronly.util.ScreenBrightnessTool;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.app.Service;

public class MainActivity extends Activity implements ViewPager.OnPageChangeListener,View.OnClickListener {


    /**
     * ViewPager
     */
    private ViewPager viewPager;
    private List<View> viewLists;
    private  ViewPageAdapter mAdapter;

    /**
     * 装点点的ImageView数组
     */
    private ImageView[] tips;

    //天气信息
    String msg,dayPictureUrl,nightPictureUrl;
    Bitmap bmImg;

    //跳转应用
    private  ComponentName componet;
    private Intent intent_go = new Intent();

    //主题
    private Boolean is_night_theme=false;
    private int[] night_bg={
            R.drawable.panel_night_selector,R.drawable.panel_night_selector, R.drawable.panel_night_selector,
            R.drawable.panel_night_selector,R.drawable.panel_night_selector,R.drawable.panel_night_selector,
            R.drawable.panel_night_selector,R.drawable.panel_night_selector,R.drawable.panel_night_selector,R.drawable.panel_night_selector,
            R.drawable.panel_night_selector,R.drawable.panel_night_selector,R.drawable.panel_night_selector,R.drawable.panel_night_selector
    };
    private int[] day_bg={R.drawable.panel_weather_selector,R.drawable.panel_navi_selector,R.drawable.panel_settings_selector,
            R.drawable.panel_dog_selector,R.drawable.panel_recoder_selector,R.drawable.panel_phone_selector,
            R.drawable.panel_music_selector,R.drawable.panel_wifi_selector,R.drawable.panel_sound_reduce_selector,R.drawable.panel_sound_add_selector,
            R.drawable.panel_video_selector,R.drawable.panel_gallery_selector,R.drawable.panel_fm_selector,R.drawable.panel_user_selector

    };

    //面板
    private RelativeLayout panel_weather,panel_navi,panel_settings,panel_dog,panel_recoder,panel_phone;
    private RelativeLayout panel_music,panel_wifi,panel_sound_reduce,panel_sound_add,panel_video,panel_fm,panel_light_reduce,panel_light_add;
    private List<RelativeLayout> panel_list;

    private LaucherApplication app;


    private AudioManager audioManager=null; //音频

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_main);

        app= (LaucherApplication)getApplication();

        initView();

        if(NetUtil.isNetworkConnected(this))
              getWeather();

        findViewById(R.id.btn_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(is_night_theme)
                changeTheme(day_bg);
            }
        });
        findViewById(R.id.btn_night).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!is_night_theme)
                changeTheme(night_bg);
            }
        });
    }


    private void initView() {






        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//         List<ResolveInfo> mApps;
//        mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
//        for (int i=0;i<mApps.size();i++)
//        {
//            ResolveInfo info=mApps.get(i);
//            //该应用的包名
//            String pkg = info.activityInfo.packageName;
//            //应用的主activity类
//            String cls = info.activityInfo.name;
//            System.out.println("componet = new ComponentName('"+pkg+","+cls+"');");
//        }

        audioManager=(AudioManager)getSystemService(Service.AUDIO_SERVICE);


        viewLists= new ArrayList<View>();
        viewLists.add(getLayoutInflater().inflate(R.layout.layout1, null));
        viewLists.add(getLayoutInflater().inflate(R.layout.layout2, null));

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ViewGroup group = (ViewGroup)findViewById(R.id.viewGroup);
        //将点点加入到ViewGroup中
        tips = new ImageView[viewLists.size()];
        for(int i=0; i<tips.length; i++){
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(10,10));
            tips[i] = imageView;
            if(i == 0){
                tips[i].setBackgroundResource(R.drawable.page_indicator_focused);
            }else{
                tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 5;
            layoutParams.rightMargin = 5;
            group.addView(imageView, layoutParams);
        }

        mAdapter =  new ViewPageAdapter(viewLists);

        //绑定自定义适配器
        viewPager.setAdapter(mAdapter);
        //设置监听，主要是设置点点的背景
        viewPager.setOnPageChangeListener(this);


        //初始化面板
        panel_weather=((RelativeLayout)(viewLists.get(0).findViewById(R.id.panel_weather)));
        panel_navi=((RelativeLayout)(viewLists.get(0).findViewById(R.id.panel_navi)));
        panel_settings=((RelativeLayout)(viewLists.get(0).findViewById(R.id.panel_settings)));
        panel_dog=((RelativeLayout)(viewLists.get(0).findViewById(R.id.panel_dog)));
        panel_recoder=((RelativeLayout)(viewLists.get(0).findViewById(R.id.panel_recoder)));
        panel_phone=((RelativeLayout)(viewLists.get(0).findViewById(R.id.panel_phone)));

        panel_music=((RelativeLayout)(viewLists.get(1).findViewById(R.id.panel_music)));
        panel_wifi=((RelativeLayout)(viewLists.get(1).findViewById(R.id.panel_wifi)));
        panel_sound_reduce=((RelativeLayout)(viewLists.get(1).findViewById(R.id.panel_sound_reduce)));
        panel_sound_add=((RelativeLayout)(viewLists.get(1).findViewById(R.id.panel_sound_add)));
        panel_video=((RelativeLayout)(viewLists.get(1).findViewById(R.id.panel_video)));

        panel_fm=((RelativeLayout)(viewLists.get(1).findViewById(R.id.panel_fm)));
        panel_light_reduce=((RelativeLayout)(viewLists.get(1).findViewById(R.id.panel_light_reduce)));
        panel_light_add=((RelativeLayout)(viewLists.get(1).findViewById(R.id.panel_light_add)));


        panel_weather.setOnClickListener(this);
        panel_navi.setOnClickListener(this);
        panel_settings.setOnClickListener(this);
        panel_dog.setOnClickListener(this);
        panel_recoder.setOnClickListener(this);
        panel_phone.setOnClickListener(this);


        panel_music.setOnClickListener(this);
        panel_wifi.setOnClickListener(this);
        panel_sound_reduce.setOnClickListener(this);
        panel_sound_add.setOnClickListener(this);
        panel_video.setOnClickListener(this);

        panel_fm.setOnClickListener(this);
        panel_light_reduce.setOnClickListener(this);
        panel_light_add.setOnClickListener(this);



        panel_list=new ArrayList<RelativeLayout>();
        panel_list.add(panel_weather);
        panel_list.add(panel_navi);
        panel_list.add(panel_settings);
        panel_list.add(panel_dog);
        panel_list.add(panel_recoder);
        panel_list.add(panel_phone);


        panel_list.add(panel_music);
        panel_list.add(panel_wifi);
        panel_list.add(panel_sound_reduce);
        panel_list.add(panel_sound_add);
        panel_list.add(panel_video);

        panel_list.add(panel_fm);
        panel_list.add(panel_light_reduce);
        panel_list.add(panel_light_add);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }




    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int arg0) {
        setImageBackground(arg0 % viewLists.size());
    }
    /**
     * 设置选中的tip的背景
     * @param selectItems
     */
    private void setImageBackground(int selectItems){
        for(int i=0; i<tips.length; i++){
            if(i == selectItems){
                tips[i].setBackgroundResource(R.drawable.page_indicator_focused);
            }else{
                tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
        }
    }


    private void changeTheme(int[] res) {
        is_night_theme=!is_night_theme;
        for (int i=0;i<panel_list.size();i++)
        {
            panel_list.get(i).setBackgroundResource(res[i]);
        }
    }



    //获取天气信息
    private void getWeather() {
        //获取token
        new AsyncTask<Integer, Integer, String>(){
            @Override
            protected String doInBackground(Integer... params) {
                try {
                    String json =  NetUtil.readParse("http://api.map.baidu.com/telematics/v3/weather?location=%E5%8E%A6%E9%97%A8&output=json&ak=sOOPnUS7usPU4rlRhuQcKEjB&mcode=E7:78:D3:A7:32:6F:38:AE:B5:81:9E:9E:52:34:B4:BE:C2:B1:5B:92;com.conqueror.d.conqueror_dog");
                    JSONObject jsonParser = new JSONObject(json);

                    String status=jsonParser.getString("status");
                    if(jsonParser.getString("status").equals("success"))
                    {
                        JSONArray results=jsonParser.getJSONArray("results");
                        JSONObject weather_data=results.getJSONObject(0);
                        String pm=weather_data.getString("pm25");
                        String currentCity=weather_data.getString("currentCity");
                        JSONArray weather=weather_data.getJSONArray("weather_data");
                        JSONObject weather_today=weather.getJSONObject(0);
                        String temperature=weather_today.getString("temperature");
                        String weather_detail=weather_today.getString("weather");
                        String wind=weather_today.getString("wind");
                        dayPictureUrl=weather_today.getString("dayPictureUrl");
                        nightPictureUrl=weather_today.getString("nightPictureUrl");

                        msg=currentCity+"\n"+temperature+"\n"+weather_detail+"\n"+wind+"\nPM指数："+pm;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return null;
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                ((TextView)(viewLists.get(0).findViewById(R.id.weather_msg))).setText(msg);
                showWeatherImage();


            }
        }.execute(0);//2015年9月9日 09:35:31

    }

    //加载天气图片
    private void showWeatherImage() {

        new AsyncTask<Integer, Integer, String>(){
            @Override
            protected String doInBackground(Integer... params) {
                try {
                    URL myFileUrl = new URL(dayPictureUrl);
                    HttpURLConnection conn = (HttpURLConnection) myFileUrl
                            .openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bmImg = BitmapFactory.decodeStream(is);
                    is.close();


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);


                ((ImageView)(viewLists.get(0).findViewById(R.id.weather_img))).setImageBitmap(bmImg);


            }
        }.execute(0);//2015年9月9日 09:35:31

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
//            case R.id.panel_user:
//                DownloadTask task = new DownloadTask(this);
//                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "http://wechat.conqueror.cn/ToolsServlet?action=getWechatPic");
//                Toast.makeText(MainActivity.this,task.getResult(),Toast.LENGTH_SHORT).show();
//                DownloadPostTask taskPost=new DownloadPostTask(this);
//                taskPost.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=");
//                task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="+app.ticket);
//                break;
            case R.id.panel_weather:
                Intent broadcastIntent = new Intent("com.wanma.action.PLAY_TTS");
                broadcastIntent.putExtra("content", ((TextView) (viewLists.get(0).findViewById(R.id.weather_msg))).getText());
                sendBroadcast(broadcastIntent);
                break;
            case R.id.panel_navi:
                componet = new ComponentName("com.autonavi.minimap", "com.autonavi.map.activity.SplashActivity");
                intent_go.setComponent(componet);
                startActivity(intent_go);
                break;
            case R.id.panel_settings:
                componet=new ComponentName("com.android.settings","com.android.settings.Settings");
                intent_go.setComponent(componet);
                startActivity(intent_go);

                break;
            case R.id.panel_dog:
                componet=new ComponentName("tk.huayu.edog","tk.huayu.edogclient.MainActivity");
                intent_go.setComponent(componet);
                startActivity(intent_go);
                break;
            case R.id.panel_recoder:
                intent_go.setClassName("com.android.camera2", "com.android.camera.CameraLauncher");
                startActivity(intent_go);
                break;
            case R.id.panel_phone:
                componet = new ComponentName("com.colink.bluetoolthe","com.colink.bluetoothe.MainActivity");
                intent_go.setComponent(componet);
                startActivity(intent_go);
                break;
            case R.id.panel_music:
                componet = new ComponentName("cn.kuwo.kwmusiccar","cn.kuwo.kwmusiccar.WelcomeActivity");
                intent_go.setComponent(componet);
                startActivity(intent_go);
                break;
            case R.id.panel_wifi:
                intent_go=new Intent("android.settings.WIFI_SETTINGS");
                startActivity(intent_go);
                break;
            case R.id.panel_sound_reduce:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI);//调低声音
                break;

            case R.id.panel_sound_add:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI);    //调高声音
                break;

            case R.id.panel_video:
                intent_go.setClassName("com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity");
                startActivity(intent_go);
                break;
            case R.id.panel_fm:
                intent_go.setClassName("com.android.inet", "com.android.inet.RadioEmission");
                startActivity(intent_go);
                break;
            case R.id.panel_light_reduce:
                ScreenBrightnessTool.Builder(MainActivity.this);
                ScreenBrightnessTool.brightnessPreviewFromPercent(MainActivity.this,0.0f);
                break;
            case R.id.panel_light_add:
                ScreenBrightnessTool.Builder(MainActivity.this);
                ScreenBrightnessTool.brightnessPreviewFromPercent(MainActivity.this, 1.0f);
                break;
        }
    }




}
