package com.yairkl.kosherliquorplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class XmlParser {

    private static final int MAX_REDIRECTS = 5;
    private static final int TIMEOUT_MS = 15000;

    public Document DOMParser(String url) {
        HttpURLConnection connection = null;
        try {
            connection = openWithRedirects(url);
            if (connection == null)
                return null;

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                Log.e("errorParser", "HTTP " + code + " for " + url + " :: " + readError(connection));
                return null;
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream input = connection.getInputStream();
            Document doc = db.parse(new InputSource(input));
            input.close();
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            err(e.getMessage() + " (url: " + url + ")");
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    // HttpURLConnection does not follow http<->https redirects on its own,
    // which silently breaks fetching once a host starts forcing HTTPS. Follow
    // redirects manually (including across protocols) up to MAX_REDIRECTS hops.
    private HttpURLConnection openWithRedirects(String url) throws IOException {
        String current = url;
        for (int i = 0; i < MAX_REDIRECTS; i++) {
            HttpURLConnection connection = (HttpURLConnection) new URL(current).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "KosherLiquor-Android");

            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_PERM
                    || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_SEE_OTHER
                    || code == 307 || code == 308) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location == null) {
                    Log.e("errorParser", "redirect with no Location from " + current);
                    return null;
                }
                // Resolve relative redirects against the current URL.
                current = new URL(new URL(current), location).toString();
                Log.v("errorParser", "following redirect to " + current);
                continue;
            }
            return connection;
        }
        Log.e("errorParser", "too many redirects starting at " + url);
        return null;
    }

    private String readError(HttpURLConnection connection) {
        InputStream stream = connection.getErrorStream();
        if (stream == null)
            return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null && sb.length() < 300)
                sb.append(line);
        } catch (IOException ignored) {
        }
        return sb.toString();
    }

    public String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }

    public final String getElementValue(Node elem) {
        if (elem == null || !elem.hasChildNodes())
            return "";
        for (Node child = elem.getFirstChild(); child != null; child = child.getNextSibling())
            if (child.getNodeType() == Node.TEXT_NODE)
                return child.getNodeValue();
        return "";
    }

    private void err(String str) {
        //Toast.makeText(context, str, Toast.LENGTH_LONG).show();
        Log.v("errorParser", str);
    }
}
