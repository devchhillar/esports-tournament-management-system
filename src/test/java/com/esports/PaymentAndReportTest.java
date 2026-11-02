package com.esports;

import com.esports.dao.*;
import com.esports.model.*;
import com.esports.service.ReportService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Management & PDF/CSV Export Test Suite")
public class PaymentAndReportTest {

    private PaymentDAO paymentDAO;
    private TournamentDAO tournamentDAO;
    private TeamDAO teamDAO;
    private ReportService reportService;

    private Tournament testTournament;
    private Team testTeam;
    private final List<File> tempFiles = new ArrayList<>();

    @BeforeEach
    void setUp() {
        paymentDAO = new PaymentDAOImpl();
        tournamentDAO = new TournamentDAOImpl();
        teamDAO = new TeamDAOImpl();
        reportService = new ReportService();

        testTournament = new Tournament();
        testTournament.setTitle("Payment Cup " + System.currentTimeMillis());
        testTournament.setGame("Valorant");
        testTournament.setFormat("SINGLE_ELIMINATION");
        testTournament.setEntryFee(500.0);
        testTournament.setPrizePool(50000.0);
        testTournament.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        testTournament.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));
        testTournament.setStatus("REGISTRATION_OPEN");
        tournamentDAO.create(testTournament);

        testTeam = new Team();
        testTeam.setTeamName("Payment Team " + System.currentTimeMillis());
        testTeam.setTeamTag("P" + (System.currentTimeMillis() % 1000));
        testTeam.setGame("Valorant");
        testTeam.setLeaderId(1);
        testTeam.setStatus("ACTIVE");
        teamDAO.create(testTeam, 1);
    }

    @AfterEach
    void tearDown() {
        if (testTournament != null && testTournament.getId() > 0) {
            tournamentDAO.delete(testTournament.getId());
        }
        if (testTeam != null && testTeam.getId() > 0) {
            teamDAO.delete(testTeam.getId());
        }
        for (File f : tempFiles) {
            if (f.exists()) {
                f.delete();
            }
        }
        tempFiles.clear();
    }

    @Test
    @DisplayName("Should record payment, query transactions, and update payment status")
    void testPaymentLifecycle() {
        String txnId = "TEST_TXN_" + System.currentTimeMillis();
        Payment payment = new Payment();
        payment.setTournamentId(testTournament.getId());
        payment.setTeamId(testTeam.getId());
        payment.setUserId(1); // admin user
        payment.setAmount(new BigDecimal("500.00"));
        payment.setPaymentType("ENTRY_FEE");
        payment.setPaymentMethod("UPI");
        payment.setTransactionId(txnId);
        payment.setStatus("COMPLETED");
        payment.setNotes("Automated test fee payment");

        boolean created = paymentDAO.recordPayment(payment);
        assertTrue(created, "Payment recording should succeed");
        assertTrue(payment.getId() > 0, "Generated payment ID should be positive");

        // Verify retrieval by ID
        Payment fetched = paymentDAO.findById(payment.getId());
        assertNotNull(fetched);
        assertEquals(txnId, fetched.getTransactionId());
        assertEquals("ENTRY_FEE", fetched.getPaymentType());
        assertEquals("COMPLETED", fetched.getStatus());
        assertEquals(0, new BigDecimal("500.00").compareTo(fetched.getAmount()));

        // Verify team paid check
        assertTrue(paymentDAO.isTeamPaid(testTournament.getId(), testTeam.getId()), "isTeamPaid should return true");

        // Verify update status
        boolean updated = paymentDAO.updateStatus(payment.getId(), "REFUNDED");
        assertTrue(updated);
        Payment refunded = paymentDAO.findById(payment.getId());
        assertNotNull(refunded);
        assertEquals("REFUNDED", refunded.getStatus());

        // Verify calculation totals are valid numbers
        assertNotNull(paymentDAO.getTotalRevenue());
        assertNotNull(paymentDAO.getTotalPrizePayouts());
    }

    @Test
    @DisplayName("Should generate valid, non-empty PDF and CSV report files")
    void testPdfAndCsvReportGeneration() throws IOException {
        File pdfTourneys = Files.createTempFile("test_tourneys_", ".pdf").toFile();
        File csvTourneys = Files.createTempFile("test_tourneys_", ".csv").toFile();
        File pdfPayments = Files.createTempFile("test_payments_", ".pdf").toFile();
        File csvPayments = Files.createTempFile("test_payments_", ".csv").toFile();
        File pdfPlayers = Files.createTempFile("test_players_", ".pdf").toFile();
        File csvPlayers = Files.createTempFile("test_players_", ".csv").toFile();

        tempFiles.addAll(List.of(pdfTourneys, csvTourneys, pdfPayments, csvPayments, pdfPlayers, csvPlayers));

        // 1. Tournaments PDF & CSV
        assertTrue(reportService.exportTournamentsToPdf(pdfTourneys));
        assertTrue(pdfTourneys.exists() && pdfTourneys.length() > 0, "PDF tournaments report must not be empty");

        assertTrue(reportService.exportTournamentsToCsv(csvTourneys));
        assertTrue(csvTourneys.exists() && csvTourneys.length() > 0, "CSV tournaments report must not be empty");

        // 2. Payments PDF & CSV
        assertTrue(reportService.exportPaymentsToPdf(pdfPayments));
        assertTrue(pdfPayments.exists() && pdfPayments.length() > 0, "PDF payments report must not be empty");

        assertTrue(reportService.exportPaymentsToCsv(csvPayments));
        assertTrue(csvPayments.exists() && csvPayments.length() > 0, "CSV payments report must not be empty");

        // 3. Players PDF & CSV
        assertTrue(reportService.exportPlayersToPdf(pdfPlayers));
        assertTrue(pdfPlayers.exists() && pdfPlayers.length() > 0, "PDF players report must not be empty");

        assertTrue(reportService.exportPlayersToCsv(csvPlayers));
        assertTrue(csvPlayers.exists() && csvPlayers.length() > 0, "CSV players report must not be empty");
    }
}
