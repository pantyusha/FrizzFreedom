/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package qarabiy;

import tasks.DownloadTask;
import tasks.RefreshFileListTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;

/**
 *
 * @author pantene
 */
public class Grabber {

    // string parameters
    public static String downloadDirectory = "download";
    public static String urlRecent = "http://rgho.st/sitemap_recent.xml";
   
    // collections
    public static ArrayList<String> urlsToAnalyze = new ArrayList<String>();
    public static ArrayList<String> urlsCompleted = new ArrayList<String>();
    public static ArrayList<String> filesDownloaded = new ArrayList<String>();
    public static ArrayList<String> filesInProcess = new ArrayList<String>();
    public static HashMap<String, String> urlsParsed = new HashMap<>();
        
    public static Queue<String> filesToDownload = new ConcurrentLinkedQueue<String>();
    public static Queue<String> filesCompleted = new ConcurrentLinkedQueue<String>();
    
    // time parameters
    public static int refreshUrlsListInterval = 3;
    public static int refreshURLsWaiting = 5;
    public static int refreshGUIWaitingMs = 200;
    public static int downloadWaitingMs = 500;
    
    // threads count
    public static int downloadThreadCount = 20;
    public static int postProcessThreadCount = 10;
    public static int linkParseThreadCount = 3;
    
    // statistics counters
    public static int pagesParsed = 0;
    public static int parsedLinks = 0;
    public static int completedDownloads = 0;
    public static int urlsInProcess = 0; 
    public static int catchedRedirects = 0;
    public static int catched403 = 0;
    public static int catched503 = 0;
    public static int catchedOtherErrors = 0;
    public static int alreadyDownloaded = 0;
    public static int skippedByCategory = 0;
    
    //
    public static boolean oldDownloadEnabled = false;
    public static boolean refreshEnabled = false;
    public static boolean downloadEnabled = false;
    
    // swimming pools
    public static ScheduledExecutorService refreshListThread = Executors.newScheduledThreadPool(1);
    public static ScheduledExecutorService downloadService;
    public static ExecutorService postProcessService;
    public static ExecutorService linkParseService;
    ThreadFactory namedThreadFactory; //TODO: naming of threads
    
    public static Logger logger;     
    public static Categorizer categorizer;
    public static boolean guiEnabled = true;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
                
        for (String param : args){
            if (param.equals("--without-gui"))
                guiEnabled = false;
        }
         
        String loggerFileName;
        
        try {
            Ini ini = new Ini(new File("FrizzFreedom.ini"));
            Preferences prefs = new IniPreferences(ini);
            loggerFileName = prefs.node("main").get("logfile", "");
            enableFileLogging(loggerFileName);
            downloadThreadCount = prefs.node("threads").getInt("download",10);
            linkParseThreadCount = prefs.node("threads").getInt("linkparse",3);
            RGLogger.out("load from config download threads: "+downloadThreadCount);
            RGLogger.out("load from config link parse threads: "+linkParseThreadCount);
            categorizer = new Categorizer(ini);
        } catch (IOException ex) {
            Logger.getLogger(Grabber.class.getName()).log(Level.SEVERE, null, ex);
            categorizer = new Categorizer();
        }        
        
        downloadService = Executors.newScheduledThreadPool(downloadThreadCount); 
        postProcessService = Executors.newFixedThreadPool(postProcessThreadCount); 
        linkParseService = Executors.newFixedThreadPool(linkParseThreadCount); 

        refreshListThread.scheduleWithFixedDelay(new RefreshFileListTask(), 0, refreshURLsWaiting, TimeUnit.SECONDS);
        
                if (!Files.exists(Paths.get("download"))) {
            try {
                Files.createDirectory(Paths.get("download"));
            } catch (IOException ex) {
                Logger.getLogger(Grabber.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (guiEnabled){
            MainWnd view = new MainWnd();
            view.setVisible(true);
        } else {
            for (int i=0; i<downloadThreadCount; i++){
                downloadService.scheduleAtFixedRate(
                        new DownloadTask(), 0, 5, TimeUnit.MILLISECONDS);
            }
        }
    } 
       
    public static void enableFileLogging(String logFileName){
        Grabber.logger = Logger.getLogger("");  
        FileHandler fh;  

        if (logFileName.isEmpty())
            return;
        
        try {  

            fh = new FileHandler(logFileName);  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  

            logger.info("My first log");  

        } catch (SecurityException | IOException e) {  
            e.printStackTrace();  
        }  
    }
    
    public static String getContentFromURL(String urlString) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String text = "";

        try {
            url = new URL(urlString);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                text+=line;
            }
 
        } catch (IOException ex) {
            Logger.getLogger(MainWnd.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }
        
        return text;
    }
}
