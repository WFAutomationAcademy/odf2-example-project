package com.workfusion.odf2.example.repository;

import com.workfusion.odf2.example.model.Email;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf2.transaction.model.Transaction;

import static org.assertj.core.api.Assertions.assertThat;

class EmailRepositoryTest {

    private static ConnectionSource connectionSource;
    private static EmailRepository repository;

    @BeforeAll
    static void beforeAll() throws SQLException {
        connectionSource = new JdbcPooledConnectionSource("jdbc:h2:mem:test_db;MODE=MSSQLServer");
        repository = new EmailRepository(connectionSource);
        TableUtils.createTable(connectionSource, Email.class);
    }

    @AfterAll
    static void afterAll() throws IOException, SQLException {
        TableUtils.dropTable(connectionSource, Email.class, false);
        connectionSource.close();
    }

    @AfterEach
    void tearDown() throws SQLException {
        TableUtils.clearTable(connectionSource, Email.class);
    }

    @Test
    @DisplayName("should find all emails by transaction id")
    void shouldFindAllEmailsByTransactionId() {
        // given
        UUID transactionId = UUID.randomUUID();

        repository.createAll(Arrays.asList(createEmail(transactionId), createEmail(UUID.randomUUID()), createEmail(transactionId)));

        // when
        List<Email> actualEmails = repository.findByTransactionId(transactionId);

        // then
        assertThat(actualEmails)
                .hasSize(2)
                .extracting(Email::getMessage)
                .containsOnly(transactionId.toString());
    }

    @Test
    @DisplayName("should return empty result if no emails found")
    void shouldReturnEmptyResultIfNoEmailsFound() {
        // when
        Optional<Email> actualEmail = repository.findById(UUID.randomUUID());

        // then
        assertThat(actualEmail).isEmpty();
    }

    private static Email createEmail(UUID transactionId) {
        Transaction transaction = new Transaction();
        transaction.setUuid(transactionId);

        Email email = new Email();
        email.setMessage(transactionId.toString());
        email.setReceived(Date.from(Instant.now()));
        email.setTransaction(transaction);
        return email;
    }

}
