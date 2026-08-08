package com.headspa.redis.repository;

import com.headspa.redis.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 使用 @DataJpaTest：只載入 JPA 相關元件，並自動切換成記憶體資料庫 (H2)，
 * 不會連到 application.yaml 裡設定的正式 MySQL，也不需要另外啟動資料庫。
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldPersistUserAndGenerateId() {
        User user = new User();
        user.setName("Liam");
        user.setPhone("0910234567");
        user.setEmail("liam@yahoo.com.tw");

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Liam");
    }

    @Test
    void findById_shouldReturnSavedUser() {
        User user = new User();
        user.setName("Sophia");
        user.setPhone("0930234567");
        user.setEmail("sophia@yahoo.com.tw");
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("sophia@yahoo.com.tw");
    }

    @Test
    void findAll_shouldReturnAllSavedUsers() {
        User u1 = new User();
        u1.setName("A");
        u1.setPhone("0911111111");
        u1.setEmail("a@test.com");

        User u2 = new User();
        u2.setName("B");
        u2.setPhone("0922222222");
        u2.setEmail("b@test.com");

        userRepository.save(u1);
        userRepository.save(u2);

        List<User> all = userRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById_shouldRemoveUser() {
        User user = new User();
        user.setName("ToDelete");
        user.setPhone("0900000000");
        user.setEmail("delete@test.com");
        User saved = userRepository.save(user);

        userRepository.deleteById(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }
}
