package controller.commands;

import controller.Controller;

public class BuySignal implements Command {

  public byte execute (Controller controller) {
    controller.buySignal();
    return(0);
  }

}

