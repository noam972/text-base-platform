package whatsapp.Server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import java.util.Map;
import whatsapp.duelActivites.Tasks.*;

import java.util.HashMap;

public class ManagingServer extends AbstractActor {
    private HashMap<String, ActorRef> users;
    private HashMap<String, ActorRef> groups;

    public ManagingServer() {
        this.users = new HashMap<String, ActorRef>();
        this.groups = new HashMap<String, ActorRef>();
    }

    public static Props props(){ //creates the actor itself.
        return Props.create(ManagingServer.class,ManagingServer::new);
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ConnectUser.class, this::handleConnect)
                .match(disconnectUserTask.class, this::clientAskToDisconnect)
                .match(getActorFromServer.class,this::isTargetConnected)
                .match(CreateGroupChatTask.class,this::createNewGroup)
                .match(UserInviteGroupTask.class,this::inviteUserToGroup)
                .match(clientAcceptInvite.class,this::addClientToGroup)
                .match(sendTextGroupTask.class,this::clientSendTextToGroup)
                .match(sendFileGroupTask.class,this::clientSendFileToGroup)
                .match(UserRemoveGroupTask.class,this::removeFromGroup)
                .match(CoadminRemoveGroupTask.class, this::removeCoAdmin)
                .match(CoadminAddGroupTask.class,this::addCoAdmin)
                .match(UserUnmuteGroupTask.class, this::clientAskToUnmute)
                .match(UserMuteGroupTask.class,this::clientAskToMute)
                .match(LeaveGroupChatTask.class,this::clientAskToLeaveGroup)
                .match(deleteGroup.class,this::removeGroup).build();
    }

    public void handleConnect(ConnectUser msg){
        if(users.containsKey(msg.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s already exists",msg.getuserName())),getSelf());
            return;
        }
        this.users.put(msg.getuserName(),msg.getSelf());
        getSender().tell(new requestRes(true,String.format("%s successfully connected",msg.getuserName())),getSelf());

    }
    private void isTargetConnected(getActorFromServer actor){
        if(!users.containsKey(actor.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",actor.getuserName())),getSelf());
            return;
        }
        getSender().tell(users.get(actor.getuserName()),getSelf());
    }
    private void createNewGroup(CreateGroupChatTask msg){
        if(groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s already exists! try again.",msg.getGroupName())),getSelf());
            return;
        }
        if(!users.containsKey(msg.getAdminName())){
            getSender().tell(new requestRes(false,String.format("%s isn't loged in! can't create a group",msg.getAdminName())),getSelf());
            return;
        }
        ActorRef newGroup=getContext().actorOf(groupActor.props(msg.getGroupName(),msg.getAdminName()),msg.getGroupName());//TODO:maybe the last name is not necessary
        this.groups.put(msg.getGroupName(),newGroup);
        newGroup.forward(msg,getContext());
    }

    private void inviteUserToGroup(UserInviteGroupTask msg){
        if(!users.containsKey(msg.getTargetName())){
            return;
        }
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }

        ActorRef existingGroup = groups.get(msg.getGroupName());
        existingGroup.forward(msg, getContext());
    }
    private void addClientToGroup(clientAcceptInvite msg){
        ActorRef targetGroup= this.groups.get(msg.getGroup_name());
        ActorRef clientToAdd=this.users.get(msg.getClientName());
        targetGroup.tell(new addClientToGroupTask(targetGroup,clientToAdd,msg.getClientName()),getSelf());
    }

    private void clientSendTextToGroup(sendTextGroupTask msg){
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetGroup= this.groups.get(msg.getGroupName());
        targetGroup.forward(msg,getContext());
    }

    private void clientSendFileToGroup(sendFileGroupTask msg){
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetGroup= this.groups.get(msg.getGroupName());
        targetGroup.forward(msg,getContext());
    }

    private void removeFromGroup(UserRemoveGroupTask msg){
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetGroup= this.groups.get(msg.getGroupName());
        targetGroup.forward(msg,getContext());
    }

    private void addCoAdmin(CoadminAddGroupTask msg){
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetGroup= this.groups.get(msg.getGroupName());
        targetGroup.forward(msg,getContext());
    }

    private void removeCoAdmin(CoadminRemoveGroupTask msg){
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetGroup= this.groups.get(msg.getGroupName());
        targetGroup.forward(msg,getContext());
    }

    private void clientAskToMute(UserMuteGroupTask msg){
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetGroup= this.groups.get(msg.getGroupName());
        targetGroup.forward(msg,getContext());
    }

    private void clientAskToUnmute(UserUnmuteGroupTask msg){
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetGroup= this.groups.get(msg.getGroupName());
        targetGroup.forward(msg,getContext());
    }

    private void clientAskToLeaveGroup(LeaveGroupChatTask msg){
        if(!groups.containsKey(msg.getGroupName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetGroup= this.groups.get(msg.getGroupName());
        targetGroup.forward(msg,getContext());
    }

    private void removeGroup(deleteGroup group){
        this.groups.remove(group.getGroupName());
        this.getContext().stop(group.getGroup());
    }

    private void clientAskToDisconnect (disconnectUserTask msg){
        for (Map.Entry group : groups.entrySet()) {
            ActorRef targetGroup = (ActorRef) group.getValue();
            targetGroup.forward(msg, getContext());
        }
        this.users.remove(msg.getuserName());
        getSender().tell(new requestRes(true,String.format("%s has been disconnected successfully!",msg.getuserName())),getSelf());
        this.getContext().stop(getSelf());//stop and close actor
    }




}
