package voice.it.firebaseloadermodule;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import voice.it.firebaseloadermodule.model.FirebaseModel;
import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;
import voice.it.firebaseloadermodule.model.FirebaseUser;

public class FirebaseLoader {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void add(FirebaseModel item, final FirebaseListener listener) {
        String collectionName = getCollectionNameByModel(item);
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
                            switch (collection) {
                                case Topics:
                                    FirebaseTopic topic = document.toObject(FirebaseTopic.class);
                                    listener.onGet(topic);
                                    break;
                                case Users:
                                    FirebaseUser user = document.toObject(FirebaseUser.class);
                                    listener.onGet(user);
                                    break;
                                case Records:
                                    FirebaseRecord record = document.toObject(FirebaseRecord.class);
                                    listener.onGet(record);
                                    break;
                                default:
                                    throw new IllegalStateException();
                            }
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

    private String getCollectionNameByModel(FirebaseModel item) {
        if (item instanceof FirebaseTopic) return FirebaseCollections.Topics.toString();
        if (item instanceof FirebaseRecord) return FirebaseCollections.Records.toString();
        if (item instanceof FirebaseUser) return FirebaseCollections.Users.toString();
        throw new IllegalArgumentException();
    }

}
