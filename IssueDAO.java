
package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.BookIssue;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {

    public void issueBook(int bookId, int memberId, int loanDays)
            throws SQLException {

        if (loanDays <= 0) {
            throw new IllegalArgumentException(
                    "Loan period must be positive.");
        }

        String lockBook = """
                SELECT available_copies
                FROM books WHERE id = ? FOR UPDATE
                """;

        String checkMember =
                "SELECT id FROM members WHERE id = ?";

        String reduceCopies = """
                UPDATE books SET available_copies = available_copies - 1
                WHERE id = ? AND available_copies > 0
                """;

        String insertIssue = """
                INSERT INTO book_issues
                (book_id, member_id, issue_date, due_date, status)
                VALUES (?, ?, ?, ?, 'ISSUED')
                """;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps =
                             con.prepareStatement(lockBook)) {
                    ps.setInt(1, bookId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Book not found.");
                        }

                        if (rs.getInt("available_copies") <= 0) {
                            throw new SQLException(
                                    "No copies available.");
                        }
                    }
                }

                try (PreparedStatement ps =
                             con.prepareStatement(checkMember)) {
                    ps.setInt(1, memberId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Member not found.");
                        }
                    }
                }

                try (PreparedStatement ps =
                             con.prepareStatement(reduceCopies)) {
                    ps.setInt(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Could not reserve a book copy.");
                    }
                }

                LocalDate today = LocalDate.now();

                try (PreparedStatement ps =
                             con.prepareStatement(insertIssue)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, memberId);
                    ps.setDate(3, Date.valueOf(today));
                    ps.setDate(4, Date.valueOf(
                            today.plusDays(loanDays)));
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void returnBook(int issueId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int bookId;

                String findIssue = """
                        SELECT book_id, status
                        FROM book_issues
                        WHERE id = ? FOR UPDATE
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(findIssue)) {
                    ps.setInt(1, issueId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Issue not found.");
                        }

                        if (!"ISSUED".equals(rs.getString("status"))) {
                            throw new SQLException(
                                    "This book has already been returned.");
                        }

                        bookId = rs.getInt("book_id");
                    }
                }

                String updateIssue = """
                        UPDATE book_issues
                        SET status = 'RETURNED', return_date = ?
                        WHERE id = ? AND status = 'ISSUED'
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(updateIssue)) {
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setInt(2, issueId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Could not complete return.");
                    }
                }

                String updateBook = """
                        UPDATE books
                        SET available_copies = available_copies + 1
                        WHERE id = ? AND available_copies < total_copies
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(updateBook)) {
                    ps.setInt(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Could not update available copies.");
                    }
                }

                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<BookIssue> findAll() throws SQLException {
        List<BookIssue> issues = new ArrayList<>();

        String sql = """
                SELECT id, book_id, member_id, issue_date,
                       due_date, return_date, status
                FROM book_issues ORDER BY id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date returnDate = rs.getDate("return_date");

                issues.add(new BookIssue(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id"),
                        rs.getDate("issue_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        returnDate == null
                                ? null : returnDate.toLocalDate(),
                        rs.getString("status")
                ));
            }
        }

        return issues;
    }
}
