package com.verifone.tony.ssltony;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by chong.z on 2018/5/9.
 */

public class HttpsUtil {
    private static final String TAG = "--------------HttpsUtil";
    private static HttpsUtil instance;
    private OkHttpClient client;


    private static String SERVER_IP = "https://10.172.64.23:8443";
    private static String SERVER_CER_PATH = "tomcat.cer";
    //private static String SERVER_CER_PATH = "client.cer";

    SharedPreferences param;
    SharedPreferences.Editor editor;

    public static HttpsUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (HttpsUtil.class) {
                if (instance == null) {
                    instance = new HttpsUtil(context);
                }
            }
        }
        return instance;
    }

    public static void destoryInstance() {
        if (instance != null) {
            instance = null;
        }
    }

    public HttpsUtil(Context context) {
        Logger.t(TAG).d( "HttpsUtil");
        if (false) {
            Logger.t(TAG).d("HttpUtil: 不需要ssl证书");
            client = new OkHttpClient();
        } else {
            Logger.t(TAG).d( "HttpUtil: 启用ssl证书");
            try {
                //client = getUnsafeOkHttpClient();

                InputStream keyStream = context.getAssets().open(SERVER_CER_PATH);
                client = setCertificates(keyStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***********************************************************************************************
     * description : Http通讯, POST 同步方法
     * Created by ZhangF3 on 2017/5/5.
     * input  : String url, String json
     * output : String response.body()
     **********************************************************************************************/
    public boolean SyncPost(String dataSend) throws IOException, RemoteException {
        Logger.t(TAG).d(  "SyncPost: executed");
        Request request;

        String url = SERVER_IP;
        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, dataSend);

            request = new Request.Builder().
                    url(url).
                    addHeader("Accept-Encoding", "deflate").
                    post(body).
                    build();
        } catch (Exception e) {
            Logger.t(TAG).d("SyncPost: 非法URL");
            e.printStackTrace();
            return false;
        }

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (ConnectException e) {
            Logger.t(TAG).d( "SyncPost() ConnectException : " + e);
            e.printStackTrace();
            return false;
        } catch (SocketTimeoutException e) {
            Logger.t(TAG).d("SyncPost() SocketTimeoutException : " + e);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Logger.t(TAG).d( "SyncPost() IOException : " + e);
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            Logger.t(TAG).d( "SyncPost() NullPointerException : " + e);
            e.printStackTrace();
            return false;
        }
        if (response == null) {
            Logger.t(TAG).d( "SyncPost() response is null");
            return false;
        }
        if (response.isSuccessful()) {
            Logger.t(TAG).d("SyncPost() Http response code: " + response.code());

            if (response.code() != 200) {
                Logger.t(TAG).d( "SyncPost() response.code != 200: " + response.code());
                return false;
            }
            return true;
        } else {
            Logger.t(TAG).d( "SyncPost() Http response code: " + response.code());
            return false;
        }
    }

    public String SyncGet() {
        String url = SERVER_IP;
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                Logger.t(TAG).d( "SyncGet() Http response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }};
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            // builder.sslSocketFactory(sslSocketFactory);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static OkHttpClient setCertificates(InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(3, TimeUnit.MINUTES).writeTimeout(3, TimeUnit.MINUTES);
            builder.sslSocketFactory(sslContext.getSocketFactory());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
