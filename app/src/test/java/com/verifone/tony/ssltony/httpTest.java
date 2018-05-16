package com.verifone.tony.ssltony;

import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by chong.z on 2018/5/16.
 * 测试http post请求，在server未回复的情况下，代码阻塞在哪里
 */

public class httpTest {

    @Test
    public void httpPost() throws Exception {
        String SERVER_IP = "http://127.0.0.1:30000";
        System.out.println(  "SyncPost IP: " + SERVER_IP);
        System.out.println();
        Request request;

        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(3, TimeUnit.MINUTES).writeTimeout(3, TimeUnit.MINUTES);
        OkHttpClient client = builder.build();

        //OkHttpClient client = new OkHttpClient();
        System.out.println(  "connet timeout(ms): " + client.connectTimeoutMillis());
        System.out.println(  "write timeout(ms): " + client.writeTimeoutMillis());
        System.out.println(  "read timeout(ms): " + client.readTimeoutMillis());

        String url = SERVER_IP;
        try {
            MediaType TEXT = MediaType.parse("x-ISO-TPDU/x-auth");
            RequestBody body = RequestBody.create(TEXT, "123");
            //RequestBody body = RequestBody.create(TEXT, "123");

            //System.out.println( "byte: " + dataSend[0] + dataSend[1]);

            request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Donjin Http 0.1")
                    .header("Cache-Control", "no-cache")
                    .header("Content-Type", "x-ISO-TPDU/x-auth")
                    .header("Accept", "*/*")
                    .post(body)
                    .build();
        } catch (Exception e) {
            System.out.println("SyncPost: 非法URL");
            e.printStackTrace();
            return;
        }

        System.out.println( "head: " + request.headers());

        Response response = null;
        try {
            System.out.println( "------------- before call : ");
            //FIXME 会阻塞在这里
            response = client.newCall(request).execute();
            System.out.println( "------------- after call : ");
        } catch (ConnectException e) {
            System.out.println( "------------- SyncPost() ConnectException : " + e);
            e.printStackTrace();
            return;
        } catch (SocketTimeoutException e) {
            System.out.println("------------- SyncPost() SocketTimeoutException : " + e);
            e.printStackTrace();
            return;
        } catch (IOException e) {
            System.out.println( "------------- SyncPost() IOException : " + e);
            e.printStackTrace();
            return;
        } catch (NullPointerException e) {
            System.out.println( "------------- SyncPost() NullPointerException : " + e);
            e.printStackTrace();
            return;
        }
        if (response == null) {
            System.out.println( "------------- SyncPost() response is null");
            return;
        }
        if (response.isSuccessful()) {
            System.out.println("------------- SyncPost() Http response code: " + response.code());
            StringBuilder stringBuilder = new StringBuilder();
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                stringBuilder.append(responseHeaders.name(i) + ": " + responseHeaders.value(i) + "\n");
            }
            System.out.println( "------------- SyncPost() Http response head: \n" + stringBuilder.toString());

            if (response.code() != 200) {
                System.out.println( "------------- SyncPost() response.code != 200: " + response.code());
                return;
            }

            System.out.println( "message : " + response.message());
            String body = response.body().string(); //只能取一次
            System.out.println( "body len = " + body.length());
            //System.out.println( "body : " + response.body().string());

            System.out.println( "body : \n" + new BigInteger(1, body.getBytes()).toString(16));

            return;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                stringBuilder.append(responseHeaders.name(i) + ": " + responseHeaders.value(i) + "\n");
            }
            System.out.println( "------------- SyncPost() Http response code: " + response.code());
            System.out.println( "------------- SyncPost() Http response head: \n" + stringBuilder.toString());

            return;
        }
    }
}
