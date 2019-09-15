package huawei.task.Model;

import java.util.List;

public class ToDoItem {

    public String item_name;
    public String item_desc;
    public String item_deadline;
    public String item_status;

    public ToDoItem(){


    }

    public ToDoItem(String item_name, String item_desc, String item_deadline, String item_status) {
        this.item_name = item_name;
        this.item_desc = item_desc;
        this.item_deadline = item_deadline;
        this.item_status = item_status;
    }


    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_desc() {
        return item_desc;
    }

    public void setItem_desc(String item_desc) {
        this.item_desc = item_desc;
    }

    public String getItem_deadline() {
        return item_deadline;
    }

    public void setItem_deadline(String item_deadline) {
        this.item_deadline = item_deadline;
    }

    public String getItem_status() {
        return item_status;
    }

    public void setItem_status(String item_status) {
        this.item_status = item_status;
    }
}

