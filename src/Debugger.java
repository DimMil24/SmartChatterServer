import com.example.smartchatters.logic.ProfileName;
import com.example.smartchatters.logic.Usernode;

public class Debugger {
	
	public static void main(String [] args) {
		
		Usernode usernode=new Usernode(new ProfileName("daddy","A","N"));
        usernode.register("PtyxioSta4");
        Usernode usernode2=new Usernode(new ProfileName("mummy","A","N"));
        usernode2.register("PtyxioSta4");
        Usernode usernode3=new Usernode(new ProfileName("mhlios","A","N"));
        usernode3.register("PtyxioSta4");
        Usernode usernode4=new Usernode(new ProfileName("mparous","A","N"));
        usernode4.register("PtyxioSta4");
        Usernode usernode5=new Usernode(new ProfileName("mpampis","A","N"));
        usernode5.register("Luben");
        Usernode usernode6=new Usernode(new ProfileName("giwrgos","A","N"));
        usernode6.register("Luben");
        usernode6.publish("Luben","C:\\Users\\Giannis\\Videos\\chiquita","23/4/2022","mkv");
        usernode5.publish("Luben","C:\\Users\\Giannis\\Videos\\copy","23/4/2022","mkv");
        Usernode usernode7=new Usernode(new ProfileName("spiros","A","N"));
        usernode7.register("Luben");
        Usernode usernode8=new Usernode(new ProfileName("antigoni","A","N"));
        usernode8.register("Luben");
        usernode.publish("PtyxioSta4","C:\\Users\\Giannis\\Videos\\copy","23/4/2022","mkv");
        usernode7.publish("Luben","C:\\Users\\Giannis\\Videos\\m","23/4/2022","jpg");
		
	}
	
}
