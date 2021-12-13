package com.google.mediapipe.apps.wearableai;

public class MessageTypes {
    //top level
    public static final String MESSAGE_TYPE_LOCAL = "MESSAGE_TYPE_LOCAL";
    public static final String MESSAGE_TYPE_ASG = "MESSAGE_TYPE_ASG";
    public static final String SEND_TO_ASG = "SEND_TO_ASG";

    //specific message types (after MESSAGE_TYPE_*)
    public static final String FINAL_TRANSCRIPT = "FINAL_TRANSCRIPT";
    public static final String INTERMEDIATE_TRANSCRIPT = "INTERMEDIATE_TRANSCRIPT";
    public static final String TRANSCRIPT_TEXT = "TRANSCRIPT_TEXT";
    public static final String TRANSCRIPT_ID = "TRANSCRIPT_ID";
    public static final String TIMESTAMP = "TIMESTAMP";
}
