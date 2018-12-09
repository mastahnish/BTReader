package pilecka.paulina.btreader;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileHelper {

    Context context;
    private static String filename = "results.txt";

    public FileHelper(Context context) {
        this.context = context;
    }

    public void removeFile(){
        String path =
                Environment.getExternalStorageDirectory().getAbsolutePath();

        File file = new File(path, filename);
        boolean deleted = file.delete();

        if(deleted) Toast.makeText(context, "Results reset", Toast.LENGTH_SHORT).show();
    }

    public void writeToFile(String data) {

        String path =
                Environment.getExternalStorageDirectory().getAbsolutePath();

        File file = new File(path, filename);

        Log.d(MainActivity.TAG, "saving in: " + file.getAbsolutePath());
        try {
            FileOutputStream fOut = new FileOutputStream(file, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.append(System.lineSeparator());
            myOutWriter.close();
            fOut.close();

        } catch (FileNotFoundException e){
            Log.e(MainActivity.TAG, "File not found " + e.toString());
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "File write failed: " + e.toString());
        }

    }


    public ArrayList<String> readFromFile() {

        ArrayList<String> results = new ArrayList<>();

        String path =
                Environment.getExternalStorageDirectory().getAbsolutePath();

        File file = new File(path, filename);

        Log.d(MainActivity.TAG, "saving in: " + file.getAbsolutePath());
        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                results.add(line);
            }
            br.close();


        } catch (FileNotFoundException e){
            Log.e(MainActivity.TAG, "File not found " + e.toString());
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "File write failed: " + e.toString());
        }

        return results;

    }
}
