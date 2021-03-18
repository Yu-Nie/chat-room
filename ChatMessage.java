import java.io.Serializable;

/**
 * [Add your documentation here]
 *
 * @author Yu Nie & Lesi He, Lab 03
 * @version 4/27/2020
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    private String message;
    private int type;
    private String recipient;

    public ChatMessage(String message, int type, String recipient) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;

    }

    public ChatMessage() {
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
