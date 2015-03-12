/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.pageInterpreters;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 *
 * @author Luis
 */
public class MyHttpClientFactory {

    public MyHttpClientFactory() {
    }

    public static HttpClient getDefaultHttpClient() {
        HttpParams my_httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(my_httpParams, 15000);
        HttpConnectionParams.setSoTimeout(my_httpParams, 15000);
        return new DefaultHttpClient(my_httpParams);
    }
}
