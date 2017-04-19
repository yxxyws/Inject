package com.pingan.inject;

import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangyun980 on 17/4/11.
 */

public class ConfigManager {

    public List<HttpRecordConfig> recordConfigList = new ArrayList<>();

    public void readConfig(InputStream stream) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, "UTF-8");

            int eventType = parser.getEventType();
            HttpRecordConfig config = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        recordConfigList.clear();
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("RecordRequest")) {
                            config = new HttpRecordConfig();
                        } else if (parser.getName().equals("url")) {
                            eventType = parser.next();
                            config.url = parser.getText();
                        } else if (parser.getName().equals("keyword")) {
                            eventType = parser.next();
                            config.keyword = parser.getText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("RecordRequest")) {
                            recordConfigList.add(config);
                            config = null;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
