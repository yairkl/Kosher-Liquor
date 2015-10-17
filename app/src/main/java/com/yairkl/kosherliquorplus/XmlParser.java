package com.yairkl.kosherliquorplus;

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

    public Document DOMParser(String url) {
        Document doc;
        try {
            URL myUrl = new URL(url);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(myUrl.openStream()));
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            err(e.getMessage());
            return null;
        }
        return doc;
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
