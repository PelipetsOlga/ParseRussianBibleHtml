package com.example.olga.parserussianbiblehtml;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private MainActivity context;
    private Button btnExport;
    private StringBuilder builder = new StringBuilder("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv = (TextView) findViewById(R.id.tv);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        Button btnParse = (Button) findViewById(R.id.btn_parse);
        btnParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // createDB();
                new ParseTask(context).execute();
            }
        });
        btnExport = (Button) findViewById(R.id.btn_import);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exportDatabase()) {
                    Toast.makeText(context, "DB is exported", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "DB isn't exported", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createDB() {
        String rootFolder = "/storage/sdcard0/bible_tmp";
        File folder = new File(rootFolder);
        BibleDataBase db = new BibleDataBase(this);
        int i=0;
        db.open();
        if (folder.isDirectory()) {
            builder.append("root folder " + folder.getAbsolutePath() + "\n\n");
            File[] files = folder.listFiles();
            for (File f : files) {
                Chapter ch = null;
                try {
                    ch = parseFile(f);
                    //ch.setTitle(getChapterFromFile(f));
                    ch.setNum(getNumFromFile(f));
                    builder.append("\n" + ch.toString() + "\n");
                } catch (IOException e) {
                    builder.append("\n" + "Exception " + f.getAbsolutePath());
                }
                db.saveChapter(ch);
                Log.d("bible", "Saved chapter "+ Integer.toString(i++));

            }
        } else {

        }
        db.close();
    }

    private int getNumFromFile(File f) {
        String fullPath = f.getAbsolutePath();
        int lastSlash = fullPath.lastIndexOf("/");
        int lastDot = fullPath.lastIndexOf(".");
        if (lastSlash >= 0) {
            return Integer.parseInt(fullPath.substring(lastSlash + 5, lastDot));
        } else
            return 0;
    }

    private String getChapterFromFile(File f) {
        String fullPath = f.getAbsolutePath();
        int lastSlash = fullPath.lastIndexOf("/");
        if (lastSlash >= 0) {
            return fullPath.substring(lastSlash + 2, lastSlash + 4);
        } else
            return "empty title";
    }

    private Chapter parseFile(File f) throws IOException {
        Chapter chapter = new Chapter();
        InputStream instream = null;
        try {
            instream = new FileInputStream(f);
            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream, "ISO-8859-1");
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line;
                String startTag = new String("<TD><P>");
                String endTag = new String("<P></TD>");
                String titleTag=new String("<H2 ALIGN=CENTER>");

                // read every line of the file into the line-variable, on line at the time
                while ((line = buffreader.readLine()) != null) {
                    int start = line.indexOf(startTag);
                    int end = line.indexOf(endTag);
                    int titlePos=line.indexOf(titleTag);
                    if (start >= 0 && end >= 0) {
                        String text = line.substring(start + 7, end);
                        chapter.addText(text);
                    }
                    if (titlePos>=0){
                        String titleChapter=line.substring(titlePos+17);
                        chapter.setTitle(titleChapter);
                        Log.d("bible", titleChapter);
                    }
                }


            }

        } catch (Exception e) {
            builder.append(e.toString());
        } finally {
            instream.close();

        }

        builder.append("\n" + "fileName= " + f.getAbsolutePath());
        //  tv.append(chapter.toString());
        //   tv.append("file is parsed" + "\n\n");

        return chapter;
    }

    private class ParseTask extends AsyncTask<Void, Void, Void> {
        private Context ctx;

        ParseTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Void doInBackground(Void... params) {
            createDB();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            btnExport.setEnabled(true);
            tv.setText(builder.toString());
            super.onPostExecute(aVoid);
        }
    }

    private boolean exportDatabase() {
        File dbFile = getDatabasePath(BibleDataBase.DATABASE_NAME);
        Calendar cal = Calendar.getInstance();
        String fileDBName = "bibleDb_" + cal.get(Calendar.YEAR) + "_"
                + (cal.get(Calendar.MONTH) + 1) + "_"
                + cal.get(Calendar.DAY_OF_MONTH) + ",_time_"
                + cal.get(Calendar.HOUR) + "_hour_" + cal.get(Calendar.MINUTE)
                + "_min" ;
        File to = new File(Environment.getExternalStorageDirectory(),
                fileDBName);
        try {
            copyFile(dbFile, to);
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);
        FileChannel fromChannel = null, toChannel = null;
        try {
            fromChannel = in.getChannel();
            toChannel = out.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            if (fromChannel != null)
                fromChannel.close();
            if (toChannel != null)
                toChannel.close();
        }
    }


}
