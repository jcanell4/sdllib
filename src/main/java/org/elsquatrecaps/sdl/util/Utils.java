package org.elsquatrecaps.sdl.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.elsquatrecaps.sdl.exception.ErrorCopyingFileFormaException;
import org.elsquatrecaps.sdl.exception.ErrorParsingUrl;

/**
 *
 * @author josep
 */
public class Utils {
    
    public static String getNormalizedData(String date){
        String ret;
        char[] separators = {'/', '-', '.'};
        int nSep=0;
        while(nSep<separators.length && date.indexOf(separators[nSep])==-1){
            ++nSep;
        }
        if(nSep<separators.length){
            String[] aDate;
            aDate = date.split(String.valueOf(separators[nSep]));
            if(aDate.length==2){
                if(aDate[0].length()>2){
//                    ret = String.format("01/%02d/%04d", Integer.parseInt(aDate[1]), Integer.parseInt(aDate[0]));
                    ret = String.format("00/%02d/%04d", Integer.parseInt(aDate[1]), Integer.parseInt(aDate[0]));
                }else{
//                    ret = String.format("01/%02d/%04d", Integer.parseInt(aDate[0]), Integer.parseInt(aDate[1]));
                    ret = String.format("00/%02d/%04d", Integer.parseInt(aDate[0]), Integer.parseInt(aDate[1]));
                }
            }else{
                if(aDate[0].length()>2){
                    ret = String.format("%02d/%02d/%04d", Integer.parseInt(aDate[2]), Integer.parseInt(aDate[1]), Integer.parseInt(aDate[0]));
                }else{
                    ret = String.format("%02d/%02d/%04d", Integer.parseInt(aDate[0]), Integer.parseInt(aDate[1]), Integer.parseInt(aDate[2]));
                }
            }
        }else{
            ret = date;
        }
        return ret;
    }
    
    public static String getDateFromText(String date, String sep){
        return getDateFromText(date, sep, false);
    }
    
