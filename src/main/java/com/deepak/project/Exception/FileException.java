package com.deepak.project.Exception;

public class FileException extends Exception {
    private String description;
    private String msg;

    public FileException(String message) {
        super(message);
    }

    public FileException(String message, String desc) {
        super(message);
        this.description = desc;
        this.msg = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
