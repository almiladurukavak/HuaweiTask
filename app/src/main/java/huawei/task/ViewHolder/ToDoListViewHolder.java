package huawei.task.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import huawei.task.R;

public class ToDoListViewHolder extends RecyclerView.ViewHolder {

    public TextView txtToDoId,txtToDoStatus,txtToDoDescription,txtToDoDeadLine,txtToDoName;

    public Button btnEdit,btnRemove,btnDetail;
    public LinearLayout button_layout;


    public ToDoListViewHolder(View itemView) {
        super(itemView);


        txtToDoId=(TextView)itemView.findViewById(R.id.txtToDoId);
        txtToDoStatus=(TextView)itemView.findViewById(R.id.txtToDoStatus);
        txtToDoDescription=(TextView)itemView.findViewById(R.id.txtToDoDescription);
        txtToDoDeadLine=(TextView)itemView.findViewById(R.id.txtToDoDeadLine);
        txtToDoName=(TextView)itemView.findViewById(R.id.txtToDoName);


        btnEdit=(Button) itemView.findViewById(R.id.btnEdit);
        btnDetail=(Button)itemView.findViewById(R.id.btnDetail);
        btnRemove=(Button)itemView.findViewById(R.id.btnRemove);
        button_layout=(LinearLayout)itemView.findViewById(R.id.button_layout);


    }





}
