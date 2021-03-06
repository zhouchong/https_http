package com.verifone.tony.ssltony;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
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
    Context context;




    //private static String SERVER_IP = "https://192.168.43.122:8443/IPPayments/inter/CardAuthorization.do";
    private static String SERVER_IP = "https://192.168.250.10:4038/IPPayments/inter/CardAuthorization.do";
    private static String SERVER_CER_PATH = "amex_cert.pem";


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
        Logger.t(TAG).d("HttpsUtil IP: " + SERVER_IP);

        Logger.t(TAG).d("HttpUtil: 启用ssl证书");
        try {
            /**
             * 不验证书
             */
            //client = getUnsafeOkHttpClient();

            InputStream keyStream = context.getAssets().open(SERVER_CER_PATH);

            /**
             * 验证书
             */
            client = getTrusClient(keyStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***********************************************************************************************
     * description : Http通讯, POST 同步方法
     * Created by ZhangF3 on 2017/5/5.
     * input  : String url, String json
     * output : String response.body()
     **********************************************************************************************/
    public boolean SyncPost(byte[] dataSend) throws IOException, RemoteException {
        Logger.t(TAG).d(  "SyncPost IP: " + SERVER_IP);
        Request request;

        Logger.t(TAG).d("Send: %s \n", new String(dataSend));

        String url = SERVER_IP;
        try {
            MediaType TEXT = MediaType.parse("plain/text");
            RequestBody body = RequestBody.create(TEXT, dataSend);

            Logger.t(TAG).d("body length = %d \n", body.contentLength());

            //Logger.t(TAG).d( "byte: " + dataSend[0] + dataSend[1]);

            request = new Request.Builder()
                    .url( url )
                    .header( "User-Agent", "Terminal;VERIFONE;V240M" )
                    .header( "Cache-Control", "no-cache" )
                    .header( "Content-Type", "plain/text" )
                    .header( "Connection", "Keep-Alive" )
                    .header( "MerchNbr", "8242718765" )
                    .header( "Host", "www359.americanexpress.com" )
                    .header( "origin", "HDFC LYRA TXN" )
                    .header( "country", "356" )
                    .header( "region", "JAPA" )
                    .header( "message", "EDC JAPA" )
                    .header( "RtInd", "030" )
                    .header( "Accept", "*/*" )
                    .post( body )
                    .build();
        } catch (Exception e) {
            Logger.t(TAG).d("SyncPost: 非法URL");
            e.printStackTrace();
            return false;
        }

        Logger.t(TAG).d( "head: ");
        Logger.t(TAG).d( request.headers());

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
            StringBuilder stringBuilder = new StringBuilder();
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                stringBuilder.append(responseHeaders.name(i) + ": " + responseHeaders.value(i) + "\n");
            }
            Logger.t(TAG).d( "SyncPost() Http response head: \n" + stringBuilder.toString());

            if (response.code() != 200) {
                Logger.t(TAG).d( "SyncPost() response.code != 200: " + response.code());
                return false;
            }

            Logger.t(TAG).d( "message : " + response.message());
            String body = response.body().string(); //只能取一次
            Logger.t(TAG).d( "Response body len = " + body.length());
            Logger.t(TAG).d( "Response body : \n" + body);

            return true;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                stringBuilder.append(responseHeaders.name(i) + ": " + responseHeaders.value(i) + "\n");
            }
            Logger.t(TAG).d( "SyncPost() Http response code: " + response.code());
            Logger.t(TAG).d( "SyncPost() Http response head: \n" + stringBuilder.toString());
            Logger.t(TAG).d( "SyncPost() Http response body: \n" + response.body().string());

            return false;
        }
    }

    public String SyncGet() {
        String url = SERVER_IP;
        Logger.t(TAG).d(  "SyncGet IP: " + SERVER_IP);
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Terminal;VERIFONE;V240M")
                .header("Cache-Control", "no-cache")
                .header("Content-Type", "plain/text")
                .header("Accept", "*/*")
                .get()
                .build();
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
            OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(true).connectTimeout(10000, TimeUnit.MILLISECONDS)
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


    /**-------------------------------------------------------------------------------------------*/
    //Tony https://www.jianshu.com/p/cc7ae2f96b64
    /**
     * 对外提供的获取支持自签名的okhttp客户端
     *
     * @param certificate 自签名证书的输入流
     * @return 支持自签名的客户端
     */
    public OkHttpClient getTrusClient(InputStream certificate) {
        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;
        try {
            trustManager = trustManagerForCertificates(certificate);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            //使用构建出的trustManger初始化SSLContext对象
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            //获得sslSocketFactory对象
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(true).connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(3, TimeUnit.MINUTES).writeTimeout(3, TimeUnit.MINUTES);
        builder.sslSocketFactory(sslSocketFactory, trustManager);

        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        return builder.build();
    }

    /**
     * 获去信任自签证书的trustManager
     *
     * @param in 自签证书输入流
     * @return 信任自签证书的trustManager
     * @throws GeneralSecurityException
     */
    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        TrustManager[] trustManagers = new TrustManager[0];
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            //通过证书工厂得到自签证书对象集合
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
            if (certificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }
            //为证书设置一个keyStore
            char[] password = "password".toCharArray(); // Any password will work.
            KeyStore keyStore = newEmptyKeyStore(password);
            int index = 0;
            //将证书放入keystore中
            for (Certificate certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }
            // Use it to build an X509 trust manager.
            //使用包含自签证书信息的keyStore去构建一个X509TrustManager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return (X509TrustManager) trustManagers[0];
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
