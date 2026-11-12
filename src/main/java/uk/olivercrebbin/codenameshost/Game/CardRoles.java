package uk.olivercrebbin.codenameshost.Game;

public enum CardRoles {
    RED_TEAM("redCard"),
    BLUE_TEAM("blueCard"),
    Bomb("bombCard"),
    Civilian("civi"),
    ;
    private String className;
    CardRoles(String className){
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
