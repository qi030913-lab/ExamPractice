import com.exam.util.DBUtil;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 创建网络通信日志表的工具类
 * 运行此程序来创建network_log表
 */
public class CreateNetworkLogTable {
    
    public static void main(String[] args) {
        String sql = "CREATE TABLE IF NOT EXISTS network_log (" +
                "    log_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID'," +
                "    student_id INT NOT NULL COMMENT '学生ID'," +
                "    message_type VARCHAR(20) NOT NULL COMMENT '消息类型：SEND(发送)/RECEIVE(接收)/SYSTEM(系统)'," +
                "    message_content TEXT NOT NULL COMMENT '消息内容'," +
                "    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "    INDEX idx_student_id (student_id)," +
                "    INDEX idx_create_time (create_time)," +
                "    FOREIGN KEY (student_id) REFERENCES user(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网络通信日志表'";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("正在创建network_log表...");
            stmt.execute(sql);
            System.out.println("✓ 表创建成功！");
            System.out.println("network_log表已经创建，可以开始使用通信日志功能了。");
            
        } catch (Exception e) {
            System.err.println("✗ 创建表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
