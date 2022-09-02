package controller.commands;

import controller.Controller;

public class Begin implements Command {

    @Override
    public byte execute(Controller controller)  {
        controller.start();
        return(0);
    }
}
