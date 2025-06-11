package org.acme.domain.chatbot.model.conversation;

public class ValidationMessage extends Message {
    private boolean validate;
    
    public ValidationMessage() {
        super();
    }
    
    public ValidationMessage(String role, String content, boolean validate) {
        super(role, content);
        this.validate = validate;
    }
    
    public boolean isValidate() {
        return validate;
    }
    
    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}