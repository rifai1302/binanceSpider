package controller.commands;

import controller.Controller;

public class BTCBalance implements Command {

  @Override
  public byte execute(Controller controller)  {
    controller.printBTC();
    return(0);
  }
}