    public static String getDateFromText(String date, String sep, boolean reversedate){
        String ret;
        String year;
        int day;
        if(reversedate){
//            ret="0001".concat(sep).concat("01").concat(sep).concat("01");
            ret="0000".concat(sep).concat("00").concat(sep).concat("00");
        }else{
//            ret="01".concat(sep).concat("01").concat(sep).concat("0001");
            ret="00".concat(sep).concat("00").concat(sep).concat("0000");
        }
        boolean found = false;
        int id = 0;
        int dayDigits = 2;
        int[] yearGroup ={1, 3, 3};
        int[] dayGroup ={3, 1, 2};
        String[] extractDatePatterns = {".*(\\d{4})[^0-9]{0,7}\\s*(%1$s)[^0-9]{0,7}\\s*(\\d{%2$d}).*",            //".*(\\d{4})[^0-9]{0,7}\\s*(oct\\.?|octubre)[^0-9]{0,7}\\s*(\\d{2}).*"
                                       ".*(\\d{%2$d})[^0-9]{0,7}\\s*(%1$s)[^0-9]{0,7}\\s*(\\d{4}).*",    //".*(\\d{1})[^0-9]{0,7}\\s*(oct\\.?|octubre)[^0-9]{0,7}\\s*(\\d{4}).*"
                                       ".*\\s+(%1$s)[^0-9]{0,7}\\s*(\\d{%2$d})[^0-9]{0,7}\\s*(\\d{4}).*"         //".*\\s+(oct.?|octubre)[^0-9]{0,5}\\s*(\\d{1})[^0-9]{0,5}\\s*(\\d{4}).*"
        };
        String[]monthPatterns = {
            "en\\.?|ene\\.?|enero|gen\\.?|gener|jan\\.?|january",
            "feb\\.?|febr\\.?|febrero|febrer|february",
            "mar\\.?|marzo|març|march",
            "ab\\.?|abr\\.?|abril|ap\\.?|apr\\.?|april",
            "may\\.?|mayo|mai\\.?|maig",
            "jun\\.?|junio|juny|june",
            "jul\\.?|julio|juliol|july",
            "ag\\.?|ago\\.?|agosto|agost|aug\\.?|august",
            "sep\\.?|septiembre|set\\.?|setembre|september",
            "oc\\.?|oct\\.?|octubre|october",
            "nov\\.?|noviembre|novembre|november",
            "dic\\.?|diciembre|des\\.?|desembre|dec\\.?|december"
        };
        
        while(!found && id<monthPatterns.length){
            for(int dd=dayDigits; !found && dd>=0; dd--){
                for(int alt=0; !found && alt<3; alt++){
                    Pattern pattern = Pattern.compile(String.format(extractDatePatterns[alt], monthPatterns[id], dd), Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(date);
                    found = matcher.find();
                    if(found){
                        year = matcher.group(yearGroup[alt]);
                        if(dd>0){
                            day = Integer.parseInt(matcher.group(dayGroup[alt]));
                        }else{
                            day=0;
                        }
                        if(reversedate){
                            ret = String.format("%s%s%02d%s%02d", year, sep, id+1, sep, day);
                        }else{
                            ret = String.format("%02d%s%02d%s%s", day, sep, id+1, sep, year);
                        }
                    }
                }
            }
            ++id;
        }
        if(!found){
            id=0;
            while(!found && id<monthPatterns.length){
                Pattern mpattern = Pattern.compile(String.format(".*(%s).*", monthPatterns[id]), Pattern.CASE_INSENSITIVE);
                Matcher mmatcher = mpattern.matcher(date);
                found = mmatcher.matches();
                if(found){
                    Pattern ypattern = Pattern.compile(".*(\\d{4}).*");
                    Matcher ymatcher = ypattern.matcher(date);
                    found = ymatcher.matches();
                    if(found){
                        year = ymatcher.group(1);
                        if(reversedate){
                            ret = String.format("%s%s%02d%s00", year, sep, id+1, sep);
                        }else{
                            ret = String.format("00%s%02d%s%s", sep, id+1, sep, year);
                        }
                    }
                }
                ++id;
            }            
        }
        return ret;
    }
    
    public static void copyToFile(InputStream in, FileOutputStream out) {
        
        copyToFile(in, out, true, true);
    }
    
    public static void copyToFile(InputStream in, FileOutputStream out, boolean closein, boolean closeout) {
        try {
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
        } catch (IOException | NullPointerException ex) {
            throw new ErrorCopyingFileFormaException(ex);        
        } finally {
            if(closein && in!=null){
                try {
                    in.close();
                } catch (IOException ex) {}
            }
            if(closeout && out!=null){
                try {
                    out.close();
                } catch (IOException ex) {}
            }
        }
    }
    
    public static String buildNormalizedFilename(String name, int maxLength){
        return buildNormalizedFilename(name).substring(0,maxLength);
    }
    
    public static String buildNormalizedFilename(String name){
        return getNormalizedText(name, "_");
    }
    
    public static String getNormalizedText(String name){
        return getNormalizedText(name, "");
    }
    
    public static String getNormalizedText(String name, String pre){
        StringBuilder strBuffer = new StringBuilder();
        String locTitle;
        String ret;
        Pattern pattern1 = Pattern.compile(
                              "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
        Pattern pattern2 = Pattern.compile(
                              "[+-.:,;<>\\{\\}\\[\\]\\*\\^\\¿\\?\\=\\)\\(\\/\\&\\%ºº$\\·\\#\\@\\|\\\\!\"]+");
        locTitle = Normalizer.normalize(name, Normalizer.Form.NFD);
        locTitle = pattern1.matcher(locTitle).replaceAll("");
        locTitle = pattern2.matcher(locTitle).replaceAll("");
        String[] words = locTitle.split(" ");
        if(words.length>0){
            strBuffer.append(pre);
        }
        for (String word : words){
            if(word.length()>0){
                strBuffer.append(word.substring(0, 1).toUpperCase());
                strBuffer.append(word.substring(1).toLowerCase());
            }
        }
        return strBuffer.toString();
    }
    
    public static String urlQueryPath(String url){
        String ret = "";
        int i;
        if(url!=null){
            i = url.indexOf("?");
            if(i>-1){
                ret = url.substring(i+1);
            }
        }
        return ret;
    }
    
    public static String relativeToAbsoluteUrl(String context, String relative){
        String ret;
        try {
            ret = new URL(new URL(context), relative).toString();
        } catch (MalformedURLException ex) {
            throw new ErrorParsingUrl(ex);
        }            
        return ret;
    }    

    public static boolean isCorrectContentType(String urlname, String contentType){
        boolean ret=false;
        URLConnection connection=null;
        try {
            URL url = new URL(urlname);
            connection = url.openConnection();
            connection.connect();    
            ret = connection.getContentType().indexOf(contentType)>-1;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
}
