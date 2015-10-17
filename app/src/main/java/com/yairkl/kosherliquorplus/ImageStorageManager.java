package com.yairkl.kosherliquorplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import static com.yairkl.kosherliquorplus.DataBaseHandler.database;

public class ImageStorageManager {
	private Context myContext;
	private File directory;
	protected String path;
	
	public ImageStorageManager(Context context) {
		myContext = context;
		directory = myContext.getDir("imageDir", Context.MODE_PRIVATE);
		path = directory.getAbsolutePath();
	}
	
	public void saveImage(String productId, Bitmap bitmap)throws IOException {
        if (bitmap!=null) {
            File myPath = new File(directory, productId + ".jpg");
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(myPath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

    public void getImage(Identifiable identifiable, int rez, ImageView imageView,ProgressBar progress){
        if (database.isImageDownloaded(identifiable))
            loadImage(identifiable, rez, imageView,progress);
        else
            loadImageFromUrl(identifiable, imageView,progress);
    }

    public void loadImage(Identifiable identifiable,int rez,ImageView imageView,ProgressBar progress){
        BitmapWorkerTask task = new BitmapWorkerTask(imageView,progress,rez);
        task.execute(identifiable);
    }

    public void loadImageFromUrl(Identifiable identifiable,ImageView imageView,ProgressBar progress){
        BitmapUrlWorkerTask task= new BitmapUrlWorkerTask(imageView,progress);
        task.execute(identifiable);
    }

    public Bitmap loadImage(Identifiable identifiable,int rez){
        File f;
        Bitmap b;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = rez;
        try {
            if (identifiable.isCategory())
                f = new File(path, "cat"+identifiable.getId() + ".jpg");

            else
                f = new File(path, identifiable.getId() + ".jpg");

            b = BitmapFactory.decodeStream(new FileInputStream(f),null,options);
            return b;
        }
        catch (FileNotFoundException e)
        {
            Log.e("file not found", identifiable.getName() + e.getMessage());
            if (identifiable.isCategory())
                b = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.category_icon_grey);
            else
               b = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.bottle_icon);
            try {
                return b;
            } catch (Exception e1) {
                Log.e("file not found","temp"+e.getMessage());
            }
        }
        return null;
    }

    public Bitmap getBitmapFromURL(String myUrl) {
	    try {
	        URL url = new URL(myUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
            if (connection.getResponseCode()==200) {
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            }else
                return null;
	    } catch (IOException e) {
            Log.e("loadImageFromUrl",e.getMessage()+"\nUrl: "+myUrl);
	        return null;
	    }
	}

    private class BitmapWorkerTask extends AsyncTask<Identifiable, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ProgressBar> progressBarReference;
        private BitmapFactory.Options options;

        public BitmapWorkerTask(ImageView imageView,ProgressBar progressBar, int rez) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            progressBarReference = new WeakReference<ProgressBar>(progressBar);
            options = new BitmapFactory.Options();
            options.inSampleSize = rez;
        }

        @Override
        protected void onPreExecute(){
            ProgressBar bar = progressBarReference.get();
            if (bar != null)
                bar.setVisibility(View.VISIBLE);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Identifiable... params) {
            File f;
            if (params[0].isCategory())
                f = new File(path, "cat"+params[0].getId()+".jpg");
            else
                f = new File(path, params[0].getId()+".jpg");
            try {
                return BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            } catch (FileNotFoundException e) {
                if (params[0].isCategory())
                    return BitmapFactory.decodeResource(myContext.getResources(), R.drawable.category_icon_grey);
                else
                   return BitmapFactory.decodeResource(myContext.getResources(), R.drawable.gray_bottle_icon);
            }
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
            ProgressBar bar = progressBarReference.get();
            if (bar != null)
                bar.setVisibility(View.INVISIBLE);
        }
    }

    private class BitmapUrlWorkerTask extends AsyncTask<Identifiable, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ProgressBar> progressBarReference;
        Identifiable identifiable;
        public BitmapUrlWorkerTask(ImageView imageView, ProgressBar progressBar) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            progressBarReference = new WeakReference<ProgressBar>(progressBar);
        }

        @Override
        protected void onPreExecute(){
            ProgressBar bar = progressBarReference.get();
            if (bar != null)
                bar.setVisibility(View.VISIBLE);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Identifiable... params) {
            identifiable = params[0];
            Bitmap bitmap = getBitmapFromURL(database.getImageUrl(params[0]));
            if (bitmap!=null){
                try {
                    if (params[0].isCategory())
                        saveImage("cat"+identifiable.getId(), bitmap);
                    else
                        saveImage(""+identifiable.getId(), bitmap);
                } catch (IOException e) {
                    Log.e("saveImage", e.getMessage());
                    //e.printStackTrace();
                }
                return bitmap;
            }
            return BitmapFactory.decodeResource(myContext.getResources(), R.drawable.gray_bottle_icon);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                database.setImageDownloaded(identifiable);
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
            ProgressBar bar = progressBarReference.get();
            if (bar != null)
                bar.setVisibility(View.INVISIBLE);
        }
    }


}
