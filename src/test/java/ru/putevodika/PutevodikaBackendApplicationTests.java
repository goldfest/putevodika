package ru.putevodika;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
		"app.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@Import(TestcontainersConfiguration.class)
class PutevodikaBackendApplicationTests {

	@Test
	void contextLoads() {
	}
}