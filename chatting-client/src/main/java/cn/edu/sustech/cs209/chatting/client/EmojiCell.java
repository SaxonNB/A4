package cn.edu.sustech.cs209.chatting.client;

import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class EmojiCell extends javafx.scene.control.ListCell<String> {
    private final ImageView emojiView = new ImageView();

    public EmojiCell() {
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(emojiView);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            Font font = Font.font("EmojiOne", FontWeight.NORMAL, 32);
            Text text = new Text(item);
            text.setFont(font);
            emojiView.setImage(text.snapshot(null, null));
            setGraphic(emojiView);
        }
    }

}
