package whatsapp.Server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import whatsapp.duelActivites.Tasks.*;


import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class groupActor extends AbstractActor {

    Router router;
    private String groupName;
    private String admin;
    private List<String> groupUsers;
    private List<String> coadmins;
    private HashMap<String,String> mutedUsers;


    public groupActor(String groupName,String admin) {
        this.router = new Router(new BroadcastRoutingLogic()); //saves all the client Actors of the group.
        this.groupName = groupName;
        this.admin = admin;
        this.groupUsers = new ArrayList<String>();
        this.coadmins = new ArrayList<String>();
        this.mutedUsers = new HashMap<String, String>();
    }

    static public Props props(String groupName, String admin) {
        return Props.create(groupActor.class, () -> new groupActor(groupName, admin));
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateGroupChatTask.class, this::groupActorCreateGroupChat)
                .match(LeaveGroupChatTask.class,this::groupActorLeaveGroupChat)
                .match(UserInviteGroupTask.class,this::groupActorUserInviteGroup)
                .match(addClientToGroupTask.class,this::addClientHere)
                .match(sendTextGroupTask.class,this::groupActorSendTextGroup)
                .match(sendFileGroupTask.class,this::groupActorSendFileGroup)
                .match(UserRemoveGroupTask.class,this::groupActorUserRemoveGroup)
                .match(CoadminAddGroupTask.class,this::groupActorCoadminAddGroup)
                .match(CoadminRemoveGroupTask.class,this::groupActorCoadminRemoveGroup)
                .match(UserMuteGroupTask.class,this::groupActorUserMuteGroup)
                .match(UserUnmuteGroupSchedulerTask.class, this::unmuteUserScheduler)
                .match(UserUnmuteGroupTask.class, this::groupActorUserUnmuteGroup)
                .match(disconnectUserTask.class, this::groupActorDisconnectGroupChat)
                .build();
    }


