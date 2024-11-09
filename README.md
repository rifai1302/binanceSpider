# Binance Spider 
## Bitcoin Daytime Scalping Bot

A Java-based Bitcoin scalping bot with a JavaFX UI, designed for daytime trading by executing short, quick trades. 
**Note**: This project is for reference purposes only and is no longer a reliable method for scalping.

## Features

- **Real-Time Price Monitoring** via the Binance API
- **Automated Scalping Strategy** with built-in stop-loss strategies such as trailing stop loss and attachable "strategists" such as a simple range spotter and a more complex Ollama-based AI strategist
- **JavaFX UI** displaying live price charts, trade history, and profit/loss stats
- **Connection failsafe** which detects internet connection loss and continuously attempts to sell until the connection is restored before resuming normal operation
