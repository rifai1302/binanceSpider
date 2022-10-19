package controller.commands;

import controller.Controller;

public class CryptoBalance implements Command {

  @Override
  public byte execute(Controller controller)  {
    controller.printCrypto();
    return(0);
  }
}
