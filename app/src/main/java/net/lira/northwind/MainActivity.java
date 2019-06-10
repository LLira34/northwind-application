package net.lira.northwind;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import net.lira.northwind.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private List<Product> listProducts = new ArrayList<Product>();
    ArrayAdapter<Product> productArrayAdapter;
    EditText txtProductName, txtQuantityPerUnit, txtUnitPrice;
    ListView lsvProducts;
    FirebaseDatabase fd;
    DatabaseReference dr;
    Product productSelected;
    MaterialBetterSpinner spnSuppliers, spnCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Relationships with the layout
        txtProductName = findViewById(R.id.txtProductName);
        txtQuantityPerUnit = findViewById(R.id.txtQuantityPerUnit);
        txtUnitPrice = findViewById(R.id.txtUnitPrice);
        lsvProducts = findViewById(R.id.lsvProducts);

        spnSuppliers = findViewById(R.id.spnSuppliers);
        spnCategories = findViewById(R.id.spnCategories);

        //Combo
        ArrayAdapter<CharSequence> arrayAdapterSupp = ArrayAdapter.createFromResource(this,
                R.array.cbmSuppliers, android.R.layout.simple_dropdown_item_1line);
        spnSuppliers.setAdapter(arrayAdapterSupp);
        ArrayAdapter<CharSequence> arrayAdapterCate = ArrayAdapter.createFromResource(this,
                R.array.cbmCategories, android.R.layout.simple_dropdown_item_1line);
        spnCategories.setAdapter(arrayAdapterCate);

        //Starting firebase
        startFirebase();
        listData();
        lsvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                productSelected = (Product) parent.getItemAtPosition(position);
                txtProductName.setText(productSelected.getProductName());
                spnSuppliers.setText(productSelected.getSupplierID());
                spnCategories.setText(productSelected.getCategoryID());
                txtQuantityPerUnit.setText(productSelected.getQuantityPerUnit());
                txtUnitPrice.setText(String.valueOf(productSelected.getUnitPrice()));
            }
        });
    }

    private void listData() {
        dr.child("Product").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listProducts.clear();
                for (DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                    Product p = objSnapshot.getValue(Product.class);
                    listProducts.add(p);
                    productArrayAdapter = new ArrayAdapter<Product>(MainActivity.this,
                            android.R.layout.simple_list_item_1, listProducts);
                    lsvProducts.setAdapter(productArrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void startFirebase() {
        FirebaseApp.initializeApp(this);
        fd = FirebaseDatabase.getInstance();
        dr = fd.getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Variables for validation
        String productName = txtProductName.getText().toString();
        String supplierID = spnSuppliers.getText().toString();
        String categoryID = spnCategories.getText().toString();
        String quantityPerUnit = txtQuantityPerUnit.getText().toString();
        String unitPrice = txtUnitPrice.getText().toString();

        switch (item.getItemId()) {
            case R.id.icon_save: {
                if (productName.equals("") || supplierID.equals("") || categoryID.equals("") ||
                        quantityPerUnit.equals("") || unitPrice.equals("")) {
                    validation();
                } else {
                    Product p = new Product();
                    p.setProductID(UUID.randomUUID().toString());
                    p.setProductName(productName);
                    p.setSupplierID(supplierID);
                    p.setCategoryID(categoryID);
                    p.setQuantityPerUnit(quantityPerUnit);
                    p.setUnitPrice(Double.parseDouble(unitPrice));
                    dr.child("Product").child(p.getProductID()).setValue(p);
                    Toast.makeText(this, "Saving...", Toast.LENGTH_LONG).show();
                    clean();
                }
                break;
            }
            case R.id.icon_update: {
                if (productName.equals("") || supplierID.equals("") || categoryID.equals("") ||
                        quantityPerUnit.equals("") || unitPrice.equals("")) {
                    Toast.makeText(this, "Selecciona un producto para actualizar", Toast.LENGTH_LONG).show();
                } else {
                    Product p = new Product();
                    p.setProductID(productSelected.getProductID());
                    p.setProductName(txtProductName.getText().toString().trim());
                    p.setSupplierID(spnSuppliers.getText().toString().trim());
                    p.setCategoryID(spnCategories.getText().toString().trim());
                    p.setQuantityPerUnit(txtQuantityPerUnit.getText().toString().trim());
                    p.setUnitPrice(Double.parseDouble(txtUnitPrice.getText().toString().trim()));
                    dr.child("Product").child(p.getProductID()).setValue(p);
                    Toast.makeText(this, "Updating...", Toast.LENGTH_LONG).show();
                    clean();
                }
                break;
            }
            case R.id.icon_delete: {
                if (productName.equals("") || supplierID.equals("") || categoryID.equals("") ||
                        quantityPerUnit.equals("") || unitPrice.equals("")) {
                    Toast.makeText(this, "Selecciona un producto para eliminar", Toast.LENGTH_LONG).show();
                } else {
                    Product p = new Product();
                    p.setProductID(productSelected.getProductID());
                    dr.child("Product").child(p.getProductID()).removeValue();
                    Toast.makeText(this, "Deleting...", Toast.LENGTH_LONG).show();
                    clean();
                }
                break;
            }
            case R.id.icon_cancel: {
                Toast.makeText(this, "Canceled...", Toast.LENGTH_LONG).show();
                clean();
                break;
            }
            default:
                break;
        }
        return true;
    }

    public void clean() {
        txtProductName.setText("");
        spnSuppliers.setText("");
        spnCategories.setText("");
        txtQuantityPerUnit.setText("");
        txtUnitPrice.setText("");
    }

    public void validation() {
        String productName = txtProductName.getText().toString();
        String supplierID = spnSuppliers.getText().toString();
        String categoryID = spnCategories.getText().toString();
        String quantityPerUnit = txtQuantityPerUnit.getText().toString();
        String unitPrice = txtUnitPrice.getText().toString();

        if (productName.equals("")) {
            txtProductName.setError("Escriba un nombre");
        } else if (supplierID.equals("")) {
            spnSuppliers.setError("Selecciona un proveedor");
        } else if (categoryID.equals("")) {
            spnCategories.setError("Selecciona una categoria");
        } else if (quantityPerUnit.equals("")) {
            txtQuantityPerUnit.setError("Escriba la cantidad por unidad");
        } else if (unitPrice.equals("")) {
            txtUnitPrice.setError("Escriba un precio");
        } else {
            System.out.println("Error aqui de Firebase culero!");
        }
    }

}//End class
