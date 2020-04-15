package whatsapp.Client;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import whatsapp.Client.clientMessages.*;
import whatsapp.duelActivites.Tasks.*;

import java.nio.file.Paths;
import java.util.Scanner;
import java.nio.file.Files;


public class Main {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("clients", ConfigFactory.load("client"));
        final ActorRef clientActor = system.actorOf(whatsapp.Client.clientActor.props(), "clientActor");//TODO:maybe fix client props


        System.out.println("Welcome to Whatsapp!");
        Scanner scanner = new Scanner(System.in);
            try{
                String input;
                while (true){
                    input = scanner.nextLine();
                    if (input.startsWith("/user")){
                        input=input.substring(input.indexOf(" ") +1);
                        toUser(clientActor, input);
                    }
                    else if (input.startsWith("/group")){
                        input=input.substring(input.indexOf(" ")+1);
                        toGroup(clientActor, input);
                    }
                    else if(input.startsWith("yes")){
                        toAcceptInvite(clientActor, input);
                    }
                    else if(input.startsWith("no")){
                        toDeclineInvite(clientActor,input);
                    }
                    else if(input.startsWith("exit"))
                        break;
                    else System.out.println("illegal command! try again.");
                }
        } finally{
                scanner.close();
                system.terminate();
            }
    }

    //----------------Methods--------------------------------------------------------------------------------------
       private static void toUser(ActorRef clientActor, String input) {
               if(input.startsWith("connect")){
                   String userName = input.substring(input.indexOf(" ") + 1); //Assuming the userName is the rest of the string
                   clientActor.tell(new askToConnect(userName), ActorRef.noSender());
                   }
           else if (input.startsWith("text")) {
                   String[] split = input.split(" ");
                   String target = split[1];
                   String message = input.substring(input.indexOf(target) + target.length() + 1);
                   clientActor.tell(new sendTextMsg(target, message), ActorRef.noSender());
               } else if (input.startsWith("file")) {
                   String[] split = input.split(" ");
                   String target = split[1];
                   String filePath = input.substring(input.indexOf(target) + target.length() + 1);
                   System.out.println(filePath);
                   byte[] file = null;
                   try {
                       file = Files.readAllBytes(Paths.get(filePath));
                   } catch (IOException e) {
                       System.out.println(filePath + "file does not exist");
                       e.printStackTrace();
                   }
                   if (file != null) {
                       clientActor.tell(new sendFileMsg(target, file), ActorRef.noSender());
                   }
               } else if (input.startsWith("disconnect")) {

                   clientActor.tell(new askToDisconnect(), ActorRef.noSender());
               } else System.out.println("illegal command! try again.");
           }
//-------------------------------------------group----------------------------------------------------------
           private static void toGroup (ActorRef clientActor, String input){
               if (input.startsWith("create")) {
                   String groupName = input.substring(input.indexOf(" ") + 1);
                   clientActor.tell(new createGroupChat(groupName), ActorRef.noSender());
               } else if (input.startsWith("leave")) {
                   String groupName = input.substring(input.indexOf(" ") + 1);
                   clientActor.tell(new leaveGroupChat(groupName), ActorRef.noSender());
               } else if (input.startsWith("send text")) {
                   String inputHandler = input.substring(input.indexOf("send text") + "send text".length() + 1);
                   String[] split = inputHandler.split(" ", 2);
                   String groupName = split[0];
                   String text = split[1];
                   clientActor.tell(new sendTextGroup(groupName, text), ActorRef.noSender());
               } else if (input.startsWith("send file")) {
                   String inputHandler = input.substring(input.indexOf("send file") + "send file".length() + 1);
                   String[] split = inputHandler.split(" ", 2);
                   String groupName = split[0];
                   String path = split[1];
                   byte[] file = null;
                   try {
                       file = Files.readAllBytes(Paths.get(path));
                   } catch (IOException e) {
                       System.out.println(path + "file does not exist");
                       e.printStackTrace();
                   }
                   if (file != null) {
                       clientActor.tell(new sendFileGroup(groupName, file), ActorRef.noSender());
                   }
               } else if (input.startsWith("user invite")) {
                   String inputHandler = input.substring(input.indexOf("user invite") + "user invite".length() + 1);
                   String[] split = inputHandler.split(" ", 2);
                   String groupName = split[0];
                   String target = split[1];
                   clientActor.tell(new userInviteGroup(groupName, target), ActorRef.noSender());
               } else if (input.startsWith("user remove")) {
                   String inputHandler = input.substring(input.indexOf("user remove") + "user remove".length() + 1);
                   String[] split = inputHandler.split(" ", 2);
                   String groupName = split[0];
                   String target = split[1];
                   clientActor.tell(new userRemoveGroup(groupName, target), ActorRef.noSender());
               } else if (input.startsWith("user mute")) {
                   String inputHandler = input.substring(input.indexOf("user mute") + "user mute".length() + 1);
                   String[] split = inputHandler.split(" ", 3);
                   String groupName = split[0];
                   String target = split[1];
                   String time = split[2];
                   clientActor.tell(new userMuteGroup(groupName, target, time), ActorRef.noSender());
               } else if (input.startsWith("user unmute")) {
                   String inputHandler = input.substring(input.indexOf("user unmute") + "user unmute".length() + 1);
                   String[] split = inputHandler.split(" ", 2);
                   String groupName = split[0];
                   String target = split[1];
                   clientActor.tell(new userUnmuteGroup(groupName, target), ActorRef.noSender());
               } else if (input.startsWith("coadmin add")) {
                   String inputHandler = input.substring(input.indexOf("coadmin add") + "coadmin add".length() + 1);
                   String[] split = inputHandler.split(" ", 2);
                   String groupName = split[0];
                   String target = split[1];
                   clientActor.tell(new coadminAddGroup(groupName, target), ActorRef.noSender());
               } else if (input.startsWith("coadmin remove")) {
                   String inputHandler = input.substring(input.indexOf("coadmin remove") + "coadmin remove".length() + 1);
                   String[] split = inputHandler.split(" ", 2);
                   String groupName = split[0];
                   String target = split[1];
                   clientActor.tell(new coadminRemoveGroup(groupName, target), ActorRef.noSender());
               } else System.out.println("illegal command! try again.");
           }
//----------------------------------group invite------------------------------------------------------------------
    private static void toAcceptInvite(ActorRef clientActor,String input){
        if(input.toLowerCase().startsWith("yes")){
            clientActor.tell(new approveOrDeclineInvite(true),ActorRef.noSender());
        }
    }
    private static void toDeclineInvite(ActorRef clientActor,String input){
        if(input.toLowerCase().startsWith("no")){
            clientActor.tell(new approveOrDeclineInvite(false),ActorRef.noSender());
        }
    }
}


