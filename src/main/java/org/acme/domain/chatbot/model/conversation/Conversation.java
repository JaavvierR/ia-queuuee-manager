package org.acme.domain.chatbot.model.conversation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;


public class Conversation {
    private String id;
    private String conversationId;
    private Instant created;
    private String nameHistory;
    private String userId;
    private String step;
    private boolean isSelectBlock;
    private String selectValue;
    private String processPhase;
    private int tokensInput;
    private int tokensOutput;
    private int tokens;
    private int maxTokens;
    private List<Message> messages;

    public Conversation() {
        this.id = UUID.randomUUID().toString();
        this.conversationId = UUID.randomUUID().toString();
        this.created = Instant.now();
        this.messages = new ArrayList<>();
        this.maxTokens = 3500;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getNameHistory() {
        return nameHistory;
    }

    public void setNameHistory(String nameHistory) {
        this.nameHistory = nameHistory;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public boolean isSelectBlock() {
        return isSelectBlock;
    }

    public void setSelectBlock(boolean selectBlock) {
        isSelectBlock = selectBlock;
    }

    public String getSelectValue() {
        return selectValue;
    }

    public void setSelectValue(String selectValue) {
        this.selectValue = selectValue;
    }

    public String getProcessPhase() {
        return processPhase;
    }

    public void setProcessPhase(String processPhase) {
        this.processPhase = processPhase;
    }

    public int getTokensInput() {
        return tokensInput;
    }

    public void setTokensInput(int tokensInput) {
        this.tokensInput = tokensInput;
    }

    public int getTokensOutput() {
        return tokensOutput;
    }

    public void setTokensOutput(int tokensOutput) {
        this.tokensOutput = tokensOutput;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public List<Message> getMessage() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("_id", this.conversationId); // O puedes usar this.id si prefieres
        map.put("conversationId", this.conversationId);
        map.put("created", this.created.toString());
        map.put("isSelectBlock", this.isSelectBlock);
        map.put("maxTokens", this.maxTokens);
        map.put("message", this.messages); // Esto asume que Message ya está bien serializable
        map.put("processPhase", this.processPhase);
        map.put("selectValue", this.selectValue);
        map.put("step", this.step);
        map.put("tokens", this.tokens);
        map.put("tokensInput", this.tokensInput);
        map.put("tokensOutput", this.tokensOutput);
        map.put("userId", this.userId);
        return map;
    }
}