//----------------------------------------------------------------------------------------------------------------
    private void groupActorCreateGroupChat(CreateGroupChatTask msg){
            this.groupUsers.add(msg.getAdminName());
            this.router=this.router.addRoutee(new ActorRefRoutee(getSender()));
            this.router.route(new requestRes(true,String.format("%s created successfully!",this.groupName)),getSelf());
        }

    private void groupActorLeaveGroupChat(LeaveGroupChatTask msg){
        if(!groupUsers.contains(msg.getuserName())){
            getSender().tell(new requestRes(false, String.format("%s is not in %s!", msg.getuserName(), this.groupName)), getSelf());
            return;
        }
        if(coadmins.contains(msg.getuserName())){
            groupUsers.remove(msg.getuserName());
           coadmins.remove(msg.getuserName());
           this.router=this.router.removeRoutee(getSender());
            this.router.route(new sendTextGroupTask(this.groupName,String.format("%s has left %s",msg.getuserName(),this.groupName),msg.getuserName()),getSelf());
            return;
        }
        if(admin.equals(msg.getuserName())){
            this.router.route(new sendTextGroupTask(this.groupName, String.format("%s admin has closed %s!", this.groupName, this.groupName), msg.getuserName()),getSelf());
            this.groupUsers.clear();
            getContext().parent().tell(new deleteGroup(this.groupName,getSelf()),getSelf());
            return;
        }
        groupUsers.remove(msg.getuserName());
        this.router = this.router.removeRoutee(getSender());
        router.route(new sendTextGroupTask(this.groupName,String.format("%s has left %s",msg.getuserName(),this.groupName),msg.getuserName()),getSelf());
    }

    private void groupActorDisconnectGroupChat(disconnectUserTask msg){
        if(!groupUsers.contains(msg.getuserName())){
            return;
        }
        if(coadmins.contains(msg.getuserName())){
            groupUsers.remove(msg.getuserName());
            coadmins.remove(msg.getuserName());
            this.router=this.router.removeRoutee(getSender());
            router.route(new sendTextGroupTask(this.groupName,String.format("%s has left %s",msg.getuserName(),this.groupName),msg.getuserName()),getSelf());
            return;
        }
        if(admin.equals(msg.getuserName())){
            router.route(new sendTextGroupTask(this.groupName, String.format("%s admin has closed %s!", this.groupName, this.groupName), msg.getuserName()),getSelf());
            this.groupUsers.clear();
            getContext().parent().tell(new deleteGroup(this.groupName,getSelf()),getSelf());
            return;
        }
        groupUsers.remove(msg.getuserName());
        this.router = this.router.removeRoutee(getSender());
        router.route(new sendTextGroupTask(this.groupName,String.format("%s has left %s",msg.getuserName(),this.groupName),msg.getuserName()),getSelf());
    }

    private void groupActorUserInviteGroup(UserInviteGroupTask msg){
        if(!((msg.getWhoInvitedName().equals(this.admin)) | coadmins.contains(msg.getWhoInvitedName()))){
            getSender().tell(new requestRes(false,String.format("You are neither an admin nor a co-admin of %s!",msg.getGroupName())),getSelf());
            return;
        }
        if(groupUsers.contains(msg.getTargetName())){
            getSender().tell(new requestRes(false,String.format("%s is already in %s",msg.getTargetName(),msg.getGroupName())),getSelf());
            return;
        }
        ActorRef targetUser = msg.getTargetRef();
        targetUser.forward(msg,getContext());


    }

    private void groupActorUserRemoveGroup(UserRemoveGroupTask msg){
        if(!((msg.getWhoRemove().equals(this.admin)) | coadmins.contains(msg.getWhoRemove()))){
            getSender().tell(new requestRes(false,String.format("You are neither an admin nor a co-admin of %s!",msg.getGroupName())),getSelf());
            return;
        }
        if(!groupUsers.contains(msg.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getuserName())),getSelf());
            return;
        }
        if(this.admin.equals(msg.getWhoRemove())) {
            if (coadmins.contains(msg.getuserName())) {
                coadmins.remove(msg.getuserName());
                groupUsers.remove(msg.getuserName());
                this.router.removeRoutee(msg.getToRemove());
                msg.getToRemove().tell(new sendTextGroupTask(this.groupName,String.format("you have been removed from %s by %s",this.groupName,msg.getWhoRemove()),msg.getWhoRemove()),getSelf());
                return;
            }
            if(!this.admin.equals(msg.getuserName())) {
                groupUsers.remove(msg.getuserName());
                this.router.removeRoutee(msg.getToRemove());
                msg.getToRemove().tell(new sendTextGroupTask(this.groupName, String.format("you have been removed from %s by %s", this.groupName, msg.getWhoRemove()), msg.getWhoRemove()), getSelf());
                return;
            }
        }

        if(coadmins.contains(msg.getWhoRemove())){
             if(coadmins.contains(msg.getuserName())){
                 getSender().tell(new requestRes
                         (false,String.format("You are co-admin and can't remove another co-admin of %s!",msg.getGroupName()))
                         ,getSelf());
                 return;
             }
             groupUsers.remove(msg.getuserName());
            this.router.removeRoutee(msg.getToRemove());
             msg.getToRemove().tell(new sendTextGroupTask(this.groupName,String.format("you have been removed from %s by %s",this.groupName,msg.getWhoRemove()),msg.getWhoRemove()),getSelf());

         }
    }

    private void groupActorUserMuteGroup(UserMuteGroupTask msg){
        if(!((msg.getWhoMute().equals(this.admin)) | coadmins.contains(msg.getWhoMute()))){
            getSender().tell(new requestRes(false,String.format("You are neither an admin nor a co-admin of %s!",msg.getGroupName())),getSelf());
            return;
        }
        else if(this.mutedUsers.containsKey(msg.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s is already on mute!",msg.getuserName())),getSelf());
            return;
        }
        this.mutedUsers.put(msg.getuserName(),msg.getTimeOfMute());
        this.router= this.router.removeRoutee(msg.getToMute());
        msg.getToMute().tell(new sendTextGroupTask(this.groupName,String.format("You are muted for %s seconds in %s!",msg.getTimeOfMute(),this.groupName),msg.getWhoMute()),getSelf());
        final long timeInteger = Integer.parseInt(msg.getTimeOfMute());

        this.getContext().getSystem().scheduler().scheduleOnce(Duration.ofSeconds(timeInteger),getSelf(),new UserUnmuteGroupSchedulerTask(msg.getGroupName(),msg.getuserName(), msg.getWhoMute(), msg.getToMute()), this.getContext().getSystem().dispatcher(), getSender());

    }

    private void unmuteUserScheduler(UserUnmuteGroupSchedulerTask msg){
        if(mutedUsers.containsKey(msg.getuserName())){
            mutedUsers.remove(msg.getuserName());
            this.router = this.router.addRoutee(new ActorRefRoutee(msg.getToUnMute()));
            msg.getToUnMute().tell(new sendTextGroupTask(this.groupName,String.format("you have been unmuted! Muting time is up!"),msg.getuserName()),getSelf());
        }
    }

    private void groupActorUserUnmuteGroup(UserUnmuteGroupTask msg){
        if(!((msg.getWhoUnmute()).equals(this.admin)) | coadmins.contains(msg.getWhoUnmute())){
            getSender().tell(new requestRes(false,String.format("You are neither an admin nor a co-admin of %s!",msg.getGroupName())),getSelf());
            return;
        }
        else if(!mutedUsers.containsKey(msg.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s is not muted!",msg.getuserName())),getSelf());
            return;
        }
        mutedUsers.remove(msg.getuserName());
        this.router = this.router.addRoutee(new ActorRefRoutee(msg.getToUnmute()));
        msg.getToUnmute().tell(new sendTextGroupTask(this.groupName, String.format("you have been unmuted in %s by %s!",msg.getGroupName(), msg.getWhoUnmute()),msg.getWhoUnmute()),getSelf());
    }

    private void groupActorCoadminAddGroup(CoadminAddGroupTask msg){
        if(!groupUsers.contains(msg.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getuserName())),getSelf());
            return;
        }
        if(coadmins.contains(msg.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s already a coadmin of %s!",msg.getuserName(),this.groupName)),getSelf());
            return;
        }
        if(!this.admin.equals(msg.getWhoAdd())){
            getSender().tell(new requestRes
                            (false,String.format("You are co-admin and can't remove another co-admin of %s!",msg.getGroupName()))
                    ,getSelf());
            return;
        }
        coadmins.add(msg.getuserName());
        msg.getUserToAdd().tell(new sendTextGroupTask(this.groupName, String.format("you have been promoted to co-admin in %s", this.groupName), msg.getWhoAdd()), getSelf());
    }

    private void groupActorCoadminRemoveGroup(CoadminRemoveGroupTask msg){
        if(!groupUsers.contains(msg.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s does not exist!",msg.getuserName())),getSelf());
            return;
        }
        if(!coadmins.contains(msg.getuserName())){
            getSender().tell(new requestRes(false,String.format("%s is not a coadmin of %s!",msg.getuserName(),this.groupName)),getSelf());
            return;
        }
        if(!this.admin.equals(msg.getWhoRemove())){
            getSender().tell(new requestRes
                            (false,String.format("You are co-admin and can't remove another co-admin of %s!",msg.getGroupName()))
                    ,getSelf());
            return;
        }
        coadmins.remove(msg.getuserName());
        msg.getUserToRemove().tell(new sendTextGroupTask(this.groupName, String.format("you have been demoted to user in %s", this.groupName), msg.getWhoRemove()), getSelf());
    }

    private void addClientHere(addClientToGroupTask toAdd){
        this.groupUsers.add(toAdd.getClientName());
        this.router=this.router.addRoutee(new ActorRefRoutee(toAdd.getClientToAdd()));
    }

    private void groupActorSendTextGroup(sendTextGroupTask msg){
        if(!groupUsers.contains(msg.getuserName())){
            getSender().tell(new requestRes(false, String.format("You are not part of %s!", this.groupName)), getSelf());
            return;
        }
        else if(mutedUsers.containsKey(msg.getuserName())){
            getSender().tell(new requestRes(false, String.format("You are muted for %s in %s!",this.mutedUsers.get(msg.getuserName()), this.groupName)), getSelf());
            return;
        }
        router.route(msg, getSelf());
    }

    private void groupActorSendFileGroup(sendFileGroupTask msg){
        if(!groupUsers.contains(msg.getuserName())){
            getSender().tell(new requestRes(false, String.format("You are not part of %s!", this.groupName)), getSelf());
            return;
        }
        if(mutedUsers.containsKey(msg.getuserName())){
            getSender().tell(new requestRes(false, String.format("You are muted for %s in %s!",this.mutedUsers.get(msg.getuserName()),this.groupName)), getSelf());
            return;
        }
        router.route(msg, getSelf());
    }

}