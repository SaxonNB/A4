package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

    private static Controller instance;
    @FXML
    ListView<Message> chatContentList;
    InputStream inputStream;
    OutputStream outputStream;
    Scanner in;
    PrintWriter out;
    String username;
    String password;
    Socket socket;
    ClientThread clientThread;

    public Controller() {
        instance = this;
    }

    public static Controller getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       /* try {
            socket=new Socket("127.0.0.1",8588);
            inputStream=socket.getInputStream();
            outputStream=socket.getOutputStream();
            in=new Scanner(inputStream);
            out=new PrintWriter(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Dialog<Pair<String, String>> loginDialog = new Dialog<>();
        loginDialog.setTitle("Login");
        loginDialog.setHeaderText(null);

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        loginDialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // 创建账户和密码输入框
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        loginDialog.getDialogPane().setContent(grid);

        // 设置“Login”按钮的响应事件
        loginDialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                if (!username.isEmpty() && !password.isEmpty()) {
                    // 开始一个新线程
                    ClientThread clientThread = new ClientThread("127.0.0.1", 8588, username, password, this);
                    new Thread(clientThread).start();
                } else {
                    // 关闭当前Stage并退出应用程序
                    System.out.println("空");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("用户名或密码不能为空！");
                    alert.showAndWait();
                    Platform.exit();
                }
            }
            return null;
        });

        // 显示对话框，并获取用户输入的账户和密码
        Optional<Pair<String, String>> result = loginDialog.showAndWait();
        /*if (result.isPresent() && !result.get().getKey().isEmpty() && !result.get().getValue().isEmpty()) {
            // 处理用户输入
            String username = result.get().getKey();
            String password = result.get().getValue();
            Message message=new Message(System.currentTimeMillis(),username,password,username,"", MessageType.ASKFORCONNECT);
            String aaa=Message.toJson(message);
            out.println(aaa);
            out.flush();
        } else {
            // 用户未输入账户或密码，退出应用程序
            System.out.println("Invalid username or password, exiting");
            Platform.exit();
        }*/

        chatContentList.setCellFactory(new MessageCellFactory());
    }

    public void failLogin() {
        // 关闭当前Stage并退出应用程序
        Platform.runLater(() -> {
            // 在 UI 线程中弹出提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Password incorrect");
            alert.setContentText("密码错误 and try again.");
            alert.showAndWait();
            Platform.exit();
            /*try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        });
    }

    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        userSel.getItems().addAll("Item 1", "Item 2", "Item 3");

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        // TODO
    }

    public void setClientThread(ClientThread clientThread) {
        this.clientThread = clientThread;
        if (clientThread != null) {
            username = clientThread.getUserName();
            password = clientThread.getUserPassword();
        }
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentFrom());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentFrom())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
