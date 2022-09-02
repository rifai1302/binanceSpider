package controller.commands;

import controller.Controller;

public interface Command {

    byte execute(Controller controller);

}
