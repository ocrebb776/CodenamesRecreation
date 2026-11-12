package uk.olivercrebbin.codenameshost;

public enum View{
    Launcher("CodeNamesLauncher","LauncherPage.fxml"),
    Home("Home","homePage.fxml"),
    GeneralHelp("HELP!","generalHelpPage.fxml"),
    WinnerPage("Game Over","winnerPage.fxml"),
    ServerInfo("Server Info","serverInfoPage.fxml"),
    CustomWordsPage("Create game with custom wods","customWords.fxml")
    ;
    private String title;
    private String fxmlPage;
    View(String title,String fxmlPage){
        this.title=title;
        this.fxmlPage=fxmlPage;
    }

    public String getFxmlPage() {
        return fxmlPage;
    }

    public String getTitle() {
        return title;
    }
}