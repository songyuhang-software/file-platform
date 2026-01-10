package file.platform;

import file.platform.entity.User;
import file.platform.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 主启动类 - 添加了CommandLineRunner来演示MyBatis和Redis的使用
 */
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserService userService) {
        return (args) -> {
            System.out.println("=== 开始演示MyBatis和Redis集成 ===");

            // 演示Redis字符串操作
            System.out.println("\n1. 演示Redis字符串操作:");
            userService.demonstrateStringRedisOperations();

            // 创建用户
            System.out.println("\n2. 创建测试用户:");
            User user = new User("testuser", "test@example.com", "password123");
            boolean created = userService.createUser(user);
            System.out.println("用户创建结果: " + created);

            if (created && user.getId() != null) {
                // 查询用户（会从缓存获取）
                System.out.println("\n3. 查询刚创建的用户:");
                User foundUser = userService.getUserById(user.getId());
                System.out.println("找到用户: " + foundUser);

                // 更新用户
                System.out.println("\n4. 更新用户信息:");
                foundUser.setEmail("updated@example.com");
                boolean updated = userService.updateUser(foundUser);
                System.out.println("用户更新结果: " + updated);

                // 再次查询（会从数据库获取，因为缓存被清除了）
                System.out.println("\n5. 查询更新后的用户:");
                User updatedUser = userService.getUserById(user.getId());
                System.out.println("更新后的用户: " + updatedUser);

                // 删除用户
                System.out.println("\n6. 删除用户:");
                boolean deleted = userService.deleteUser(user.getId());
                System.out.println("用户删除结果: " + deleted);
            }

            System.out.println("\n=== MyBatis和Redis集成演示完成 ===");
        };
    }
}