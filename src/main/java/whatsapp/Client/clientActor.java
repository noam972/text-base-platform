package whatsapp.Client;

import akka.actor.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import whatsapp.Client.clientMessages.*;
import whatsapp.duelActivites.Tasks.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static whatsapp.Client.clientMessages.timeSent;


public class clientActor extends AbstractActor {
    String userName=null;
    userWasInvited msgRecived=null;


    ActorSelection managingServer=getContext().actorSelection("akka://whatsapp@127.0.0.1:3553/user/ManagingServer");
    final static Timeout timeout = new Timeout(Duration.create(3, TimeUnit.SECONDS));

    public static Props props(){ //creates the actor itself.
        return Props.create(clientActor.class, clientActor::new);
    }

    public void Log(String message) {
        System.out.println(message);
    }

    @Override
        public Receive createReceive() {
            return receiveBuilder().match(askToConnect.class,x->connectNewClient(x.userName))
                    .match(askToDisconnect.class, x->disconnectClient(this.userName))
                    .match(sendTextMsg.class,x->clientSendText(x.target,x.text))
                    .match(getTextMsg.class,x->clientGotText(x.source,x.text))
                    .match(sendFileMsg.class,x->clientSendFile(x.target,x.file))
                    .match(getFileMsg.class,x->clientGotFile(x.source,x.file))
                    .match(createGroupChat.class,x->clientCreateGroup(x.group_name))
                    .match(requestRes.class,x->handlResult(x.isSuccess(),x.getMessage()))
                    .match(userInviteGroup.class,x->handleGroupInvite(x.group_name,x.target))
                    .match(UserInviteGroupTask.class,x->clientWasInvited(x.getGroupName()))
                    .match(approveOrDeclineInvite.class,x->answerToInvite(x.getAnswer()))
                    .match(clientAcceptInvite.class,x->askServerToAddClientToGroup(x.getGroup_name(),x.getClientName()))
                    .match(sendTextGroup.class,x->clientSendTextGroup(x.target_group_name,x.text))
                    .match(sendFileGroup.class,x->clientSendfileGroup(x.group_name,x.file))
                    .match(sendTextGroupTask.class,x->clientGotTextFromGroup(x.getGroupName(),x.getText(),x.getuserName()))
                    .match(sendFileGroupTask.class,x->clientGotFileFromGroup(x.getGroupName(),x.getFile()))
                    .match(userRemoveGroup.class,x->clientRemoveFromGroup(x.group_name,x.target))
                    .match(coadminAddGroup.class,x->askToAddCoAdmin(x.group_name,x.target))
                    .match(coadminRemoveGroup.class, x-> askToRemoveCoAdmin(x.group_name, x.target))
                    .match(userMuteGroup.class,x->clientAskToMute(x.group_name,x.target,x.howMuchTime))
                    .match(userUnmuteGroup.class, x->clientAskToUnmute(x.group_name, x.target))
                    .match(leaveGroupChat.class,x->userAskToLeaveGroup(x.group_name))
                    .build();
        }


        private void connectNewClient(String userName) {
            if (this.userName == null) { //this actor is already connected under some name;
                Future<Object> future = Patterns.ask(managingServer, new ConnectUser(userName, getSelf()), timeout);
                try {
                    Object result = Await.result(future, timeout.duration());
                    if (result instanceof requestRes) {
                        if (((requestRes) result).isSuccess()) {
                            this.userName = userName;
                        }
                        Log(((requestRes) result).getMessage());
                    } else {
                        Log("server is offline");
                    }
                } catch (Exception e) {
                    Log("server is offline");
                }
            }
            else Log("this client is already connected ");
        }


        private void clientSendText(String target,String text){
            ActorRef targetActor=getActorFromServer(target);
            if(targetActor!=null) {
                targetActor.tell(new getTextMsg(userName, text), ActorRef.noSender());
            }

        }

        private void clientGotText(String source,String msg){
            String str=timeSent()+"[user]"+'['+source+']'+msg;
            Log(str);
        }

        private void clientSendFile(String target,byte[] file){
            ActorRef targetActor=getActorFromServer(target);
            if(targetActor!=null){
                targetActor.tell(new getFileMsg(userName,file),ActorRef.noSender());
            }
        }

