package utils.SBRAEngine;

public class SBRAException extends Exception {
	//Parameterless Constructor
    public SBRAException() {}

    //Constructor that accepts a message
    public SBRAException(String message) {
       super(message);
    }
}
