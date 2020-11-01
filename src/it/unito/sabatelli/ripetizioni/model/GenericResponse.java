package it.unito.sabatelli.ripetizioni.model;

public class GenericResponse {
  boolean result = false;
  String  errorOccurred = null;

  public boolean isResult() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }

  public String getErrorOccurred() {
    return errorOccurred;
  }

  public void setErrorOccurred(String errorOccurred) {
    this.errorOccurred = errorOccurred;
  }
}
