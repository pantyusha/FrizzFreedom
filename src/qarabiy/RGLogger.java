/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package qarabiy;

import java.util.logging.Level;

/**
 *
 * @author pantene
 */
public class RGLogger {
    public static void out(String msg){
        System.out.println(Thread.currentThread().getName()+" "+msg);
        Grabber.logger.info(Thread.currentThread().getName()+" "+msg);
    }
    public static void err(String msg){
        System.err.println(Thread.currentThread().getName()+" "+msg);
        Grabber.logger.info(Thread.currentThread().getName()+" "+msg);
    }
}
