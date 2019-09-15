package huawei.task.ViewHolder;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import huawei.task.Model.ToDoItem;
import huawei.task.R;


class MyViewHolder extends RecyclerView.ViewHolder{


    public TextView name, desc,status,deadline;

    public MyViewHolder(View itemView) {
        super(itemView);



      /*  name=(TextView)itemView.findViewById(R.id.item_name);
        desc=(TextView)itemView.findViewById(R.id.item_desc);
        status=(TextView)itemView.findViewById(R.id.item_status);
        deadline=(TextView)itemView.findViewById(R.id.item_deadline);

*/
    }
}


public class ToDoDetailAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<ToDoItem> items;
    public ToDoDetailAdapter(List<ToDoItem> items){


        this.items=items;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_detail_layout,parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ToDoItem item=items.get(position);


            holder.name.setText(String.format("Name : %s",item.getItem_name()));
            holder.status.setText(String.format("Status : %s",item.getItem_status()));
            holder.desc.setText(String.format("Description : %s",item.getItem_desc()));
            holder.deadline.setText(String.format("Deadline : %s",item.getItem_deadline()));





    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
