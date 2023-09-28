package com.hcdc.capstone.taskprocess;

import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;
import com.hcdc.capstone.TimerViewModel;
import com.hcdc.capstone.network.ApiClient;
import com.hcdc.capstone.network.ApiService;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

public class TaskProgress extends BaseActivity {
    private TimerViewModel timerViewModel;
    private static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    private static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    private static final String TIMER_PREFS = "TimerPrefs";
    private static final String PREF_START_TIME = "startTime";
    private static final String PREF_REMAINING_TIME = "remainingTime";
    private static final String PREF_TIMER_RUNNING = "timerRunning";
    private FirebaseFirestore db;
    private Button startButton;
    private Button doneButton;
    private ImageView uploadButton;
    private TextView timerTextView;
    private static final int IMAGE_PICK_REQUEST_CODE = 100;

    private static final int CAMERA_CAPTURE_REQUEST_CODE = 200;
    private String currentUserUID;
    private boolean timerRunning = false;
    private long startTime = 0L;
    private long taskDurationMillis;
    private final Handler handler = new Handler();
    private Uri selectedImageUri;
    public static HashMap<String, String> remoteMsgHeaders = null;


    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAnnDyey0:APA91bEFvAHQXeK_dpb0GbX8K27RVhe7mJX45_pPnaumLhhoiazeVAythGSSRxNYSS2JAalYA3dfDyqfprJk_TrN6gYyslGR6bsPJ_BeRZVAv4_pvDKqAN1mHq1Wh-5AhJwcurC6nRE5"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (timerRunning) {
                long millis = System.currentTimeMillis() - startTime;
                long remainingMillis = taskDurationMillis - millis;
                if (remainingMillis <= 0) {
                    timerTextView.setText("Time's up!");
                    timerRunning = false;
                    doneButton.setVisibility(View.VISIBLE);
                } else {
                    int seconds = (int) (remainingMillis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    int hours = minutes / 60;
                    minutes = minutes % 60;

                    timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                    handler.postDelayed(this, 1000);
                }
            }
        }
    };

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String taskName, String location) {
        try {
            JSONObject data = new JSONObject();
            data.put("title", taskName);
            data.put("body", location);

            JSONObject payload = new JSONObject();
            payload.put("to", "fPNQlFuyiS-IapSjNiDOqD:APA91bFDDSYUQs0wgsLosvKJLVbn_ankKDYRL1KO3qPgc4Cel5bbMrTpqzdKh9ca9d0cz1hkP10v-iBnrYs7Bh6Cv1roF_dz-NOR_OayYdwhxgwBu1InGZRTGNLpeFpRUm7N3Mn_tjpo");
            payload.put("notification", data);

            sendNotificationToFCM(payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationToFCM(String payload) {
        String authorizationKey = "key=AAAAnnDyey0:APA91bEFvAHQXeK_dpb0GbX8K27RVhe7mJX45_pPnaumLhhoiazeVAythGSSRxNYSS2JAalYA3dfDyqfprJk_TrN6gYyslGR6bsPJ_BeRZVAv4_pvDKqAN1mHq1Wh-5AhJwcurC6nRE5";
        String contentType = "application/json";

        ApiClient.getClient().create(ApiService.class).sendMessage(
                createHeaders(authorizationKey, contentType),
                payload
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray results = responseJSON.getJSONArray("results");
                            if (responseJSON.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("ERROR: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    @NonNull
    private HashMap<String, String> createHeaders(String authorization, String contentType) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(REMOTE_MSG_AUTHORIZATION, authorization);
        headers.put(REMOTE_MSG_CONTENT_TYPE, contentType);
        return headers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_test);

        timerViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(TimerViewModel.class);
        boolean doneButtonNotClicked = !timerViewModel.isDoneButtonClicked();

        startButton = findViewById(R.id.startButton);
        doneButton = findViewById(R.id.doneButton);
        timerTextView = findViewById(R.id.timerTextView);
        TextView taskNameTextView = findViewById(R.id.taskTitle4);
        TextView taskPointsTextView = findViewById(R.id.taskPoint);
        TextView taskDescriptionTextView = findViewById(R.id.taskDesc);
        TextView taskLocationTextView = findViewById(R.id.taskLocation);
        TextView taskTimeFrameTextView = findViewById(R.id.taskTimeFrame);
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String taskName = getIntent().getStringExtra("taskName");
        String taskPoints = getIntent().getStringExtra("taskPoints");
        String taskDescription = getIntent().getStringExtra("taskDescription");
        String taskLocation = getIntent().getStringExtra("taskLocation");
        currentUserUID = auth.getCurrentUser().getUid();

        int timeFrameHours = getIntent().getIntExtra("timeFrameHours", 0);
        int timeFrameMinutes = getIntent().getIntExtra("timeFrameMinutes", 0);

        taskNameTextView.setText(taskName);
        taskPointsTextView.setText(taskPoints);
        taskDescriptionTextView.setText(taskDescription);
        taskLocationTextView.setText(taskLocation);

        long timeFrameMillis = (timeFrameHours * 60L + timeFrameMinutes) * 60 * 1000;
        taskDurationMillis = timeFrameMillis;

        int initialSeconds = (int) ((timeFrameMillis % (60 * 1000)) / 1000);
        timerTextView.setText(String.format("%02d:%02d:%02d", timeFrameHours, timeFrameMinutes, initialSeconds));

        String taskTimeFrame = timeFrameHours + " hours " + timeFrameMinutes + " minutes";
        taskTimeFrameTextView.setText(taskTimeFrame);

        View tasksubmitCustomDialog = LayoutInflater.from(TaskProgress.this).inflate(R.layout.tasksubmit_dialog, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TaskProgress.this);
        alertDialog.setView(tasksubmitCustomDialog);
        ImageButton cancelButton = tasksubmitCustomDialog.findViewById(R.id.closeSubmission);
        Button submitButton = tasksubmitCustomDialog.findViewById(R.id.taskSubmission);
        uploadButton = tasksubmitCustomDialog.findViewById(R.id.upload_submission);
        final AlertDialog dialog = alertDialog.create();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_CAPTURE_REQUEST_CODE);
                }
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize a Firestore batch write
                WriteBatch batch = db.batch();

                // Read data from Firestore
                db.collection("user_acceptedTask")
                        .whereEqualTo("acceptedBy", currentUserUID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    String documentId = documentSnapshot.getId();
                                    Map<String, Object> acceptedTaskData = documentSnapshot.getData();

                                    // Update "isCompleted" field
                                    batch.update(db.collection("user_acceptedTask").document(documentId), "isCompleted", true);

                                    // Perform other batch operations (if needed)

                                    // Commit the batch
                                    batch.commit()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Batch write successful, now proceed with other operations
                                                    long remainingMillis = taskDurationMillis - (System.currentTimeMillis() - startTime);
                                                    int remainingSeconds = (int) (remainingMillis / 1000);
                                                    int remainingMinutes = remainingSeconds / 60;
                                                    remainingSeconds = remainingSeconds % 60;
                                                    int remainingHours = remainingMinutes / 60;
                                                    remainingMinutes = remainingMinutes % 60;

                                                    String remainingTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);

                                                    acceptedTaskData.put("isCompleted", true);
                                                    acceptedTaskData.put("remainingTime", remainingTime);

                                                    // Check if an image is selected
                                                    if (selectedImageUri != null) {
                                                        // Upload the image to Firebase Storage
                                                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                                        StorageReference imageRef = storageRef.child("images/" + currentUserUID + "_" + System.currentTimeMillis());

                                                        imageRef.putFile(selectedImageUri)
                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                            @Override
                                                                            public void onSuccess(Uri uri) {
                                                                                String imageUrl = uri.toString();

                                                                                // Store the image URL in Firestore
                                                                                storeImageUrlInFirestore(imageUrl);

                                                                                // Update the acceptedTaskData with the image URL
                                                                                acceptedTaskData.put("imageUrl", imageUrl);

                                                                                // Add the accepted task to "completed_task" collection
                                                                                db.collection("completed_task")
                                                                                        .add(acceptedTaskData)
                                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                            @Override
                                                                                            public void onSuccess(DocumentReference documentReference) {
                                                                                                // Delete the document from "user_acceptedTask"
                                                                                                db.collection("user_acceptedTask")
                                                                                                        .document(documentId)
                                                                                                        .delete()
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void aVoid) {
                                                                                                                // Document deleted successfully
                                                                                                                Intent intent = new Intent(TaskProgress.this, taskFragment.class);
                                                                                                                startActivity(intent);
                                                                                                                finish();
                                                                                                            }
                                                                                                        })
                                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                                            @Override
                                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                                // Handle failure
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                // Handle failure
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle image upload failure
                                                                    }
                                                                });
                                                    } else {
                                                        // No image selected
                                                        // Add the accepted task to "completed_task" collection
                                                        db.collection("completed_task")
                                                                .add(acceptedTaskData)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        // Delete the document from "user_acceptedTask"
                                                                        db.collection("user_acceptedTask")
                                                                                .document(documentId)
                                                                                .delete()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        // Document deleted successfully
                                                                                        Intent intent = new Intent(TaskProgress.this, taskFragment.class);
                                                                                        startActivity(intent);
                                                                                        finish();
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        // Handle failure
                                                                                    }
                                                                                });
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle failure
                                                                    }
                                                                });
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Handle batch write failure
                                                }
                                            });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure
                            }
                        });
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                if (!timerRunning) {
                    timerRunning = true;
                    startTime = System.currentTimeMillis();
                    handler.postDelayed(timerRunnable, 1000);
                    startTimer();
                    startButton.setVisibility(View.GONE);
                    doneButton.setVisibility(View.VISIBLE);

                    db.collection("user_acceptedTask")
                            .whereEqualTo("acceptedBy", currentUserUID)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        String documentId = documentSnapshot.getId();

                                        db.collection("user_acceptedTask")
                                                .document(documentId)
                                                .update("isStarted", true)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle failure
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        timerViewModel.setStartTime(startTime);
        timerViewModel.setTimerRunning(timerRunning);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        startTime = timerViewModel.getStartTime();
        timerRunning = timerViewModel.isTimerRunning();

        if (timerRunning) {
            handler.post(timerRunnable);
            startButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.VISIBLE);
        }
    }

    private void storeImageUrlInFirestore(String imageUrl) {
        db.collection("images")
                .add(new HashMap<String, Object>() {{
                    put("imageUrl", imageUrl);
                }})
                .addOnSuccessListener(documentReference -> {
                    // Image URL stored successfully in Firestore
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Get the captured image Uri
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            selectedImageUri = getImageUri(imageBitmap);
        }
    }

    private Uri getImageUri(Bitmap imageBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "Title", null);
        return Uri.parse(path);
    }



    @Override
    public void onBackPressed() {
        if (timerRunning) {
            Toast.makeText(this, " Task is in progress. Cannot go back. ", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing() && timerRunning) {
            // Start the TimerService as a foreground service
            Intent serviceIntent = new Intent(this, TimerService.class);
            serviceIntent.putExtra("taskDurationMillis", taskDurationMillis);
            serviceIntent.putExtra("startTime", startTime);
            startForegroundService(serviceIntent);

            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            long remainingTime = taskDurationMillis - elapsedTime;

            // Save the current timer state in shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences(TIMER_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(PREF_START_TIME, currentTime);
            editor.putLong(PREF_REMAINING_TIME, remainingTime);
            editor.putBoolean(PREF_TIMER_RUNNING, true);
            editor.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(TIMER_PREFS, MODE_PRIVATE);
        if (sharedPreferences.getBoolean(PREF_TIMER_RUNNING, false)) {
            startTime = sharedPreferences.getLong(PREF_START_TIME, 0);
            taskDurationMillis = sharedPreferences.getLong(PREF_REMAINING_TIME, 0);
            if (taskDurationMillis > 0) {
                // Restart the timer when the app is relaunched
                startTimer();
            }
        }
    }


    private void startTimer() {
        if (!timerRunning) {
            timerRunning = true;
            startTime = System.currentTimeMillis();
            handler.postDelayed(timerRunnable, 0);

            startButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.VISIBLE);

        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TimerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isMyServiceRunning()) {
            Intent broadcastIntent = new Intent("StopTimerService");
            sendBroadcast(broadcastIntent);
        }
    }

    private void stopTimer() {
        if (timerRunning) {
            handler.removeCallbacks(timerRunnable);
            timerRunning = false;
        }
    }

    public static class TimerService extends Service {
        private final Handler handler = new Handler();
        private Runnable timerRunnable;
        private long startTime;
        private long taskDurationMillis;
        private static final int NOTIFICATION_ID = 1;
        private static final String CHANNEL_ID = "TimerServiceChannel";
        private static final String SILENT_CHANNEL_ID = "SilentTimerChannel";
        private String currentTimerValue = "00:00:00";
        private NotificationCompat.Builder builder;
        private NotificationManagerCompat notificationManager;

        private final BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, @NonNull Intent intent) {
                if ("StopTimerService".equals(intent.getAction())) {
                    stopForeground(true);
                    stopSelf();
                }
            }
        };

        @Override
        public void onCreate() {
            super.onCreate();
            IntentFilter filter = new IntentFilter("StopTimerService");
            registerReceiver(stopServiceReceiver, filter);
            createNotificationChannels();
            builder = createNotification(currentTimerValue);
            notificationManager = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        private void createNotificationChannels() {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationChannel silentChannel = new NotificationChannel(
                    SILENT_CHANNEL_ID,
                    "Silent Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            manager.createNotificationChannel(silentChannel);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (intent != null) {
                taskDurationMillis = intent.getLongExtra("taskDurationMillis", 0);
                startTime = intent.getLongExtra("startTime", 0);
                timerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        long millis = System.currentTimeMillis() - startTime;
                        long remainingMillis = taskDurationMillis - millis;
                        if (remainingMillis > 0) { // Update the notification only if there's remaining time
                            int seconds = (int) (remainingMillis / 1000);
                            int minutes = seconds / 60;
                            seconds %= 60;
                            int hours = minutes / 60;
                            minutes %= 60;
                            currentTimerValue = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                            updateNotification(currentTimerValue);
                            handler.postDelayed(this, 1000);
                        }
                    }
                };
                if (taskDurationMillis > 0) {
                    // Create and start the foreground service notification
                    NotificationCompat.Builder builder = createNotification(currentTimerValue);
                    Notification notification = builder.build();
                    startForeground(NOTIFICATION_ID, notification);
                    handler.post(timerRunnable);
                } else {
                    stopSelf();
                }
            }
            return START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(stopServiceReceiver);
            handler.removeCallbacks(timerRunnable);
        }

        private void updateNotification(String timerValue) {
            RemoteViews customView = new RemoteViews(getPackageName(), R.drawable.custom_notification_layout);
            customView.setTextViewText(R.id.notification_text, "Time Remaining: " + timerValue);
            builder.setCustomContentView(customView);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        @NonNull
        private NotificationCompat.Builder createNotification(String timerValue) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SILENT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_timer)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true);
            builder.setDefaults(0);
            RemoteViews customView = new RemoteViews(getPackageName(), R.drawable.custom_notification_layout);
            customView.setTextViewText(R.id.notification_text, "Time Remaining: " + timerValue);
            builder.setCustomContentView(customView);
            Intent notificationIntent = new Intent(this, TaskProgress.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);
            return builder;
        }
    }
}