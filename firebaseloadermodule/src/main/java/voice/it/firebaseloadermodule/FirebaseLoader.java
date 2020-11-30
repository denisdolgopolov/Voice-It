package voice.it.firebaseloadermodule;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import voice.it.firebaseloadermodule.cnst.FirebaseCollections;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListListener;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListener;
import voice.it.firebaseloadermodule.listeners.FirebaseListener;
import voice.it.firebaseloadermodule.model.FirebaseModel;
import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;
import voice.it.firebaseloadermodule.model.FirebaseUser;

public class FirebaseLoader {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void add(FirebaseModel item, final FirebaseListener listener) {
        String collectionName = FirebaseHelper.getCollectionNameByModel(item);
        db.collection(collectionName)
                .document(item.getUuid())
                .set(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure(e.getLocalizedMessage());
                    }
                });
    }


    public void getByUUID(final FirebaseCollections collection,
                          String uuid,
                          final FirebaseGetListener listener) throws IllegalStateException {
        db.collection(collection.toString())
                .document(uuid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()
                                || task.getResult() == null
                                || !task.getResult().exists()) {
                            listener.onFailure("I don't get value");
                            return;
                        }

                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            FirebaseModel model = getModel(document, collection);
                            listener.onGet(model);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure(e.getLocalizedMessage());
                    }
                });
    }

    public <T extends FirebaseModel> void getAll(final FirebaseCollections collection,
                                                 final FirebaseGetListListener<T> listener) {
        db.collection(collection.toString())
                .get()
                .addOnCompleteListener(getListOnCompleteListener(listener, collection))
                .addOnFailureListener(getListOnFailureListener(listener));
    }

    public <T extends FirebaseModel> void getAll(final FirebaseCollections parentType,
                                                 final String parentUUID,
                                                 final FirebaseGetListListener<T> listener) {
        String key = getKeyByParentType(parentType);

        db.collection(FirebaseCollections.Records.toString())
                .whereEqualTo(key, parentUUID)
                .get()
                .addOnCompleteListener(getListOnCompleteListener(listener, FirebaseCollections.Records))
                .addOnFailureListener(getListOnFailureListener(listener));
    }


    private <T extends FirebaseModel> OnCompleteListener<QuerySnapshot>
    getListOnCompleteListener(final FirebaseGetListListener<T> listener,
                              final FirebaseCollections collection) {
        return new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null) {
                    listener.onFailure("I don't get value");
                    return;
                }

                ArrayList list = new ArrayList<T>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(getModel(document, collection));
                }
                listener.onGet(list);
            }
        };
    }

    private <T extends FirebaseModel> OnFailureListener
    getListOnFailureListener(final FirebaseGetListListener<T> listener) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailure(e.getLocalizedMessage());
            }
        };
    }

    private FirebaseModel getModel(DocumentSnapshot document, FirebaseCollections collection) {
        switch (collection) {
            case Topics:
                return document.toObject(FirebaseTopic.class);
            case Users:
                return document.toObject(FirebaseUser.class);
            case Records:
                    return document.toObject(FirebaseRecord.class);
            default:
                throw new IllegalStateException();
        }
    }

    private String getKeyByParentType(FirebaseCollections parentType) {
        if (parentType == FirebaseCollections.Topics)
            return "topicUUID";
        else
            return "userUUID";
    }

}
