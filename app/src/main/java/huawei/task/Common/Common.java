package huawei.task.Common;


import android.location.Location;


import java.util.Locale;

import huawei.task.Model.ToDoList;

public class Common {


        public static ToDoList currentToDo;
        public static final String UPDATE="Update";
        public static final String DELETE="Delete";
        public static final String MARK="Mark";


    public static String convertCodeToStatus(String code){
            if (code.equals("0")) {

                    return "Not started";

            }else if(code.equals("1")){

                    return "In Progress";

            }else {

                    return "Complete";

            }



        }




}
