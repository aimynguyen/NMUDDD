const { onDocumentCreated } = require("firebase-functions/v2/firestore");
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