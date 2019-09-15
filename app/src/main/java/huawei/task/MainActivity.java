package huawei.task;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huawei.task.Common.Common;
import huawei.task.Interface.ItemClickListener;
import huawei.task.Model.ToDoList;
import huawei.task.ViewHolder.ToDoListViewHolder;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<ToDoList, ToDoListViewHolder> adapter;
    FirebaseRecyclerAdapter<ToDoList, ToDoListViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBarName;

    MaterialSearchBar materialSearchBarStatus;


    LinearLayout button_layout;

    TextView statusText;

    FirebaseDatabase db;
    DatabaseReference todo;

    ToDoList newToDo;

    MaterialSpinner spinner;
    EditText edtName, edtDescription, edtDeadline;

    FloatingActionButton fab;

    Context context = this;
    RelativeLayout rootLayout;


    private static final int WRITE_REQUEST_CODE = 300;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String url;
    Button download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        download=(Button)findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckForSDCard.isSDCardPresent()) {

                    //check if app has permission to write to the external storage.
                    if (EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Get the URL entered
                        url = "https://taskhuawei.firebaseio.com/ToDoList";

                        new DownloadFile().execute(url);

                    } else {
                        //If permission is not present request for the same.
                        EasyPermissions.requestPermissions(MainActivity.this, getString(R.string.write_file), WRITE_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
                    }


                } else {
                    Toast.makeText(getApplicationContext(),
                            "SD Card not found", Toast.LENGTH_LONG).show();


                }
            }
        });


        db = FirebaseDatabase.getInstance();
        todo = db.getReference("ToDoList");

        recyclerView = (RecyclerView) findViewById(R.id.listToDo);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddToDoDialog();
            }
        });

        loadToDoList();

        materialSearchBarName = (MaterialSearchBar) findViewById(R.id.materialSearchBarName);
        // materialSearchBar.setSpeechMode(false);

        loadSuggest();

        materialSearchBarName.setCardViewElevation(10);
        materialSearchBarName.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest = new ArrayList<String>();
                for (String search : suggestList) {

                    if (search.toLowerCase().contains(materialSearchBarName.getText().toLowerCase()))
                        suggest.add(search);

                }
                materialSearchBarName.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBarName.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.sign_out_button:

                FirebaseAuth.getInstance().signOut();
                Intent i =new Intent(MainActivity.this,LoginActivity.class);
                startActivity(i);
                break;

        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MainActivity.this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //Download the file once permission is granted
        url = "https://taskhuawei.firebaseio.com/ToDoList";
        new DownloadFile().execute(url);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "Permission has been denied");
    }

    /**
     * Async Task to download file from URL
     */
    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(MainActivity.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());

                //Append timestamp to file name
                fileName = timestamp + "_" + fileName;

                //External directory path to save file
                folder = Environment.getExternalStorageDirectory() + File.separator + "androiddeft/";

                //Create androiddeft folder if it does not exist
                File directory = new File(folder);

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return "Downloaded at: " + folder + fileName;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }

    private void loadSuggest() {
        final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        todo.orderByChild("name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                            ToDoList item = postSnapshot.getValue(ToDoList.class);
                            suggestList.add(item.getName());

                        }
                        materialSearchBarName.setLastSuggestions(suggestList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void startSearch(CharSequence text) {

        Query searchByName = todo.orderByChild("name").equalTo(text.toString());

        FirebaseRecyclerOptions<ToDoList> searchOptions = new FirebaseRecyclerOptions.Builder<ToDoList>()
                .setQuery(searchByName, ToDoList.class)
                .build();


        searchAdapter = new FirebaseRecyclerAdapter<ToDoList, ToDoListViewHolder>(searchOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ToDoListViewHolder viewHolder, final int position, @NonNull ToDoList model) {


                viewHolder.txtToDoId.setText(adapter.getRef(position).getKey());
                viewHolder.txtToDoStatus.setText(Common.convertCodeToStatus(model.getStatus()));

                viewHolder.txtToDoDescription.setText(model.getDescription());
                viewHolder.txtToDoDeadLine.setText(model.getDeadline());
                viewHolder.txtToDoName.setText(model.getName());

                final ToDoList local = model;
                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent todoDetail = new Intent(MainActivity.this, ToDoDetail.class);
                        todoDetail.putExtra("todoId", adapter.getRef(position).getKey());
                        startActivity(todoDetail);

                    }
                });
            }

            @NonNull
            @Override
            public ToDoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_layout, parent, false);

                return new ToDoListViewHolder(itemView);

            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);

    }

    private void showAddToDoDialog() {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);


        alertDialog.setTitle("Create To-Do List");
        alertDialog.setMessage("Please fill full information");


        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_item_layout, null);


        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtDescription = add_menu_layout.findViewById(R.id.edtDescription);
        edtDeadline = add_menu_layout.findViewById(R.id.edtDeadline);


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
        final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                ToDoList item = new ToDoList(
                        edtName.getText().toString(),
                        edtDescription.getText().toString(),
                        edtDeadline.getText().toString(),
                        "0",
                        currentuser
                );

                String order_number = String.valueOf(System.currentTimeMillis());

                todo.child(order_number)
                        .setValue(item);


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                newToDo = null;
                loadToDoList();
            }
        });

        alertDialog.show();

    }


    private void loadToDoList() {
        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();


        Query getToDoByUser = todo.orderByChild("userId")
                .equalTo(currentuser);

        FirebaseRecyclerOptions<ToDoList> options = new FirebaseRecyclerOptions.Builder<ToDoList>()
                .setQuery(getToDoByUser, ToDoList.class)
                .build();


        adapter = new FirebaseRecyclerAdapter<ToDoList, ToDoListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ToDoListViewHolder viewHolder, final int position,
                                            @NonNull final ToDoList model) {

                viewHolder.txtToDoId.setText(adapter.getRef(position).getKey());
                viewHolder.txtToDoStatus.setText(Common.convertCodeToStatus(model.getStatus()));

                viewHolder.txtToDoDescription.setText(model.getDescription());
                viewHolder.txtToDoDeadLine.setText(model.getDeadline());
                viewHolder.txtToDoName.setText(model.getName());

                viewHolder.txtToDoStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUpdateStatus(adapter.getRef(position).getKey(), adapter.getItem(position));
                    }
                });
                viewHolder.button_layout.setVisibility(View.VISIBLE);
                viewHolder.btnEdit.setWidth(0);
                viewHolder.btnRemove.setWidth(0);
                viewHolder.btnDetail.setWidth(0);


                android.widget.LinearLayout.LayoutParams params1 = new android.widget.LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);


                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showUpdateDialog(adapter.getRef(position).getKey(), adapter.getItem(position));

                    }
                });

                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteToDo(adapter.getRef(position).getKey());

                    }
                });
                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent todoDetail = new Intent(MainActivity.this, ToDoDetail.class);
                        todoDetail.putExtra("todoId", adapter.getRef(position).getKey());
                        startActivity(todoDetail);

                    }
                });


            }

            @NonNull
            @Override
            public ToDoListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.todo_layout, viewGroup, false);

                return new ToDoListViewHolder(itemView);
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

    private void deleteToDo(String key) {

        todo.child(key).removeValue();
        adapter.notifyDataSetChanged();


    }


    private void showUpdateStatus(String key, final ToDoList item) {


        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);


        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_todo_layout, null);
        spinner = (MaterialSpinner) view.findViewById(R.id.statusSpinner);


        spinner.setItems("Not started", "In Progress", "Complete");


        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();


        alertDialog.setView(view);
        final String localKey = key;


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {


                Map<String, Object> update_name = new HashMap<>();
                update_name.put("status", String.valueOf(spinner.getSelectedIndex()));

                FirebaseDatabase.getInstance()
                        .getReference("ToDoList")
                        .child(localKey)
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();

                                if (task.isSuccessful()) {

                                    Toast.makeText(MainActivity.this, "Status Updated", Toast.LENGTH_SHORT).show();


                                }
                            }
                        });

                adapter.notifyDataSetChanged();


