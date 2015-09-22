package se.chalmers.student.eyetracker;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Soroush on 19/10/14.
 */
public class ToXml {
    //private static String filename = "file.txt";

    public static void save( String filename,Context cnx ){

        FileOutputStream fos;

        try {
            fos = cnx.openFileOutput(filename, Context.MODE_APPEND);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag("", "root");

            for (int j = 0; j < 3; j++) {

                serializer.startTag("", "record");

                serializer.text("THIS IS A XML");

                serializer.endTag("", "record");
            }

            serializer.endTag("", "root");
            serializer.endDocument();
            serializer.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
