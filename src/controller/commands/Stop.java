package controller.commands;

import controller.Controller;

public class Stop implements Command {

    public byte execute (Controller controller) {
        controller.stop();
        return(0);
    }
}
