package voice.it.firebaseloadermodule;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.List;

import voice.it.firebaseloadermodule.cnst.FileLoadState;
import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseGetUriListener;
import voice.it.firebaseloadermodule.model.FirebaseModel;
import voice.it.firebaseloadermodule.service.IntentManager;
import voice.it.firebaseloadermodule.service.ServiceAction;
import voice.it.firebaseloadermodule.service.ServiceLoadFileState;

public class FirebaseFileLoader {
    private final FirebaseStorage db = FirebaseStorage.getInstance();
    private final Context context;

    public FirebaseFileLoader(Context context) {
        this.context = context;
    }

    public void uploadFile(final InputStream stream,
                           FirebaseFileTypes type,
                           final Long size,
                           final FirebaseModel item) {
        final Intent intent = new Intent(context, ServiceLoadFileState.class);
        final IntentManager manager = new IntentManager(context);
        manager.sendIntent(intent);

        intent.setAction(ServiceAction.GET_ITEM);
        intent.putExtra(FileLoadState.COMPLETED, item);
        manager.sendIntent(intent);

        final UploadTask uploadTask = db.getReference(type.toString())
                .child(item.getUuid())
                .putStream(stream);

        final OnProgressListener<UploadTask.TaskSnapshot> progressListener = new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int progress = (int) Math.floor(snapshot.getBytesTransferred() * 100 / size);

                intent.setAction(ServiceAction.PROGRESS);
                intent.putExtra(FileLoadState.PROGRESS, progress);
                manager.sendIntent(intent);
            }
        };
        final OnCompleteListener<UploadTask.TaskSnapshot> completeListener = new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                uploadTask.removeOnProgressListener(progressListener);
                if (task.isSuccessful()) {
                    intent.setAction(ServiceAction.SUCCESS);
                } else {
                    intent.setAction(ServiceAction.FAILED);
                }
                manager.sendIntent(intent);
            }
        };
        final OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadTask.removeOnProgressListener(progressListener);
                intent.setAction(ServiceAction.FAILED);
                manager.sendIntent(intent);
            }
        };
        uploadTask.addOnProgressListener(progressListener)
                .addOnCompleteListener(completeListener)
                .addOnFailureListener(failureListener);
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

    public void stopUpload(FirebaseModel firebaseModel) {
        List<UploadTask> tasks = db
                .getReference(FirebaseHelper.getCollectionNameByModel(firebaseModel))
                .child(firebaseModel.getUuid())
                .getActiveUploadTasks();
        for (UploadTask task : tasks) {
            task.cancel();
        }
    }
}
