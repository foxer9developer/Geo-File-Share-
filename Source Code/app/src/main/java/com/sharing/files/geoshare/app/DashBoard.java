package com.sharing.files.geoshare.app;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
public class DashBoard extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {
    ImageButton browsefiles,downloadfiles;
    Button uploadfiles,logout;
    TextView notification;
    Uri allFilesUri;
    FirebaseStorage storage;
    FirebaseDatabase database;
    private FirebaseAuth auth;
    ProgressDialog progressDialog;
    EditText accesskey,radius;
    private static final int MY_PERMISSION_REQUEST_CODE = 1399;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1398;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    DatabaseReference ref;
    GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        browsefiles = findViewById(R.id.browse_files);
        uploadfiles = findViewById(R.id.upload_files_dashboard);
        notification = findViewById(R.id.your_file);
        auth = FirebaseAuth.getInstance();
        FirebaseAuth.getInstance().getCurrentUser();
        accesskey = findViewById(R.id.access_key_upload);
        accesskey.setInputType(InputType.TYPE_CLASS_TEXT);
        downloadfiles = findViewById(R.id.download_dashboard);
        radius = findViewById(R.id.radius);
        radius.setInputType(InputType.TYPE_CLASS_NUMBER);
        new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(DashBoard.this, Login.class));
                    finish();
                }
            }
        };
        logout = findViewById(R.id.logout_button);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        browsefiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(DashBoard.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        });
        uploadfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allFilesUri != null)
                    uploadFile(allFilesUri);
                else
                    Toast.makeText(DashBoard.this,"Select A File",Toast.LENGTH_SHORT).show();
            }
        });
        downloadfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(),DownloadFile.class));
            }
        });
    }
    private void signOut() {
        auth.signOut();
        startActivity(new Intent(DashBoard.this,Login.class));
        finish();
    }
    private void uploadFile(final Uri allFilesUri) {
        geoFire = new GeoFire(ref);
        setUpLocation();
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("... Uploading File ...");
        progressDialog.setProgress(0);
        progressDialog.show();
        final String fileName = accesskey.getText().toString();
        final StorageReference storageReference = storage.getReference();
        storageReference.child(fileName).putFile(allFilesUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.child(fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DatabaseReference reference = database.getReference();
                        reference.child(fileName).setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(DashBoard.this,"File Successfully Uploaded",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(DashBoard.this,DashBoard.class));
                                    finish();
                                }
                                else
                                    Toast.makeText(DashBoard.this,"File Not Uploaded",Toast.LENGTH_SHORT).show();
                            }
                        });
                        reference.child(fileName).child("Latitude").setValue(mLastLocation.getLatitude());
                        reference.child(fileName).child("Longitude").setValue(mLastLocation.getLongitude());
                        reference.child(fileName).child("Radius").setValue(Double.parseDouble(radius.getText().toString()));
                        reference.child(fileName).child("URL").setValue(uri.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DashBoard.this,"File Not Uploaded",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }
    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(DashBoard.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DashBoard.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DashBoard.this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(DashBoard.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DashBoard.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        int UPDATE_INTERVAL = 3000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        int FASTEST_INTERVAL = 1000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        int DISPLACEMENT = 5;
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(DashBoard.this).addConnectionCallbacks(DashBoard.this).addOnConnectionFailedListener(DashBoard.this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(DashBoard.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,DashBoard.this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else {
                Toast.makeText(DashBoard.this,"This Device Is Not Supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1 : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectAllFiles();
                } else {
                    Toast.makeText(getBaseContext(), "Please Provide Permissions", Toast.LENGTH_SHORT).show();
                }
            }
            case MY_PERMISSION_REQUEST_CODE : if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPlayServices()) {
                    buildGoogleApiClient();
                    createLocationRequest();
                    displayLocation();
                }
            }
            break;
        }
    }
    private void selectAllFiles() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,86);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==86 && resultCode==RESULT_OK && data!=null) {
            allFilesUri = data.getData();
            notification.setText(allFilesUri.toString());
        } else {
            Toast.makeText(DashBoard.this,"Please Select A File",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(DashBoard.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DashBoard.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,DashBoard.this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}