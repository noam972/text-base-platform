package whatsapp.duelActivites;


import akka.actor.ActorRef;

import java.io.Serializable;
//tasks that are mutual to server,clients and groups.

public class Tasks {
    public static class ConnectUser implements Serializable {
        final String userName;
        final ActorRef self;

        public ConnectUser (String userName, ActorRef self){
            this.userName = userName;
            this.self = self;
        }

        public String getuserName() {
            return userName;
        }

        public ActorRef getSelf() {
            return self;
        }
    }

    public static class disconnectUserTask implements Serializable{
        final String userName;
        final ActorRef self;

        public disconnectUserTask(String userName, ActorRef self) {
            this.userName = userName;
            this.self = self;
        }

        public String getuserName() {
            return userName;
        }

        public ActorRef getSelf() {
            return self;
        }
    }

    public static class requestRes implements Serializable {
        final String message;
        final boolean success;

        public requestRes(boolean success,String message) {
            this.success = success;
            this.message=message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }



    public static class getActorFromServer implements Serializable{
        final String userName;
        public getActorFromServer(String userName){
            this.userName=userName;
        }

        public String getuserName() {
            return userName;
        }
    }


//------------------------------------------Group Tasks---------------------------------
    public static class CreateGroupChatTask implements Serializable{
        final String groupName;
        final String adminName;

        public CreateGroupChatTask(String groupName,String adminName){
            this.groupName=groupName;
            this.adminName=adminName;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getAdminName() {
            return adminName;
        }
    }

    public static class LeaveGroupChatTask implements Serializable{
        final String groupName;
        final String userName;

        public LeaveGroupChatTask(String groupName,String userName){
            this.groupName=groupName;
            this.userName=userName;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getuserName() {
            return userName;
        }
    }

    public static class deleteGroup implements Serializable{
        final String groupName;
        final ActorRef group;
        public deleteGroup(String groupName,ActorRef group){
            this.groupName=groupName;
            this.group=group;
        }

        public String getGroupName() {
            return groupName;
        }

        public ActorRef getGroup() {
            return group;
        }
    }

    public static class sendTextGroupTask implements Serializable{
        final String groupName;
        final String text;
        final String userName;

        public sendTextGroupTask(String groupName, String text, String userName) {
            this.groupName = groupName;
            this.text = text;
            this.userName = userName;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public String getText() {
            return text;
        }

        public String getuserName() {
            return this.userName;
        }
    }

    public static class sendFileGroupTask implements Serializable{
        final String groupName;
        final byte[] file;
        final String userName;

        public sendFileGroupTask(String groupName, byte[] file, String userName) {
            this.groupName = groupName;
            this.file = file;
            this.userName = userName;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public byte [] getFile(){return file;}

        public String getuserName() {
            return this.userName;
        }
    }

    public static class UserInviteGroupTask implements Serializable{
        final String groupName;
        final String whoInvited;
        final String target;
        final ActorRef targetRef;

        public UserInviteGroupTask(String groupName, String whoInvited, String target,ActorRef targetRef) {
            this.groupName = groupName;
            this.whoInvited = whoInvited;
            this.target = target;
            this.targetRef=targetRef;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public String getWhoInvitedName() {
            return whoInvited;
        }

        public String getTargetName() {
            return this.target;
        }

        public ActorRef getTargetRef(){return this.targetRef;}
    }

    public static class UserRemoveGroupTask implements Serializable{
        final String groupName;
        final String userName;
        final String whoRemove;
        final ActorRef toRemove;

        public UserRemoveGroupTask(String groupName, String userName, String whoRemove,ActorRef toRemove) {
            this.groupName = groupName;
            this.userName = userName;
            this.whoRemove = whoRemove;
            this.toRemove=toRemove;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public String getuserName() {
            return userName;
        }

        public String getWhoRemove() {
            return this.whoRemove;
        }

        public ActorRef getToRemove() {
            return toRemove;
        }
    }

    public static class UserMuteGroupTask implements Serializable{
        final String groupName;
        final String userName;
        final String whoMute;
        final ActorRef toMute;
        final String timeOfMute;

