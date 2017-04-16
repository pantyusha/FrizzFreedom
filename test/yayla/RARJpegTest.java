/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package yayla;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import qarabiy.RarJpegDetector;
import static qarabiy.RarJpegDetector.isArchive;

/**
 *
 * @author pantene
 */
public class RARJpegTest {
    
    String testResourcesDir = Paths.get(System.getProperty("user.dir"), "test_resources").toString();
 
    public RARJpegTest() {
    }
    
    public void main(final String[] args) {
        testIsArchives();
        testIsNotArchives();
    }

    InputStream getTestFileStream(String fileName) throws FileNotFoundException {
        return new FileInputStream(new File(testResourcesDir, fileName));
    }
    
    @Test
    public void testIsNotArchives() {
        try { 
            assertTrue(isArchive(getTestFileStream("demo.7z")));
            assertTrue(isArchive(getTestFileStream("demo-7z.jpg")));
            assertTrue(isArchive(getTestFileStream("demo-7z.png")));
            
            assertTrue(isArchive(getTestFileStream("demo.rar")));
            assertTrue(isArchive(getTestFileStream("demo-rar.jpg")));
            assertTrue(isArchive(getTestFileStream("demo-rar.png")));
            
            assertTrue(isArchive(getTestFileStream("demo.zip")));
            assertTrue(isArchive(getTestFileStream("demo-zip.jpg")));
            assertTrue(isArchive(getTestFileStream("demo-zip.png")));
            
            assertTrue(isArchive(getTestFileStream("demo.jpeg")));
        } catch (IOException ex) {
            Logger.getLogger(RARJpegTest.class.getName()).log(Level.SEVERE, null, ex);
            assertFalse(true);
        }
    }
    
    @Test
    public void testIsArchives() {
        try {
            assertFalse(isArchive(getTestFileStream("demo.doc")));
            assertFalse(isArchive(getTestFileStream("demo.jpg")));
            assertFalse(isArchive(getTestFileStream("demo.png")));
        } catch (IOException ex) {
            Logger.getLogger(RARJpegTest.class.getName()).log(Level.SEVERE, null, ex);
            assertFalse(true);
        }
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
