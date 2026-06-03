package org.example.javaspringbootjooqsample;

import org.example.javaspringbootjooqsample.support.MySqlSpringBootTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag("smoke")
class JavaSpringbootJooqSampleApplicationTests extends MySqlSpringBootTestSupport {

    @Test
    void contextLoads() {
        // given
        // 전체 애플리케이션 컨텍스트를 로드할 설정이 준비되어 있습니다.

        // when
        // SpringBootTest가 컨텍스트 로딩을 수행합니다.

        // then
        // 예외 없이 컨텍스트가 로드되면 테스트가 성공합니다.
    }

}
