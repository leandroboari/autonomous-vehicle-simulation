package com.leandroboari.autonomousvehicle;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    public static void saveCarDataToFirestore(String raceName, String carName, double x, double y, double angle, String color, double maxSpeed, double minSpeed, double penalties, double speed) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Dados do carro para serem salvos
        Map<String, Object> carData = new HashMap<>();
        carData.put("x", x);
        carData.put("y", y);
        carData.put("angle", angle);
        carData.put("color", color);
        carData.put("maxSpeed", maxSpeed);
        carData.put("minSpeed", minSpeed);
        carData.put("penalties", penalties);
        carData.put("speed", speed);
        carData.put("timestamp", Timestamp.now());

        // Primeiro adiciona um campo de timestamp no documento principal "raceName"
        Map<String, Object> raceData = new HashMap<>();
        raceData.put("timestamp", Timestamp.now());

        db.collection("raceLastStates")
                .document(raceName)
                .set(raceData)  // Define o documento "raceName" com o campo timestamp
                .addOnSuccessListener(aVoid -> {
                    // Após adicionar o campo timestamp no documento, adiciona a subcoleção "cars"
                    db.collection("raceLastStates")
                            .document(raceName)
                            .collection("cars")
                            .document(carName)
                            .set(carData)  // Cria o documento do carro com os dados fornecidos
                            .addOnSuccessListener(bVoid -> {
                                Log.d("Firestore", "Car data successfully saved!");
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "Error saving car data", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error setting race timestamp", e);
                });
    }

    public interface FirestoreCallback {
        void onCallback(ArrayList<Map<String, Object>> carList);
    }

    public static void getAllCarsFromLastRaceState(FirestoreCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ArrayList<Map<String, Object>> carList = new ArrayList<>();

        Log.d("Firestore", "Fetching documents in 'raceLastStates' collection...");

        db.collection("raceLastStates")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(1)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        db.collection("raceLastStates")
                        .document(document.getId())
                        .collection("cars")
                        .get()
                        .addOnCompleteListener(carsTask -> {
                            if (carsTask.isSuccessful()) {
                                if (!carsTask.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot carDocument : carsTask.getResult()) {
                                        carList.add(carDocument.getData());
                                    }
                                    callback.onCallback(carList);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}