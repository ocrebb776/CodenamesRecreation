package uk.olivercrebbin.codenameshost.Server;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SessionManager {
    protected Map<String,Session> sessionMap = new HashMap<>();
    enum SessionRole{
        BlUE_TEAM,
        RED_TEAM,
        OBSERVER,
    }
    enum AvaliableView{
        REGULAR,
        SPYMASTER
    }
     class Session{
        public String name;
        public String code;
        public final String id;
        private SessionRole role;
        private AvaliableView avaliableView;
        private Date lastConnet;
        public Session(String name,String code,String role){
            Random rgen = new Random();
            this.name = name;
            this.code = code;
            if(role.equals("BLUE_TEAM")) this.role = SessionRole.BlUE_TEAM;
            else if(role.equals("RED_TEAM")) this.role = SessionRole.RED_TEAM;
            else this.role = SessionRole.OBSERVER;
            this.lastConnet = new Date();

            this.avaliableView = AvaliableView.SPYMASTER;
            String newId;
            do {
                newId = padLeftZeros("" + rgen.nextInt(0, 100000), 5);

            }while (sessionMap.getOrDefault(newId,null)!=null);
            this.id = newId;
            addSession(this);
        }

         public Date getLastConnet() {
             return lastConnet;
         }
         public void markConnect(){
            lastConnet = new Date();
         }

         public SessionRole getRole() {
             return role;
         }
     }
    public  String addUser(String name,String code,String role){
        Session session = new Session(name,code,role);
        addSession(session);
        return session.id;
    }
    public  Session getSession(String id){
        return sessionMap.get(id);
    }
    public  void addSession(Session session){
        sessionMap.put(session.id,session);
    }
    public  boolean validateSession(String name,String code,String id){
        Session session = getSession(id);
        if(session == null) return false;
        if(!session.name.equals(name)) return false;
        if(!session.code.equals(code)) return false;
        session.markConnect();
        return true;
    }
    static String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }
}
