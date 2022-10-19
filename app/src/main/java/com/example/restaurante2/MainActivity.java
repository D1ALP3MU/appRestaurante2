package com.example.restaurante2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Instanciar Firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String idCustomer; // Variable que contendrá el id de cada cliente

    EditText ident, fullname, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Instanciar  referenciar los IDs del archivo xml
        ident = (EditText) findViewById(R.id.etIdent);
        fullname = (EditText) findViewById(R.id.etfullname);
        email = (EditText) findViewById(R.id.etemail);
        password = (EditText) findViewById(R.id.etpassword);
        Button btnsave = findViewById(R.id.btnsave);
        Button btnsearch = findViewById(R.id.btnsearch);
        Button btnedit = findViewById(R.id.btnedit);
        Button btndelete = findViewById(R.id.btndelete);

        btnsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchCustomer(ident.getText().toString());
            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomer(ident.getText().toString(), fullname.getText().toString(), email.getText().toString(), password.getText().toString());
            }
        });

        btnedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarCustomer(ident.getText().toString(), fullname.getText().toString(), email.getText().toString(), password.getText().toString());
            }
        });
        
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirmación de borrado
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("¿ Está seguro de eliminar el cliente con Id: " + ident.getText().toString() + " ?");
                alertDialogBuilder.setPositiveButton("Sí",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // Se eliminará el cliente con el id respectivo
                            db.collection("customer").document(idCustomer)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this,"Cliente borrado correctamente...",Toast.LENGTH_SHORT).show();

                                            //Limpiar las cajas de texto
                                            ident.setText("");
                                            fullname.setText("");
                                            email.setText("");
                                            password.setText("");
                                            ident.requestFocus(); //Enviar el foco al ident
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                alertDialogBuilder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

    }

    private void editarCustomer(String sident, String sfuillname, String semail, String spassword) {
        Map<String, Object> mcustomer = new HashMap<>();
        mcustomer.put("Ident", sident);
        mcustomer.put("Fullname", sfuillname);
        mcustomer.put("Email", semail);
        mcustomer.put("Password", spassword);

        db.collection("customer").document(idCustomer)
                .set(mcustomer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this,"Cliente actualizado correctmente...",Toast.LENGTH_SHORT).show();

                        // Vaciar las cajas de texto
                        ident.setText("");
                        fullname.setText("");
                        email.setText("");
                        password.setText("");
                        ident.requestFocus(); //Enviar el foco al ident
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchCustomer(String sident) {
        db.collection("customer")
                .whereEqualTo("Ident", sident)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) { // Si encontró el documento
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    idCustomer = document.getId();
                                    Toast.makeText(getApplicationContext(),"ID customer: " + idCustomer, Toast.LENGTH_LONG).show();
                                    fullname.setText(document.getString("Fullname"));
                                    email.setText(document.getString("Email"));
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"El Id del cliente no existe...",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void saveCustomer(String sident, String sfullname, String semail, String spassword) {
        // Buscar la id entificación del cliente nuevo
        db.collection("customer")
                .whereEqualTo("Ident", sident)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) { // Si no encuentra el documento
                                // Guardar los datos del cliente (customer)
                                Map<String, Object> customer = new HashMap<>(); // Tabla cursor
                                customer.put("Ident", sident);
                                customer.put("Fullname", sfullname);
                                customer.put("Email", semail);
                                customer.put("Password", spassword);

                                db.collection("customer")
                                        .add(customer)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(getApplicationContext(), "Cliente agregado con éxito...", Toast.LENGTH_SHORT).show();

                                                //Limpiar las cajas de texto
                                                ident.setText("");
                                                fullname.setText("");
                                                email.setText("");
                                                password.setText("");
                                                ident.requestFocus(); //Enviar el foco al ident

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Error! el cliente no se agregó...", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"El Id del cliente ya existe, inténtelo con otro",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });


    }
}