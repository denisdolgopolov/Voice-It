package voice.it.firebaseloadermodule;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseGetUriListener;
import voice.it.firebaseloadermodule.listeners.FirebaseUploadListener;

public class FirebaseFileLoader {
    private final FirebaseStorage db = FirebaseStorage.getInstance();

    public FirebaseFileLoader(Context context) {
        try {
            loadFile(context.getAssets()
                            .open("12345.mp3"),
                    FirebaseFileTypes.RECORDS,
                    "5673",
                    new FirebaseUploadListener() {
                @Override
                public void onFailure(String error) {
                    Log.d("firebase", error);
                }

                @Override
                public void onSuccess() {
                    Log.d("firebase", "onSuccess");
                }

                @Override
                public void onProgress(int progress) {
                    Log.d("firebase", "progress " + progress);
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFile(InputStream stream,
                         FirebaseFileTypes type, String uuid,
                         final FirebaseUploadListener listener) {
        db.getReference(type.toString())
                .child(uuid)
                .putStream(stream)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        int progress = (int) Math.floor(snapshot.getBytesTransferred());
                        listener.onProgress(progress);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                            listener.onSuccess();
                        else
                            listener.onFailure("fail");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure(e.getLocalizedMessage());
                    }
                });
    }

    public void getDownloadUri(FirebaseFileTypes type,
                               String uuid,
                               final FirebaseGetUriListener listener) {
        db.getReference(type.toString())
                .child(uuid)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        listener.onGet(uri);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure(e.getLocalizedMessage());
                    }
                });
    }
}
