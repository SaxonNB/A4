package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Controller implements Initializable {

    private static Controller instance;
    public static int currentRoom = -1;
    ObservableList<Message> messages = FXCollections.observableArrayList();
    public void setCurrentRoom(int room){
        currentRoom = room;
    }
    public int getCurrentRoom(){
        return currentRoom;
    }
    @FXML
    public Label currentOnlineCnt;
    @FXML
    public ListView roomUsers;
    @FXML
    public ListView<String> chatList;
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
        loginDialog.getDialogPane().getButtonTypes().addAll(loginButtonType);

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
                //System.out.println(username + "  " + password);
                if (!username.isEmpty() && !password.isEmpty()) {
                    // 开始一个新线程
                    clientThread = new ClientThread("127.0.0.1", 8588, username, password, this);
                    thread = new Thread(clientThread);
                    thread.start();
                } else {
                    // 关闭当前Stage并退出应用程序
                    // System.out.println("空");
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

        chatList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String selectedItem = chatList.getSelectionModel().getSelectedItem();
                if (selectedItem!=null) {
                    List<String> a = Arrays.asList(selectedItem.split(";", -1));
                    CopyOnWriteArrayList<String> selectedUsers = new CopyOnWriteArrayList<>(a);
                    Message message = new Message(System.currentTimeMillis(), username, null, null, null, MessageType.CREATNEWROOM);
                    ChatRoom tempRoom = new ChatRoom(-1,selectedUsers,null);
                    message.setChatRoom(tempRoom);
                    clientThread.send(message);
                    /*Message message = new Message(System.currentTimeMillis(), "", "", "", "1", MessageType.CREATNEWROOM);
                    List<String> a = Arrays.asList(selectedItem.split(",", -1));
                    message.setAskchat(a);
                    String x = Message.toJson(message);
                    out.println(x);
                    out.flush();*/
                }
            }
        });
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
     * @param onlineUserList 用户集
     */
    public void setOnlineUserList(CopyOnWriteArrayList<String> onlineUserList) {

        this.onlineUserList = onlineUserList;
        CopyOnWriteArrayList<String> tmeplsit = onlineUserList;
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

            /*//数据源
            ObservableList<String> users = FXCollections.observableList(onlineUserList);
            onlineUsersList.setItems(users);
            //自定义ListView
            onlineUsersList.setCellFactory(new MyListCellFactory());

            // Add items to the list
            onlineUsersList.getItems().addAll(onlineUserList);
*/
            currentUsername.setText(username);
            //设置在线用户人数
            currentOnlineCnt.setText(userCount + "");
          //  System.out.println(onlineUserList + "   66666");
            onlineUsersList.setItems(FXCollections.observableArrayList(onlineUserList));

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
           // System.out.println("111111111111");
            clientThread.send(message);
            clientThread.setExit(true);
            //clientThread.getS().close();
            thread.interrupt();
        }
        System.exit(0);
    }


    /**
     * 更新聊天室列表
     */

    public void refreshChatList(ArrayList<String> list){
        Platform.runLater(()->{
            chatList.setItems(FXCollections.observableArrayList(list));
        });
    }


    /**
     * 下载文件
     */
    public void downloadFile(ActionEvent actionEvent){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("文件下载");
        dialog.setHeaderText(null);
        dialog.setContentText("请输入文件名:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String fileName = result.get();
            // send request to server
            Message m=new Message(System.currentTimeMillis(),username,null,null,"我下载了文件："+fileName,MessageType.ASKFORFILE);
            m.setFilename(fileName);
            ChatRoom chatRoom = new ChatRoom(currentRoom,null,null);
            m.setChatRoom(chatRoom);
            clientThread.send(m);
        }
    }

    /**
     * 发送消息
     */
    @FXML
    public void doSendMessage() {
        String text = inputArea.getText().trim(); // 获取输入框的文本内容
        if (!text.isEmpty()& currentRoom!=-1) { // 确保输入内容不为空且选择过某个聊天
            Message m=new Message(System.currentTimeMillis(),username,"","",text,MessageType.SENDMESSAGE);
            ChatRoom tempRoom = new ChatRoom(currentRoom,null,null);
            m.setChatRoom(tempRoom);
            clientThread.send(m);
            inputArea.clear();
        }
    }

    @FXML
    public void createChat() {
        // 获取所有在线用户列表，假设为List<String> allUsers
        CopyOnWriteArrayList<String> allUsers = onlineUserList;

        // 弹出窗口，让用户选择要邀请的人
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(20));

        Label label = new Label("Select users to join the group chat:");
        vBox.getChildren().add(label);

        // 创建一个 CheckBox 列表，用于选择用户
        List<CheckBox> checkBoxList = new ArrayList<>();
        for (String user : allUsers) {
            if (!user.equals(username)) {
                CheckBox checkBox = new CheckBox(user);
                checkBoxList.add(checkBox);
                vBox.getChildren().add(checkBox);
            }
        }

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            // 获取选择的用户，创建群聊
            int x = 0;
            CopyOnWriteArrayList<String> selectedUsers = new CopyOnWriteArrayList<>();
            for (CheckBox checkBox : checkBoxList) {
                if (checkBox.isSelected()) {
                    x++;
                    selectedUsers.add(checkBox.getText());
                }
            }
            if (x > 0) {
                selectedUsers.add(username);
                Message message = new Message(System.currentTimeMillis(), username, null, null, username+"创建了群聊", MessageType.CREATNEWROOM);
                ChatRoom tempRoom = new ChatRoom(-1,selectedUsers,null);
                message.setChatRoom(tempRoom);
                clientThread.send(message);

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("人数不够");
                alert.setContentText("人数不够，请选择至少1人");
                alert.showAndWait();
            }
            // 在这里创建群聊，将 selectedUsers 传入即可

            stage.close();
        });
        vBox.getChildren().add(okBtn);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * 选择上传文件
     * @param actionEvent
     * @throws Exception
     */
    @FXML
    private void selectFile(ActionEvent actionEvent) throws Exception {
        /*FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        File file = fileChooser.showOpenDialog(inputArea.getScene().getWindow());
        if (file != null) {
            sendFile(file);
        }*/
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(new  Stage () );
        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                //System.out.println(fileContent);
                String encodedFile = Base64.getEncoder().encodeToString(fileContent);
               // System.out.println(encodedFile);
                String fileName=selectedFile.getName();
                Message m=new Message(System.currentTimeMillis(),username,null,null,"我发了一个文件："+fileName,MessageType.SENDFIEL);
                m.setFilename(fileName);
                m.setFiledata(encodedFile);
                ChatRoom chatRoom = new ChatRoom(currentRoom,null,null);
                m.setChatRoom(chatRoom);
                clientThread.send(m);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    /**
     *初始化聊天窗口内容
     */
    public void initializeChatWindows(Message message){
        Platform.runLater(() ->{
            messages.clear();
            messages.add(message);
            chatContentList.setItems(messages);
            chatContentList.setCellFactory(new MessageCellFactory());
        });
    }


    /**
     * 选择表情
     */
    public void selectEmoji(ActionEvent actionEvent) {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Emoji");
        dialog.setHeaderText("Please select an emoji:");
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ListView<String> listView = new ListView<>();
        listView.setCellFactory(param -> new EmojiCell());
        ObservableList<String> emojiList = FXCollections.observableArrayList("😊",  "👍", "❤ ","😂", "😊", "👍", "👎", "🤔", "😘", "😍", "🤩", "🙏", "👋", "💪", "🤢", "🤮", "🤯", "😱", "😴");
        listView.setItems(emojiList);
        listView.getSelectionModel().selectFirst();

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            inputArea.appendText(result.get());
        }
    }


    /**
     * 服务器断开连接
     */
    public void serverClose(){
        thread.interrupt();
        Platform.exit();
    }
    /**
     * @param clientThread
     */

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

                    VBox container = new VBox();

                    for (int i = 0; i < msg.getAllHistoryMessage().size(); i++) {
                        HBox wrapper = new HBox();
                        Label nameLabel = new Label(msg.getAllHistoryMessage().get(i).getFrom() );
                        Label msgLabel = new Label(msg.getAllHistoryMessage().get(i).getContent());


                        nameLabel.setPrefSize(50, 20);
                        nameLabel.setWrapText(true);
                        nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
                        nameLabel.setTextFill(Color.web("#FF76a3"));
                        Font font = Font.font("Segoe UI Emoji", Font.getDefault().getSize());

                        String message = msg.getAllHistoryMessage().get(i).getContent().replaceAll("\\\\u([0-9A-Fa-f]{4})", "&#x$1;");
                        message = StringEscapeUtils.unescapeHtml4(message);

                        msgLabel.setFont(font);
                        msgLabel.setText(message);

                        if (msg.getAllHistoryMessage().get(i).getFrom().equals(username)) {
                            wrapper.setAlignment(Pos.TOP_RIGHT);

                            wrapper.getChildren().addAll(msgLabel, nameLabel);
                            nameLabel.setPadding(new Insets(0, 20, 0, 0));
                        } else {

                            wrapper.setAlignment(Pos.TOP_LEFT);
                            wrapper.getChildren().addAll(nameLabel, msgLabel);
                            nameLabel.setPadding(new Insets(0, 0, 0, 20));
                        }

                        container.getChildren().add(wrapper);
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(container);
                }
            };
        }
    }
}
