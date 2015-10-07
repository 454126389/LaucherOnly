package com.conqueror.d.laucheronly.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.conqueror.d.laucheronly.LaucherApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class DownloadPostTask extends AsyncTask<String, Integer, String> {

    ProgressDialog m_pDialog;
    LaucherApplication app;

    public String getResult() {
        return result;
    }



    private String result;
    public DownloadPostTask(Context context){

        app= (LaucherApplication)context.getApplicationContext();

        m_pDialog = new ProgressDialog(context);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // 设置ProgressDialog 标题
        m_pDialog.setTitle("提示");

        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("这是一个圆形进度条对话框");

        // 设置ProgressDialog 标题图标
//        m_pDialog.setIcon(R.drawable.img1);

        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);

        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(true);

        // 设置ProgressDialog 的一个Button
        m_pDialog.setButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //点击“确定按钮”取消对话框
                dialog.cancel();
            }
        });


    }



    protected String doInBackground(String... urls) {
        try{
            //创建连接
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            //设置参数，仿html表单提交


            JSONObject postData = new JSONObject();
            JSONObject scene_id = new JSONObject();
            JSONArray action_info=new JSONArray();
            JSONObject scene=new JSONObject();
            postData.put("expire_seconds", 3600);
            postData.put("action_name", "QR_SCENE");
            scene_id.put("scene_id", "123");
            scene.put("scene",scene_id);
            postData.put("action_info",scene);

            post.setEntity(new StringEntity(postData.toString(), HTTP.UTF_8));
            //发送HttpPost请求，并返回HttpResponse对象
            HttpResponse httpResponse = httpClient.execute(post);
       /*     // 判断请求响应状态码，状态码为200表示服务端成功响应了客户端的请求
            if(httpResponse.getStatusLine().getStatusCode() == 200){
            //获取返回结果
                String result = EntityUtils.toString(httpResponse.getEntity());
            }*/

            HttpEntity entity = httpResponse.getEntity();
            long length = entity.getContentLength();
            InputStream is = entity.getContent();
            String s = null;
            if(is != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buf = new byte[128];

                int ch;

                int count = 0;

                while((ch = is.read(buf)) != -1) {

                    baos.write(buf, 0, ch);

                    count += ch;

                    if(length > 0) {
                        // 如果知道响应的长度，调用publishProgress（）更新进度
                        publishProgress((int) ((count / (float) length) * 100));
                    }

                    // 让线程休眠100ms
                    Thread.sleep(100);
                }
                s = new String(baos.toByteArray());
            }
            // 返回结果
            return s;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        m_pDialog.show();
    }

    protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        m_pDialog.setProgress(progress[0]);
    }

    protected void onPostExecute(String result) {
//            showDialog("Downloaded " + result + " bytes");
        Log.d("result_post",result);
/*        JSONObject jsonParser = null;
        try {
            jsonParser = new JSONObject(result);
            app.token=jsonParser.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        this.result=result;
        m_pDialog.dismiss();


    }
}