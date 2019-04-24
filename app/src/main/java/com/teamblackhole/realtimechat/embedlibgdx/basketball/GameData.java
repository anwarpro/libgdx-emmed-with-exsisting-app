package com.teamblackhole.realtimechat.embedlibgdx.basketball;

public class GameData {
    private static final GameData ourInstance = new GameData();

    public static GameData getInstance() {
        return ourInstance;
    }

    private boolean hint;
    private boolean back;

    private GameData() {
    }

    public boolean isHint() {
        return hint;
    }

    public void setHint(boolean hint) {
        this.hint = hint;
    }

    public boolean isBack() {
        return back;
    }

    public void setBack(boolean back) {
        this.back = back;
    }
}
