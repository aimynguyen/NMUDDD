const { onDocumentCreated, onDocumentDeleted } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

// Bắt sự kiện tạo document mới trong bảng notifications
exports.sendPushNotification = onDocumentCreated("notifications/{notificationId}", async (event) => {

    // 1. Lấy dữ liệu thông báo vừa được thêm vào
    const newNotif = event.data.data();

    if (!newNotif) {
        return console.log("Không có dữ liệu");
    }

    const receiverId = newNotif.receiverId;
    if (!receiverId) return console.log("Không có receiverId");

    // 2. Chui vào bảng users tìm cái fcmToken của người đó
    const userDoc = await admin.firestore().collection("users").doc(receiverId).get();

    if (!userDoc.exists) {
        return console.log("Không tìm thấy User");
    }

    const fcmToken = userDoc.data().fcmToken;
    if (!fcmToken) {
        return console.log("User này chưa có fcmToken (Chưa cài app/chưa đăng nhập)");
    }

    // 3. Đóng gói dữ liệu để bắn rung điện thoại
    const payload = {
        notification: {
            title: "AuraLog",
            body: newNotif.message // Nội dung lấy từ thông báo
        },
        token: fcmToken
    };

    // 4. Gửi thông báo đến điện thoại
    try {
        const response = await admin.messaging().send(payload);
        console.log("Đã gửi thành công:", response);
    } catch (error) {
        console.log("Lỗi gửi thông báo:", error);
    }
});

// Bắt sự kiện tạo tin nhắn mới để bắn Push Notification
exports.sendChatNotification = onDocumentCreated("chats/{chatId}/messages/{messageId}", async (event) => {
    const newMessage = event.data.data();
    if (!newMessage) return console.log("Không có dữ liệu tin nhắn");

    const senderId = newMessage.senderId;
    const content = newMessage.content;

    // 1. Lấy thông tin phòng chat để tìm người nhận
    const chatId = event.params.chatId;
    const chatDoc = await admin.firestore().collection("chats").doc(chatId).get();
    if (!chatDoc.exists) return console.log("Không tìm thấy phòng chat");

    const participants = chatDoc.data().participants;
    if (!participants || participants.length !== 2) return;

    // 2. Lọc ra người nhận (người có uid khác với senderId)
    const receiverId = participants.find(id => id !== senderId);
    if (!receiverId) return;

    // 3. Lấy thông tin người nhận (để lấy fcmToken) và người gửi (để lấy tên)
    const [receiverDoc, senderDoc] = await Promise.all([
        admin.firestore().collection("users").doc(receiverId).get(),
        admin.firestore().collection("users").doc(senderId).get()
    ]);

    if (!receiverDoc.exists) return console.log("Không tìm thấy người nhận");
    
    const fcmToken = receiverDoc.data().fcmToken;
    if (!fcmToken) return console.log("Người nhận chưa có fcmToken");

    const senderName = senderDoc.exists ? senderDoc.data().userName : "Ai đó";

    // 4. Đóng gói payload bắn Push Notification
    const payload = {
        notification: {
            title: `Tin nhắn mới từ ${senderName}`,
            body: content
        },
        data: {
            chatId: chatId
        },
        token: fcmToken
    };

    // 5. Gửi thông báo
    try {
        const response = await admin.messaging().send(payload);
        console.log("Đã gửi Push Notification tin nhắn thành công:", response);
    } catch (error) {
        console.log("Lỗi gửi Push Notification tin nhắn:", error);
    }
});

// Bắt sự kiện khi một bài viết (Post) bị xóa khỏi Firestore
exports.deletePostImageOnPostDelete = onDocumentDeleted("posts/{postId}", async (event) => {
    // Lưu ý: Đối với onDocumentDeleted, event.data.data() có thể null nếu trigger mới.
    // Dùng event.data.before.data() trong v1, nhưng ở v2 onDocumentDeleted thì data() là dữ liệu cũ.
    const deletedPost = event.data.data();
    
    // Nếu bài viết không có ảnh thì bỏ qua
    if (!deletedPost || !deletedPost.imageUrl) {
        return console.log("Bài viết không có ảnh, không cần xóa Storage.");
    }

    const imageUrl = deletedPost.imageUrl;

    try {
        // Lấy tham chiếu đến Storage Bucket mặc định của dự án
        const bucket = admin.storage().bucket(); 
        
        // Link ảnh Firebase thường có dạng: https://.../b/.../o/post_images%2F<uid>%2F<ten_file>?alt=media...
        // Ta cần decode URL để lấy ra đường dẫn file gốc (vd: post_images/uid/ten_file.jpg)
        const decodedUrl = decodeURIComponent(imageUrl);
        const match = decodedUrl.match(/\/o\/(.+?)\?/);
        
        if (match && match[1]) {
            const filePath = match[1];
            // Lệnh thực thi xóa ảnh
            await bucket.file(filePath).delete();
            console.log(`Đã tự động xóa ảnh rác thành công: ${filePath}`);
        } else {
             console.log("Không thể trích xuất đường dẫn ảnh từ URL này.");
        }
    } catch (error) {
        console.log("Lỗi khi xóa ảnh trên Storage:", error);
    }
});