        public UserMuteGroupTask(String groupName, String userName, String whoMute,ActorRef toMute,String timeOfMute) {
            this.groupName = groupName;
            this.userName = userName;
            this.whoMute = whoMute;
            this.toMute=toMute;
            this.timeOfMute=timeOfMute;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public String getuserName() {
            return userName;
        }

        public String getWhoMute() {
            return this.whoMute;
        }

        public ActorRef getToMute() {
            return toMute;
        }

        public String getTimeOfMute() {
            return timeOfMute;
        }
    }

    public static class UserUnmuteGroupTask implements Serializable{
        final String groupName;
        final String userName;
        final String whoUnmute;
        final ActorRef toUnmute;

        public UserUnmuteGroupTask(String groupName, String userName, String whoUnmute, ActorRef toUnmute) {
            this.groupName = groupName;
            this.userName = userName;
            this.whoUnmute = whoUnmute;
            this.toUnmute = toUnmute;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getuserName() {
            return userName;
        }

        public String getWhoUnmute() {
            return whoUnmute;
        }

        public ActorRef getToUnmute() {
            return toUnmute;
        }
    }

    public static class CoadminAddGroupTask implements Serializable{
        final String groupName;
        final String userName;
        final String whoAdd;
        final ActorRef userToAdd;

        public CoadminAddGroupTask(String groupName, String userName,String whoAdd,ActorRef userToAdd) {
            this.groupName = groupName;
            this.userName = userName;
            this.whoAdd=whoAdd;
            this.userToAdd=userToAdd;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public String getuserName() {
            return userName;
        }

        public String getWhoAdd() {
            return whoAdd;
        }

        public ActorRef getUserToAdd() {
            return userToAdd;
        }
    }

    public static class CoadminRemoveGroupTask implements Serializable{
        final String groupName;
        final String userName;
        final String whoRemove;
        final ActorRef userToRemove;

        public CoadminRemoveGroupTask(String groupName, String userName, String whoRemove, ActorRef userToRemove) {
            this.groupName = groupName;
            this.userName = userName;
            this.whoRemove = whoRemove;
            this.userToRemove = userToRemove;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public String getuserName() {
            return userName;
        }

        public String getWhoRemove() {
            return this.whoRemove;
        }

        public ActorRef getUserToRemove() {
            return userToRemove;
        }
    }

    public static class approveOrDeclineInvite implements Serializable{
        final boolean answer;

        public approveOrDeclineInvite(boolean answer){
            this.answer=answer;
        }

        public boolean getAnswer() {
            return answer;
        }
    }

    public static class clientAcceptInvite implements Serializable{
        final String group_name;
        final String clientName;
        public clientAcceptInvite(String group_name,String clientName){
            this.group_name=group_name;
            this.clientName=clientName;
        }

        public String getGroup_name() {
            return group_name;
        }

        public String getClientName() {
            return clientName;
        }
    }

    public static class addClientToGroupTask implements Serializable{
        final ActorRef group;
        final ActorRef clientToAdd;
        final String clientName;
        public addClientToGroupTask(ActorRef group,ActorRef clientToAdd,String clientName){
            this.group=group;
            this.clientToAdd=clientToAdd;
            this.clientName=clientName;
        }

        public ActorRef getGroup() {
            return group;
        }

        public ActorRef getClientToAdd() {
            return clientToAdd;
        }

        public String getClientName() {
            return clientName;
        }
    }

    public static class UserUnmuteGroupSchedulerTask implements Serializable{
        final String groupName;
        final String userName;
        final String whoUnmute;
        final ActorRef toUnMute;

        public UserUnmuteGroupSchedulerTask(String groupName, String userName, String whoUnmute, ActorRef toUnMute) {
            this.groupName = groupName;
            this.userName = userName;
            this.whoUnmute = whoUnmute;
            this.toUnMute = toUnMute;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getuserName() {
            return userName;
        }

        public String getWhoUnmute() {
            return whoUnmute;
        }

        public ActorRef getToUnMute() {
            return toUnMute;
        }
    }
}
