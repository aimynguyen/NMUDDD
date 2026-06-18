package com.example.diary_app.core;

import java.util.Random;

public class Quotes {
    private static final String[] QUOTES = {
            "Không có ngày nào là tẻ nhạt nếu bạn biết cách lưu giữ những điều nhỏ bé. Hôm nay của bạn thế nào?",
            "Hãy dành vài phút tĩnh lặng để nhìn lại những gì đã qua. Bạn đang làm rất tốt đấy!",
            "Những bức ảnh có thể phai màu, nhưng kỷ niệm được ghi chép lại bằng cảm xúc sẽ còn mãi.",
            "Cảm xúc nào cũng đáng được trân trọng. Đừng ngần ngại lưu lại dù là niềm vui hay nỗi buồn.",
            "Chỉ cần hôm nay bạn nỗ lực hơn hôm qua một chút, đó đã là một thành công rất đáng tự hào rồi.",
            "Hãy hít một hơi thật sâu. Mọi muộn phiền rồi cũng sẽ trôi qua như những đám mây trên trời.",
            "Bạn không cần phải hoàn hảo để được yêu thương. Hãy trân trọng phiên bản hiện tại của chính mình.",
            "Cuộc sống có lúc thăng lúc trầm. Dù thế nào, mong bạn vẫn luôn giữ một nụ cười thật ấm áp.",
            "Mỗi ngày mới là một trang giấy trắng. Hãy chọn những gam màu tươi sáng nhất để vẽ lên đó nhé.",
            "Mọi nỗ lực của bạn dù nhỏ bé đến đâu cũng đều mang lại ý nghĩa. Hãy cứ kiên nhẫn bước tiếp nhé!",
            "Đôi khi việc dũng cảm nhất bạn có thể làm trong ngày là quyết định cố gắng thêm lần nữa vào ngày mai.",
            "Hãy đối xử dịu dàng với chính mình, giống như cách bạn yêu thương một người bạn thân.",
            "Bạn không cần phải đuổi theo thời gian. Mọi bông hoa đều có một thời điểm nở rộ của riêng mình.",
            "Nghỉ ngơi không phải là từ bỏ, mà là trạm sạc năng lượng để bạn đi được chặng đường xa hơn.",

            "Every day may not be good, but there is always something good in every day.",
            "Take a deep breath. It’s just a bad day, not a bad life.",
            "You are enough just as you are. Don't be too hard on yourself.",
            "Growth is not always linear. Be proud of every little step you take.",
            "Your feelings are valid, and it's completely okay to take a pause to process them.",
            "Small steps in the right direction can turn out to be the biggest steps of your life.",

            "Người bạn nhỏ của bạn đang rất nhớ bạn đấy! Hãy ghé thăm và chơi cùng bé một chút nào.",
            "Mỗi tương tác của bạn đều giúp thú cưng trưởng thành hơn. Cùng nhau cố gắng mỗi ngày nhé!",
            "Dù ngoài kia có hối hả thế nào, luôn có một góc nhỏ bình yên ở đây chờ bạn trở về.",
            "Chăm sóc một điều gì đó nhỏ bé cũng là cách để xoa dịu chính tâm hồn mình. Bé pet đang đợi bạn đấy!",
            "Sự kiên trì của bạn hôm nay là phần thưởng cho người bạn nhỏ ngày mai. Tiếp tục phát huy nhé!"
    };

    public static String getRandomQuote() {
        Random random = new Random();
        int index = random.nextInt(QUOTES.length);
        return QUOTES[index];
    }
}