        private void clientGotFile(String source, byte[] file){
        try {
            Path p = Paths.get("received-files");
            Files.write(p,file);
            Log(timeSent()+"[user]"+'['+source+']'+"File recived:"+p);
        } catch (Exception e) {
           Log("file path not valid! try to send it again.");
         }
        }


        private void clientCreateGroup(String groupName){
            managingServer.tell(new CreateGroupChatTask(groupName,this.userName),getSelf());
        }


        private void handlResult(boolean res,String msg){
            Log(msg);
        }


        private void handleGroupInvite(String groupName,String target){
            ActorRef targetRef=getActorFromServer(target);
            managingServer.tell(new UserInviteGroupTask(groupName,this.userName,target,targetRef),getSelf());
        }

        private void clientWasInvited(String groupName){
           Log(String.format("You have been invited to %s, Accept?",groupName));
            msgRecived=new userWasInvited(getSender(),groupName);

        }


        private void answerToInvite(boolean ans){
            if (msgRecived==null){
                return;
            }
            else if(ans){
                getSelf().tell(new clientAcceptInvite(msgRecived.toWhichGroup,userName),getSelf());
                msgRecived=null; //reset
            }
            else if(!ans){
                msgRecived=null;
                return;
            }
        }

        private void askServerToAddClientToGroup(String groupName,String clientToAdd){
            managingServer.tell(new clientAcceptInvite(groupName,clientToAdd),getSelf());
            Log(String.format("Welcome to %s",groupName));
        }

        private void clientSendTextGroup(String targetGroup,String text){
            managingServer.tell(new sendTextGroupTask(targetGroup,text,this.userName),getSelf());
        }

        private void clientSendfileGroup(String targetGroup,byte [] file){
            managingServer.tell(new sendFileGroupTask(targetGroup,file,this.userName),getSelf());
        }

        private void clientGotTextFromGroup(String groupName,String text,String source){
            Log(timeSent()+'['+groupName+']'+'['+source+']'+text);
        }

        private void clientGotFileFromGroup(String targetGroup,byte [] file){
                try {
                    Path p = Paths.get("received-files");
                    Files.write(p,file);
                    Log(timeSent()+"File recived:" + p);
                } catch (Exception e) {
                    Log("file path not valid! try to send it again.");
                }

        }

        private void clientRemoveFromGroup(String groupName,String target){
        ActorRef targetRef=getActorFromServer(target);
            managingServer.tell(new UserRemoveGroupTask(groupName,target,this.userName,targetRef),getSelf());
        }

        private void askToAddCoAdmin(String groupName,String target){
            ActorRef targetToAdd=getActorFromServer(target);
            managingServer.tell(new CoadminAddGroupTask(groupName,target,this.userName,targetToAdd),getSelf());
        }

        private void askToRemoveCoAdmin(String groupName,String target){
            ActorRef targetToRemove=getActorFromServer(target);
            managingServer.tell(new CoadminRemoveGroupTask(groupName,target,this.userName,targetToRemove),getSelf());
        }

        private void clientAskToMute(String groupName,String target,String howMuchTime){
            ActorRef targetToMute=getActorFromServer(target);
            managingServer.tell(new UserMuteGroupTask(groupName,target,this.userName,targetToMute,howMuchTime),getSelf());
        }

        private void clientAskToUnmute(String groupName,String target){
            ActorRef targetToUnmute=getActorFromServer(target);
            managingServer.tell(new UserUnmuteGroupTask(groupName,target,this.userName,targetToUnmute),getSelf());
        }

        private void userAskToLeaveGroup(String groupName){
            managingServer.tell(new LeaveGroupChatTask(groupName,this.userName),getSelf());
        }


        public ActorRef getActorFromServer(String userName){
        ActorRef actor=null;
        Future<Object> future=Patterns.ask(managingServer, new getActorFromServer(userName),timeout);
            try {
                Object res = Await.result(future, timeout.duration());
                if (res instanceof requestRes) {
                    Log(((requestRes) res).getMessage());
                }
                else actor=(ActorRef) res;
            } catch (Exception e) {
               Log("server is offline");
            }
            if(actor==ActorRef.noSender()){
                actor= null;
            }
            return actor;
        }

        public void disconnectClient(String target){
            ActorRef targetToDisconnect= getActorFromServer(target);
            managingServer.tell(new disconnectUserTask(this.userName, targetToDisconnect), getSelf());
        }
}


