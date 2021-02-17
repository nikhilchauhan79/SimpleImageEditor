package com.example.imagegallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditImageActivity extends AppCompatActivity {
    public static final int RESULT_INTENT_CODE = 123;
    public static final int BACK_CODE = 123;
    private ImageView editImageView;
    Button blackAndWhite,cropImageButton,saveButton,undoButton;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        editImageView = findViewById(R.id.edit_image_view);
        blackAndWhite = findViewById(R.id.black_and_white);
        saveButton=findViewById(R.id.save_button);
        undoButton=findViewById(R.id.undo_button);
        cropImageButton=findViewById(R.id.crop_image_button);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extras = getIntent().getExtras();
        String path = extras.getString("path");
        Log.d("path in edit", "onCreate: " + "edit activity" + path);


        Bitmap myBitmap = BitmapFactory.decodeFile(path);
        editImageView.setImageBitmap(myBitmap);

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editImageView.setImageBitmap(myBitmap);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToGallery();
            }
        });
        cropImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(EditImageActivity.this, CropImageActivity.class);
//
//                Bundle dataBundle = new Bundle();
//
//
//                dataBundle.putString("path", path);
//                intent.putExtras(dataBundle);
//                startActivity(intent);
                CropImage.startPickImageActivity(EditImageActivity.this);


            }
        });

        blackAndWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap blackWhite=toGrayscale(myBitmap);
                editImageView.setImageBitmap(blackWhite);
            }
        });
    }

    public static Bitmap toGrayscale(Bitmap srcImage) {

        Bitmap bmpGrayscale = Bitmap.createBitmap(srcImage.getWidth(), srcImage.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(srcImage, 0, 0, paint);

        return bmpGrayscale;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if(id== android.R.id.home){
            Bundle dataBundle = new Bundle();
            Intent intent=new Intent(EditImageActivity.this,MainActivity.class);

            String path=saveToGallery();
            dataBundle.putString("path", path);

            intent.putExtras(dataBundle);
            startActivity(intent);
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode== Activity.RESULT_OK){
            Uri imageUri=CropImage.getPickImageResultUri(this,data);

            if(CropImage.isReadExternalStoragePermissionsRequired(this,imageUri)){
                uri=imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
            }else{
                startCrop(imageUri);
            }

        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                editImageView.setImageURI(result.getUri());
                Toast.makeText(this, "Image updated successfully", Toast.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Bundle dataBundle = new Bundle();
        Intent intent=new Intent(EditImageActivity.this,MainActivity.class);

        Log.d("edit", "onBackPressed: ");
        String path=saveToGallery();
        Log.d("edit", "onBackPressed: "+path);
        dataBundle.putString("path", path);

        intent.putExtras(dataBundle);
        setResult(BACK_CODE, intent);
        finish();
    }

    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);

    }

    private String saveToGallery() {
        BitmapDrawable drawable = (BitmapDrawable) editImageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        FileOutputStream outputStream = null;
        File image = Environment.getExternalStorageDirectory();
        File directory = new File(image.getAbsolutePath() + "/pictures");
        directory.mkdir();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(directory, fileName);
        Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        try {
            outputStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(outFile));
            sendBroadcast(intent);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFile.getAbsolutePath();
    }

}