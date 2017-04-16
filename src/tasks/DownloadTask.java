/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import qarabiy.Grabber;
import qarabiy.RGLogger;
/**
 *
 * @author pantene
 */
public class DownloadTask implements Runnable {

    private String name = "";
    private String toPath = "";

    public DownloadTask(String name, String toPath) {
        this.name = name;
        this.toPath = toPath;
    }

    public DownloadTask() {
    }

    @Override
    public void run() {
        RGLogger.out(" download thread started...");
        Object newUrl = null;
        int triesToDownload = 10;
        while(true){
//            if (!Grabber.downloadEnabled){
//                Grabber.urlsInProcess--;
//                return;
//            }   
            RGLogger.out("ENTER");
            try {
                if (newUrl == null) {
                        boolean waitForQueue = false;
                        while (Grabber.filesToDownload.isEmpty()){
                            if (!waitForQueue){
                                RGLogger.out("wait for files...");
                                waitForQueue = true;
                            }
                            Thread.sleep(50);
                        }
                        triesToDownload = 10;
                        newUrl = Grabber.filesToDownload.remove();
//                        Grabber.filesInProcess.add(newUrl.toString());
                        Grabber.urlsInProcess++;
                        RGLogger.out(" take URL "+newUrl.toString());
                } else {
                    try { Thread.sleep(1000); } catch (InterruptedException ex1) {}
                    if (triesToDownload==0){
                        Grabber.catchedOtherErrors++;
                        Grabber.filesInProcess.remove(newUrl);
                        RGLogger.err("PROEBALSO");
                        Grabber.urlsInProcess--;
                        newUrl = null;
                        continue;
                    }
                    RGLogger.out(" tries download again "+newUrl.toString());
                    triesToDownload--;
                }
                
                Random rand = new Random();
                Thread.sleep(100+rand.nextInt(1000));
                
                String[] strs = newUrl.toString().split("/");
                String filename = URLDecoder.decode(strs[strs.length-1],"UTF-8");
                
                String categoryPath = Grabber.categorizer.getDownloadDirectory(filename, "");
                
                if (categoryPath == null){
                    System.err.println(Thread.currentThread().getName()+" skipped file by its category");
                    Grabber.filesInProcess.remove(newUrl);
                    newUrl = null;
                    Grabber.urlsInProcess--;
                    Grabber.skippedByCategory++;
                    continue;
                } else 
                if (Files.exists(Paths.get("download"+File.separator+categoryPath+File.separator+filename))) {
                    System.err.println(Thread.currentThread().getName()+" detect that file already downloaded");
                    Grabber.filesInProcess.remove(newUrl);
                    Grabber.urlsCompleted.add(newUrl.toString());
                    newUrl = null;
                    Grabber.urlsInProcess--;
                    Grabber.alreadyDownloaded++;
                    continue;
                }
                
                if (!Files.exists(Paths.get("download"+File.separator+categoryPath))) {
                    Files.createDirectory(Paths.get("download"+File.separator+categoryPath));
                }
                
                Response response = Jsoup.connect(newUrl.toString()).followRedirects(false).execute();
                if (response.header("location").startsWith("http://rgho.st/")) {
                    RGLogger.err("detected inner redirect, skip file");
                    Grabber.catchedRedirects++;
                    Grabber.linkParseService.submit(new GetFileLinksTask(response.header("location")));
                } else {     
                    RGLogger.out("redirected to: "+response.header("location"));
                    downloadFile(newUrl.toString(), "download"+File.separator+categoryPath, filename);
                    if (categoryPath.equals("Растровые изображения") ||
                        categoryPath.equals("no extension") ||
                        categoryPath.equals("unknown"))
                        Grabber.postProcessService.submit(new RarJpegCheckTask("download"+File.separator+categoryPath+File.separator+filename));
                    Grabber.filesCompleted.add(filename);
                    Grabber.completedDownloads++;
                    RGLogger.out(" download "+newUrl.toString());
                }
                
                newUrl = null;
                Grabber.urlsInProcess--;
            
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
            } catch (java.io.FileNotFoundException ex) {
                RGLogger.err("- file not found, ebat' O_o");
                Grabber.catchedOtherErrors++;
                Grabber.filesInProcess.remove(newUrl);
                Grabber.urlsInProcess--;
                newUrl = null;
            } catch (java.net.SocketTimeoutException ex) {
                RGLogger.err("- socket timeout exception");
                Grabber.catchedOtherErrors++;
            } catch (org.jsoup.HttpStatusException ex) {
                RGLogger.err("- JSOUP error while checking redirect: "+ex.getStatusCode());
                if (ex.getStatusCode() == 403)
                    Grabber.catched403++;
                if (ex.getStatusCode() == 503)
                    Grabber.catched503++;
            } catch (IOException ex) {
                RGLogger.err("- error while downloading: "+ex.getLocalizedMessage());
                Grabber.catchedOtherErrors++;
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                RGLogger.err("end cycle");
            }
            RGLogger.err("WOW");
        }
    }
    
    /**
     *
     * @param url
     * @param path
     */
    public void downloadFile(String url, String path, String filename) throws MalformedURLException, IOException  {
        RGLogger.out("enter download block");
        URL fileLink;
        fileLink = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(fileLink.openStream());
        FileOutputStream fos = new FileOutputStream(path+File.separator+filename);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        RGLogger.out("exit download block");
    }
    
}