package com.abhi.toyswap.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.R;
import com.abhi.toyswap.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class AddItemActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView closeScreenImageView;
    private FrameLayout cameraPreviewLayout;
    private TextView descriptionTextView;
    private ImageView captureImageView;
    private ImageView previewImageView;
    private ImageView loadGalleryImageview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int SELECT_PICTURE = 3;
    public static final int PIC_CROP = 4;
    private Button retakeButton;
    private Button postButton;


    private static final String TAG = "Abhi";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private HandlerThread mBackgroundThread;
    private TextureView textureView;
    private final int CROP_PIC_REQUEST_CODE = 12;
    private Size previewSize;
    // private CropView cropview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activty_post_new_item);

        closeScreenImageView = (ImageView) findViewById(R.id.image_post_new_item_close);
        retakeButton = (Button) findViewById(R.id.button_post_new_item_retake);
        postButton = (Button) this.findViewById(R.id.button_post_new_item_post);
        descriptionTextView = (TextView) this.findViewById(R.id.text_post_new_item_camera_description);
        previewImageView = (ImageView) this.findViewById(R.id.image_post_new_item_preview);
        captureImageView = (ImageView) this.findViewById(R.id.image_post_new_item_capture);
        loadGalleryImageview = (ImageView) this.findViewById(R.id.image_post_new_item_gallery);
        postButton.setOnClickListener(this);
        retakeButton.setOnClickListener(this);
        closeScreenImageView.setOnClickListener(this);
        loadGalleryImageview.setOnClickListener(this);
        if (this.getIntent().getBooleanExtra("IsPrimaryImage", false)) {
            descriptionTextView.setVisibility(View.VISIBLE);
        }

        textureView = (TextureView) findViewById(R.id.frame_layout_post_new_item_camera_preview);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        captureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();

            }
        });
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();

            if (captureImageView.getVisibility() != View.VISIBLE) {
                retakeButton.setVisibility(View.GONE);
                postButton.setVisibility(View.GONE);
                captureImageView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            mCameraOpenCloseLock.release();
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

            mCameraOpenCloseLock.release();
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            if (cameraDevice != null) {
                cameraDevice.close();

            }
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(AddItemActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            return;
        }
        try {

            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

           /* if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }*/
            //   width = 640;
            // height = 480;

            ImageReader reader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);

            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            //  surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            outputSurfaces.add(new Surface(surfaceTexture));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            Calendar currentDate = Calendar.getInstance();

            file = new File(AddItemActivity.this.getCacheDir()+"/" + currentDate.get(Calendar.HOUR_OF_DAY) + currentDate.get(Calendar.MINUTE) + currentDate.get(Calendar.SECOND) + currentDate.get(Calendar.DATE) + currentDate.get(Calendar.MONTH) + currentDate.get(Calendar.YEAR) + "pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // retakeButton.setVisibility(View.VISIBLE);

                            //  previewImageView.setVisibility(View.VISIBLE);
                            // Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                            //previewImageView.setImageBitmap(bmp);
                            // performCrop(getImageContentUri(AddItemActivity.this,file));

                            performCrop(Uri.fromFile(file));


                        }
                    });

                    //closeCamera();

                    // createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {

                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mBackgroundHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;

                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(AddItemActivity.this, "Configuration Failed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //TODO Look for a way to make this horizontal
    private Size getPreferredPreviewSize(Size[] mapSizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for (Size option : mapSizes) {
            if (width > height) { //If the screen is in landscape
                Toast.makeText(getApplicationContext(), "Screen is Landscape", Toast.LENGTH_SHORT).show();
                if (option.getWidth() > width && option.getHeight() > height) {
                    collectorSizes.add(option);
                }
            } else { //if the screen is in portrait
                Toast.makeText(getApplicationContext(), "Screen is Portrait", Toast.LENGTH_SHORT).show();
                if (option.getWidth() > height && option.getHeight() > width) {
                    collectorSizes.add(option);
                }
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() + rhs.getHeight());
                }
            });
        }

        return mapSizes[0];
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            assert map != null;
            // imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            imageDimension = map.getOutputSizes(ImageFormat.YUV_420_888)[0];

            //    mPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);


            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddItemActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }

            try {
                if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                manager.openCamera(cameraId, stateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(AddItemActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable() && captureImageView.getVisibility() == View.VISIBLE) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    postButton.setVisibility(View.VISIBLE);
                    captureImageView.setVisibility(View.GONE);
                    loadGalleryImageview.setVisibility(View.GONE);
                    textureView.setVisibility(View.GONE);
                    Uri resultUri = result.getUri();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        textureView.setVisibility(View.GONE);

                        previewImageView.setVisibility(View.VISIBLE);
                        previewImageView.setImageBitmap(bitmap);

                        File tempFile = new File(resultUri.getPath());

                        Calendar currentDate = Calendar.getInstance();
                        file = new File(AddItemActivity.this.getCacheDir()+"/" + currentDate.get(Calendar.HOUR_OF_DAY) + currentDate.get(Calendar.MINUTE) + currentDate.get(Calendar.SECOND) + currentDate.get(Calendar.DATE) + currentDate.get(Calendar.MONTH) + currentDate.get(Calendar.YEAR) + "pic.jpg");

                        FileUtils.copyFile(tempFile, file);

                    } catch (IOException e) {
                        Log.i("Abhi", "Exception e:" + e.getMessage());
                        e.printStackTrace();
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
            }
            case SELECT_PICTURE: {
                if (data != null) {
                    String[] projection = {MediaStore.Images.Media.DATA};

                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();

                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                            cursor.moveToFirst();
                            postButton.setVisibility(View.VISIBLE);
                            loadGalleryImageview.setVisibility(View.GONE);

                            performCrop(uri);
                            cursor.close();
                        }


                    } else {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String selectedImagePath = cursor.getString(columnIndex);
                        //  Bitmap picture = BitmapFactory.decodeFile(selectedImagePath);
                        //  previewImageView.setImageBitmap(picture);
                        postButton.setVisibility(View.VISIBLE);
                        loadGalleryImageview.setVisibility(View.GONE);
                       /* Calendar currentDate = Calendar.getInstance();
                        file = new File(Environment.getExternalStorageDirectory() + "/ToyApp/" + currentDate.get(Calendar.HOUR_OF_DAY) + currentDate.get(Calendar.MINUTE) + currentDate.get(Calendar.SECOND) + currentDate.get(Calendar.DATE) + currentDate.get(Calendar.MONTH) + currentDate.get(Calendar.YEAR) + "pic.jpg");
                        OutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                            picture.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
                            outputStream.flush();
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("Abhi", "ERROR");
                        }*/
                        performCrop(selectedImage);

                        cursor.close();

                    }
                } else {
                    AddItemActivity.this.finish();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

    }


    private void performCrop(final Uri picUri) {
        try {

            (CropImage.activity(picUri).setGuidelines(CropImageView.Guidelines.OFF)).setAllowFlipping(false).setAllowRotation(false).setAutoZoomEnabled(false).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(4, 3).setFixAspectRatio(true).setMinCropResultSize(2400, 1800).setMaxCropResultSize(2400, 1800)
                    .start(this);

        } catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_post_new_item_close: {
                finish();
                break;
            }
            case R.id.image_post_new_item_gallery: {
                closeCamera();
                captureImageView.setVisibility(View.GONE);
                textureView.setVisibility(View.GONE);
                previewImageView.setVisibility(View.VISIBLE);

                //        CropImage.activity(null).setGuidelines(CropImageView.Guidelines.OFF).start(this);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setType("image/*");

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
                break;
            }
            case R.id.image_post_new_item_capture: {
                break;
            }
            case R.id.button_post_new_item_retake: {
                if (file != null) {
                    file.delete();
                }

                retakeButton.setVisibility(View.GONE);
                postButton.setVisibility(View.GONE);
                captureImageView.setVisibility(View.VISIBLE);
                //   previewImageView.setVisibility(View.GONE);
                textureView.setVisibility(View.VISIBLE);
                // updatePreview();
                // openCamera();
                //    createCameraPreview();

                break;
            }
            case R.id.button_post_new_item_post: {
                Intent finishIntent = new Intent();

                finishIntent.putExtra("FilePath", file.getName());
                finishIntent.putExtra("IsFromGallery", false);


                setResult(RESULT_OK, finishIntent);
                finish();

                break;
            }
        }
    }
}