//                Snackbar.make(rootLayout, "Item  " + item.getName() + " was updated", Snackbar.LENGTH_SHORT).show();

                loadToDoList();

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

    private void showUpdateDialog(final String key, final ToDoList item) {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);


        alertDialog.setTitle("Edit To-Do List");
        alertDialog.setMessage("Please fill full information");


        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.update_item, null);


        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtDescription = add_menu_layout.findViewById(R.id.edtDescription);
        edtDeadline = add_menu_layout.findViewById(R.id.edtDeadline);
        edtName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        edtDeadline.setText(item.getDeadline());


        edtDeadline.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Todays year, month, day value.
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // months starts from 0
                                // add 1 to months value
                                month += 1;
                                // selected days of year, month and dayOfMonth values
                                // set values to edittext
                                edtDeadline.setText(dayOfMonth + "/" + month + "/" + year);
                            }
                        }, year, month, day);
                // when datepicker open, show todays calendar values

                // set buttons
                dpd.setButton(DatePickerDialog.BUTTON_POSITIVE, "Select", dpd);
                dpd.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", dpd);
                dpd.show();
            }
        });


        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_exposure_plus_1_black_24dp);


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                Map<String, Object> update_status = new HashMap<>();
                update_status.put("deadline", edtDeadline.getText().toString());

                update_status.put("name", edtName.getText().toString());
                update_status.put("description", edtDescription.getText().toString());


                FirebaseDatabase.getInstance()
                        .getReference("ToDoList")
                        .child(key)
                        .updateChildren(update_status)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();

                                if (task.isSuccessful()) {

                                    Toast.makeText(MainActivity.this, "ToDo List updated", Toast.LENGTH_SHORT).show();


                                }
                            }
                        });

                loadToDoList();

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                loadToDoList();
            }
        });

        alertDialog.show();


    }


}







