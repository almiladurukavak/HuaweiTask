package huawei.task.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import huawei.task.Common.Common;
import huawei.task.Interface.ItemClickListener;
import huawei.task.R;

public class ToDoItemViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener ,
        View.OnCreateContextMenuListener{


    public TextView item_name,item_status,item_desc,item_deadline,todo_id,todo_name,todo_status,todo_desc,todo_deadline;

    public  Button edit;
    private ItemClickListener itemClickListener;


    public ToDoItemViewHolder(View itemView) {
        super(itemView);


        item_name=(TextView)itemView.findViewById(R.id.item_name);
        item_status=(TextView)itemView.findViewById(R.id.item_status);
        item_desc=(TextView)itemView.findViewById(R.id.item_desc);
        item_deadline=(TextView)itemView.findViewById(R.id.item_deadline);

        edit=(Button)itemView.findViewById(R.id.btnEdit);




        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {


            menu.setHeaderTitle("Select the action");

            menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
            menu.add(0, 1, getAdapterPosition(), Common.DELETE);


    }
}

