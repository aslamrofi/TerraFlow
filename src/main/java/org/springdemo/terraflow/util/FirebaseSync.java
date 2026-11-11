package org.springdemo.terraflow.util;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Firebase Admin SDK's Realtime Database client is callback-based (it
 * shares its API with the client SDKs rather than offering Futures like most
 * Admin SDK surfaces), but the rest of this app is a synchronous Spring MVC
 * service layer. These helpers bridge the two: they block the calling thread
 * until the Firebase callback fires, then return a plain value or throw.
 */
public final class FirebaseSync {

    private static final int TIMEOUT_SECONDS = 10;

    private FirebaseSync() {
    }

    public static DataSnapshot read(Query query) {
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return await(future);
    }

    public static void write(DatabaseReference ref, Object value) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ref.setValue(value, (error, reference) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(null);
            }
        });
        await(future);
    }

    public static void remove(DatabaseReference ref) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ref.removeValue((error, reference) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(null);
            }
        });
        await(future);
    }

    private static <T> T await(CompletableFuture<T> future) {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Firebase operation failed or timed out", e);
        }
    }
}
