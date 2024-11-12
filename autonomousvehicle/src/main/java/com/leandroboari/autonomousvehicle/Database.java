package com.leandroboari.autonomousvehicle;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Database {

    public static void saveCarDataToFirestore(double x, double y, double angle, String color, double maxSpeed, double minSpeed, double penalties, double speed) {
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

        // Caminho da coleção e documento
        db.collection("raceLastStates")
                .document()
                .collection("cars")
                .document()
                .set(carData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Dados salvos com sucesso
                        Log.d("Firestore", "Car data successfully saved!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Falha ao salvar os dados
                        Log.w("Firestore", "Error saving car data", e);
                    }
                });
    }

    public static void getLastCarData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("raceLastStates")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            Map<String, Object> carData = document.getData();
                            Log.d("Firestore", "Last car data: " + carData.toString());
                        } else {
                            Log.d("Firestore", "No car data found.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error fetching last car data", e);
                    }
                });
    }
}