package whatsapp.Client;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.time.LocalDateTime;

public class clientMessages {

    static public class askToConnect implements Serializable {
        final String userName;

        public askToConnect(String userName) { //TODO:can it be private? its a constructor.
            this.userName = userName;
        }
    }

    public static class askToDisconnect implements Serializable {


        public askToDisconnect() {
        }
    }

    public static class sendTextMsg implements Serializable {
        String target;
        String text;

        public sendTextMsg(String target, String text) {
            this.text = text;
            this.target = target;
        }
    }

    public static class sendFileMsg implements Serializable {
        String target;
        byte[] file;

        public sendFileMsg(String target, byte[] file) {
            this.target = target;
            this.file = file;
        }
    }


    public static class getTextMsg implements Serializable {
        String source;
        String text;

        public getTextMsg(String source, String text) {
            this.source = source;
            this.text = text;
        }

    }

    public static String timeSent(){
        LocalDateTime time = LocalDateTime.now();
        String timeStr="["+time.getHour()+":"+time.getMinute()+"]";
        return timeStr;
    }

    public static class getFileMsg implements Serializable{
        String source;
        byte [] file;
        public getFileMsg(String source,byte[] file){
            this.source=source;
            this.file=file;
        }
    }
//----------------groups ----------------------------------------
    public static class createGroupChat implements Serializable {
    String group_name;
        public createGroupChat(String group_name) {
            this.group_name = group_name;
         }
    }
    public static class leaveGroupChat implements Serializable{
        String group_name;
        public leaveGroupChat(String group_name){
            this.group_name=group_name;
        }
    }
    public static class sendTextGroup implements Serializable{
        String target_group_name;
        String text;
        public sendTextGroup(String target_group_name,String text){
            this.target_group_name=target_group_name;
            this.text=text;
        }
    }

    public static class sendFileGroup implements Serializable{
        String group_name;
        byte [] file;
        public sendFileGroup(String group_name, byte[] file){
            this.group_name = group_name;
            this.file = file;
        }
    }


    public static class userInviteGroup implements Serializable { //if this client created the group or is a coAdmin of a group
        final String group_name;
        final String target;

        public userInviteGroup(String group_name, String target) {
            this.target = target;
            this.group_name = group_name;
        }

        public String getGroup_name() {
            return group_name;
        }

        public String getTarget() {
            return target;
        }
    }

    public static class userRemoveGroup implements Serializable{
        String group_name;
        String target;
        public userRemoveGroup(String group_name,String target){
            this.group_name = group_name;
            this.target = target;
        }
    }

    public static class userMuteGroup implements Serializable{
        String group_name;
        String target;
        String howMuchTime;
        public userMuteGroup(String group_name,String target,String howMuchTime){
            this.group_name = group_name;
            this.target = target;
            this.howMuchTime=howMuchTime;
        }
    }

    public static class userUnmuteGroup implements Serializable{
        String group_name;
        String target;
        public userUnmuteGroup(String group_name,String target){
            this.group_name = group_name;
            this.target = target;
        }
    }

    public static class coadminAddGroup implements Serializable{
        String group_name;
        String target;
        public coadminAddGroup(String group_name,String target){
            this.group_name = group_name;
            this.target = target;
        }
    }    public static class coadminRemoveGroup implements Serializable{
        String group_name;
        String target;
        public coadminRemoveGroup(String group_name,String target){
            this.group_name = group_name;
            this.target = target;
        }
    }

    public static class userWasInvited implements Serializable{
        final ActorRef whoInvited;
        final String toWhichGroup;
        public userWasInvited(ActorRef whoInvited,String toWhichGroup){
            this.whoInvited=whoInvited;
            this.toWhichGroup=toWhichGroup;
        }
    }
}
