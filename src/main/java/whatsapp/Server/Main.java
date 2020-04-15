package whatsapp.Server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args){
        final ActorSystem system = ActorSystem.create("whatsapp", ConfigFactory.load("server"));

        try{
            final ActorRef manager= system.actorOf(ManagingServer.props(),"ManagingServer");
            System.out.println("Press Enter to shut down server!");
            System.in.read();
        }
        catch (IOException exp){
            System.out.println(exp.getMessage());
        }
        finally {
            system.terminate();
        }
    }

}
