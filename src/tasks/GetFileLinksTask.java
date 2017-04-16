/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import qarabiy.Grabber;
import qarabiy.MainWnd;
import qarabiy.RGLogger;

/**
 *
 * @author pantene
 */
public class GetFileLinksTask implements Runnable {

    public String pageUrl;
    
    public GetFileLinksTask(String url) {
        pageUrl = url;
    }

    @Override
    public void run() {
//        if (!Grabber.refreshEnabled)
//            return;
        
        int connectTries = 3;
        
        while (connectTries > 0){
            try {
                org.jsoup.nodes.Document doc = Jsoup.connect(pageUrl).get();
                Elements links = doc.select("#download-btn");
                links.addAll(0, doc.select("#release_filesets > tbody > tr > td > a.btn"));
                for (org.jsoup.nodes.Element link : links){
                    Grabber.filesToDownload.add(link.attr("href"));
                    Grabber.parsedLinks++;
                } 
                RGLogger.out("parsed page "+pageUrl);
                return;
            } catch (org.jsoup.HttpStatusException ex) {
                RGLogger.out("JSOUP error while parsing: "+ex.getStatusCode());
                if (ex.getStatusCode() == 403)
                    Grabber.catched403++;
                if (ex.getStatusCode() == 503)
                    Grabber.catched503++;
                try { Thread.sleep(1000); } catch (InterruptedException ex1) {}
            } catch (java.net.SocketTimeoutException ex) { //TODO: add java.net.ConnectException
                RGLogger.err("Socket timeout exception");
                Grabber.catchedOtherErrors++;
            } catch (IOException ex) { 
                Logger.getLogger(GetFileLinksTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            connectTries--;
        }
//        System.out.println("Page parse completed");
        System.out.println(Thread.currentThread().getName()+" failed in parse of "+pageUrl);
    }
  
    
    public ArrayList getRecentURLs(String xml) { 
        ArrayList<String> urls = new ArrayList<String>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            Element root = doc.getDocumentElement();
            NodeList links = root.getElementsByTagName("loc");
            for (int i = 0; i < links.getLength(); i++) {
                Node url = links.item(i);
                urls.add(url.getTextContent());
            }
            
            return urls;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MainWnd.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(MainWnd.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWnd.class.getName()).log(Level.SEVERE, null, ex);
        }
        return urls;
    } 
}