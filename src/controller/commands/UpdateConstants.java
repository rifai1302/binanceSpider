package controller.commands;

import controller.Controller;

public class UpdateConstants implements Command {
    @Override
    public byte execute(Controller controller) {
        controller.updateConstants();
        return 0;
    }
}
