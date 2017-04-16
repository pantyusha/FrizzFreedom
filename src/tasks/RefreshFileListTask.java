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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
public class RefreshFileListTask implements Runnable {

    public RefreshFileListTask() {
    }

    @Override
    public void run() {
        RGLogger.out("filelist refresh at "+new Date());
        
        String recentXml = Grabber.getContentFromURL(Grabber.urlRecent);
        ArrayList<String> urls = getRecentURLs(recentXml);
        RGLogger.out("total parsed "+Integer.toString(urls.size()));
        int addedUrls = 0;
        boolean skipAll = false;
        if (Grabber.oldDownloadEnabled == false && Grabber.urlsParsed.isEmpty())
            skipAll = true;
        
        for (int i=0; i<urls.size(); i++){
            String url = urls.get(i);
            
            if (skipAll){
                Grabber.urlsParsed.put(url, "skip");
                continue;
            }
            
            if (Grabber.urlsParsed.containsKey(url))
                continue;
            addedUrls++;
            
            Grabber.urlsParsed.put(url, "new");
            Grabber.linkParseService.submit(new GetFileLinksTask(url));
        
        }
        Grabber.pagesParsed+=addedUrls;
        RGLogger.out("added new urls: "+addedUrls); 
    }
  
    
    public ArrayList<String> getRecentURLs(String xml) { 
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