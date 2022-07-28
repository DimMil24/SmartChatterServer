import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.example.smartchatters.logic.ProfileName;
import com.example.smartchatters.logic.Usernode;

public class User1 {

	public static void main(String args []) {
		
		Usernode user1=new Usernode(new ProfileName("antigoni","A","N"));
		user1.register("Luben");
		Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = dateFormat.format(date);

try {
	TimeUnit.SECONDS.sleep(1);
}
catch (Exception e) {
	System.out.println("Oops! Something went wrong!");
}
try {
	TimeUnit.SECONDS.sleep(1);
}
catch (Exception e) {
	System.out.println("Oops! Something went wrong!");
}
		user1.register("MixalisKaiOiAlloi");
		user1.publish("Luben","C:\\Users\\Giannis\\Desktop\\monke",strDate,"jpg");
		user1.publish("Luben", "test", strDate, null);
		user1.publish("Luben", "test2", strDate, null);
		user1.publish("Luben","C:\\Users\\Giannis\\Desktop\\thatrexei",strDate,"mkv");
		user1.publish("Luben", "tes3", strDate, null);
		user1.publish("MixalisKaiOiAlloi","C:\\Users\\Giannis\\Desktop\\monke",strDate,"jpg");
	}
	
}
