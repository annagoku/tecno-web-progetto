package it.unito.sabatelli.ripetizioni.model;

public class GenericResponse {
  boolean result = true;
  String  errorOccurred = null;
  String  sessionId = null;

  public boolean isResult() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }

  public String getErrorOccurred() {
    return errorOccurred;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setErrorOccurred(String errorOccurred) {
    this.errorOccurred = errorOccurred;
  }
}
