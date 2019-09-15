package huawei.task.Model;

import java.util.List;

public class ToDoList {
    private String name;

    private String description;
    private String deadline;
    private String status;

    private String userId;

    public ToDoList(){


    }

    public ToDoList(String name, String description, String deadline, String status,  String userId) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
        this.userId = userId;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}