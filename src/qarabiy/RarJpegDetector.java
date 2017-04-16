/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package qarabiy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pantene
 */
public class RarJpegDetector {
    
    public static HashMap<String,byte[]> patterns = new HashMap<String,byte[]>();

    static {
        patterns.put("7zip", new byte[] {0x37,0x7a,(byte)0xbc,(byte)0xaf,0x27,0x1c});
        patterns.put("rar 5.0", new byte[]{0x52,0x61,0x72,0x21,0x1A,0x07,0x01,0x00});
        patterns.put("zip", new byte[]{0x50,0x4B,0x03,0x04});
        patterns.put("empty zip", new byte[]{0x50,0x4B,0x05,0x06});
        patterns.put("spanned zip", new byte[]{0x50,0x4B,0x07,0x08});
        patterns.put("rar < 5.0", new byte[]{0x52,0x61,0x72,0x21,0x1A,0x07,0x00});
    }
       
    public static boolean isArchive(InputStream content) throws IOException {
        byte[] contentBytes = readFully(content);
        
        for (Map.Entry<String,byte[]>  ptrn : patterns.entrySet()){
            int found = indexOf(contentBytes, ptrn.getValue());
            if (found != -1){
                System.out.println(String.format(
                       "Detected media type for given file %s",
                        ptrn.getKey()));
                return true;
            }
        }
        
        return false;
    }    
    
    public static byte[] readFully(InputStream input) throws IOException
    {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    public static int indexOf(byte[] data, byte[] pattern) {
        int[] failure = computeFailure(pattern);

        int j = 0;
        if (data.length == 0) return -1;

        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) { j++; }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }
}
