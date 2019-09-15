package huawei.task;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import huawei.task.Common.Common;
import huawei.task.Interface.ItemClickListener;
import huawei.task.Model.ToDoItem;
import huawei.task.Model.ToDoList;
import huawei.task.ViewHolder.ToDoDetailAdapter;
import huawei.task.ViewHolder.ToDoItemViewHolder;
import huawei.task.ViewHolder.ToDoListViewHolder;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class ToDoDetail extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton fab;
    //Firebase
    FirebaseDatabase db;
    DatabaseReference todolist,todo;

    List<ToDoItem> todoItems=new ArrayList<>();

    String todoId="";
    FirebaseRecyclerAdapter<ToDoItem,ToDoItemViewHolder> adapter;
    EditText edtName,edtDescription,edtDeadline;


    ToDoItem newTodo;
    MaterialSpinner spinner;

    Context context = this;

    RelativeLayout rootLayout;

    Button edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_detail_layout);

        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db=FirebaseDatabase.getInstance();
        todolist=db.getReference("ToDoList");
        recyclerView=(RecyclerView)findViewById(R.id.recycler_item);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout=(RelativeLayout)findViewById(R.id.rootLayout);



        fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddItemDialog();
            }
        });

        if(getIntent()!=null)
            todoId=getIntent().getStringExtra("todoId");
        if(!todoId.isEmpty() )
            loadListToDo(todoId);






    }


    @Override
    public void onBackPressed() {
        Intent i =new Intent(ToDoDetail.this,MainActivity.class);
        startActivity(i);
    }

    private void showAddItemDialog() {


        AlertDialog.Builder alertDialog=new AlertDialog.Builder(ToDoDetail.this);


        alertDialog.setTitle("Create New Item");
        alertDialog.setMessage("Please fill full information");


        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.add_new_item_layout,null);


        edtName=add_menu_layout.findViewById(R.id.edtName);
        edtDescription=add_menu_layout.findViewById(R.id.edtDescription);
        edtDeadline=add_menu_layout.findViewById(R.id.edtDeadline);


        edtDeadline.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Şimdiki zaman bilgilerini alıyoruz. güncel yıl, güncel ay, güncel gün.
                final Calendar takvim = Calendar.getInstance();
                int yil = takvim.get(Calendar.YEAR);
                int ay = takvim.get(Calendar.MONTH);
                int gun = takvim.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // ay değeri 0 dan başladığı için (Ocak=0, Şubat=1,..,Aralık=11)
                                // değeri 1 artırarak gösteriyoruz.
                                month += 1;
                                // year, month ve dayOfMonth değerleri seçilen tarihin değerleridir.
                                // Edittextte bu değerleri gösteriyoruz.
                                edtDeadline.setText(dayOfMonth + "/" + month + "/" + year);
                            }
                        }, yil, ay, gun);
                // datepicker açıldığında set edilecek değerleri buraya yazıyoruz.
                // şimdiki zamanı göstermesi için yukarda tanmladığımz değşkenleri kullanyoruz.

                // dialog penceresinin button bilgilerini ayarlıyoruz ve ekranda gösteriyoruz.
                dpd.setButton(DatePickerDialog.BUTTON_POSITIVE, "Seç", dpd);
                dpd.setButton(DatePickerDialog.BUTTON_NEGATIVE, "İptal", dpd);
                dpd.show();
            }
        });




        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_exposure_plus_1_black_24dp);

        //Set button

            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();

                      ToDoItem item = new ToDoItem(
                                edtName.getText().toString(),
                                edtDescription.getText().toString(),
                                edtDeadline.getText().toString(),
                                "0"

                        );
                            String list_number = String.valueOf(System.currentTimeMillis());

                            todolist.child(todoId).child("todoItems").child(list_number)
                                    .setValue(item);



                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    newTodo=null;
                    loadListToDo(todoId);
                }
            });

        alertDialog.show();

    }

    private void showUpdateStatus(String key, final ToDoItem item) {


        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(ToDoDetail.this);


        LayoutInflater inflater=this.getLayoutInflater();
        final View view =inflater.inflate(R.layout.update_todo_layout,null);
        spinner=(MaterialSpinner)view.findViewById(R.id.statusSpinner);


        spinner.setItems("Not started", "In Progress", "Complete");


        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();



        alertDialog.setView(view);
        final String localKey=key;



        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();

                Map<String,Object> update_status=new HashMap<>();
                update_status.put("item_status",String.valueOf(spinner.getSelectedIndex()));

                FirebaseDatabase.getInstance()
                        .getReference("ToDoList")
                        .child(todoId)
                        .child("todoItems")
                        .child(localKey)
                        .updateChildren(update_status)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();

                                if(task.isSuccessful()){

                                    Toast.makeText(ToDoDetail.this, "Status updated", Toast.LENGTH_SHORT).show();



                                }
                            }
                        });


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alertDialog.show();


    }

    private void loadListToDo(String todoId) {

        Query getToDoByUser= todolist.child(todoId).child("todoItems");


        FirebaseRecyclerOptions<ToDoItem> options=new FirebaseRecyclerOptions.Builder<ToDoItem>()
                .setQuery(getToDoByUser,ToDoItem.class)
                .build();



        adapter=new FirebaseRecyclerAdapter<ToDoItem, ToDoItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ToDoItemViewHolder viewHolder, final int position, @NonNull ToDoItem model) {
                viewHolder.item_name.setText(model.getItem_name());
              //  viewHolder.item_status.setText(adapter.getRef(position).getKey());
                viewHolder.item_status.setText(Common.convertCodeToStatus(model.getItem_status()));
                viewHolder.item_desc.setText(model.getItem_desc());
                viewHolder.item_deadline.setText(model.getItem_deadline());

                viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUpdateStatus(adapter.getRef(position).getKey(), adapter.getItem(position));
                    }
                });


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {


                    }
                });

            }

            @NonNull
            @Override
            public ToDoItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View itemView=LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.todoitem_item,viewGroup,false);

                return new ToDoItemViewHolder(itemView);

            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
       // adapter.stopListening();
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {




            if (item.getTitle().equals(Common.UPDATE)) {

                showUpdateItemDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));


            } else if (item.getTitle().equals(Common.DELETE)) {

                deleteItem(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));


            }





        return super.onContextItemSelected(item);
    }



    private void deleteItem(String key, final ToDoItem item) {
        final String localKey=key;


        todolist.child(todoId).child("todoItems").child(localKey).removeValue();


    }

    private void showUpdateItemDialog(final String key, final ToDoItem item) {


        AlertDialog.Builder alertDialog=new AlertDialog.Builder(ToDoDetail.this);


            alertDialog.setTitle("Update Item");
            alertDialog.setMessage("Please fill full information");


        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.update_item,null);


        edtName=add_menu_layout.findViewById(R.id.edtName);
        edtDescription=add_menu_layout.findViewById(R.id.edtDescription);
        edtDeadline=add_menu_layout.findViewById(R.id.edtDeadline);
        edtName.setText(item.getItem_name());
        edtDescription.setText(item.getItem_desc());
        edtDeadline.setText(item.item_deadline);


        edtDeadline.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Şimdiki zaman bilgilerini alıyoruz. güncel yıl, güncel ay, güncel gün.
                final Calendar takvim = Calendar.getInstance();
                int yil = takvim.get(Calendar.YEAR);
                int ay = takvim.get(Calendar.MONTH);
                int gun = takvim.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // ay değeri 0 dan başladığı için (Ocak=0, Şubat=1,..,Aralık=11)
                                // değeri 1 artırarak gösteriyoruz.
                                month += 1;
                                // year, month ve dayOfMonth değerleri seçilen tarihin değerleridir.
                                // Edittextte bu değerleri gösteriyoruz.
                                edtDeadline.setText(dayOfMonth + "/" + month + "/" + year);
                            }
                        }, yil, ay, gun);
                // datepicker açıldığında set edilecek değerleri buraya yazıyoruz.
                // şimdiki zamanı göstermesi için yukarda tanmladığımz değşkenleri kullanyoruz.

                // dialog penceresinin button bilgilerini ayarlıyoruz ve ekranda gösteriyoruz.
                dpd.setButton(DatePickerDialog.BUTTON_POSITIVE, "Seç", dpd);
                dpd.setButton(DatePickerDialog.BUTTON_NEGATIVE, "İptal", dpd);
                dpd.show();
            }
        });




        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_exposure_plus_1_black_24dp);


            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {

                    Map<String,Object> update_status=new HashMap<>();
                    update_status.put("item_deadline",edtDeadline.getText().toString());

                    update_status.put("item_name",edtName.getText().toString());
                    update_status.put("item_description",edtDescription.getText().toString());


                    FirebaseDatabase.getInstance()
                            .getReference("ToDoList")
                            .child(todoId)
                            .child("todoItems")
                            .child(key)
                            .updateChildren(update_status)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dialog.dismiss();

                                    if(task.isSuccessful()){

                                        Toast.makeText(ToDoDetail.this, "ToDo Item was updated", Toast.LENGTH_SHORT).show();



                                    }
                                }
                            });

                    Snackbar.make(rootLayout, "Item  " + item.getItem_name() + " was updated", Snackbar.LENGTH_SHORT).show();

                    loadListToDo(todoId);

                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    loadListToDo(todoId);
                }
            });

        alertDialog.show();


    }

}
