package controller.commands;

import controller.Controller;

public class SellSignal implements Command {

  public byte execute (Controller controller) {
    controller.sellSignal();
    return(0);
  }

}

