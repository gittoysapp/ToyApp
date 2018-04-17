
package com.abhi.toyswap.Connection;

import com.abhi.toyswap.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;



/**
 * @author Abhishek Gupta
 */

public class Connection {

    int statusCode;

    public Connection() {


    }

    public String getResponseFromWebservice(int requestType, JSONObject requestParameter) {
        URL url;
        StringBuilder sb = new StringBuilder();
        Utils.log("Request="+requestParameter.toString());

        HttpURLConnection urlConnection = null;
        try {
            url = new URL(Utils.createWebServiceURL(requestType));

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(requestParameter.toString());
            out.close();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                InputStream in = urlConnection.getInputStream();

                BufferedReader br = null;

                String line;
                try {

                    br = new BufferedReader(new InputStreamReader(in));
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Utils.log("For Request:" + requestType + " Response From Webservice=" + sb.toString());
        return sb.toString();
    }

}
