/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package qarabiy;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.ini4j.Ini;

/**
 *
 * @author pantene
 */
public class Categorizer {

    private static HashMap<String,String> directories = new HashMap<String,String>();
    private static ArrayList<Category> categoryList = new ArrayList<Category>();

    public Categorizer() {

    }
    
    public Categorizer(Ini config) {
        
        int catNumber = Integer.parseInt(config.get("categories","count"));
        System.out.println("Categories loaded: "+catNumber);
        for (int i=1; i<catNumber+1; i++) {
        
            String exts = config.get("category"+i,"exts");
            String catName = config.get("category"+i, "name");
            boolean isEnabled = config.get("category"+i, "enabled").equals("1");
            
            categoryList.add(new Category(catName, isEnabled));   
            
            String[] extArray = exts.split("\\.");
            for (int j=0; j<extArray.length; j++) {
                directories.put(extArray[j], catName);
            }
        }
        
        System.out.println("Extension loaded: "+Integer.toString(directories.size()));
    }
        
    public static void changeCategoryState(String name, boolean enabled){
        for (Category cat : categoryList)
        if (cat.name.equals(name)){
            cat.isEnabled = enabled;
            return;
        }
    }
    
    public static ArrayList<Category> getCategoriesEnableList() {
        return categoryList;
    }
    
    public static String getDownloadDirectory(String filename, String html) {
        String ext = getFileExtension(filename).toLowerCase();
        if (ext.equals(""))
            return "no extension";
        else {
            if (directories.containsKey(ext)){
                String dirName = directories.get(ext);
                for (Category cat : categoryList)
                    if (cat.name.equals(dirName)){
                        return cat.isEnabled ? dirName : null;
                    }
                // не должно сюда доходить!!!
                return dirName;
            }
            else
                return "unknown";
        }
    }
    
    private static String getFileExtension(String fileName) {
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else 
            return "";
    }
}
