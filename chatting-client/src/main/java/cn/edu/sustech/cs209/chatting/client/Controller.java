package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

    private static Controller instance;
    @FXML
    public Label currentOnlineCnt;
    @FXML
    public ListView roomUsers;
    @FXML
    public ListView chatList;
    @FXML
    public ListView onlineUsersList;
    @FXML
    public Label currentUsername;
    @FXML
    ListView<Message> chatContentList;
    @FXML
    private TextArea inputArea;

    InputStream inputStream;
    OutputStream outputStream;
    Scanner in;
    PrintWriter out;
    String username;
    String password;
    Socket socket;
    private static FileInputStream fileIn;
    private static DataOutputStream DataOUT;
    public static ClientThread clientThread;
    public static Thread thread;
    public CopyOnWriteArrayList<String> onlineUserList;
    public CopyOnWriteArrayList<ChatRoom> chatRoomList;
    public CopyOnWriteArrayList<String> roomUserList;

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
                System.out.println(username + "  " + password);
                if (!username.isEmpty() && !password.isEmpty()) {
                    // 开始一个新线程
                    clientThread = new ClientThread("127.0.0.1", 8588, username, password, this);
                    thread = new Thread(clientThread);
                    thread.start();
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
            alert.setHeaderText("登陆失败");
            alert.setContentText("密码错误或账户已登录");
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
    public void stopButtonAction(ActionEvent e) throws IOException {
        shutdown();
    }

    /**
     * 设置在线用户列表，并显示在线人数(需要在列表中排除本机用户)
     *
     * @param onlineUserList 用户集
     */
    public void setOnlineUserList(CopyOnWriteArrayList<String> onlineUserList) {

        this.onlineUserList = onlineUserList;
		/*
		System.out.print("UserList:");
		for(UserInfo user: userInfolist){
			System.out.println(user.getUsername());
		}
		*/

        //在线用户数量
        int userCount = onlineUserList.size();

        //本机用户不需要显示
        for (String user : onlineUserList) {
            if (user.equals(username)) {
                onlineUserList.remove(user);
                break;
            }
        }
        //设置在线用户列表
        Platform.runLater(() -> {

            //数据源
            ObservableList<String> users = FXCollections.observableList(onlineUserList);
            onlineUsersList.setItems(users);
            //自定义ListView
            onlineUsersList.setCellFactory(new MyListCellFactory());

            // Add items to the list
            onlineUsersList.getItems().addAll(onlineUserList);

            //设置在线用户人数
            currentOnlineCnt.setText(userCount + "");
        });
    }


    private static class MyListCellFactory implements Callback<ListView<String>, ListCell<String>> {
        @Override
        public ListCell<String> call(ListView<String> param) {
            return new MyListCell();
        }
    }

    private static class MyListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                // Add custom graphic or icon if needed
            }
        }
    }

    public void shutdown() throws IOException {
        Message message = new Message(System.currentTimeMillis(), username, null, null, null, MessageType.DISCONNECT);
        if (clientThread != null) {
            clientThread.send(message);
            clientThread.getS().close();
            clientThread.setExit(true);
        }
        System.exit(0);
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

    @FXML
    private void selectFile() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        File file = fileChooser.showOpenDialog(inputArea.getScene().getWindow());
        if (file != null) {
            sendFile(file);
        }
    }

    /**
     * 向客户端传输文件
     *
     * @throws Exception
     */
    private void sendFile(File file) throws Exception {
        try {
            if (file.exists()) {
                fileIn = new FileInputStream(file);
                DataOUT = new DataOutputStream(clientThread.getS().getOutputStream());

                //文件名和长度
                DataOUT.writeUTF(file.getName());
                DataOUT.flush();
                DataOUT.writeLong(file.length());
                DataOUT.flush();

                //开始传输文件
                System.out.println("=========Start to transfer=========");
                byte[] bytes = new byte[1024];
                int length = 0;
                long progress = 0;
                while ((length = fileIn.read(bytes, 0, bytes.length)) != -1) {
                    DataOUT.write(bytes, 0, length);
                    DataOUT.flush();
                    progress += length;
                    System.out.println("| " + (100 * progress / file.length()) + "% |");
                }
                System.out.println();
                System.out.println("=====File transferred successfully=====");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {                                //关闭数据流
            if (fileIn != null) {
                fileIn.close();
            }
            if (DataOUT != null) {
                DataOUT.close();
            }
        }
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
