/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tasks;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import qarabiy.RGLogger;
import qarabiy.RarJpegDetector;
/**
 *
 * @author pantene
 */
public class RarJpegCheckTask implements Runnable {

    private String fileName = "";

    public RarJpegCheckTask(String filename) {
        this.fileName = filename;
    }

    @Override
    public void run() {
        RGLogger.out(Thread.currentThread().getName()+" check rarjpeg in "+fileName);
        try {
            boolean isArchive = RarJpegDetector.isArchive(new FileInputStream(fileName));
            if (isArchive){
                File archFile = new File(fileName);
                String dir = archFile.getAbsoluteFile().getParent();
                RGLogger.out("abs path: "+dir);
                RGLogger.out("new path: "+dir+File.separator+"Архивы спецслужб");
                if (!Files.exists(Paths.get(dir+File.separator+"Архивы спецслужб"))) {
                    Files.createDirectory(Paths.get(dir+File.separator+"Архивы спецслужб"));
                }
                
                RGLogger.out("new file: "+dir+File.separator+"Архивы спецслужб"+File.separator+archFile.getName());
                int triesToMove = 3;
                Path fileFrom = Paths.get(fileName);
                Path fileTo = Paths.get(dir+File.separator+"Архивы спецслужб"+File.separator+archFile.getName());
                Path fileCopyTo = Paths.get(dir+File.separator+"Архивы спецслужб"+File.separator+"COPY_"+archFile.getName());
                
                while (triesToMove > 0) {
                    try {
                        triesToMove--;
                        Files.move(fileFrom, fileTo);
                    } catch (IOException ex) {
                        RGLogger.err("Cannot move file: "+fileFrom.toString()+" to "+fileTo.toString());
                        try { Thread.sleep(5000); } catch (InterruptedException ex1) {}
                        if (triesToMove==0){
                            Files.copy(fileFrom, fileCopyTo);
                        }
                    }
                }
                
            } else {
                RGLogger.out(Thread.currentThread().getName()+" not detected rarjpeg");  
            }
                    
        } catch (IOException ex) {
            Logger.getLogger(RarJpegCheckTask.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
}