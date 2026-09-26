package com.warnes.erp.exception;

public class StockAlreadyInitializedException extends RuntimeException {
  public StockAlreadyInitializedException(String message) {
    super(message);
  }
}
